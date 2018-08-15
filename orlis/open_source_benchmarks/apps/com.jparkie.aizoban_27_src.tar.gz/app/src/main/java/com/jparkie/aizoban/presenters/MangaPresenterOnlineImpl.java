package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.events.ChapterQueryEvent;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.presenters.mapper.MangaMapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.MangaView;
import com.jparkie.aizoban.views.activities.ChapterActivity;
import com.jparkie.aizoban.views.activities.MangaActivity;
import com.jparkie.aizoban.views.adapters.ChapterListingsAdapter;
import com.jparkie.aizoban.views.fragments.AddToQueueFragment;
import com.jparkie.aizoban.views.fragments.MarkReadFragment;
import com.jparkie.aizoban.views.fragments.ResumeChapterFragment;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;

public class MangaPresenterOnlineImpl implements MangaPresenter {
    public static final String TAG = MangaPresenterOnlineImpl.class.getSimpleName();

    private static final String REQUEST_PARCELABLE_KEY = TAG + ":" + "RequestParcelableKey";

    private static final String INITIALIZED_PARCELABLE_KEY = TAG + ":" + "InitializedParcelableKey";
    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private MangaView mMangaView;
    private MangaMapper mMangaMapper;
    private ChapterListingsAdapter mChapterListingsAdapter;

    private RequestWrapper mRequest;
    private Manga mManga;
    private com.jparkie.aizoban.models.databases.FavouriteManga mFavouriteManga;

    private boolean mInitialized;
    private Parcelable mPositionSavedState;

    private Subscription mQueryBothMangaAndChaptersSubscription;
    private Subscription mQueryFavouriteMangaSubscription;
    private Subscription mUpdateSubscription;
    private Subscription mQueryRecentChapterSubscription;

    public MangaPresenterOnlineImpl(MangaView mangaView, MangaMapper mangaMapper) {
        mMangaView = mangaView;
        mMangaMapper = mangaMapper;
    }

    @Override
    public void handleInitialArguments(Intent arguments) {
        if (arguments != null) {
            if (arguments.hasExtra(MangaActivity.REQUEST_ARGUMENT_KEY)) {
                mRequest = arguments.getParcelableExtra(MangaActivity.REQUEST_ARGUMENT_KEY);

                arguments.removeExtra(MangaActivity.REQUEST_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        mMangaView.initializeToolbar();
        mMangaView.initializeSwipeRefreshLayout();
        mMangaView.initializeAbsListView();
        mMangaView.initializeEmptyRelativeLayout();
    }

    @Override
    public void initializeDataFromUrl() {
        mChapterListingsAdapter = new ChapterListingsAdapter(mMangaView.getContext());

        mMangaMapper.registerAdapter(mChapterListingsAdapter);

        initializeFavouriteManga();

        if (!mInitialized) {
            updateDataFromUrl();
        }
    }

    @Override
    public void registerForEvents() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(ChapterQueryEvent event) {
        if (event != null) {
            queryBothMangaAndChaptersFromUrl();
        }
    }

    @Override
    public void unregisterForEvents() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        queryBothMangaAndChaptersFromUrl();
    }

    @Override
    public void saveState(Bundle outState) {
        if (mRequest != null) {
            outState.putParcelable(REQUEST_PARCELABLE_KEY, mRequest);
        }

        outState.putBoolean(INITIALIZED_PARCELABLE_KEY, mInitialized);

        if (mMangaMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mMangaMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(REQUEST_PARCELABLE_KEY)) {
            mRequest = savedState.getParcelable(REQUEST_PARCELABLE_KEY);

            savedState.remove(REQUEST_PARCELABLE_KEY);
        }
        if (savedState.containsKey(INITIALIZED_PARCELABLE_KEY)) {
            mInitialized = savedState.getBoolean(INITIALIZED_PARCELABLE_KEY, false);

            savedState.remove(INITIALIZED_PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mPositionSavedState = savedState.getParcelable(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryBothMangaAndChaptersSubscription != null) {
            mQueryBothMangaAndChaptersSubscription.unsubscribe();
            mQueryBothMangaAndChaptersSubscription = null;
        }
        if (mQueryFavouriteMangaSubscription != null) {
            mQueryFavouriteMangaSubscription.unsubscribe();
            mQueryFavouriteMangaSubscription = null;
        }
        if (mUpdateSubscription != null) {
            mUpdateSubscription.unsubscribe();
            mUpdateSubscription = null;
        }
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mChapterListingsAdapter != null) {
            mChapterListingsAdapter.setCursor(null);
            mChapterListingsAdapter = null;
        }
    }

    @Override
    public void onApplyColorChange(int color) {
        if (mChapterListingsAdapter != null) {
            mChapterListingsAdapter.setColor(color);
        }
    }

    @Override
    public void onSwipeRefresh() {
        updateDataFromUrl();
    }

    @Override
    public void onChapterClick(int position) {
        if (mChapterListingsAdapter != null) {
            Chapter selectedChapter = (Chapter) mChapterListingsAdapter.getItem(position);
            if (selectedChapter != null) {
                String chapterSource = selectedChapter.getSource();
                String chapterUrl = selectedChapter.getUrl();

                final RequestWrapper chapterRequest = new RequestWrapper(chapterSource, chapterUrl);

                if (mQueryRecentChapterSubscription != null) {
                    mQueryRecentChapterSubscription.unsubscribe();
                    mQueryRecentChapterSubscription = null;
                }

                mQueryRecentChapterSubscription = QueryManager
                        .queryRecentChapterFromRequest(chapterRequest, false)
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
                                // Do Nothing.
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNext(Cursor recentChapterCursor) {
                                if (recentChapterCursor != null) {
                                    RecentChapter recentChapter = QueryManager.toObject(recentChapterCursor, RecentChapter.class);
                                    if (recentChapter != null) {
                                        if (((FragmentActivity) mMangaView.getContext()).getSupportFragmentManager().findFragmentByTag(ResumeChapterFragment.TAG) == null) {
                                            ResumeChapterFragment resumeChapterFragment = ResumeChapterFragment.newInstance(recentChapter);

                                            resumeChapterFragment.show(((FragmentActivity) mMangaView.getContext()).getSupportFragmentManager(), ResumeChapterFragment.TAG);
                                        }

                                        return;
                                    }
                                }

                                Intent chapterIntent = ChapterActivity.constructOnlineChapterActivityIntent(mMangaView.getContext(), chapterRequest, 0);
                                mMangaView.getContext().startActivity(chapterIntent);
                            }
                        });
            }
        }
    }

    @Override
    public void onFavourite() {
        try {
            if (mManga != null) {
                if (mFavouriteManga != null) {
                    QueryManager.deleteObjectToApplicationDatabase(mFavouriteManga);

                    mFavouriteManga = null;

                    mMangaView.setFavouriteButton(false);
                } else {
                    mFavouriteManga = DefaultFactory.FavouriteManga.constructDefault();
                    mFavouriteManga.setSource(mManga.getSource());
                    mFavouriteManga.setUrl(mManga.getUrl());
                    mFavouriteManga.setName(mManga.getName());
                    mFavouriteManga.setThumbnailUrl(mManga.getThumbnailUrl());

                    QueryManager.putObjectToApplicationDatabase(mFavouriteManga);

                    mMangaView.setFavouriteButton(true);
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onOptionRefresh() {
        updateDataFromUrl();

        mMangaView.scrollToTop();
    }

    @Override
    public void onOptionMarkRead() {
        if (((FragmentActivity) mMangaView.getContext()).getSupportFragmentManager().findFragmentByTag(MarkReadFragment.TAG) == null) {
            MarkReadFragment markReadFragment = MarkReadFragment.newOnlineInstance(mRequest);

            markReadFragment.show(((FragmentActivity) mMangaView.getContext()).getSupportFragmentManager(), MarkReadFragment.TAG);
        }
    }

    @Override
    public void onOptionDownload() {
        if (((FragmentActivity) mMangaView.getContext()).getSupportFragmentManager().findFragmentByTag(AddToQueueFragment.TAG) == null) {
            AddToQueueFragment addToQueueFragment = AddToQueueFragment.newInstance(mRequest);

            addToQueueFragment.show(((FragmentActivity) mMangaView.getContext()).getSupportFragmentManager(), AddToQueueFragment.TAG);
        }
    }

    @Override
    public void onOptionToTop() {
        mMangaView.scrollToTop();
    }

    @Override
    public void onOptionDelete() {
        // Do Nothing.
    }

    @Override
    public void onOptionSelectAll() {
        // Do Nothing.
    }

    @Override
    public void onOptionClear() {
        // Do Nothing.
    }

    private void initializeFavouriteManga() {
        if (mQueryFavouriteMangaSubscription != null) {
            mQueryFavouriteMangaSubscription.unsubscribe();
            mQueryFavouriteMangaSubscription = null;
        }

        if (mRequest != null) {
            mQueryFavouriteMangaSubscription = QueryManager
                    .queryFavouriteMangaFromRequest(mRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Cursor>() {
                        @Override
                        public void onCompleted() {
                            if (mFavouriteManga != null) {
                                mMangaView.initializeFavouriteButton(true);
                            } else {
                                mMangaView.initializeFavouriteButton(false);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Cursor favouriteCursor) {
                            if (favouriteCursor != null && favouriteCursor.getCount() != 0) {
                                mFavouriteManga = QueryManager.toObject(favouriteCursor, com.jparkie.aizoban.models.databases.FavouriteManga.class);
                            }
                        }
                    });
        }
    }

    private void queryBothMangaAndChaptersFromUrl() {
        if (mQueryBothMangaAndChaptersSubscription != null) {
            mQueryBothMangaAndChaptersSubscription.unsubscribe();
            mQueryBothMangaAndChaptersSubscription = null;
        }

        if (mRequest != null) {
            Observable<Cursor> queryMangaFromUrlObservable = QueryManager
                    .queryMangaFromRequest(mRequest);
            Observable<Cursor> queryChaptersFromUrlObservable = QueryManager
                    .queryChaptersOfMangaFromRequest(mRequest, false);
            Observable<List<String>> queryRecentChapterUrlsObservable = QueryManager
                    .queryRecentChaptersOfMangaFromRequest(mRequest, false)
                    .flatMap(new Func1<Cursor, Observable<RecentChapter>>() {
                        @Override
                        public Observable<RecentChapter> call(Cursor recentChaptersCursor) {
                            List<RecentChapter> recentChapters = QueryManager.toList(recentChaptersCursor, RecentChapter.class);
                            return Observable.from(recentChapters.toArray(new RecentChapter[recentChapters.size()]));
                        }
                    })
                    .flatMap(new Func1<RecentChapter, Observable<String>>() {
                        @Override
                        public Observable<String> call(RecentChapter recentChapter) {
                            return Observable.just(recentChapter.getUrl());
                        }
                    })
                    .toList();

            mQueryBothMangaAndChaptersSubscription = Observable.zip(queryMangaFromUrlObservable, queryChaptersFromUrlObservable, queryRecentChapterUrlsObservable,
                    new Func3<Cursor, Cursor, List<String>, Pair<Pair<Cursor, Cursor>, List<String>>>() {
                        @Override
                        public Pair<Pair<Cursor, Cursor>, List<String>> call(Cursor mangaCursor, Cursor chaptersCursor, List<String> recentChapterUrls) {
                            Pair<Cursor, Cursor> cursorPair = Pair.create(mangaCursor, chaptersCursor);

                            return Pair.create(cursorPair, recentChapterUrls);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Pair<Pair<Cursor, Cursor>, List<String>>>() {
                        @Override
                        public void onCompleted() {
                            if (mManga != null) {
                                if (mManga.isInitialized()) {
                                    mMangaView.setTitle(mManga.getName());
                                    mMangaView.setName(mManga.getName());
                                    mMangaView.setDescription(mManga.getDescription());
                                    mMangaView.setAuthor(mManga.getAuthor());
                                    mMangaView.setArtist(mManga.getArtist());
                                    mMangaView.setGenre(mManga.getGenre());
                                    mMangaView.setIsCompleted(mManga.isCompleted());
                                    mMangaView.setThumbnail(mManga.getThumbnailUrl());

                                    mMangaView.hideEmptyRelativeLayout();
                                    mMangaView.showListViewIfHidden();
                                }
                            }

                            restorePosition();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Pair<Pair<Cursor, Cursor>, List<String>> pairListPair) {
                            Pair<Cursor, Cursor> cursorPair = pairListPair.first;
                            List<String> recentChapterUrls = pairListPair.second;

                            if (cursorPair != null) {
                                Cursor mangaCursor = cursorPair.first;
                                if (mangaCursor != null && mangaCursor.getCount() != 0) {
                                    mManga = QueryManager.toObject(mangaCursor, Manga.class);
                                }

                                Cursor chaptersCursor = cursorPair.second;
                                if (chaptersCursor != null && chaptersCursor.getCount() != 0) {
                                    mMangaView.hideChapterStatusError();
                                } else {
                                    mMangaView.showChapterStatusError();
                                }

                                if (mChapterListingsAdapter != null) {
                                    mChapterListingsAdapter.setCursor(chaptersCursor);
                                }
                            }

                            if (recentChapterUrls != null) {
                                if (mChapterListingsAdapter != null) {
                                    mChapterListingsAdapter.setRecentChapterUrls(recentChapterUrls);
                                }
                            }
                        }
                    });
        }
    }

    private void updateDataFromUrl() {
        if (mUpdateSubscription != null) {
            mUpdateSubscription.unsubscribe();
            mUpdateSubscription = null;
        }

        if (mRequest != null) {
            mMangaView.showRefreshing();

            Observable<Manga> updateMangaFromUrl = AizobanManager
                    .pullMangaFromNetwork(mRequest);
            Observable<List<Chapter>> updateChaptersFromUrl = AizobanManager
                    .pullChaptersFromNetwork(mRequest)
                    .onErrorReturn(new Func1<Throwable, List<Chapter>>() {
                        @Override
                        public List<Chapter> call(Throwable throwable) {
                            // Swallow Error with Empty Chapter List.
                            return null;
                        }
                    });

            mUpdateSubscription = Observable.zip(updateMangaFromUrl, updateChaptersFromUrl,
                    new Func2<Manga, List<Chapter>, Pair<Manga, List<Chapter>>>() {
                        @Override
                        public Pair<Manga, List<Chapter>> call(Manga manga, List<Chapter> chapterList) {
                            return Pair.create(manga, chapterList);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Pair<Manga, List<Chapter>>>() {
                        @Override
                        public void onCompleted() {
                            mMangaView.hideRefreshing();

                            queryBothMangaAndChaptersFromUrl();

                            mInitialized = true;
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }

                            mMangaView.hideRefreshing();
                            mMangaView.toastMangaError();
                        }

                        @Override
                        public void onNext(Pair<Manga, List<Chapter>> mangaListPair) {
                            // Do Nothing.
                        }
                    });
        }
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mMangaMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }
}
