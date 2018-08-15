package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.downloads.DownloadService;
import com.jparkie.aizoban.controllers.events.DownloadChapterQueryEvent;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.presenters.mapper.QueueMapper;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.views.QueueView;
import com.jparkie.aizoban.views.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class QueuePresenterImpl implements QueuePresenter {
    public static final String TAG = QueuePresenterImpl.class.getSimpleName();

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private QueueView mQueueView;
    private QueueMapper mQueueMapper;
    private QueueAdapter mQueueAdapter;

    private Parcelable mPositionSavedState;

    private Subscription mQueryDownloadChapterSubscription;
    private Subscription mServiceUpdateSubscription;
    private PublishSubject<Observable<DownloadChapterQueryEvent>> mServiceUpdatePublishSubject;

    public QueuePresenterImpl(QueueView queueView, QueueMapper queueMapper) {
        mQueueView = queueView;
        mQueueMapper = queueMapper;

        initializeServiceUpdatePublishSubject();
    }

    @Override
    public void initializeViews() {
        mQueueView.initializeToolbar();
        mQueueView.initializeAbsListView();
        mQueueView.initializeEmptyRelativeLayout();
    }

    @Override
    public void initializeDataFromDatabase() {
        mQueueAdapter = new QueueAdapter(mQueueView.getContext());

        mQueueMapper.registerAdapter(mQueueAdapter);
    }

    @Override
    public void registerForEvents() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(DownloadChapterQueryEvent event) {
        if (event != null) {
            mServiceUpdatePublishSubject.onNext(Observable.just(event));
        }
    }

    @Override
    public void unregisterForEvents() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        queryNonCompletedDownloadChaptersFromDatabase();
    }

    @Override
    public void saveState(Bundle outState) {
        if (mQueueMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mQueueMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mPositionSavedState = savedState.getParcelable(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }
        if (mServiceUpdateSubscription != null) {
            mServiceUpdateSubscription.unsubscribe();
            mServiceUpdateSubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mQueueAdapter != null) {
            mQueueAdapter.setCursor(null);
            mQueueAdapter = null;
        }
    }

    @Override
    public void onOptionStartDownloader() {
        Intent startService = new Intent(mQueueView.getContext(), DownloadService.class);
        startService.putExtra(DownloadService.INTENT_START_DOWNLOAD, DownloadService.INTENT_START_DOWNLOAD);
        mQueueView.getContext().startService(startService);
    }

    @Override
    public void onOptionStopDownloader() {
        Intent startService = new Intent(mQueueView.getContext(), DownloadService.class);
        startService.putExtra(DownloadService.INTENT_STOP_DOWNLOAD, DownloadService.INTENT_STOP_DOWNLOAD);
        mQueueView.getContext().startService(startService);
    }

    @Override
    public void onOptionToTop() {
        mQueueView.scrollToTop();
    }

    @Override
    public void onOptionCancel() {
        if (mQueueAdapter != null) {
            ArrayList<DownloadChapter> downloadChaptersToCancel = new ArrayList<DownloadChapter>();

            SparseBooleanArray checkedItems = mQueueMapper.getCheckedItemPositions();
            for (int index = 0; index < mQueueAdapter.getCount(); index++) {
                if (checkedItems.get(index)) {
                    DownloadChapter downloadChapter = (DownloadChapter)mQueueAdapter.getItem(index);
                    if (downloadChapter != null) {
                        downloadChaptersToCancel.add(downloadChapter);
                    }
                }
            }

            Intent startService = new Intent(mQueueView.getContext(), DownloadService.class);
            startService.putExtra(DownloadService.INTENT_CANCEL_DOWNLOAD, downloadChaptersToCancel);
            mQueueView.getContext().startService(startService);
        }
    }

    @Override
    public void onOptionSelectAll() {
        mQueueView.selectAll();
    }

    @Override
    public void onOptionClear() {
        mQueueView.clear();
    }

    private void initializeServiceUpdatePublishSubject() {
        mServiceUpdatePublishSubject = PublishSubject.create();
        mServiceUpdateSubscription = Observable.switchOnNext(mServiceUpdatePublishSubject)
                .debounce(DownloadUtils.TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadChapterQueryEvent>() {
                    @Override
                    public void onCompleted() {
                        queryNonCompletedDownloadChaptersFromDatabase();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(DownloadChapterQueryEvent downloadChapterUpdateEvent) {
                        // Do Nothing.

                        onCompleted();
                    }
                });
    }

    private void queryNonCompletedDownloadChaptersFromDatabase() {
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }

        mQueryDownloadChapterSubscription = QueryManager
                .queryNonCompletedDownloadChapters()
                .map(new Func1<Cursor, Cursor>() {
                    @Override
                    public Cursor call(Cursor incomingCursor) {
                        if (incomingCursor != null && incomingCursor.getCount() != 0) {
                            return incomingCursor;
                        }

                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Cursor>() {
                    @Override
                    public void onCompleted() {
                        restorePosition();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(Cursor cursor) {
                        if (mQueueAdapter != null) {
                            mQueueAdapter.setCursor(cursor);
                        }

                        if (cursor != null) {
                            mQueueView.hideEmptyRelativeLayout();
                        } else {
                            mQueueView.showEmptyRelativeLayout();
                        }
                    }
                });
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mQueueMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }
}
