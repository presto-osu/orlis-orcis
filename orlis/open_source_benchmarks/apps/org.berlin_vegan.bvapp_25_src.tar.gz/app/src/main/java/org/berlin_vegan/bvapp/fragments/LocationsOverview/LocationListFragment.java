/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.fragments.LocationsOverview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;
import org.berlin_vegan.bvapp.adapters.LocationAdapter;
import org.berlin_vegan.bvapp.data.Locations;
import org.berlin_vegan.bvapp.helpers.DividerItemDecoration;
import org.berlin_vegan.bvapp.views.LocationRecycleView;

/**
 * Created by micu on 27/02/16.
 */
public class LocationListFragment extends Fragment {

    private LocationRecycleView mRecyclerView;
    private LocationAdapter mLocationAdapter;
    private Locations mLocations;
    private LocationsOverviewActivity mParentActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public LocationListFragment() {
        super();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentActivity = (LocationsOverviewActivity) getActivity();
        mLocations = mParentActivity.getLocations();
        mSwipeRefreshLayout = mParentActivity.getSwipeRefreshLayout();
        mLocationAdapter = mParentActivity.getLocationAdapter();

        mRecyclerView = new LocationRecycleView(getContext());

        if (mRecyclerView != null) {
            setupRecyclerView(mRecyclerView, mSwipeRefreshLayout, mLocations);
        }

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        return mRecyclerView;
    }

    private void setupRecyclerView(LocationRecycleView recyclerView, final SwipeRefreshLayout swipeRefreshLayout, Locations locations) {
        recyclerView.setLocations(locations);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(mLocationAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
        recyclerView.setEmptyViews(getActivity().findViewById(R.id.location_list_empty_favorites_textview), getActivity().findViewById(R.id.location_list_empty_search_textview));


    }
}