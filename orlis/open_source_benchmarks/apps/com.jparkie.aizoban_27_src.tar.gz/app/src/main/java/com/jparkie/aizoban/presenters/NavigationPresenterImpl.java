package com.jparkie.aizoban.presenters;

import android.database.Cursor;
import android.os.Bundle;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.events.NavigationItemSelectEvent;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.presenters.mapper.NavigationMapper;
import com.jparkie.aizoban.utils.NavigationUtils;
import com.jparkie.aizoban.utils.wrappers.NavigationWrapper;
import com.jparkie.aizoban.views.NavigationView;
import com.jparkie.aizoban.views.adapters.NavigationAdapter;
import com.jparkie.aizoban.views.fragments.NavigationFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class NavigationPresenterImpl implements NavigationPresenter {
    public static final String TAG = NavigationPresenterImpl.class.getSimpleName();

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private NavigationView mNavigationView;
    private NavigationMapper mNavigationMapper;

    private NavigationAdapter mNavigationAdapter;
    private int mCurrentPosition;

    private Subscription mQueryRandomMangaSubscription;

    public NavigationPresenterImpl(NavigationView navigationView, NavigationMapper navigationMapper) {
        mNavigationView = navigationView;
        mNavigationMapper = navigationMapper;
    }

    @Override
    public void handleInitialArguments(Bundle arguments) {
        if (arguments != null) {
            if (arguments.containsKey(NavigationFragment.POSITION_ARGUMENT_KEY)) {
                mCurrentPosition = arguments.getInt(NavigationFragment.POSITION_ARGUMENT_KEY);

                arguments.remove(NavigationFragment.POSITION_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        mNavigationView.initializeAbsListView();
        mNavigationView.initializeSourceTextView(AizobanManager.getNameFromPreferenceSource().toBlocking().single());

        initializeThumbnailImageView();
    }

    @Override
    public void initializeNavigationFromResources() {
        List<NavigationWrapper> navigationItems = new ArrayList<NavigationWrapper>();
        navigationItems.add(NavigationUtils.POSITION_CATALOGUE, new NavigationWrapper(R.drawable.ic_photo_library_white_24dp, R.string.navigation_catalogue_title));
        navigationItems.add(NavigationUtils.POSITION_LATEST, new NavigationWrapper(R.drawable.ic_new_releases_white_24dp, R.string.navigation_latest_title));
        navigationItems.add(NavigationUtils.POSITION_EXPLORE, new NavigationWrapper(R.drawable.ic_explore_white_24dp, R.string.navigation_explore_title));
        navigationItems.add(NavigationUtils.POSITION_DOWNLOAD, new NavigationWrapper(R.drawable.ic_file_download_white_24dp, R.string.navigation_download_title));
        navigationItems.add(NavigationUtils.POSITION_FAVOURITE, new NavigationWrapper(R.drawable.ic_favourite_white_24dp, R.string.navigation_favourite_title));
        navigationItems.add(NavigationUtils.POSITION_RECENT, new NavigationWrapper(R.drawable.ic_history_white_24dp, R.string.navigation_recent_title));
        navigationItems.add(NavigationUtils.POSITION_QUEUE, new NavigationWrapper(R.drawable.ic_cloud_queue_white_24dp, R.string.navigation_queue_title));
        navigationItems.add(NavigationUtils.POSITION_SETTINGS, new NavigationWrapper(R.drawable.ic_settings_applications_white_24dp, R.string.navigation_settings_title));

        mNavigationAdapter = new NavigationAdapter(mNavigationView.getContext(), navigationItems, mCurrentPosition);
        mNavigationMapper.registerAdapter(mNavigationAdapter);

        mNavigationView.highlightPosition(mCurrentPosition);
    }

    @Override
    public void saveState(Bundle outState) {
        outState.putInt(POSITION_PARCELABLE_KEY, mCurrentPosition);
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mCurrentPosition = savedState.getInt(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryRandomMangaSubscription != null) {
            mQueryRandomMangaSubscription.unsubscribe();
            mQueryRandomMangaSubscription = null;
        }
    }

    @Override
    public void onNavigationItemClick(int position) {
        if (position != mCurrentPosition) {
            if (mCurrentPosition == NavigationUtils.POSITION_SETTINGS && position != NavigationUtils.POSITION_SETTINGS) {
                mNavigationView.initializeSourceTextView(AizobanManager.getNameFromPreferenceSource().toBlocking().single());
            }
            if (position != NavigationUtils.POSITION_EXPLORE) {
                mCurrentPosition = position;

                mNavigationAdapter.setCurrentPosition(mCurrentPosition);
            }

            mNavigationView.highlightPosition(mCurrentPosition);

            EventBus.getDefault().post(new NavigationItemSelectEvent(position));
        }
    }

    private void initializeThumbnailImageView() {
        mQueryRandomMangaSubscription = QueryManager
                .queryExploreMangaFromPreferenceSource()
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
                    public void onNext(Cursor randomCursor) {
                        if (randomCursor != null && randomCursor.getCount() != 0) {
                            Manga manga = cupboard().withCursor(randomCursor).get(Manga.class);
                            if (manga != null) {
                                mNavigationView.setThumbnail(manga.getThumbnailUrl());
                            }
                        }
                    }
                });
    }
}
