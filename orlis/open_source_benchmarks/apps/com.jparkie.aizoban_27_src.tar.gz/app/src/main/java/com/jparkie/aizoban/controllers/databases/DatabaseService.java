package com.jparkie.aizoban.controllers.databases;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.events.ChapterQueryEvent;
import com.jparkie.aizoban.controllers.events.DownloadChapterQueryEvent;
import com.jparkie.aizoban.controllers.events.FavouriteMangaQueryEvent;
import com.jparkie.aizoban.controllers.events.RecentChapterQueryEvent;
import com.jparkie.aizoban.models.databases.FavouriteManga;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.models.downloads.DownloadManga;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class DatabaseService extends IntentService {
    public static final String TAG = DatabaseService.class.getSimpleName();

    public static final String INTENT_CREATE_RECENT_CHAPTERS = TAG + ":" + "CreateRecentChaptersIntent";
    public static final String INTENT_DELETE_FAVOURITE_MANGA = TAG + ":" + "DeleteFavouriteMangaIntent";
    public static final String INTENT_DELETE_RECENT_CHAPTERS = TAG + ":" + "DeleteRecentChaptersIntent";
    public static final String INTENT_DELETE_DOWNLOAD_MANGA = TAG + ":" + "DeleteDownloadMangaIntent";
    public static final String INTENT_DELETE_DOWNLOAD_CHAPTERS = TAG + ":" + "DeleteDownloadChaptersIntent";

    public DatabaseService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        handleCreateRecentChaptersIntent(intent);
        handleDeleteFavouriteMangaIntent(intent);
        handleDeleteRecentChaptersIntent(intent);
        handleDeleteDownloadMangaIntent(intent);
        handleDeleteDownloadChaptersIntent(intent);
    }

    private void handleCreateRecentChaptersIntent(Intent createRecentChaptersIntent) {
        if (createRecentChaptersIntent != null) {
            if (createRecentChaptersIntent.hasExtra(INTENT_CREATE_RECENT_CHAPTERS)) {
                ArrayList<RecentChapter> recentChaptersToCreate = createRecentChaptersIntent.getParcelableArrayListExtra(INTENT_CREATE_RECENT_CHAPTERS);
                if (recentChaptersToCreate != null) {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    sqLiteDatabase.beginTransaction();
                    try {
                        for (RecentChapter recentChapter : recentChaptersToCreate) {
                            if (recentChapter != null) {
                                QueryManager.putObjectToApplicationDatabase(recentChapter);
                            }
                        }
                        sqLiteDatabase.setTransactionSuccessful();
                    } finally {
                        sqLiteDatabase.endTransaction();
                    }
                }

                EventBus.getDefault().post(new ChapterQueryEvent());
                EventBus.getDefault().post(new DownloadChapterQueryEvent());
                EventBus.getDefault().post(new RecentChapterQueryEvent());

                createRecentChaptersIntent.removeExtra(INTENT_CREATE_RECENT_CHAPTERS);
            }
        }
    }

    private void handleDeleteFavouriteMangaIntent(Intent deleteFavoriteMangaIntent) {
        if (deleteFavoriteMangaIntent != null) {
            if (deleteFavoriteMangaIntent.hasExtra(INTENT_DELETE_FAVOURITE_MANGA)) {
                ArrayList<FavouriteManga> favouriteMangasToDelete = deleteFavoriteMangaIntent.getParcelableArrayListExtra(INTENT_DELETE_FAVOURITE_MANGA);
                if (favouriteMangasToDelete != null) {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    sqLiteDatabase.beginTransaction();
                    try {
                        for (FavouriteManga favouriteManga : favouriteMangasToDelete) {
                            if (favouriteManga != null) {
                                QueryManager.deleteObjectToApplicationDatabase(favouriteManga);
                            }
                        }
                        sqLiteDatabase.setTransactionSuccessful();
                    } finally {
                        sqLiteDatabase.endTransaction();
                    }
                }

                EventBus.getDefault().post(new FavouriteMangaQueryEvent());

                deleteFavoriteMangaIntent.removeExtra(INTENT_DELETE_FAVOURITE_MANGA);
            }
        }
    }

    private void handleDeleteRecentChaptersIntent(Intent deleteRecentChaptersIntent) {
        if (deleteRecentChaptersIntent != null) {
            if (deleteRecentChaptersIntent.hasExtra(INTENT_DELETE_RECENT_CHAPTERS)) {
                ArrayList<RecentChapter> recentChaptersToDelete = deleteRecentChaptersIntent.getParcelableArrayListExtra(INTENT_DELETE_RECENT_CHAPTERS);
                if (recentChaptersToDelete != null) {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    sqLiteDatabase.beginTransaction();
                    try {
                        for (RecentChapter recentChapter : recentChaptersToDelete) {
                            if (recentChapter != null) {
                                QueryManager.deleteObjectToApplicationDatabase(recentChapter);
                            }
                        }
                        sqLiteDatabase.setTransactionSuccessful();
                    } finally {
                        sqLiteDatabase.endTransaction();
                    }
                }

                EventBus.getDefault().post(new RecentChapterQueryEvent());

                deleteRecentChaptersIntent.removeExtra(INTENT_DELETE_RECENT_CHAPTERS);
            }
        }
    }

    private void handleDeleteDownloadMangaIntent(Intent deleteDownloadMangaIntent) {
        if (deleteDownloadMangaIntent != null) {
            if (deleteDownloadMangaIntent.hasExtra(INTENT_DELETE_DOWNLOAD_MANGA)) {
                DownloadManga downloadMangaToDelete = deleteDownloadMangaIntent.getParcelableExtra(INTENT_DELETE_DOWNLOAD_MANGA);
                if (downloadMangaToDelete != null) {
                    Cursor downloadCursor = QueryManager.queryDownloadChaptersOfDownloadManga(new RequestWrapper(downloadMangaToDelete.getSource(), downloadMangaToDelete.getUrl()), true)
                            .toBlocking()
                            .single();

                    if (downloadCursor != null && downloadCursor.getCount() != 0) {
                        downloadCursor.close();
                        downloadCursor = null;
                    } else {
                        QueryManager.deleteObjectToApplicationDatabase(downloadMangaToDelete);
                    }
                }

                deleteDownloadMangaIntent.removeExtra(INTENT_DELETE_DOWNLOAD_MANGA);
            }
        }
    }

    private void handleDeleteDownloadChaptersIntent(Intent deleteDownloadChaptersIntent) {
        if (deleteDownloadChaptersIntent != null) {
            if (deleteDownloadChaptersIntent.hasExtra(INTENT_DELETE_DOWNLOAD_CHAPTERS)) {
                ArrayList<DownloadChapter> downloadChaptersToDelete = deleteDownloadChaptersIntent.getParcelableArrayListExtra(INTENT_DELETE_DOWNLOAD_CHAPTERS);
                if (downloadChaptersToDelete != null) {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    sqLiteDatabase.beginTransaction();
                    try {
                        for (DownloadChapter downloadChapter : downloadChaptersToDelete) {
                            if (downloadChapter != null) {
                                DiskUtils.deleteFiles(new File(downloadChapter.getDirectory()));
                                QueryManager.deleteObjectToApplicationDatabase(downloadChapter);
                            }
                        }
                        sqLiteDatabase.setTransactionSuccessful();
                    } finally {
                        sqLiteDatabase.endTransaction();
                    }
                }

                EventBus.getDefault().post(new DownloadChapterQueryEvent());

                deleteDownloadChaptersIntent.removeExtra(INTENT_DELETE_DOWNLOAD_CHAPTERS);
            }
        }
    }
}
