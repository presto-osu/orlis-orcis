package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.CataloguePresenter;
import com.jparkie.aizoban.presenters.CataloguePresenterImpl;
import com.jparkie.aizoban.presenters.mapper.CatalogueMapper;
import com.jparkie.aizoban.views.CatalogueView;
import com.melnykov.fab.FloatingActionButton;

public class CatalogueFragment extends Fragment implements CatalogueView, CatalogueMapper {
    public static final String TAG = CatalogueFragment.class.getSimpleName();

    private CataloguePresenter mCataloguePresenter;

    private GridView mGridView;
    private RelativeLayout mEmptyRelativeLayout;
    private FloatingActionButton mPreviousButton;
    private FloatingActionButton mNextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCataloguePresenter = new CataloguePresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View catalogueView = inflater.inflate(R.layout.fragment_catalogue, container, false);

        mGridView = (GridView) catalogueView.findViewById(R.id.gridView);
        mEmptyRelativeLayout = (RelativeLayout) catalogueView.findViewById(R.id.emptyRelativeLayout);
        mPreviousButton = (FloatingActionButton) catalogueView.findViewById(R.id.previousButton);
        mNextButton = (FloatingActionButton) catalogueView.findViewById(R.id.nextButton);

        return catalogueView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCataloguePresenter.restoreState(savedInstanceState);
        }

        mCataloguePresenter.initializeViews();

        mCataloguePresenter.initializeSearch();

        mCataloguePresenter.initializeDataFromPreferenceSource();
    }

    @Override
    public void onStart() {
        super.onStart();

        mCataloguePresenter.registerForEvents();
    }

    @Override
    public void onStop() {
        mCataloguePresenter.unregisterForEvents();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCataloguePresenter.destroyAllSubscriptions();
        mCataloguePresenter.releaseAllResources();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCataloguePresenter.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.catalogue, menu);
        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                InputMethodManager searchKeyboard = (InputMethodManager) AizobanApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                searchKeyboard.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mCataloguePresenter.onQueryTextChange(query);

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                mCataloguePresenter.onOptionFilter();
                return true;
            case R.id.action_to_top:
                mCataloguePresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // CatalogueView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_catalogue);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeAbsListView() {
        if (mGridView != null) {
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCataloguePresenter.onMangaClick(position);
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_photo_library_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_catalogue);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.catalogue_instructions);
        }
    }

    @Override
    public void initializeButtons() {
        if (mPreviousButton != null) {
            mPreviousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCataloguePresenter.onPreviousClick();
                }
            });
        }
        if (mNextButton != null) {
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCataloguePresenter.onNextClick();
                }
            });
        }
        if (mGridView != null) {
            if (mPreviousButton != null && mNextButton != null) {
                mGridView.setOnScrollListener(new FloatingActionButtonsOnScrollListenerImpl());
            }
        }
    }

    @Override
    public void setSubtitlePositionText(int position) {
        ActionBar supportActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        if (supportActionBar != null) {
            if (mGridView.getAdapter() != null) {
                supportActionBar.setSubtitle(getString(R.string.catalogue_subtitle_page) + " " + position);
            }
        }
    }

    @Override
    public void hideEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            mEmptyRelativeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mGridView != null) {
            mGridView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void toastNoPreviousPage() {
        Toast.makeText(getActivity(), R.string.toast_no_previous_page, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastNoNextPage() {
        Toast.makeText(getActivity(), R.string.toast_no_next_page, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // CatalogueMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mGridView != null) {
            mGridView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable getPositionState() {
        if (mGridView != null) {
            return mGridView.onSaveInstanceState();
        } else {
            return null;
        }
    }

    @Override
    public void setPositionState(Parcelable state) {
        if (mGridView != null) {
            mGridView.onRestoreInstanceState(state);
        }
    }

    // OnScrollListener():

    private class FloatingActionButtonsOnScrollListenerImpl implements AbsListView.OnScrollListener {
        private int mLastScrollY;
        private int mPreviousFirstVisibleItem;
        private int mScrollThreshold;

        public FloatingActionButtonsOnScrollListenerImpl() {
            mScrollThreshold = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // Do Nothing.
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(totalItemCount == 0) {
                return;
            }

            if (firstVisibleItem == mPreviousFirstVisibleItem) {
                int newScrollY = getTopItemScrollY();

                boolean isSignificantDelta = Math.abs(mLastScrollY - newScrollY) > mScrollThreshold;
                if (isSignificantDelta) {
                    if (mLastScrollY > newScrollY) {
                        hideFloatinActionButtons();
                    } else {
                        showFloatinActionButtons();
                    }
                }

                mLastScrollY = newScrollY;
            } else {
                if (firstVisibleItem > mPreviousFirstVisibleItem) {
                    hideFloatinActionButtons();
                } else {
                    showFloatinActionButtons();
                }

                mLastScrollY = getTopItemScrollY();

                mPreviousFirstVisibleItem = firstVisibleItem;
            }
        }

        private int getTopItemScrollY() {
            if (mGridView == null || mGridView.getChildAt(0) == null) {
                return 0;
            }

            View topChild = mGridView.getChildAt(0);

            return topChild.getTop();
        }

        private void showFloatinActionButtons() {
            if (mPreviousButton != null && mNextButton != null) {
                mPreviousButton.show();
                mNextButton.show();
            }
        }

        private void hideFloatinActionButtons() {
            if (mPreviousButton != null && mNextButton != null) {
                mPreviousButton.hide();
                mNextButton.hide();
            }
        }
    }
}
