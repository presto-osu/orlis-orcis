package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.controllers.sources.UpdatePageMarker;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.presenters.mapper.LatestMangaMapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.LatestMangaView;
import com.jparkie.aizoban.views.activities.MangaActivity;
import com.jparkie.aizoban.views.adapters.LatestMangaAdapter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LatestMangaPresenterImpl implements LatestMangaPresenter {
    public static final String TAG = LatestMangaPresenterImpl.class.getSimpleName();

    private static final String ALLOW_LOADING_PARCElABLE_KEY = TAG + ":" + "AllowLoadingParcelableKey";

    private static final String INITIALIZED_PARCELABLE_KEY = TAG + ":" + "InitializedParcelableKey";
    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private LatestMangaView mLatestMangaView;
    private LatestMangaMapper mLatestMangaMapper;
    private LatestMangaAdapter mLatestMangaAdapter;

    private boolean mAllowLoading;
    private boolean mIsLoading;
    private UpdatePageMarker mCurrentUpdatePageMarker;

    private boolean mInitialized;
    private Parcelable mPositionSavedState;

    private Subscription mQueryLatestMangaSubscription;
    private Subscription mUpdateLibrarySubscription;

    public LatestMangaPresenterImpl(LatestMangaView latestMangaView, LatestMangaMapper latestMangaMapper) {
        mLatestMangaView = latestMangaView;
        mLatestMangaMapper = latestMangaMapper;

        mAllowLoading = true;
        mIsLoading = false;
        mCurrentUpdatePageMarker = DefaultFactory.UpdatePageMarker.constructDefault();
    }

    @Override
    public void initializeViews() {
        mLatestMangaView.initializeToolbar();
        mLatestMangaView.initializeSwipeRefreshLayout();
        mLatestMangaView.initializeAbsListView();
        mLatestMangaView.initializeEmptyRelativeLayout();
    }

    @Override
    public void initializeDataFromPreferenceSource() {
        mLatestMangaAdapter = new LatestMangaAdapter(mLatestMangaView.getContext());
        mLatestMangaAdapter.setOnLatestPositionListener(new LatestMangaAdapter.OnLatestPositionListener() {
            @Override
            public void onLatestPosition(int position) {
                if (shouldUpdateLibrary(position)) {
                    updateDataFromPreferenceSource();
                }
            }
        });

        mLatestMangaMapper.registerAdapter(mLatestMangaAdapter);

        queryLatestMangaFromPreferenceSource();

        if (!mInitialized) {
            updateDataFromPreferenceSource();
        }
    }

    @Override
    public void saveState(Bundle outState) {
        outState.putBoolean(ALLOW_LOADING_PARCElABLE_KEY, mAllowLoading);

        if (mCurrentUpdatePageMarker != null) {
            outState.putParcelable(UpdatePageMarker.PARCELABLE_KEY, mCurrentUpdatePageMarker);
        }

        outState.putBoolean(INITIALIZED_PARCELABLE_KEY, mInitialized);

        if (mLatestMangaMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mLatestMangaMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(ALLOW_LOADING_PARCElABLE_KEY)) {
            mAllowLoading = savedState.getBoolean(ALLOW_LOADING_PARCElABLE_KEY);

            savedState.remove(ALLOW_LOADING_PARCElABLE_KEY);
        }
        if (savedState.containsKey(UpdatePageMarker.PARCELABLE_KEY)) {
            mCurrentUpdatePageMarker = savedState.getParcelable(UpdatePageMarker.PARCELABLE_KEY);

            savedState.remove(UpdatePageMarker.PARCELABLE_KEY);
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
        if (mQueryLatestMangaSubscription != null) {
            mQueryLatestMangaSubscription.unsubscribe();
            mQueryLatestMangaSubscription = null;
        }
        if (mUpdateLibrarySubscription != null) {
            mUpdateLibrarySubscription.unsubscribe();
            mUpdateLibrarySubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mLatestMangaAdapter != null) {
            mLatestMangaAdapter.setCursor(null);
            mLatestMangaAdapter = null;
        }
    }

    @Override
    public void onMangaClick(int position) {
        if (mLatestMangaAdapter != null) {
            Manga selectedManga = (Manga) mLatestMangaAdapter.getItem(position);
            if (selectedManga != null) {
                String mangaSource = selectedManga.getSource();
                String mangaUrl = selectedManga.getUrl();

                Intent mangaIntent = MangaActivity.constructOnlineMangaActivityIntent(mLatestMangaView.getContext(), new RequestWrapper(mangaSource, mangaUrl));
                mLatestMangaView.getContext().startActivity(mangaIntent);
            }
        }
    }

    @Override
    public void onSwipeRefresh() {
        refreshDataFromPreferenceSource();
    }

    @Override
    public void onOptionRefresh() {
        refreshDataFromPreferenceSource();

        mLatestMangaView.scrollToTop();
    }

    @Override
    public void onOptionToTop() {
        mLatestMangaView.scrollToTop();
    }

    private boolean shouldUpdateLibrary(int position) {
        return mAllowLoading && !mIsLoading && (position > mCurrentUpdatePageMarker.getLastMangaPosition() * 0.75);
    }

    private void queryLatestMangaFromPreferenceSource() {
        if (mQueryLatestMangaSubscription != null) {
            mUpdateLibrarySubscription.unsubscribe();
            mUpdateLibrarySubscription = null;
        }

        mQueryLatestMangaSubscription = QueryManager
                .queryLatestMangasFromPreferenceSource()
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
                        if (mLatestMangaAdapter != null) {
                            mLatestMangaAdapter.setCursor(cursor);
                        }

                        if (cursor != null) {
                            mLatestMangaView.hideEmptyRelativeLayout();
                        }
                    }
                });
    }

    private void updateDataFromPreferenceSource() {
        if (mUpdateLibrarySubscription != null) {
            mUpdateLibrarySubscription.unsubscribe();
            mUpdateLibrarySubscription = null;
        }

        mIsLoading = true;

        mLatestMangaView.showRefreshing();

        mUpdateLibrarySubscription = AizobanManager
                .pullLatestUpdatesFromNetwork(mCurrentUpdatePageMarker)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UpdatePageMarker>() {
                    @Override
                    public void onCompleted() {
                        mAllowLoading = true;
                        mIsLoading = false;

                        mLatestMangaView.hideRefreshing();

                        queryLatestMangaFromPreferenceSource();

                        mInitialized = true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        mAllowLoading = false;
                        mIsLoading = false;

                        mLatestMangaView.hideRefreshing();
                        mLatestMangaView.toastLatestError();

                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(UpdatePageMarker updatePageMarker) {
                        mCurrentUpdatePageMarker.appendUpdatePageMarker(updatePageMarker);
                    }
                });
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mLatestMangaMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }

    private void refreshDataFromPreferenceSource() {
        mCurrentUpdatePageMarker = DefaultFactory.UpdatePageMarker.constructDefault();
        mAllowLoading = true;
        mIsLoading = false;

        updateDataFromPreferenceSource();
    }
}