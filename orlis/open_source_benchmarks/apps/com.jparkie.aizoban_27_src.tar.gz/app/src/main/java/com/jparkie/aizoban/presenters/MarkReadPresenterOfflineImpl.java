package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.databases.DatabaseService;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.presenters.mapper.MarkReadMapper;
import com.jparkie.aizoban.utils.wrappers.DownloadChapterSortCursorWrapper;
import com.jparkie.aizoban.utils.wrappers.RecentOfflineChapterFilteringCursorWrapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.MarkReadView;
import com.jparkie.aizoban.views.adapters.MarkReadOfflineAdapter;
import com.jparkie.aizoban.views.fragments.MarkReadFragment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class MarkReadPresenterOfflineImpl implements MarkReadPresenter {
    public static final String TAG = MarkReadPresenterOfflineImpl.class.getSimpleName();

    private static final String REQUEST_PARCELABLE_KEY = TAG + ":" + "RequestParcelableKey";

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private MarkReadView mMarkReadView;
    private MarkReadMapper mMarkReadMapper;
    private MarkReadOfflineAdapter mMarkReadOfflineAdapter;

    private RequestWrapper mRequest;

    private Parcelable mPositionSavedState;

    private Subscription mQueryBothChaptersSubscription;

    public MarkReadPresenterOfflineImpl(MarkReadView markReadView, MarkReadMapper markReadMapper) {
        mMarkReadView = markReadView;
        mMarkReadMapper = markReadMapper;
    }

    @Override
    public void handleInitialArguments(Bundle arguments) {
        if (arguments != null) {
            if (arguments.containsKey(MarkReadFragment.REQUEST_ARGUMENT_KEY)) {
                mRequest = arguments.getParcelable(MarkReadFragment.REQUEST_ARGUMENT_KEY);

                arguments.remove(MarkReadFragment.REQUEST_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        mMarkReadView.initializeEmptyRelativeLayout();
    }

    @Override
    public void initializeDataFromDatabase() {
        mMarkReadOfflineAdapter = new MarkReadOfflineAdapter(mMarkReadView.getContext());

        mMarkReadMapper.registerAdapter(mMarkReadOfflineAdapter);

        queryUnreadChaptersFromDatabase();
    }

    @Override
    public void overrideDialogButtons() {
        mMarkReadView.overrideToggleButton();
    }

    @Override
    public void saveState(Bundle outState) {
        if (mRequest != null) {
            outState.putParcelable(REQUEST_PARCELABLE_KEY, mRequest);
        }
        if (mMarkReadMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mMarkReadMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(REQUEST_PARCELABLE_KEY)) {
            mRequest = savedState.getParcelable(REQUEST_PARCELABLE_KEY);

            savedState.remove(REQUEST_PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mPositionSavedState = savedState.getParcelable(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryBothChaptersSubscription != null) {
            mQueryBothChaptersSubscription.unsubscribe();
            mQueryBothChaptersSubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mMarkReadOfflineAdapter != null) {
            mMarkReadOfflineAdapter.setCursor(null);
            mMarkReadOfflineAdapter = null;
        }
    }

    @Override
    public void onQueueButtonClick() {
        if (mMarkReadOfflineAdapter != null) {
            ArrayList<RecentChapter> recentChaptersToCreate = new ArrayList<RecentChapter>();

            SparseBooleanArray checkedItems = mMarkReadMapper.getCheckedItemPositions();
            for (int index = 0; index < mMarkReadOfflineAdapter.getCount(); index++) {
                if (checkedItems.get(index)) {
                    DownloadChapter downloadChapter = (DownloadChapter) mMarkReadOfflineAdapter.getItem(index);
                    if (downloadChapter != null) {
                        RecentChapter recentChapter = DefaultFactory.RecentChapter.constructDefault();
                        recentChapter.setSource(downloadChapter.getSource());
                        recentChapter.setUrl(downloadChapter.getUrl());
                        recentChapter.setParentUrl(downloadChapter.getParentUrl());
                        recentChapter.setName(downloadChapter.getName());
                        recentChapter.setOffline(true);
                        recentChapter.setThumbnailUrl(null);
                        recentChapter.setDate(System.currentTimeMillis());
                        recentChapter.setPageNumber(0);

                        recentChaptersToCreate.add(recentChapter);
                    }
                }
            }

            if (recentChaptersToCreate.size() != 0) {
                Intent startService = new Intent(mMarkReadView.getContext(), DatabaseService.class);
                startService.putExtra(DatabaseService.INTENT_CREATE_RECENT_CHAPTERS, recentChaptersToCreate);
                mMarkReadView.getContext().startService(startService);
            }
        }
    }

    @Override
    public void onToggleButtonClick() {
        if (mMarkReadMapper.getCheckedItemCount() == 0) {
            mMarkReadView.selectAll();
        } else {
            mMarkReadView.clear();
        }
    }

    private void queryUnreadChaptersFromDatabase() {
        if (mQueryBothChaptersSubscription != null) {
            mQueryBothChaptersSubscription.unsubscribe();
            mQueryBothChaptersSubscription = null;
        }

        if (mRequest != null) {
            Observable<Cursor> queryDownloadChaptersFromUrlObservable = QueryManager
                    .queryDownloadChaptersOfDownloadManga(mRequest, true);
            Observable<List<String>> queryChapterUrlsFromUrlObservable = QueryManager
                    .queryChaptersOfMangaFromRequest(mRequest, false)
                    .flatMap(new Func1<Cursor, Observable<Chapter>>() {
                        @Override
                        public Observable<Chapter> call(Cursor chapterCursor) {
                            List<Chapter> chapters = QueryManager.toList(chapterCursor, Chapter.class);
                            return Observable.from(chapters.toArray(new Chapter[chapters.size()]));
                        }
                    })
                    .flatMap(new Func1<Chapter, Observable<String>>() {
                        @Override
                        public Observable<String> call(Chapter chapter) {
                            return Observable.just(chapter.getUrl());
                        }
                    })
                    .toList();

            Observable<Cursor> querySortedDownloadChaptersObservable = Observable.zip(queryDownloadChaptersFromUrlObservable, queryChapterUrlsFromUrlObservable,
                    new Func2<Cursor, List<String>, Cursor>() {
                        @Override
                        public Cursor call(Cursor downloadChapterCursor, List<String> sortedChapterUrls) {
                            return new DownloadChapterSortCursorWrapper(downloadChapterCursor, sortedChapterUrls);
                        }
                    });

            Observable<List<String>> queryRecentChaptersFromUrlObservable = QueryManager
                    .queryRecentChaptersOfMangaFromRequest(mRequest, true)
                    .flatMap(new Func1<Cursor, Observable<RecentChapter>>() {
                        @Override
                        public Observable<RecentChapter> call(Cursor recentChapterCursor) {
                            List<RecentChapter> recentChapters = QueryManager.toList(recentChapterCursor, RecentChapter.class);
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

            mQueryBothChaptersSubscription = Observable.zip(querySortedDownloadChaptersObservable, queryRecentChaptersFromUrlObservable,
                    new Func2<Cursor, List<String>, Cursor>() {
                        @Override
                        public Cursor call(Cursor downloadChaptersCursor, List<String> recentChapterUrls) {
                            return new RecentOfflineChapterFilteringCursorWrapper(downloadChaptersCursor, recentChapterUrls);
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
                        public void onNext(Cursor filteredCursor) {
                            if (mMarkReadOfflineAdapter != null) {
                                mMarkReadOfflineAdapter.setCursor(filteredCursor);
                            }

                            if (filteredCursor != null && filteredCursor.getCount() != 0) {
                                mMarkReadView.hideEmptyRelativeLayout();
                            } else {
                                mMarkReadView.showEmptyRelativeLayout();
                            }
                        }
                    });
        }
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mMarkReadMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }
}
