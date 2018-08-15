/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rastating.droidbeard.comparators.EpisodeComparator;
import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.Episode;
import com.rastating.droidbeard.entities.Season;
import com.rastating.droidbeard.entities.TVShow;
import com.rastating.droidbeard.entities.TVShowSummary;
import com.rastating.droidbeard.net.ApiResponseListener;
import com.rastating.droidbeard.net.DeleteShowTask;
import com.rastating.droidbeard.net.EpisodeSearchTask;
import com.rastating.droidbeard.net.FetchShowTask;
import com.rastating.droidbeard.net.SetEpisodeStatusTask;
import com.rastating.droidbeard.net.SetPausedTask;
import com.rastating.droidbeard.net.SickbeardAsyncTask;
import com.rastating.droidbeard.net.SickbeardTaskPool;
import com.rastating.droidbeard.net.TaskPoolSubscriber;
import com.rastating.droidbeard.ui.CrossFader;
import com.rastating.droidbeard.ui.EpisodeItem;
import com.rastating.droidbeard.ui.EpisodeItemClickListener;
import com.rastating.droidbeard.ui.LoadingAnimation;
import com.rastating.droidbeard.ui.SeasonTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowFragment extends DroidbeardFragment implements ApiResponseListener<TVShow>, EpisodeItemClickListener, View.OnClickListener {
    private TVShowSummary mShowSummary;
    private ImageView mBanner;
    private ImageView mLoadingImage;
    private View mDataContainer;
    private TVShow mShow;
    private TextView mAirs;
    private TextView mStatus;
    private TextView mLocation;
    private TextView mQuality;
    private TextView mLanguage;
    private ImageView mLanguageIcon;
    private ImageView mFlattenFolders;
    private ImageView mPaused;
    private ImageView mAirByDate;
    private LinearLayout mSeasonContainer;
    private Button mPauseButton;
    private Button mDeleteButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<EpisodeItem> mSelectedEpisodes;

    private boolean mDisposingActionMode;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.episode_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case 0:
                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.set_archived:
                case R.id.set_ignored:
                case R.id.set_skipped:
                case R.id.set_wanted:
                    onSetStatusItemSelected(item);
                    mode.finish();
                    return true;

                case R.id.search:
                    onEpisodeSearchItemSelected();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mDisposingActionMode = true;

            for (int i = mSelectedEpisodes.size() - 1; i >= 0 && mSelectedEpisodes.size() > 0; i--)  {
                EpisodeItem item = mSelectedEpisodes.get(i);
                item.setChecked(false);
            }

            mSelectedEpisodes.clear();
            mDisposingActionMode = false;
            getActivity().setTheme(R.style.SickBeardTheme);
        }
    };
    private boolean mReturnToUpcomingEpisodes;

    public ShowFragment() {
        mShowSummary = null;
        mSelectedEpisodes = new ArrayList<EpisodeItem>();
    }

    public void setTvShowSummary(TVShowSummary show) {
        mShowSummary = show;
        setTitle(show.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.SickBeardTheme_LightActionBar);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View root = localInflater.inflate(R.layout.fragment_show, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                onRefreshButtonPressed();

                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.materialPrimaryDark, R.color.materialPrimary, R.color.navigation_list_item_selected, R.color.unaired_episode_background);


        if (savedInstanceState != null) {
            mShowSummary = (TVShowSummary) savedInstanceState.getParcelable("summary");
        }

        // Get references to all required views.
        mBanner = (ImageView) root.findViewById(R.id.banner);
        mLoadingImage = (ImageView) root.findViewById(R.id.loading);
        mDataContainer = root.findViewById(R.id.data);
        mAirs = (TextView) root.findViewById(R.id.airs);
        mStatus = (TextView) root.findViewById(R.id.status);
        mLocation = (TextView) root.findViewById(R.id.location);
        mQuality = (TextView) root.findViewById(R.id.quality);
        mLanguage = (TextView) root.findViewById(R.id.language);
        mLanguageIcon = (ImageView) root.findViewById(R.id.language_icon);
        mFlattenFolders = (ImageView) root.findViewById(R.id.flatten_folders);
        mPaused = (ImageView) root.findViewById(R.id.paused);
        mAirByDate = (ImageView) root.findViewById(R.id.air_by_date);
        mSeasonContainer = (LinearLayout) root.findViewById(R.id.season_container);
        mPauseButton = (Button) root.findViewById(R.id.toggle_pause);
        mDeleteButton = (Button) root.findViewById(R.id.delete);

        mPauseButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        mDataContainer.setAlpha(0.0f);
        mLoadingImage.setAlpha(1.0f);
        mLoadingImage.startAnimation(new LoadingAnimation());
        onRefreshButtonPressed();

        return root;
    }

    @Override
    public void onApiRequestFinished(SickbeardAsyncTask sender, TVShow result) {
        if (activityStillExists()) {
            mShow = result;
            if (mShow != null) {
                populateViews();
            }
            new CrossFader(mLoadingImage, mDataContainer, 500).start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefreshButtonPressed() {
        // Start loading animation.
        //mDataContainer.setAlpha(0.0f);
        //mLoadingImage.setAlpha(1.0f);
        //mLoadingImage.startAnimation(new LoadingAnimation());

        // Start fetching the show information.
        FetchShowTask task = new FetchShowTask(getActivity());
        task.addResponseListener(this);
        task.start(mShowSummary.getTvDbId());

        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("summary", mShowSummary);
    }

    private void populateViews() {
        if (mShow.getBanner() != null) {
            mBanner.setImageBitmap(mShow.getBanner());
        }

        mAirs.setText(mShow.getAirs() + " on " + mShow.getNetwork());
        mStatus.setText(mShow.getStatus());
        mLocation.setText(mShow.getLocation());
        mQuality.setText(mShow.getQuality());
        mLanguage.setText(mShow.getLanguage().getCode());
        mLanguageIcon.setImageResource(mShow.getLanguage().getIconResId());
        mFlattenFolders.setImageResource(mShow.getFlattenFolders() ? R.drawable.yes16 : R.drawable.no16);
        mAirByDate.setImageResource(mShow.getAirByDate() ? R.drawable.yes16 : R.drawable.no16);

        setupPauseViews(mShow.getPaused());

        mSeasonContainer.removeAllViews();
        List<Season> seasons = mShow.getSeasons();
        if (seasons != null) {
            for (Season season : seasons) {
                SeasonTable table = new SeasonTable(getActivity(), season);
                table.setTitle(season.getTitle());

                List<Episode> episodes = season.getEpisodes();
                Collections.sort(episodes, new EpisodeComparator());
                Collections.reverse(episodes);

                for (Episode episode : episodes) {
                    EpisodeItem item = table.addEpisode(episode);
                    item.setOnItemClickListener(this);
                    item.setOnCreateContextMenuListener(this);
                }

                mSeasonContainer.addView(table);
            }
        }
    }

    private void setupPauseViews(boolean paused) {
        mPaused.setImageResource(paused ? R.drawable.yes16 : R.drawable.no16);
        mPauseButton.setText(paused ? getString(R.string.unpause) : getString(R.string.pause));
    }

    @Override
    public void onItemClick(EpisodeItem item, int seasonNumber, int episodeNumber, String name) {
        if (!mDisposingActionMode) {
            if (item.isChecked()) {
                mSelectedEpisodes.add(item);
            } else {
                mSelectedEpisodes.remove(item);
            }

            if (mActionMode == null && mSelectedEpisodes.size() > 0) {
                getActivity().setTheme(R.style.SickBeardTheme_LightActionBar);
                mActionMode = getActivity().startActionMode(mActionModeCallback);
            } else if (mActionMode != null && mSelectedEpisodes.size() == 0) {
                mActionMode.finish();
            }

            if (mActionMode != null && mSelectedEpisodes.size() > 0) {
                mActionMode.setTitle(String.valueOf(mSelectedEpisodes.size()) + " selected");
            }
        }
    }

    private SetEpisodeStatusTask buildEpisodeStatusTask(SeasonTable seasonTable, EpisodeItem episodeItem, long tvdbid, int season, int episode, String status) {
        // Create finals for use in callback...
        final EpisodeItem finalEpisodeItem = episodeItem;
        final SeasonTable finalSeasonTable = seasonTable;
        final int finalEpisode = episode;
        final String finalStatus = status;

        SetEpisodeStatusTask task = new SetEpisodeStatusTask(getActivity(), tvdbid, season, episode);
        task.addResponseListener(new ApiResponseListener<Boolean>() {
            @Override
            public void onApiRequestFinished(SickbeardAsyncTask sender, Boolean result) {
                if (result) {
                    if (finalEpisode > 0) {
                        finalEpisodeItem.setStatus(finalStatus);
                        finalEpisodeItem.setChecked(false);
                    }
                    else {
                        finalSeasonTable.setChecked(false);
                        for (EpisodeItem item : finalSeasonTable.getEpisodeItems()) {
                            item.setStatus(finalStatus);
                            item.setChecked(false);
                        }
                    }
                }
            }
        });

        return task;
    }

    private void onSetStatusItemSelected(MenuItem item) {
        // Group selected episodes into a list of individual episodes and full seasons...
        ArrayList<SeasonTable> selectedSeasons = new ArrayList<SeasonTable>();
        ArrayList<EpisodeItem> selectedEpisodes = new ArrayList<EpisodeItem>();
        for (EpisodeItem episode : mSelectedEpisodes) {
            SeasonTable seasonTable = episode.getSeasonTable();
            if (seasonTable.allEpisodesChecked()) {
                if (!selectedSeasons.contains(seasonTable)) {
                    selectedSeasons.add(seasonTable);
                }
            }
            else {
                selectedEpisodes.add(episode);
            }
        }

        String status = "";
        if (item.getItemId() == R.id.set_wanted) {
            status = "wanted";
        } else if (item.getItemId() == R.id.set_skipped) {
            status = "skipped";
        } else if (item.getItemId() == R.id.set_ignored) {
            status = "ignored";
        } else {
            status = "archived";
        }

        SickbeardTaskPool<String> pool = new SickbeardTaskPool<String>();

        for (SeasonTable seasonTable : selectedSeasons) {
            Season season = seasonTable.getSeason();
            SetEpisodeStatusTask task = buildEpisodeStatusTask(seasonTable, null, season.getTVDBID(), season.getSeasonNumber(), -1, status);
            pool.addTask(task);
        }

        for (EpisodeItem episodeItem : selectedEpisodes) {
            Episode episode = episodeItem.getEpisode();
            SetEpisodeStatusTask task = buildEpisodeStatusTask(null, episodeItem, episode.getTVDBID(), episode.getSeasonNumber(), episode.getEpisodeNumber(), status);
            pool.addTask(task);
        }

        final ProgressDialog dialog = createProgressDialog("Performing Status Updates", "Please wait...");
        dialog.show();
        pool.setTaskPoolSubscriber(new TaskPoolSubscriber() {
            @Override
            public void executionFinished() {
                dialog.dismiss();
                mSelectedEpisodes.clear();
            }
        });
        pool.start(status);
    }

    private void onEpisodeSearchItemSelected() {
        SickbeardTaskPool<Void> pool = new SickbeardTaskPool<Void>();
        for (EpisodeItem episodeItem : mSelectedEpisodes) {
            Episode episode = episodeItem.getEpisode();
            EpisodeSearchTask task = new EpisodeSearchTask(getActivity(), episode.getTVDBID(), episode.getSeasonNumber(), episode.getEpisodeNumber());
            pool.addTask(task);
        }

        final ProgressDialog dialog = createProgressDialog("Searching for Selected Episodes", "Please wait...");
        dialog.show();
        pool.setTaskPoolSubscriber(new TaskPoolSubscriber() {
            @Override
            public void executionFinished() {
                dialog.dismiss();

                for (int i = mSelectedEpisodes.size() - 1; i >= 0; i--)  {
                    EpisodeItem item = mSelectedEpisodes.get(i);
                    item.setChecked(false);
                }

                mSelectedEpisodes.clear();
                onRefreshButtonPressed();
            }
        });
        pool.start();
    }

    private ProgressDialog createProgressDialog(String title, String message) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        return dialog;
    }

    public void setShouldReturnToUpcomingEpisodes(boolean value) {
        mReturnToUpcomingEpisodes = value;
    }

    public boolean shouldReturnToUpcomingEpisodes() {
        return mReturnToUpcomingEpisodes;
    }



    @Override
    public void onClick(View v) {
        if (v == mPauseButton) {
            mShow.setPaused(!mShow.getPaused());
            setupPauseViews(mShow.getPaused());
            new SetPausedTask(ShowFragment.this.getActivity(), mShowSummary.getTvDbId()).start(mShow.getPaused());
        }
        else if (v == mDeleteButton) {
            new AlertDialog.Builder(ShowFragment.this.getActivity())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this show?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int whichButton) {
                        final ProgressDialog dialog = createProgressDialog("Deleting Show", "Please wait...");
                        dialog.show();

                        DeleteShowTask task = new DeleteShowTask(ShowFragment.this.getActivity());
                        task.addResponseListener(new ApiResponseListener<Boolean>() {
                            @Override
                            public void onApiRequestFinished(SickbeardAsyncTask sender, Boolean result) {
                                dialog.dismiss();
                                getMainActivity().displayAndRefreshShowsFragment();
                            }
                        });
                        task.start(mShowSummary.getTvDbId());
                    }
                })
                .setNegativeButton("No", null)
                .show();
        }
    }
}