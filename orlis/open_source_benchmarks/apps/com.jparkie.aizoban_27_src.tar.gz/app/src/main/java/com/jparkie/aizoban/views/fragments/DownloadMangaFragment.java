package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.DownloadMangaPresenter;
import com.jparkie.aizoban.presenters.DownloadMangaPresenterImpl;
import com.jparkie.aizoban.presenters.mapper.DownloadMangaMapper;
import com.jparkie.aizoban.views.DownloadMangaView;

public class DownloadMangaFragment extends Fragment implements DownloadMangaView, DownloadMangaMapper{
    public static final String TAG = DownloadMangaFragment.class.getSimpleName();

    private DownloadMangaPresenter mDownloadMangaPresenter;

    private ListView mListView;
    private RelativeLayout mEmptyRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDownloadMangaPresenter = new DownloadMangaPresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View downloadMangaView = inflater.inflate(R.layout.fragment_download_manga, container, false);

        mListView = (ListView) downloadMangaView.findViewById(R.id.listView);
        mEmptyRelativeLayout = (RelativeLayout) downloadMangaView.findViewById(R.id.emptyRelativeLayout);

        return downloadMangaView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mDownloadMangaPresenter.restoreState(savedInstanceState);
        }

        mDownloadMangaPresenter.initializeViews();

        mDownloadMangaPresenter.initializeSearch();

        mDownloadMangaPresenter.initializeDataFromDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();

        mDownloadMangaPresenter.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDownloadMangaPresenter.destroyAllSubscriptions();
        mDownloadMangaPresenter.releaseAllResources();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mDownloadMangaPresenter.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.download, menu);
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
                mDownloadMangaPresenter.onQueryTextChange(query);

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_to_top:
                mDownloadMangaPresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // DownloadMangaView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_download_manga);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeAbsListView() {
        if (mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mDownloadMangaPresenter.onDownloadMangaClick(position);
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_file_download_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_download_manga);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.download_manga_instructions);
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
        if (mListView != null) {
            mListView.smoothScrollToPosition(0);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // DownloadMangaMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable getPositionState() {
        if (mListView != null) {
            return mListView.onSaveInstanceState();
        } else {
            return null;
        }
    }

    @Override
    public void setPositionState(Parcelable state) {
        if (mListView != null) {
            mListView.onRestoreInstanceState(state);
        }
    }
}
