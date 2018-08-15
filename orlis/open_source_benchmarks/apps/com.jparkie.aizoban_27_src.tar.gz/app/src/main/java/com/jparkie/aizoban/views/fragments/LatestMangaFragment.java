package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.LatestMangaPresenter;
import com.jparkie.aizoban.presenters.LatestMangaPresenterImpl;
import com.jparkie.aizoban.presenters.mapper.LatestMangaMapper;
import com.jparkie.aizoban.views.LatestMangaView;

public class LatestMangaFragment extends Fragment implements LatestMangaView, LatestMangaMapper {
    public static final String TAG = LatestMangaFragment.class.getSimpleName();

    private LatestMangaPresenter mLatestMangaPresenter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridView mGridView;
    private RelativeLayout mEmptyRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mLatestMangaPresenter = new LatestMangaPresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View latestView = inflater.inflate(R.layout.fragment_latest_manga, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) latestView.findViewById(R.id.swipeRefreshLayout);
        mGridView = (GridView) latestView.findViewById(R.id.gridView);
        mEmptyRelativeLayout = (RelativeLayout) latestView.findViewById(R.id.emptyRelativeLayout);

        return latestView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mLatestMangaPresenter.restoreState(savedInstanceState);
        }

        mLatestMangaPresenter.initializeViews();

        mLatestMangaPresenter.initializeDataFromPreferenceSource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLatestMangaPresenter.destroyAllSubscriptions();
        mLatestMangaPresenter.releaseAllResources();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mLatestMangaPresenter.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.latest, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mLatestMangaPresenter.onOptionRefresh();
                return true;
            case R.id.action_to_top:
                mLatestMangaPresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // LatestView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_latest_manga);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeSwipeRefreshLayout() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics()));
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accentPinkA200));
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mLatestMangaPresenter.onSwipeRefresh();
                }
            });
        }
    }

    @Override
    public void initializeAbsListView() {
        if (mGridView != null) {
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mLatestMangaPresenter.onMangaClick(position);
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_new_releases_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_latest);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.latest_instructions);
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
        // Do Nothing.
    }

    @Override
    public void showRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void hideRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void scrollToTop() {
        if (mGridView != null) {
            mGridView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void toastLatestError() {
        Toast.makeText(getActivity(), R.string.toast_latest_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // LatestMapper:

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
}
