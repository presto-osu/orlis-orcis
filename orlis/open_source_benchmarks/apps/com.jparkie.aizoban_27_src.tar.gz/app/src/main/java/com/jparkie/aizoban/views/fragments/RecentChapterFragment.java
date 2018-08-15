package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.RecentChapterPresenter;
import com.jparkie.aizoban.presenters.RecentChapterPresenterImpl;
import com.jparkie.aizoban.presenters.mapper.RecentChapterMapper;
import com.jparkie.aizoban.views.RecentChapterView;

public class RecentChapterFragment extends Fragment implements RecentChapterView, RecentChapterMapper {
    public static final String TAG = RecentChapterFragment.class.getSimpleName();

    private RecentChapterPresenter mRecentChapterPresenter;

    private ListView mListView;
    private RelativeLayout mEmptyRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mRecentChapterPresenter = new RecentChapterPresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View favouriteMangaView = inflater.inflate(R.layout.fragment_favourite_manga, container, false);

        mListView = (ListView) favouriteMangaView.findViewById(R.id.listView);
        mEmptyRelativeLayout = (RelativeLayout) favouriteMangaView.findViewById(R.id.emptyRelativeLayout);

        return favouriteMangaView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mRecentChapterPresenter.restoreState(savedInstanceState);
        }

        mRecentChapterPresenter.initializeViews();

        mRecentChapterPresenter.initializeSearch();

        mRecentChapterPresenter.initializeDataFromDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRecentChapterPresenter.registerForEvents();
    }

    @Override
    public void onResume() {
        super.onResume();

        mRecentChapterPresenter.onResume();
    }

    @Override
    public void onStop() {
        mRecentChapterPresenter.unregisterForEvents();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRecentChapterPresenter.destroyAllSubscriptions();
        mRecentChapterPresenter.releaseAllResources();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRecentChapterPresenter.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favourite, menu);
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
                mRecentChapterPresenter.onQueryTextChange(query);

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_to_top:
                mRecentChapterPresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // RecentChapterView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_recent_chapter);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void initializeAbsListView() {
        if (mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mRecentChapterPresenter.onRecentChapterClick(position);
                }
            });
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mode.setTitle(getResources().getString(R.string.recent_chapter_selection_title) + " " + mListView.getCheckedItemCount());
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle(getResources().getString(R.string.recent_chapter_selection_title) + " " + mListView.getCheckedItemCount());

                    MenuInflater menuInflater = getActivity().getMenuInflater();
                    menuInflater.inflate(R.menu.recent_selection, menu);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    int id = item.getItemId();

                    switch (id) {
                        case R.id.action_delete:
                            mRecentChapterPresenter.onOptionDelete();
                            mode.finish();
                            return true;
                        case R.id.action_select_all:
                            mRecentChapterPresenter.onOptionSelectAll();
                            return false;
                        case R.id.action_clear:
                            mRecentChapterPresenter.onOptionClear();
                            return false;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    // Do Nothing.
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_history_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_recent_chapter);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.recent_chapter_instructions);
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
    public void selectAll() {
        if (mListView != null) {
            for (int index = 0; index < mListView.getCount(); index++) {
                mListView.setItemChecked(index, true);
            }
        }
    }

    @Override
    public void clear() {
        if (mListView != null) {
            for (int index = 0; index < mListView.getCount(); index++) {
                mListView.setItemChecked(index, false);
            }
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // RecentChapterMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public SparseBooleanArray getCheckedItemPositions() {
        if (mListView != null) {
            return mListView.getCheckedItemPositions();
        } else {
            return null;
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
