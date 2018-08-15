package com.jparkie.aizoban.controllers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jparkie.aizoban.controllers.databases.ApplicationContract;
import com.jparkie.aizoban.controllers.databases.ApplicationSQLiteOpenHelper;
import com.jparkie.aizoban.controllers.databases.LibraryContract;
import com.jparkie.aizoban.controllers.databases.LibrarySQLiteOpenHelper;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.databases.FavouriteManga;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.models.downloads.DownloadManga;
import com.jparkie.aizoban.models.downloads.DownloadPage;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;

import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.DatabaseCompartment;
import rx.Observable;
import rx.Subscriber;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class QueryManager {
    private static final Object mAddPagesLock = new Object();
    private static final Object mAddMangaLock = new Object();

    private QueryManager() {
        throw new AssertionError();
    }

    public static Observable<Cursor> queryMangaFromRequest(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(LibraryContract.Manga.COLUMN_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    Cursor mangaCursor = cupboard().withDatabase(sqLiteDatabase).query(Manga.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(mangaCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryCatalogueMangasFromPreferenceSource(final SearchCatalogueWrapper searchCatalogueWrapper) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();

                    StringBuilder mangaSelection = new StringBuilder();
                    List<String> mangaSelectionArgs = new ArrayList<String>();
                    String mangaOrderBy = null;
                    String mangaLimit = null;

                    mangaSelection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
                    mangaSelectionArgs.add(AizobanManager.getNameFromPreferenceSource().toBlocking().single());
                    mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_NAME + " != ?");
                    mangaSelectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_NAME));
                    mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_RANK + " != ?");
                    mangaSelectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_RANK));

                    if (searchCatalogueWrapper != null) {
                        for (String currentGenre : searchCatalogueWrapper.getGenresArgs()) {
                            mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_GENRE + " LIKE ?");
                            mangaSelectionArgs.add("%" + currentGenre + "%");
                        }

                        if (searchCatalogueWrapper.getNameArgs() != null) {
                            mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_NAME + " LIKE ?");
                            mangaSelectionArgs.add("%" + searchCatalogueWrapper.getNameArgs() + "%");
                        }
                        if (searchCatalogueWrapper.getStatusArgs() != null && !searchCatalogueWrapper.getStatusArgs().equals(SearchUtils.STATUS_ALL)) {
                            mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_COMPLETED + " = ?");
                            mangaSelectionArgs.add(searchCatalogueWrapper.getStatusArgs());
                        }
                        if (searchCatalogueWrapper.getOrderByArgs() != null) {
                            mangaOrderBy = searchCatalogueWrapper.getOrderByArgs() + " ASC";
                        }
                        if (searchCatalogueWrapper.getOffsetArgs() > -1) {
                            mangaLimit = String.valueOf(searchCatalogueWrapper.getOffsetArgs()) + "," + String.valueOf(SearchUtils.LIMIT_COUNT);
                        }
                    }

                    Cursor catalogueMangasCursor = sqLiteDatabase.query(
                            cupboard().getTable(Manga.class),
                            new String[]{LibraryContract.Manga.COLUMN_NAME, LibraryContract.Manga.COLUMN_SOURCE, LibraryContract.Manga.COLUMN_THUMBNAIL_URL, LibraryContract.Manga.COLUMN_URL},
                            mangaSelection.toString(),
                            mangaSelectionArgs.toArray(new String[mangaSelectionArgs.size()]),
                            null,
                            null,
                            mangaOrderBy,
                            mangaLimit
                    );

                    subscriber.onNext(catalogueMangasCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryLatestMangasFromPreferenceSource() {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(AizobanManager.getNameFromPreferenceSource().toBlocking().single());
                    selection.append(" AND ").append(LibraryContract.Manga.COLUMN_UPDATED + " != ?");
                    selectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_UPDATED));

                    Cursor latestMangasCursor = cupboard().withDatabase(sqLiteDatabase).query(Manga.class)
                            .withProjection(new String[]{LibraryContract.Manga.COLUMN_ID, LibraryContract.Manga.COLUMN_NAME, LibraryContract.Manga.COLUMN_SOURCE, LibraryContract.Manga.COLUMN_THUMBNAIL_URL, LibraryContract.Manga.COLUMN_URL, LibraryContract.Manga.COLUMN_UPDATE_COUNT, LibraryContract.Manga.COLUMN_UPDATED})
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .orderBy(LibraryContract.Manga.COLUMN_UPDATED + " DESC")
                            .getCursor();

                    subscriber.onNext(latestMangasCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryExploreMangaFromPreferenceSource() {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(AizobanManager.getNameFromPreferenceSource().toBlocking().single());
                    selection.append(" AND ").append(LibraryContract.Manga.COLUMN_THUMBNAIL_URL + " != ?");
                    selectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_THUMBNAIL_URL));

                    Cursor exploreMangaCursor = cupboard().withDatabase(sqLiteDatabase).query(Manga.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .orderBy("RANDOM()")
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(exploreMangaCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Integer> clearLatestUpdatesMangas() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();

                    ContentValues updateValues = new ContentValues(2);
                    updateValues.put(LibraryContract.Manga.COLUMN_UPDATE_COUNT, DefaultFactory.Manga.DEFAULT_UPDATE_COUNT);
                    updateValues.put(LibraryContract.Manga.COLUMN_UPDATED, DefaultFactory.Manga.DEFAULT_UPDATED);

                    int numberCleared = sqLiteDatabase.update(
                            cupboard().getTable(Manga.class),
                            updateValues,
                            LibraryContract.Manga.COLUMN_UPDATED + " != ?",
                            new String[]{String.valueOf(DefaultFactory.Manga.DEFAULT_UPDATED)}
                    );

                    subscriber.onNext(numberCleared);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryChapterFromRequest(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.Chapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    Cursor chapterCursor = cupboard().withDatabase(sqLiteDatabase).query(Chapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(chapterCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryChaptersOfMangaFromRequest(final RequestWrapper request, final boolean isAscending) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.Chapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_PARENT_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    DatabaseCompartment.QueryBuilder<Chapter> chapterQueryBuilder = cupboard().withDatabase(sqLiteDatabase).query(Chapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]));

                    if (isAscending) {
                        chapterQueryBuilder = chapterQueryBuilder.orderBy(ApplicationContract.Chapter.COLUMN_NUMBER + " ASC");
                    } else {
                        chapterQueryBuilder = chapterQueryBuilder.orderBy(ApplicationContract.Chapter.COLUMN_NUMBER + " DESC");
                    }

                    Cursor chaptersOfMangaCursor = chapterQueryBuilder.getCursor();

                    subscriber.onNext(chaptersOfMangaCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryAdjacentChapterFromRequestAndNumber(final RequestWrapper request, final int adjacentNumber) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.Chapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_PARENT_URL + " = ?");
                    selectionArgs.add(request.getUrl());
                    selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_NUMBER + " = ?");
                    selectionArgs.add(String.valueOf(adjacentNumber));

                    Cursor adjacentChapter = cupboard().withDatabase(sqLiteDatabase).query(Chapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(adjacentChapter);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryFavouriteMangaFromRequest(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.FavouriteManga.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.FavouriteManga.COLUMN_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    Cursor favouriteManga = cupboard().withDatabase(sqLiteDatabase).query(FavouriteManga.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(favouriteManga);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryFavouriteMangasFromName(final String name) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor favouriteMangasFromName = cupboard().withDatabase(sqLiteDatabase).query(FavouriteManga.class)
                            .withSelection(ApplicationContract.FavouriteManga.COLUMN_NAME + " LIKE ?", "%" + name + "%")
                            .orderBy(ApplicationContract.FavouriteManga.COLUMN_NAME + " ASC")
                            .getCursor();

                    subscriber.onNext(favouriteMangasFromName);

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Integer> deleteAllFavouriteMangas() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    int amountDeleted = cupboard().withDatabase(sqLiteDatabase).delete(FavouriteManga.class, null, null);

                    subscriber.onNext(amountDeleted);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryRecentChapterFromRequest(final RequestWrapper request, final boolean isOffline) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.RecentChapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    if (isOffline) {
                        selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_OFFLINE + " = ?");
                        selectionArgs.add(String.valueOf(1));
                    } else {
                        selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_OFFLINE + " = ?");
                        selectionArgs.add(String.valueOf(0));
                    }

                    Cursor recentChapter = cupboard().withDatabase(sqLiteDatabase).query(RecentChapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(recentChapter);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryRecentChaptersOfMangaFromRequest(final RequestWrapper request, final boolean isOffline) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.RecentChapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_PARENT_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    if (isOffline) {
                        selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_OFFLINE + " = ?");
                        selectionArgs.add(String.valueOf(1));
                    } else {
                        selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_OFFLINE + " = ?");
                        selectionArgs.add(String.valueOf(0));
                    }

                    Cursor recentChapter = cupboard().withDatabase(sqLiteDatabase).query(RecentChapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .getCursor();

                    subscriber.onNext(recentChapter);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryRecentChaptersFromName(final String name) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor recentChaptersFromName = cupboard().withDatabase(sqLiteDatabase).query(RecentChapter.class)
                            .withSelection(ApplicationContract.RecentChapter.COLUMN_NAME + " LIKE ?", "%" + name + "%")
                            .orderBy(ApplicationContract.RecentChapter.COLUMN_DATE + " DESC")
                            .getCursor();

                    subscriber.onNext(recentChaptersFromName);

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Integer> deleteAllRecentChapters() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    int amountDeleted = cupboard().withDatabase(sqLiteDatabase).delete(RecentChapter.class, null, null);

                    subscriber.onNext(amountDeleted);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryDownloadMangaFromRequest(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.DownloadManga.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.DownloadManga.COLUMN_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    Cursor downloadManga = cupboard().withDatabase(sqLiteDatabase).query(DownloadManga.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(downloadManga);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryDownloadMangaFromName(final String name) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor downloadMangaCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadManga.class)
                            .withSelection(ApplicationContract.DownloadManga.COLUMN_NAME + " LIKE ?", "%" + name + "%")
                            .orderBy(ApplicationContract.DownloadManga.COLUMN_NAME + " ASC")
                            .getCursor();

                    subscriber.onNext(downloadMangaCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<DownloadManga> addDownloadMangaIfNone(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<DownloadManga>() {
            @Override
            public void call(Subscriber<? super DownloadManga> subscriber) {
                synchronized (mAddMangaLock) {
                    try {
                        ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                        SQLiteDatabase applicationDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.DownloadManga.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(request.getSource());
                        selection.append(" AND ").append(ApplicationContract.DownloadManga.COLUMN_URL + " = ?");
                        selectionArgs.add(request.getUrl());

                        DownloadManga mangaToDownload = null;

                        applicationDatabase.beginTransaction();
                        try {
                            mangaToDownload = cupboard().withDatabase(applicationDatabase).query(DownloadManga.class)
                                    .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                    .limit(1)
                                    .get();

                            if (mangaToDownload == null) {
                                Cursor mangaCursor = QueryManager.queryMangaFromRequest(request)
                                        .toBlocking()
                                        .single();

                                Manga requestManga = cupboard().withCursor(mangaCursor).get(Manga.class);

                                if (requestManga != null) {
                                    mangaToDownload = DefaultFactory.DownloadManga.constructDefault();
                                    mangaToDownload.setSource(requestManga.getSource());
                                    mangaToDownload.setUrl(requestManga.getUrl());
                                    mangaToDownload.setArtist(requestManga.getArtist());
                                    mangaToDownload.setAuthor(requestManga.getAuthor());
                                    mangaToDownload.setDescription(requestManga.getDescription());
                                    mangaToDownload.setGenre(requestManga.getGenre());
                                    mangaToDownload.setName(requestManga.getName());
                                    mangaToDownload.setCompleted(requestManga.isCompleted());
                                    mangaToDownload.setThumbnailUrl(requestManga.getThumbnailUrl());

                                    cupboard().withDatabase(applicationDatabase).put(mangaToDownload);
                                }
                            }

                            applicationDatabase.setTransactionSuccessful();
                        } finally {
                            applicationDatabase.endTransaction();
                        }

                        subscriber.onNext(mangaToDownload);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static Observable<Cursor> queryDownloadChapterFromRequest(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.DownloadChapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    Cursor downloadChapterCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .limit(1)
                            .getCursor();

                    subscriber.onNext(downloadChapterCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryDownloadChaptersOfDownloadManga(final RequestWrapper request, final boolean isCompleted) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.DownloadChapter.COLUMN_SOURCE + " = ?");
                    selectionArgs.add(request.getSource());
                    selection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_PARENT_URL + " = ?");
                    selectionArgs.add(request.getUrl());

                    if (isCompleted) {
                        selection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_FLAG + " = ?");
                        selectionArgs.add(String.valueOf(DownloadUtils.FLAG_COMPLETED));
                    }

                    Cursor downloadChaptersOfDownloadMangaCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .getCursor();

                    subscriber.onNext(downloadChaptersOfDownloadMangaCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryAvailableDownloadChapters(final int dequeueLimit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor runningDownloadChapterCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                            .withSelection(ApplicationContract.DownloadChapter.COLUMN_FLAG + " < ?", String.valueOf(DownloadUtils.FLAG_RUNNING))
                            .limit(dequeueLimit)
                            .getCursor();

                    subscriber.onNext(runningDownloadChapterCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryRunningDownloadChapters() {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor runningDownloadChapterCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                            .withSelection(ApplicationContract.DownloadChapter.COLUMN_FLAG + " = ?", String.valueOf(DownloadUtils.FLAG_RUNNING))
                            .getCursor();

                    subscriber.onNext(runningDownloadChapterCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryNonCompletedDownloadChapters() {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor nonCompletedDownloadChaptersCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                            .withSelection(ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?", String.valueOf(DownloadUtils.FLAG_COMPLETED))
                            .getCursor();

                    subscriber.onNext(nonCompletedDownloadChaptersCursor);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Integer> updateDownloadChapter(final Long id, final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    int amountUpdated = cupboard().withDatabase(sqLiteDatabase).update(DownloadChapter.class, updateValues, ApplicationContract.DownloadChapter.COLUMN_ID + " = ?", String.valueOf(id));

                    subscriber.onNext(amountUpdated);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Cursor> queryDownloadPagesOfDownloadChapter(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    Cursor downloadPagesOfDownloadChapterCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadPage.class)
                            .withSelection(ApplicationContract.DownloadPage.COLUMN_PARENT_URL + " = ?", request.getUrl())
                            .getCursor();

                    if (downloadPagesOfDownloadChapterCursor != null && downloadPagesOfDownloadChapterCursor.getCount() != 0) {
                        subscriber.onNext(downloadPagesOfDownloadChapterCursor);
                    } else {
                        throw new IllegalArgumentException("No DownloadPages of Download Chapter: " + request.getSource());
                    }

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<List<DownloadPage>> addDownloadPagesForDownloadChapter(final DownloadChapter downloadChapter, final List<String> imageUrls) {
        return Observable.create(new Observable.OnSubscribe<List<DownloadPage>>() {
            @Override
            public void call(Subscriber<? super List<DownloadPage>> subscriber) {
                synchronized (mAddPagesLock) {
                    try {
                        List<DownloadPage> downloadPageList = new ArrayList<DownloadPage>(imageUrls.size());
                        for (int index = 0; index < imageUrls.size(); index++) {
                            DownloadPage downloadPage = DefaultFactory.DownloadPage.constructDefault();
                            downloadPage.setUrl(imageUrls.get(index));
                            downloadPage.setParentUrl(downloadChapter.getUrl());
                            downloadPage.setDirectory(downloadChapter.getDirectory());
                            downloadPage.setName(String.valueOf(index));
                            downloadPage.setFlag(DownloadUtils.FLAG_PENDING);

                            downloadPageList.add(downloadPage);
                        }

                        ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                        SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                        sqLiteDatabase.beginTransaction();
                        try {
                            for (DownloadPage downloadPage : downloadPageList) {
                                cupboard().withDatabase(sqLiteDatabase).put(downloadPage);
                            }
                            sqLiteDatabase.setTransactionSuccessful();
                        } finally {
                            sqLiteDatabase.endTransaction();
                        }

                        subscriber.onNext(downloadPageList);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static Observable<Integer> updateDownloadPage(final Long id, final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    int amountUpdated = cupboard().withDatabase(sqLiteDatabase).update(DownloadPage.class, updateValues, ApplicationContract.DownloadPage.COLUMN_ID + " = ?", String.valueOf(id));

                    subscriber.onNext(amountUpdated);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Integer> deleteDownloadPagesOfDownloadChapter(final RequestWrapper request) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                    int amountDeleted = cupboard().withDatabase(sqLiteDatabase).delete(DownloadPage.class, ApplicationContract.DownloadPage.COLUMN_PARENT_URL + " = ?", request.getUrl());

                    subscriber.onNext(amountDeleted);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Boolean> queryShouldDownloadServiceStop() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    boolean shouldStop = false;

                    ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                    SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
                    StringBuilder selection = new StringBuilder();
                    List<String> selectionArgs = new ArrayList<String>();

                    selection.append(ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?");
                    selectionArgs.add(String.valueOf(DownloadUtils.FLAG_COMPLETED));
                    selection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?");
                    selectionArgs.add(String.valueOf(DownloadUtils.FLAG_CANCELED));

                    Cursor notCompletedCursor = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                            .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                            .getCursor();

                    if (notCompletedCursor != null) {
                        try {
                            if (notCompletedCursor.getCount() == 0) {
                                shouldStop = true;
                            }
                        } finally {
                            notCompletedCursor.close();
                            notCompletedCursor = null;
                        }
                    }

                    subscriber.onNext(shouldStop);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<Boolean> queryAllowDownloadServiceToPublishDownloadChapter(final DownloadChapter downloadChapter) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    boolean allowPublishing = false;

                    if (downloadChapter != null) {
                        ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
                        SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

                        DownloadChapter updatedDownloadChapter = cupboard().withDatabase(sqLiteDatabase).query(DownloadChapter.class)
                                .withSelection(ApplicationContract.DownloadChapter.COLUMN_ID + " = ?", String.valueOf(downloadChapter.getId()))
                                .limit(1)
                                .get();

                        if (updatedDownloadChapter  != null) {
                            if (updatedDownloadChapter .getFlag() != DownloadUtils.FLAG_CANCELED) {
                                allowPublishing = true;
                            }
                        }
                    }

                    subscriber.onNext(allowPublishing);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static <T> T toObject(Cursor objectCursor, Class<T> classType) {
        return cupboard().withCursor(objectCursor).get(classType);
    }

    public static <T> List<T> toList(Cursor listCursor, Class<T> classType) {
        return cupboard().withCursor(listCursor).list(classType);
    }

    public static void putObjectToApplicationDatabase(Object object) {
        ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
        SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

        cupboard().withDatabase(sqLiteDatabase).put(object);
    }

    public static void deleteObjectToApplicationDatabase(Object object) {
        ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
        SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();

        cupboard().withDatabase(sqLiteDatabase).delete(object);
    }
}
