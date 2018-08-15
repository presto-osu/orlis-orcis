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

import com.github.clans.fab.FloatingActionMenu;
import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.TVShowSummary;
import com.rastating.droidbeard.adapters.TVShowSummaryAdapter;
import com.rastating.droidbeard.net.ApiResponseListener;
import com.rastating.droidbeard.net.FetchShowSummariesTask;
import com.rastating.droidbeard.net.SickbeardAsyncTask;

public class ShowsFragment extends ListViewFragment implements ApiResponseListener<TVShowSummary[]> {
    private TVShowSummaryAdapter mAdapter;
    private boolean mLoading;
    private boolean mArgumentsRead;

    private SwipeRefreshLayout swipeRefreshLayout;

    FloatingActionMenu floatingActionsMenu;

    public ShowsFragment() {
        setTitle(R.string.app_name);
    }

    public void setFloatingActionMenu(FloatingActionMenu floatingActionsMenu) {
        this.floatingActionsMenu = floatingActionsMenu;
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

        Bundle args = getArguments();
        if (args != null && !mArgumentsRead && args.getBoolean("resetAdapter", false)) {
            mAdapter = null;
            mArgumentsRead = true;
        }

        if (mAdapter != null) {
            setAdapter(mAdapter);
            showListView(true);
        }
        else {
            onRefreshButtonPressed();
        }

        setDivider(R.color.divider, 1);

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        view.setSelected(true);
        TVShowSummary show = mAdapter.getItem(position);
        if (show != null) {
            FragmentManager manager = this.getFragmentManager();
            ShowFragment fragment = new ShowFragment();
            fragment.setTvShowSummary(show);
            manager.beginTransaction().replace(R.id.container, fragment).commit();
        }

        if (floatingActionsMenu != null) {
            floatingActionsMenu.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onApiRequestFinished(SickbeardAsyncTask sender, TVShowSummary[] objects) {
        if (activityStillExists()) {
            mLoading = false;

            if (objects != null) {
                Preferences preferences = new Preferences(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                int layoutID = preferences.getShowBannersInShowList() ? R.layout.tv_show_list_banner_item : R.layout.tv_show_list_item;
                mAdapter = TVShowSummaryAdapter.createInstance(this.getActivity(), inflater, layoutID, objects);
                setAdapter(mAdapter);
                showListView();
            } else {
                showError(getString(R.string.error_fetching_show_list), sender.getLastException());
            }
        }
    }

    @Override
    public void onRefreshButtonPressed() {
        if (!mLoading) {
            mLoading = true;

            // Show the loading animation during the initial loading of the fragment.
            if (mAdapter == null) {
                showLoadingAnimation();
            }

            FetchShowSummariesTask task = new FetchShowSummariesTask(getMainActivity());
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
}