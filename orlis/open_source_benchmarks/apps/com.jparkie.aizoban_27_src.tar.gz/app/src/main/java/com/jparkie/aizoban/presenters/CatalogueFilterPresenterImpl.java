package com.jparkie.aizoban.presenters;

import android.os.Bundle;
import android.os.Parcelable;

import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.events.SearchCatalogueWrapperSubmitEvent;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.presenters.mapper.CatalogueFilterMapper;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;
import com.jparkie.aizoban.views.CatalogueFilterView;
import com.jparkie.aizoban.views.adapters.CatalogueFilterAdapter;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CatalogueFilterPresenterImpl implements CatalogueFilterPresenter {
    public static final String TAG = CatalogueFilterPresenterImpl.class.getSimpleName();

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private CatalogueFilterView mCatalogueFilterView;
    private CatalogueFilterMapper mCatalogueFilterMapper;

    private SearchCatalogueWrapper mSearchCatalogueWrapper;

    private Parcelable mPositionSavedState;

    public CatalogueFilterPresenterImpl(CatalogueFilterView catalogueFilterView, CatalogueFilterMapper catalogueFilterMapper) {
        mCatalogueFilterView = catalogueFilterView;
        mCatalogueFilterMapper = catalogueFilterMapper;
    }

    @Override
    public void handleInitialArguments(Bundle arguments) {
        if (arguments != null) {
            if (arguments.containsKey(SearchCatalogueWrapper.PARCELABLE_KEY)) {
                mSearchCatalogueWrapper = arguments.getParcelable(SearchCatalogueWrapper.PARCELABLE_KEY);

                arguments.remove(SearchCatalogueWrapper.PARCELABLE_KEY);
            }
        }
    }

    @Override
    public void initializeDataFromPreferenceSource() {
        if (mSearchCatalogueWrapper != null) {
            List<String> availableGenres = AizobanManager.getGenresFromPreferenceSource()
                    .toBlocking()
                    .single();

            mCatalogueFilterMapper.registerAdapter(new CatalogueFilterAdapter(mCatalogueFilterView.getContext(), availableGenres, mSearchCatalogueWrapper.getGenresArgs()));
            mCatalogueFilterMapper.setSelectedStatus(mSearchCatalogueWrapper.getStatusArgs());
            mCatalogueFilterMapper.setSelectedOrderBy(mSearchCatalogueWrapper.getOrderByArgs());

            restorePosition();

            if (availableGenres.size() == 0) {
                mCatalogueFilterView.hideGenres();
            }
        }
    }

    @Override
    public void overrideDialogButtons() {
        mCatalogueFilterView.overrideClearButton();
    }

    @Override
    public void saveState(Bundle outState) {
        if (mSearchCatalogueWrapper != null) {
            mSearchCatalogueWrapper.setGenresArgs(mCatalogueFilterMapper.getSelectedGenres());
            mSearchCatalogueWrapper.setStatusArgs(mCatalogueFilterMapper.getSelectedStatus());
            mSearchCatalogueWrapper.setOrderByArgs(mCatalogueFilterMapper.getSelectedOrderBy());

            outState.putParcelable(SearchCatalogueWrapper.PARCELABLE_KEY, mSearchCatalogueWrapper);
        }
        if (mCatalogueFilterMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mCatalogueFilterMapper.getPositionState());
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
    public void onFilterButtonClick() {
        mSearchCatalogueWrapper.setGenresArgs(mCatalogueFilterMapper.getSelectedGenres());
        mSearchCatalogueWrapper.setStatusArgs(mCatalogueFilterMapper.getSelectedStatus());
        mSearchCatalogueWrapper.setOrderByArgs(mCatalogueFilterMapper.getSelectedOrderBy());
        mSearchCatalogueWrapper.setOffsetArgs(DefaultFactory.SearchCatalogueWrapper.DEFAULT_OFFSET);

        EventBus.getDefault().post(new SearchCatalogueWrapperSubmitEvent(mSearchCatalogueWrapper));
    }

    @Override
    public void onClearButtonClick() {
        mCatalogueFilterMapper.setSelectedGenres(new ArrayList<String>());
        mCatalogueFilterMapper.setSelectedStatus(DefaultFactory.SearchCatalogueWrapper.DEFAULT_STATUS);
        mCatalogueFilterMapper.setSelectedOrderBy(DefaultFactory.SearchCatalogueWrapper.DEFAULT_ORDER_BY);
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mCatalogueFilterMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }
}
