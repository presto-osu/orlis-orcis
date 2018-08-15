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

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.TVShowSummary;
import com.rastating.droidbeard.entities.UpcomingEpisode;
import com.rastating.droidbeard.net.ApiResponseListener;
import com.rastating.droidbeard.net.FetchUpcomingEpisodesTask;
import com.rastating.droidbeard.net.SickbeardAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComingEpisodesFragment extends ListViewFragment implements ApiResponseListener<UpcomingEpisode[]> {
    private UpcomingEpisode[] mEpisodes;
    SimpleAdapter mAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    public ComingEpisodesFragment() {
        setTitle("Coming Episodes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                onRefreshButtonPressed();

                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.materialPrimaryDark, R.color.materialPrimary, R.color.navigation_list_item_selected, R.color.unaired_episode_background);

        setChoiceMode(ListView.CHOICE_MODE_NONE);
        setListSelector(android.R.color.transparent);
        setBackgroundColor(getResources().getColor(android.R.color.white));
        setDivider(R.color.divider,1);

        showLoadingAnimation();
        if (mAdapter == null) {
            onRefreshButtonPressed();
        }
        else {
            setAdapter(mAdapter);
            showListView(true);
        }

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        UpcomingEpisode episode = mEpisodes[position];
        TVShowSummary show = new TVShowSummary(episode.getShowName());
        show.setTvDbId(episode.getTVDBID());

        FragmentManager manager = this.getFragmentManager();
        ShowFragment fragment = new ShowFragment();
        fragment.setShouldReturnToUpcomingEpisodes(true);
        fragment.setTvShowSummary(show);
        manager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void onApiRequestFinished(SickbeardAsyncTask sender, UpcomingEpisode[] result) {
        if (activityStillExists()) {
            if (result != null) {
                mEpisodes = result;
                ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
                Preferences preferences = new Preferences(getActivity());

                if (preferences.getEmphasizeShowNameFlag()) {
                    for (UpcomingEpisode episode : result) {
                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put("name", episode.getShowName());
                        item.put("desc", String.format("%s - %dx%d - %s", episode.getAirdateString("yyyy-MM-dd"), episode.getSeasonNumber(), episode.getEpisodeNumber(), episode.getName()));
                        data.add(item);
                    }
                }
                else {
                    for (UpcomingEpisode episode : result) {
                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put("name", episode.getName());
                        item.put("desc", String.format("%s - %dx%d - %s", episode.getShowName(), episode.getSeasonNumber(), episode.getEpisodeNumber(), episode.getAirdateString("yyyy-MM-dd")));
                        data.add(item);
                    }
                }

                String[] from = new String[]{"name", "desc"};
                int[] to = new int[]{R.id.episode, R.id.event_details};
                final UpcomingEpisode[] episodes = result;
                mAdapter = new SimpleAdapter(getActivity(), data, R.layout.historical_event_item, from, to) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        switch (episodes[position].getUpcomingStatus()) {
                            case CURRENT:
                                view.setBackgroundColor(getResources().getColor(R.color.upcoming_episode_current));
                                break;

                            case DISTANT:
                                view.setBackgroundColor(getResources().getColor(R.color.upcoming_episode_distant));
                                break;

                            case FUTURE:
                                view.setBackgroundColor(getResources().getColor(R.color.upcoming_episode_future));
                                break;

                            case PAST:
                                view.setBackgroundColor(getResources().getColor(R.color.upcoming_episode_past));
                                break;
                        }

                        return view;
                    }
                };

                setAdapter(mAdapter);
                showListView();
            } else {
                showError(getString(R.string.error_fetching_coming_episodes), sender.getLastException());
            }
        }
    }

    @Override
    public void onRefreshButtonPressed() {
        //showLoadingAnimation();
        FetchUpcomingEpisodesTask task = new FetchUpcomingEpisodesTask(getActivity());
        task.addResponseListener(this);
        task.start();

        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }
}