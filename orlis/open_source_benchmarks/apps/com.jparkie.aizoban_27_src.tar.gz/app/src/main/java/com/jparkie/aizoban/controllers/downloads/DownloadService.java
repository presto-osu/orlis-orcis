package com.jparkie.aizoban.controllers.downloads;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.databases.ApplicationSQLiteOpenHelper;
import com.jparkie.aizoban.controllers.events.DownloadChapterQueryEvent;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.NavigationUtils;
import com.jparkie.aizoban.utils.PreferenceUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.activities.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DownloadService extends Service implements Observer<File> {
    public static final String TAG = DownloadService.class.getSimpleName();

    public static final String INTENT_QUEUE_DOWNLOAD = TAG + ":" + "QueueDownloadIntent";
    public static final String INTENT_CANCEL_DOWNLOAD = TAG + ":" + "CancelDownloadIntent";
    public static final String INTENT_START_DOWNLOAD = TAG + ":" + "StartDownloadIntent";
    public static final String INTENT_STOP_DOWNLOAD = TAG + ":" + "StopDownloadIntent";
    public static final String INTENT_RESTART_DOWNLOAD = TAG + ":" + "RestartDownloadIntent";

    private final static int DOWNLOAD_NOTIFICATION_ID = 1337;

    private static final int DOWNLOAD_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int DOWNLOAD_MAXIMUM_POOL_SIZE = (DOWNLOAD_CORE_POOL_SIZE > 0) ? DOWNLOAD_CORE_POOL_SIZE : 2;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private NotificationCompat.Builder mDownloadNotificationBuilder;

    private PowerManager.WakeLock mWakeLock;

    private ThreadPoolExecutor mDownloadThreadPoolExecutor;

    private PublishSubject<DownloadChapter> mDownloadChapterPublishSubject;

    private ConcurrentHashMap<String, Subscription> mDownloadUrlToSubscriptionMap;
    private Subscription mDownloadChapterPublishSubjectSubscription;
    private Subscription mNetworkChangeBroadcastSubscription;

    private boolean mIsInitialized;
    private boolean mIsStopping;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                handleQueueDownloadIntent(intent);
                handleCancelDownloadIntent(intent);
                handleStartDownloadIntent(intent);
                handleStopDownloadIntent(intent);
                handleRestartDownloadIntent(intent);

                subscriber.onCompleted();
            }
        })
        .subscribeOn(Schedulers.newThread())
        .subscribe();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destoryAllSubscriptions();

        releaseWakeLock();
    }

    // Observer<File>:

    @Override
    public void onCompleted() {
        if (QueryManager.queryShouldDownloadServiceStop().toBlocking().single()) {
            stopForeground(false);
            stopSelf();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNext(File imageFile) {
        // Do Nothing.
    }

    private void initialize() {
        if (mIsInitialized) {
            return;
        }

        initializeWakeLock();
        initializeThreadPoolExecutor();
        initializeDownloadChapterPublishSubject();
        initializeNetworkChangeBroadcastObservable();
        initializeNotification();

        mIsInitialized = true;
    }

    private void initializeWakeLock() {
        mWakeLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + ":" + "WakeLock");

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    private void initializeThreadPoolExecutor() {
        mDownloadThreadPoolExecutor = new ThreadPoolExecutor(
                DOWNLOAD_MAXIMUM_POOL_SIZE,
                DOWNLOAD_MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new LinkedBlockingDeque<Runnable>()
        );
    }

    private void initializeDownloadChapterPublishSubject() {
        mDownloadUrlToSubscriptionMap = new ConcurrentHashMap<String, Subscription>();

        mDownloadChapterPublishSubject = PublishSubject.create();
        mDownloadChapterPublishSubjectSubscription = mDownloadChapterPublishSubject
                .filter(new Func1<DownloadChapter, Boolean>() {
                    @Override
                    public Boolean call(DownloadChapter downloadChapter) {
                        return QueryManager.queryAllowDownloadServiceToPublishDownloadChapter(downloadChapter).toBlocking().single();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<DownloadChapter>() {
                    @Override
                    public void call(DownloadChapter allowedChapter) {
                        startDownloadToSubscriptionMap(allowedChapter);
                    }
                });
    }

    private void initializeNetworkChangeBroadcastObservable() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        mNetworkChangeBroadcastSubscription = AndroidObservable
                .fromBroadcast(this, intentFilter)
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        if (isNetworkAvailableForDownloads()) {
                            queueDownloadChapters();

                            startForeground(DOWNLOAD_NOTIFICATION_ID, mDownloadNotificationBuilder.build());

                            if (!mWakeLock.isHeld()) {
                                mWakeLock.acquire();
                            }
                        } else {
                            stopForeground(false);

                            if (mWakeLock.isHeld()) {
                                mWakeLock.release();
                            }
                        }
                    }
                });
    }

    private void startDownloadToSubscriptionMap(final DownloadChapter downloadChapter) {
        final String hashKey = DiskUtils.hashKeyForDisk(downloadChapter.getUrl());

        Subscription newSubscription = AizobanManager.downloadChapterFromNetwork(downloadChapter)
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        Subscription finalSubscription = mDownloadUrlToSubscriptionMap.remove(hashKey);
                        if (finalSubscription != null) {
                            finalSubscription.unsubscribe();
                            finalSubscription = null;
                        }

                        if (isNetworkAvailableForDownloads()) {
                            queueDownloadChapters();
                        }
                    }
                })
                .subscribeOn(Schedulers.from(mDownloadThreadPoolExecutor))
                .subscribe(this);

        mDownloadUrlToSubscriptionMap.put(hashKey, newSubscription);
    }

    private void destoryAllSubscriptions() {
        if (mDownloadUrlToSubscriptionMap != null) {
            for (Subscription downloadSubscription : mDownloadUrlToSubscriptionMap.values()) {
                if (downloadSubscription != null) {
                    downloadSubscription.unsubscribe();
                    downloadSubscription = null;
                }
            }
        }
        if (mDownloadChapterPublishSubjectSubscription != null) {
            mDownloadChapterPublishSubjectSubscription.unsubscribe();
            mDownloadChapterPublishSubjectSubscription = null;
        }
        if (mNetworkChangeBroadcastSubscription != null) {
            mNetworkChangeBroadcastSubscription.unsubscribe();
            mNetworkChangeBroadcastSubscription = null;
        }
    }

    private synchronized void handleQueueDownloadIntent(Intent queueDownloadIntent) {
        if (queueDownloadIntent != null) {
            if (queueDownloadIntent.hasExtra(INTENT_QUEUE_DOWNLOAD)) {
                ArrayList<Chapter> chaptersToDownload = queueDownloadIntent.getParcelableArrayListExtra(INTENT_QUEUE_DOWNLOAD);
                if (chaptersToDownload != null) {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase applicationDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    applicationDatabase.beginTransaction();
                    try {
                        for (Chapter chapterToDownload : chaptersToDownload) {
                            if (chapterToDownload != null) {
                                DownloadChapter downloadChapter = DefaultFactory.DownloadChapter.constructDefault();
                                downloadChapter.setSource(chapterToDownload.getSource());
                                downloadChapter.setUrl(chapterToDownload.getUrl());
                                downloadChapter.setParentUrl(chapterToDownload.getParentUrl());
                                downloadChapter.setName(chapterToDownload.getName());

                                boolean isExternalStorage = PreferenceUtils.isExternalStorage();
                                if (isExternalStorage) {
                                    File externalDirectory = new File(PreferenceUtils.getDownloadDirectory());
                                    File sourceDirectory = new File(externalDirectory, downloadChapter.getSource());
                                    File urlHashDirectory = new File(sourceDirectory, DiskUtils.hashKeyForDisk(downloadChapter.getUrl()));

                                    downloadChapter.setDirectory(urlHashDirectory.getAbsolutePath());
                                } else {
                                    File internalDirectory = getApplicationContext().getFilesDir();
                                    File sourceDirectory = new File(internalDirectory, downloadChapter.getSource());
                                    File urlHashDirectory = new File(sourceDirectory, DiskUtils.hashKeyForDisk(downloadChapter.getUrl()));

                                    downloadChapter.setDirectory(urlHashDirectory.getAbsolutePath());
                                }

                                if (isExternalStorage) {
                                    File externalDirectory = new File(PreferenceUtils.getDownloadDirectory());
                                    File noMediaFile = new File(externalDirectory, ".nomedia");

                                    if (!noMediaFile.exists()) {
                                        if (!externalDirectory.exists()) {
                                            externalDirectory.mkdirs();
                                        }

                                        try {
                                            noMediaFile.createNewFile();
                                        } catch (IOException e) {
                                            // Do Nothing.
                                        }
                                    }
                                }

                                downloadChapter.setFlag(DownloadUtils.FLAG_PENDING);

                                QueryManager.putObjectToApplicationDatabase(downloadChapter);
                            }
                        }
                        applicationDatabase.setTransactionSuccessful();
                    } finally {
                        applicationDatabase.endTransaction();
                    }
                }

                EventBus.getDefault().post(new DownloadChapterQueryEvent());

                queueDownloadIntent.removeExtra(INTENT_QUEUE_DOWNLOAD);

                if (mIsInitialized) {
                    if (QueryManager.queryShouldDownloadServiceStop().toBlocking().single()) {
                        stopForeground(false);
                        stopSelf();
                    }
                } else {
                    stopForeground(false);
                    stopSelf();
                }
            }
        }
    }

    private synchronized void handleCancelDownloadIntent(Intent cancelDownloadIntent) {
        if (cancelDownloadIntent != null) {
            if (cancelDownloadIntent.hasExtra(INTENT_CANCEL_DOWNLOAD)) {
                ArrayList<DownloadChapter> downloadChaptersToCancel = cancelDownloadIntent.getParcelableArrayListExtra(INTENT_CANCEL_DOWNLOAD);
                if (downloadChaptersToCancel != null) {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    sqLiteDatabase.beginTransaction();
                    try {
                        for (DownloadChapter downloadChapter : downloadChaptersToCancel) {
                            if (downloadChapter != null) {
                                if (mIsInitialized) {
                                    String hashKey = DiskUtils.hashKeyForDisk(downloadChapter.getUrl());
                                    if (mDownloadUrlToSubscriptionMap.containsKey(hashKey)) {
                                        Subscription currentSubscription = mDownloadUrlToSubscriptionMap.remove(hashKey);
                                        currentSubscription.unsubscribe();
                                        currentSubscription = null;
                                    }
                                }

                                DiskUtils.deleteFiles(new File(downloadChapter.getDirectory()));

                                QueryManager.deleteObjectToApplicationDatabase(downloadChapter);
                                QueryManager.deleteDownloadPagesOfDownloadChapter(new RequestWrapper(downloadChapter.getSource(), downloadChapter.getUrl()))
                                        .toBlocking()
                                        .single();
                            }
                        }
                        sqLiteDatabase.setTransactionSuccessful();
                    } finally {
                        sqLiteDatabase.endTransaction();
                    }
                }

                EventBus.getDefault().post(new DownloadChapterQueryEvent());

                cancelDownloadIntent.removeExtra(INTENT_CANCEL_DOWNLOAD);

                if (mIsInitialized) {
                    if (isNetworkAvailableForDownloads()) {
                        queueDownloadChapters();
                    }

                    if (QueryManager.queryShouldDownloadServiceStop().toBlocking().single()) {
                        stopForeground(false);
                        stopSelf();
                    }
                } else {
                    stopForeground(false);
                    stopSelf();
                }
            }
        }
    }

    private synchronized void handleStartDownloadIntent(Intent startDownloadIntent) {
        if (startDownloadIntent != null) {
            if (startDownloadIntent.hasExtra(INTENT_START_DOWNLOAD)) {
                initialize();

                EventBus.getDefault().post(new DownloadChapterQueryEvent());

                startDownloadIntent.removeExtra(INTENT_START_DOWNLOAD);

                if (isNetworkAvailableForDownloads()) {
                    queueDownloadChapters();
                }

                if (QueryManager.queryShouldDownloadServiceStop().toBlocking().single()) {
                    stopForeground(false);
                    stopSelf();
                }
            }
        }
    }

    private void handleStopDownloadIntent(Intent stopDownloadIntent) {
        if (stopDownloadIntent != null) {
            if (stopDownloadIntent.hasExtra(INTENT_STOP_DOWNLOAD)) {
                mIsStopping = true;

                destoryAllSubscriptions();

                pauseDownloadChapters();

                EventBus.getDefault().post(new DownloadChapterQueryEvent());

                stopDownloadIntent.removeExtra(INTENT_STOP_DOWNLOAD);

                stopForeground(false);
                stopSelf();
            }
        }
    }

    private synchronized void handleRestartDownloadIntent(Intent restartDownloadIntent) {
        if (restartDownloadIntent != null) {
            if (restartDownloadIntent.hasExtra(INTENT_RESTART_DOWNLOAD)) {
                pauseDownloadChapters();

                restartDownloadIntent.removeExtra(INTENT_RESTART_DOWNLOAD);

                stopForeground(false);
                stopSelf();
            }
        }
    }

    private synchronized void queueDownloadChapters() {
        if (mIsStopping) {
            return;
        }

        if (mDownloadChapterPublishSubject != null) {
            int dequeueLimit = DOWNLOAD_MAXIMUM_POOL_SIZE;

            Cursor runningCursor = QueryManager.queryRunningDownloadChapters()
                    .toBlocking()
                    .single();

            if (runningCursor != null) {
                dequeueLimit -= runningCursor.getCount();

                runningCursor.close();
                runningCursor = null;
            }

            if (dequeueLimit > 0) {
                Cursor availableDownloadChapterCursor = QueryManager.queryAvailableDownloadChapters(dequeueLimit)
                        .toBlocking()
                        .single();

                if (availableDownloadChapterCursor != null && availableDownloadChapterCursor.getCount() != 0) {
                    List<DownloadChapter> downloadChapters = QueryManager.toList(availableDownloadChapterCursor, DownloadChapter.class);

                    if (downloadChapters != null) {
                        for (DownloadChapter currentDownloadChapter : downloadChapters) {
                            currentDownloadChapter.setFlag(DownloadUtils.FLAG_RUNNING);

                            QueryManager.putObjectToApplicationDatabase(currentDownloadChapter);
                        }

                        for (DownloadChapter streamDownloadChapter : downloadChapters) {
                            mDownloadChapterPublishSubject.onNext(streamDownloadChapter);
                        }

                        EventBus.getDefault().post(new DownloadChapterQueryEvent());
                    }
                }
            }
        }
    }

    private synchronized void pauseDownloadChapters() {
        Cursor nonCompletedCursor = QueryManager.queryNonCompletedDownloadChapters()
                .toBlocking()
                .single();

        if (nonCompletedCursor != null) {
            List<DownloadChapter> downloadChapters = QueryManager.toList(nonCompletedCursor, DownloadChapter.class);

            ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
            SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
            sqLiteDatabase.beginTransaction();
            try {
                for (DownloadChapter downloadChapter : downloadChapters) {
                    downloadChapter.setFlag(DownloadUtils.FLAG_PAUSED);

                    QueryManager.putObjectToApplicationDatabase(downloadChapter);
                }
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    private boolean isNetworkAvailableForDownloads() {
        boolean isWiFiOnly = PreferenceUtils.isWiFiOnly();

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            if (isWiFiOnly) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    private void initializeNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(MainActivity.POSITION_ARGUMENT_KEY, NavigationUtils.POSITION_QUEUE);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mDownloadNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(getText(R.string.notification_download_title))
                .setContentText(getText(R.string.notification_download_text))
                .setProgress(0, 0, true)
                .setContentIntent(pendingIntent);

        startForeground(DOWNLOAD_NOTIFICATION_ID, mDownloadNotificationBuilder.build());
    }
}
