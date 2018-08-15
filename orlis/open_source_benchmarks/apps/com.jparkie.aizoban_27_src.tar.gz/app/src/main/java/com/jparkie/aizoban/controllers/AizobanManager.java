package com.jparkie.aizoban.controllers;

import android.content.ContentValues;
import android.database.Cursor;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.controllers.caches.CacheProvider;
import com.jparkie.aizoban.controllers.databases.ApplicationContract;
import com.jparkie.aizoban.controllers.events.DownloadChapterQueryEvent;
import com.jparkie.aizoban.controllers.factories.SourceFactory;
import com.jparkie.aizoban.controllers.networks.MangaService;
import com.jparkie.aizoban.controllers.sources.UpdatePageMarker;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.models.downloads.DownloadPage;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AizobanManager {
    private AizobanManager() {
        throw new AssertionError();
    }

    public static Observable<String> getNameFromPreferenceSource() {
        return SourceFactory.constructSourceFromPreferences().getName();
    }

    public static Observable<String> getBaseUrlFromPreferenceSource() {
        return SourceFactory.constructSourceFromPreferences().getBaseUrl();
    }

    public static Observable<String> getInitialUpdateUrlFromPreferenceSource() {
        return SourceFactory.constructSourceFromPreferences().getInitialUpdateUrl();
    }

    public static Observable<List<String>> getGenresFromPreferenceSource() {
        return SourceFactory.constructSourceFromPreferences().getGenres();
    }

    public static Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(final UpdatePageMarker newUpdate) {
        return SourceFactory.constructSourceFromPreferences().pullLatestUpdatesFromNetwork(newUpdate);
    }

    public static Observable<Manga> pullMangaFromNetwork(final RequestWrapper request) {
        return SourceFactory.constructSourceFromName(request.getSource()).pullMangaFromNetwork(request);
    }

    public static Observable<List<Chapter>> pullChaptersFromNetwork(final RequestWrapper request) {
        return SourceFactory.constructSourceFromName(request.getSource()).pullChaptersFromNetwork(request);
    }

    public static Observable<String> pullImageUrlsFromNetwork(final RequestWrapper request) {
        return AizobanManager.getImageUrlsFromDiskCache(request.getUrl())
                .onBackpressureBuffer()
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return SourceFactory.constructSourceFromName(request.getSource()).pullImageUrlsFromNetwork(request);
                    }
                });
    }

    public static Observable<File> downloadChapterFromNetwork(final DownloadChapter downloadChapter) {
        final RequestWrapper downloadRequest = new RequestWrapper(downloadChapter.getSource(), downloadChapter.getUrl());
        final MangaService mangaService = MangaService.getTemporaryInstance();

        final AtomicBoolean isUnsubscribed = new AtomicBoolean(false);

        return QueryManager
                .queryDownloadPagesOfDownloadChapter(downloadRequest)
                .flatMap(new Func1<Cursor, Observable<List<DownloadPage>>>() {
                    @Override
                    public Observable<List<DownloadPage>> call(Cursor downloadPagesCursor) {
                        return Observable.just(QueryManager.toList(downloadPagesCursor, DownloadPage.class));
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends List<DownloadPage>>>() {
                    @Override
                    public Observable<? extends List<DownloadPage>> call(Throwable throwable) {
                        return AizobanManager
                                .pullImageUrlsFromNetwork(downloadRequest)
                                .subscribeOn(Schedulers.io())
                                .toList()
                                .flatMap(new Func1<List<String>, Observable<List<DownloadPage>>>() {
                                    @Override
                                    public Observable<List<DownloadPage>> call(List<String> imageUrls) {
                                        return QueryManager.addDownloadPagesForDownloadChapter(downloadChapter, imageUrls);
                                    }
                                });
                    }
                })
                .doOnNext(new Action1<List<DownloadPage>>() {
                    @Override
                    public void call(List<DownloadPage> downloadPages) {
                        ContentValues updateValues = new ContentValues(1);
                        updateValues.put(ApplicationContract.DownloadChapter.COLUMN_TOTAL_PAGES, downloadPages.size());

                        QueryManager.updateDownloadChapter(downloadChapter.getId(), updateValues)
                                .toBlocking()
                                .single();

                        EventBus.getDefault().post(new DownloadChapterQueryEvent());
                    }
                })
                .flatMap(new Func1<List<DownloadPage>, Observable<File>>() {
                    @Override
                    public Observable<File> call(final List<DownloadPage> downloadPages) {
                        return Observable.from(downloadPages.toArray(new DownloadPage[downloadPages.size()]))
                                .filter(new Func1<DownloadPage, Boolean>() {
                                    @Override
                                    public Boolean call(DownloadPage downloadPage) {
                                        return downloadPage.getFlag() != DownloadUtils.FLAG_COMPLETED;
                                    }
                                })
                                .flatMap(new Func1<DownloadPage, Observable<File>>() {
                                    @Override
                                    public Observable<File> call(final DownloadPage downloadPage) {
                                        return mangaService
                                                .getResponse(downloadPage.getUrl())
                                                .flatMap(new Func1<Response, Observable<File>>() {
                                                    @Override
                                                    public Observable<File> call(Response response) {
                                                        String fileDirectory = downloadPage.getDirectory();
                                                        String fileName = downloadPage.getName();
                                                        String fileType = response.body().contentType().subtype();
                                                        InputStream fileData = response.body().byteStream();

                                                        return AizobanManager.saveInputStreamToDirectory(fileData, fileDirectory, fileName + "." + fileType);
                                                    }
                                                })
                                                .doOnCompleted(new Action0() {
                                                    @Override
                                                    public void call() {
                                                        ContentValues pageValues = new ContentValues(1);
                                                        pageValues.put(ApplicationContract.DownloadPage.COLUMN_FLAG, DownloadUtils.FLAG_COMPLETED);
                                                        QueryManager.updateDownloadPage(downloadPage.getId(), pageValues)
                                                                .toBlocking()
                                                                .single();

                                                        ContentValues chapterValues = new ContentValues(1);
                                                        chapterValues.put(ApplicationContract.DownloadChapter.COLUMN_CURRENT_PAGE, downloadPages.indexOf(downloadPage) + 1);
                                                        QueryManager.updateDownloadChapter(downloadChapter.getId(), chapterValues)
                                                                .toBlocking()
                                                                .single();

                                                        EventBus.getDefault().post(new DownloadChapterQueryEvent());
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        isUnsubscribed.set(true);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (!isUnsubscribed.get()) {
                            ContentValues updateValues = new ContentValues(1);
                            updateValues.put(ApplicationContract.DownloadChapter.COLUMN_FLAG, DownloadUtils.FLAG_FAILED);

                            QueryManager.updateDownloadChapter(downloadChapter.getId(), updateValues)
                                    .toBlocking()
                                    .single();

                            EventBus.getDefault().post(new DownloadChapterQueryEvent());
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Cursor downloadChapterCursor = QueryManager.queryDownloadChapterFromRequest(downloadRequest)
                                .toBlocking()
                                .single();

                        if (downloadChapterCursor != null) {
                            DownloadChapter updatedDownloadChapter = QueryManager.toObject(downloadChapterCursor, DownloadChapter.class);

                            if (updatedDownloadChapter != null) {
                                if (updatedDownloadChapter.getCurrentPage() != 0 && updatedDownloadChapter.getTotalPages() != 0) {
                                    if (updatedDownloadChapter.getCurrentPage() == updatedDownloadChapter.getTotalPages()) {
                                        QueryManager.deleteDownloadPagesOfDownloadChapter(downloadRequest)
                                                .toBlocking()
                                                .single();

                                        ContentValues updateValues = new ContentValues(1);
                                        updateValues.put(ApplicationContract.DownloadChapter.COLUMN_FLAG, DownloadUtils.FLAG_COMPLETED);

                                        QueryManager.updateDownloadChapter(downloadChapter.getId(), updateValues)
                                                .toBlocking()
                                                .single();

                                        QueryManager.addDownloadMangaIfNone(new RequestWrapper(downloadChapter.getSource(), downloadChapter.getParentUrl()))
                                                .toBlocking()
                                                .single();
                                    }
                                }
                            }
                        }

                        EventBus.getDefault().post(new DownloadChapterQueryEvent());
                    }
                });
    }

    public static Observable<GlideDrawable> cacheFromImagesOfSize(final List<String> imageUrls) {
        return Observable.create(new Observable.OnSubscribe<GlideDrawable>() {
            @Override
            public void call(Subscriber<? super GlideDrawable> subscriber) {
                try {
                    for (String imageUrl : imageUrls) {
                        if (!subscriber.isUnsubscribed()) {
                            FutureTarget<GlideDrawable> cacheFuture = Glide.with(AizobanApplication.getInstance())
                                    .load(imageUrl)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

                            subscriber.onNext(cacheFuture.get(MangaService.READ_TIMEOUT, TimeUnit.SECONDS));
                        }
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread());
    }

    public static Observable<Boolean> clearImageCache() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    boolean isSuccessful = true;

                    File imageCacheDirectory = Glide.getPhotoCacheDir(AizobanApplication.getInstance());
                    if (imageCacheDirectory.isDirectory()) {
                        for (File cachedFile : imageCacheDirectory.listFiles()) {
                            if (!cachedFile.delete()) {
                                isSuccessful = false;
                            }
                        }
                    } else {
                        isSuccessful = false;
                    }

                    File urlCacheDirectory = CacheProvider.getInstance().getCacheDir();
                    if (urlCacheDirectory.isDirectory()) {
                        for (File cachedFile : urlCacheDirectory.listFiles()) {
                            if (!cachedFile.delete()) {
                                isSuccessful = false;
                            }
                        }
                    } else {
                        isSuccessful = false;
                    }

                    subscriber.onNext(isSuccessful);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Observable<String> getImageUrlsFromDiskCache(final String chapterUrl) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String[] imageUrls = CacheProvider.getInstance().getImageUrlsFromDiskCache(chapterUrl);

                    for (String imageUrl : imageUrls) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(imageUrl);
                        }
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Observable<File> saveInputStreamToDirectory(final InputStream inputStream, final String directory, final String name) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                try {
                    subscriber.onNext(DiskUtils.saveInputStreamToDirectory(inputStream, directory, name));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
