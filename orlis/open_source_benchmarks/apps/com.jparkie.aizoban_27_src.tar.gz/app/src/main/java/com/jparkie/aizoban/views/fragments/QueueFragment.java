package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.QueuePresenter;
import com.jparkie.aizoban.presenters.QueuePresenterImpl;
import com.jparkie.aizoban.presenters.mapper.QueueMapper;
import com.jparkie.aizoban.views.QueueView;

public class QueueFragment extends Fragment implements QueueView, QueueMapper {
    public static final String TAG = QueueFragment.class.getSimpleName();

    private QueuePresenter mQueuePresenter;

    private ListView mListView;
    private RelativeLayout mEmptyRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mQueuePresenter = new QueuePresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View queueView = inflater.inflate(R.layout.fragment_queue, container, false);

        mListView = (ListView) queueView.findViewById(R.id.listView);
        mEmptyRelativeLayout = (RelativeLayout) queueView.findViewById(R.id.emptyRelativeLayout);

        return queueView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mQueuePresenter.restoreState(savedInstanceState);
        }

        mQueuePresenter.initializeViews();

        mQueuePresenter.initializeDataFromDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();

        mQueuePresenter.registerForEvents();
    }

    @Override
    public void onResume() {
        super.onResume();

        mQueuePresenter.onResume();
    }

    @Override
    public void onStop() {
        mQueuePresenter.unregisterForEvents();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mQueuePresenter.destroyAllSubscriptions();
        mQueuePresenter.releaseAllResources();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mQueuePresenter.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.queue, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_downloader:
                mQueuePresenter.onOptionStartDownloader();
                return true;
            case R.id.action_stop_downloader:
                mQueuePresenter.onOptionStopDownloader();
                return true;
            case R.id.action_to_top:
                mQueuePresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // QueueView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_queue);
        }
    }

    @Override
    public void initializeAbsListView() {
        if (mListView != null) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mode.setTitle(getResources().getString(R.string.queue_selection_title) + " " + mListView.getCheckedItemCount());
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle(getResources().getString(R.string.queue_selection_title) + " " + mListView.getCheckedItemCount());

                    MenuInflater menuInflater = getActivity().getMenuInflater();
                    menuInflater.inflate(R.menu.queue_selection, menu);

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
                        case R.id.action_cancel:
                            mQueuePresenter.onOptionCancel();
                            mode.finish();
                            return true;
                        case R.id.action_select_all:
                            mQueuePresenter.onOptionSelectAll();
                            return false;
                        case R.id.action_clear:
                            mQueuePresenter.onOptionClear();
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
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_cloud_queue_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_queued_downloads);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.queue_downloads_instructions);
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

    // QueueMapper:

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
