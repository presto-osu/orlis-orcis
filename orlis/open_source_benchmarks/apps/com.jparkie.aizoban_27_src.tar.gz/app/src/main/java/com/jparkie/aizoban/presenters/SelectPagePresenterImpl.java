package com.jparkie.aizoban.presenters;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.jparkie.aizoban.controllers.events.SelectPageEvent;
import com.jparkie.aizoban.views.SelectPageView;
import com.jparkie.aizoban.views.fragments.SelectPageFragment;

import de.greenrobot.event.EventBus;

public class SelectPagePresenterImpl implements SelectPagePresenter {
    public static final String TAG = SelectPagePresenterImpl.class.getSimpleName();

    private static final String CURRENT_PAGE_PARCELABLE_KEY = TAG + ":" + "CurrentPageParcelableKey";
    private static final String PAGE_SIZE_PARCELABLE_KEY = TAG + ":" + "PageSizeParcelableKey";

    private SelectPageView mSelectPageView;

    private int mCurrentPage;
    private int mPageSize;
    private ArrayAdapter<Integer> mPagesAdapter;

    public SelectPagePresenterImpl(SelectPageView selectPageView) {
        mSelectPageView = selectPageView;
    }

    @Override
    public void handleInitialArguments(Bundle arguments) {
        if (arguments != null) {
            if (arguments.containsKey(SelectPageFragment.CURRENT_PAGE_ARGUMENT_KEY) && arguments.containsKey(SelectPageFragment.PAGE_SIZE_ARGUMENT_KEY)) {
                mCurrentPage = arguments.getInt(SelectPageFragment.CURRENT_PAGE_ARGUMENT_KEY, 0);
                mPageSize = arguments.getInt(SelectPageFragment.PAGE_SIZE_ARGUMENT_KEY, 0);

                arguments.remove(SelectPageFragment.CURRENT_PAGE_ARGUMENT_KEY);
                arguments.remove(SelectPageFragment.PAGE_SIZE_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        Integer[] pagesArray = new Integer[mPageSize];
        for (int index = 0; index < mPageSize; index++) {
            pagesArray[index] = index + 1;
        }

        mPagesAdapter = new ArrayAdapter<Integer>(mSelectPageView.getContext(), android.R.layout.simple_spinner_dropdown_item, pagesArray);
        mSelectPageView.initializeSpinner(mPagesAdapter);
        mSelectPageView.setSpinnerPageNumber(mCurrentPage);
    }

    @Override
    public void saveState(Bundle outState) {
        outState.putInt(CURRENT_PAGE_PARCELABLE_KEY, mCurrentPage);
        outState.putInt(PAGE_SIZE_PARCELABLE_KEY, mPageSize);
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(CURRENT_PAGE_PARCELABLE_KEY) && savedState.containsKey(PAGE_SIZE_PARCELABLE_KEY)) {
            mCurrentPage = savedState.getInt(CURRENT_PAGE_PARCELABLE_KEY, 0);
            mPageSize = savedState.getInt(PAGE_SIZE_PARCELABLE_KEY, 0);

            savedState.remove(CURRENT_PAGE_PARCELABLE_KEY);
            savedState.remove(PAGE_SIZE_PARCELABLE_KEY);
        }
    }

    @Override
    public void onItemSelected(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < mPageSize) {
            mCurrentPage = pageNumber;
        }
    }

    @Override
    public void onOkButtonClick() {
        EventBus.getDefault().post(new SelectPageEvent(mCurrentPage));
    }

    @Override
    public void onCancelButtonClick() {
        // Do Nothing.
    }
}
