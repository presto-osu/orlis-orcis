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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.HistoricalEvent;
import com.rastating.droidbeard.net.ApiResponseListener;
import com.rastating.droidbeard.net.FetchHistoryTask;
import com.rastating.droidbeard.net.SickbeardAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HistoryFragment extends ListViewFragment implements ApiResponseListener<HistoricalEvent[]> {

    private SwipeRefreshLayout swipeRefreshLayout;

    public HistoryFragment() {
        setTitle(R.string.title_history);
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

        setDivider(R.color.divider, 1);

        showLoadingAnimation();
        onRefreshButtonPressed();

        return root;
    }

    @Override
    public void onApiRequestFinished(SickbeardAsyncTask sender, HistoricalEvent[] result) {
        if (activityStillExists()) {
            if (result != null) {
                ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>(result.length);
                for (HistoricalEvent event : result) {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("name", String.format("%s %dx%d", event.getShowName(), event.getSeason(), event.getEpisodeNumber()));
                    item.put("desc", String.format("%s (%s) on %s", event.getStatus(), event.getQuality(), event.getDate()));
                    data.add(item);
                }

                String[] from = new String[]{"name", "desc"};
                int[] to = new int[]{R.id.episode, R.id.event_details};
                SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.historical_event_item, from, to) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        if (position % 2 == 0) {
                            view.setBackgroundResource(R.drawable.alternate_list_item_bg);
                        } else {
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }

                        return view;
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        return false;
                    }
                };
                setAdapter(adapter);

                showListView();
            } else {
                showError(getString(R.string.error_fetching_history), sender.getLastException());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onRefreshButtonPressed() {
        //showLoadingAnimation();
        FetchHistoryTask task = new FetchHistoryTask(getActivity());
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