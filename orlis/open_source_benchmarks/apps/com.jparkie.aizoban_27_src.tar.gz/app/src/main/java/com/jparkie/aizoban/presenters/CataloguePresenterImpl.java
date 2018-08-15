package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.events.SearchCatalogueWrapperSubmitEvent;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.presenters.mapper.CatalogueMapper;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;
import com.jparkie.aizoban.views.CatalogueView;
import com.jparkie.aizoban.views.activities.MangaActivity;
import com.jparkie.aizoban.views.adapters.CatalogueAdapter;
import com.jparkie.aizoban.views.fragments.CatalogueFilterFragment;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class CataloguePresenterImpl implements CataloguePresenter {
    public static final String TAG = CataloguePresenterImpl.class.getSimpleName();

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private CatalogueView mCatalogueView;
    private CatalogueMapper mCatalogueMapper;
    private CatalogueAdapter mCatalogueAdapter;

    private SearchCatalogueWrapper mSearchCatalogueWrapper;

    private Parcelable mPositionSavedState;

    private Subscription mQueryCatalogueMangaSubscription;
    private Subscription mSearchViewSubscription;
    private PublishSubject<Observable<String>> mSearchViewPublishSubject;

    public CataloguePresenterImpl(CatalogueView catalogueView, CatalogueMapper catalogueMapper) {
        mCatalogueView = catalogueView;
        mCatalogueMapper = catalogueMapper;

        mSearchCatalogueWrapper = DefaultFactory.SearchCatalogueWrapper.constructDefault();
    }

    @Override
    public void initializeViews() {
        mCatalogueView.initializeToolbar();
        mCatalogueView.initializeAbsListView();
        mCatalogueView.initializeEmptyRelativeLayout();
        mCatalogueView.initializeButtons();
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
                        queryCatalogueMangaFromPreferenceSource();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(String query) {
                        if (mSearchCatalogueWrapper != null) {
                            mSearchCatalogueWrapper.setNameArgs(query);
                            mSearchCatalogueWrapper.setOffsetArgs(DefaultFactory.SearchCatalogueWrapper.DEFAULT_OFFSET);
                        }

                        onCompleted();
                    }
                });
    }

    @Override
    public void initializeDataFromPreferenceSource() {
        mCatalogueAdapter = new CatalogueAdapter(mCatalogueView.getContext());

        mCatalogueMapper.registerAdapter(mCatalogueAdapter);

        queryCatalogueMangaFromPreferenceSource();
    }

    @Override
    public void registerForEvents() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(SearchCatalogueWrapperSubmitEvent event) {
        if (event != null && event.getSearchCatalogueWrapper() != null) {
            mSearchCatalogueWrapper = event.getSearchCatalogueWrapper();

            queryCatalogueMangaFromPreferenceSource();
        }
    }

    @Override
    public void unregisterForEvents() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void saveState(Bundle outState) {
        if (mSearchCatalogueWrapper != null) {
            outState.putParcelable(SearchCatalogueWrapper.PARCELABLE_KEY, mSearchCatalogueWrapper);
        }
        if (mCatalogueMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mCatalogueMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(SearchCatalogueWrapper.PARCELABLE_KEY)) {
            mSearchCatalogueWrapper = savedState.getParcelable(SearchCatalogueWrapper.PARCELABLE_KEY);

            savedState.remove(SearchCatalogueWrapper.PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mPositionSavedState = savedState.getParcelable(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryCatalogueMangaSubscription != null) {
            mQueryCatalogueMangaSubscription.unsubscribe();
            mQueryCatalogueMangaSubscription = null;
        }
        if (mSearchViewSubscription != null) {
            mSearchViewSubscription.unsubscribe();
            mSearchViewSubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mCatalogueAdapter != null) {
            mCatalogueAdapter.setCursor(null);
            mCatalogueAdapter = null;
        }
    }

    @Override
    public void onMangaClick(int position) {
        if (mCatalogueAdapter != null) {
            Manga selectedManga = (Manga) mCatalogueAdapter.getItem(position);
            if (selectedManga != null) {
                String mangaSource = selectedManga.getSource();
                String mangaUrl = selectedManga.getUrl();

                Intent mangaIntent = MangaActivity.constructOnlineMangaActivityIntent(mCatalogueView.getContext(), new RequestWrapper(mangaSource, mangaUrl));
                mCatalogueView.getContext().startActivity(mangaIntent);
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
    public void onPreviousClick() {
        if (mSearchCatalogueWrapper != null) {
            int currentOffset = mSearchCatalogueWrapper.getOffsetArgs();
            if (currentOffset - SearchUtils.LIMIT_COUNT >= 0) {
                mSearchCatalogueWrapper.setOffsetArgs(currentOffset - SearchUtils.LIMIT_COUNT);

                queryCatalogueMangaFromPreferenceSource();

                return;
            }
        }

        mCatalogueView.toastNoPreviousPage();
    }

    @Override
    public void onNextClick() {
        if (mSearchCatalogueWrapper != null) {
            if (mCatalogueAdapter != null) {
                if (mCatalogueAdapter.getCount() == SearchUtils.LIMIT_COUNT) {
                    int currentOffset = mSearchCatalogueWrapper.getOffsetArgs();

                    mSearchCatalogueWrapper.setOffsetArgs(currentOffset + SearchUtils.LIMIT_COUNT);

                    queryCatalogueMangaFromPreferenceSource();

                    return;
                }
            }
        }

        mCatalogueView.toastNoNextPage();
    }

    @Override
    public void onOptionFilter() {
        if (((FragmentActivity)mCatalogueView.getContext()).getSupportFragmentManager().findFragmentByTag(CatalogueFilterFragment.TAG) == null) {
            CatalogueFilterFragment catalogueFilterFragment = CatalogueFilterFragment.newInstance(mSearchCatalogueWrapper);

            catalogueFilterFragment.show(((FragmentActivity)mCatalogueView.getContext()).getSupportFragmentManager(), CatalogueFilterFragment.TAG);
        }
    }

    @Override
    public void onOptionToTop() {
        mCatalogueView.scrollToTop();
    }

    private void queryCatalogueMangaFromPreferenceSource() {
        if (mQueryCatalogueMangaSubscription != null) {
            mQueryCatalogueMangaSubscription.unsubscribe();
            mQueryCatalogueMangaSubscription = null;
        }

        if (mSearchCatalogueWrapper != null) {
            mQueryCatalogueMangaSubscription = QueryManager
                    .queryCatalogueMangasFromPreferenceSource(mSearchCatalogueWrapper)
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
                            if (mCatalogueAdapter != null) {
                                mCatalogueAdapter.setCursor(cursor);
                            }

                            if (cursor != null) {
                                mCatalogueView.hideEmptyRelativeLayout();
                            } else {
                                mCatalogueView.showEmptyRelativeLayout();
                            }

                            mCatalogueView.setSubtitlePositionText(getPageNumber());
                        }
                    });
        }
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mCatalogueMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }

    private int getPageNumber() {
        return (mSearchCatalogueWrapper != null) ? (mSearchCatalogueWrapper.getOffsetArgs() + SearchUtils.LIMIT_COUNT) / SearchUtils.LIMIT_COUNT : 0;
    }
}
