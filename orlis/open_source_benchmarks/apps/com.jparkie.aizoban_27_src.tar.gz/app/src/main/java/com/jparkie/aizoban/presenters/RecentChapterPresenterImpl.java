package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.databases.DatabaseService;
import com.jparkie.aizoban.controllers.events.RecentChapterQueryEvent;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.presenters.mapper.RecentChapterMapper;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.RecentChapterView;
import com.jparkie.aizoban.views.activities.ChapterActivity;
import com.jparkie.aizoban.views.adapters.RecentChapterAdapter;

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

public class RecentChapterPresenterImpl implements RecentChapterPresenter {
    public static final String TAG = RecentChapterPresenterImpl.class.getSimpleName();

    private static final String SEARCH_NAME_PARCELABLE_KEY = TAG + ":" + "SearchNameParcelableKey";

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private RecentChapterView mRecentChapterView;
    private RecentChapterMapper mRecentChapterMapper;
    private RecentChapterAdapter mRecentChapterAdapter;

    private String mSearchName;

    private Parcelable mPositionSavedState;

    private Subscription mQueryRecentChapterSubscription;
    private Subscription mSearchViewSubscription;
    private PublishSubject<Observable<String>> mSearchViewPublishSubject;

    public RecentChapterPresenterImpl(RecentChapterView recentChapterView, RecentChapterMapper recentChapterMapper) {
        mRecentChapterView = recentChapterView;
        mRecentChapterMapper = recentChapterMapper;

        mSearchName = "";
    }

    @Override
    public void initializeViews() {
        mRecentChapterView.initializeToolbar();
        mRecentChapterView.initializeAbsListView();
        mRecentChapterView.initializeEmptyRelativeLayout();
    }

    @Override
    public void initializeSearch() {
        mSearchViewPublishSubject = PublishSubject.create();
        mSearchViewSubscription = Observable.switchOnNext(mSearchViewPublishSubject)
                .debounce(SearchUtils.TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        queryRecentChaptersFromDatabase();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(String query) {
                        if (query != null) {
                            mSearchName = query;
                        }

                        onCompleted();
                    }
                });
    }

    @Override
    public void initializeDataFromDatabase() {
        mRecentChapterAdapter = new RecentChapterAdapter(mRecentChapterView.getContext());

        mRecentChapterMapper.registerAdapter(mRecentChapterAdapter);
    }

    @Override
    public void registerForEvents() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(RecentChapterQueryEvent event) {
        if (event != null) {
            queryRecentChaptersFromDatabase();
        }
    }

    @Override
    public void unregisterForEvents() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        queryRecentChaptersFromDatabase();
    }

    @Override
    public void saveState(Bundle outState) {
        if (mSearchName != null) {
            outState.putString(SEARCH_NAME_PARCELABLE_KEY, mSearchName);
        }
        if (mRecentChapterMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mRecentChapterMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(SEARCH_NAME_PARCELABLE_KEY)) {
            mSearchName = savedState.getString(SEARCH_NAME_PARCELABLE_KEY);

            savedState.remove(SEARCH_NAME_PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mPositionSavedState = savedState.getParcelable(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }
        if (mSearchViewSubscription != null) {
            mSearchViewSubscription.unsubscribe();
            mSearchViewSubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mRecentChapterAdapter != null) {
            mRecentChapterAdapter.setCursor(null);
            mRecentChapterAdapter = null;
        }
    }

    @Override
    public void onRecentChapterClick(int position) {
        if (mRecentChapterAdapter != null) {
            RecentChapter selectedRecentChapter = (RecentChapter)mRecentChapterAdapter.getItem(position);
            if (selectedRecentChapter != null) {
                String chapterSource = selectedRecentChapter.getSource();
                String chapterUrl = selectedRecentChapter.getUrl();
                int pageNumber = selectedRecentChapter.getPageNumber();

                Intent chapterIntent = null;
                if (!selectedRecentChapter.isOffline()) {
                    chapterIntent = ChapterActivity.constructOnlineChapterActivityIntent(mRecentChapterView.getContext(), new RequestWrapper(chapterSource, chapterUrl), pageNumber);
                } else {
                    chapterIntent = ChapterActivity.constructOfflineChapterActivityIntent(mRecentChapterView.getContext(), new RequestWrapper(chapterSource, chapterUrl), pageNumber);
                }

                mRecentChapterView.getContext().startActivity(chapterIntent);
            }
        }
    }

    @Override
    public void onQueryTextChange(String query) {
        if (mSearchViewPublishSubject != null) {
            mSearchViewPublishSubject.onNext(Observable.just(query));
        }
    }

    @Override
    public void onOptionToTop() {
        mRecentChapterView.scrollToTop();
    }

    @Override
    public void onOptionDelete() {
        if (mRecentChapterAdapter != null) {
            ArrayList<RecentChapter> recentChaptersToDelete = new ArrayList<RecentChapter>();

            SparseBooleanArray checkedItems = mRecentChapterMapper.getCheckedItemPositions();
            for (int index = 0; index < mRecentChapterAdapter.getCount(); index++) {
                if (checkedItems.get(index)) {
                    RecentChapter recentChapter = (RecentChapter)mRecentChapterAdapter.getItem(index);
                    recentChaptersToDelete.add(recentChapter);
                }
            }

            Intent startService = new Intent(mRecentChapterView.getContext(), DatabaseService.class);
            startService.putExtra(DatabaseService.INTENT_DELETE_RECENT_CHAPTERS, recentChaptersToDelete);
            mRecentChapterView.getContext().startService(startService);
        }
    }

    @Override
    public void onOptionSelectAll() {
        mRecentChapterView.selectAll();
    }

    @Override
    public void onOptionClear() {
        mRecentChapterView.clear();
    }

    private void queryRecentChaptersFromDatabase() {
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }

        if (mSearchName != null) {
            mQueryRecentChapterSubscription = QueryManager
                    .queryRecentChaptersFromName(mSearchName)
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
                            if (mRecentChapterAdapter != null) {
                                mRecentChapterAdapter.setCursor(cursor);
                            }

                            if (cursor != null) {
                                mRecentChapterView.hideEmptyRelativeLayout();
                            } else {
                                mRecentChapterView.showEmptyRelativeLayout();
                            }
                        }
                    });
        }
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mRecentChapterMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }
}
