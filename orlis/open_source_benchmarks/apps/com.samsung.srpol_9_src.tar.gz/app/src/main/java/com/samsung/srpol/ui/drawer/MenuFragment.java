/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.ui.drawer;

import com.samsung.srpol.R;
import com.samsung.srpol.loader.AppListLoader;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class MenuFragment extends Fragment {

    private static final String SAVED_POSITION = "saved_position";

    private NavigationDrawerItemListener mDrawerItemListener;
    
    private ListView mMenuListView;
    private CategoryArrayAdapter mAdapter;

    private int mSelectedPosition = 0;

    public MenuFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedPosition = savedInstanceState
                    .getInt(SAVED_POSITION);
            selectItem(mSelectedPosition);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View mRootView = (View) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mMenuListView = (ListView) mRootView
                .findViewById(R.id.fragment_listView);
        mMenuListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        selectItem(position);
                    }
                });
        mAdapter = new CategoryArrayAdapter(getActivity().getApplicationContext());
        mMenuListView.setAdapter(mAdapter);

        mMenuListView.setItemChecked(mSelectedPosition, true);
        return mRootView;
    }

    public void refresh() {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.addAll(AppListLoader.getCategories());
            mAdapter.notifyDataSetChanged();
        }
    }


    private void selectItem(int position) {
        mSelectedPosition = position;
        if (mMenuListView != null) {
            mMenuListView.setItemChecked(position, true);
        }

        if (mDrawerItemListener != null) {
            mDrawerItemListener.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDrawerItemListener = null;
    }

    public void setDrawerItemListener(NavigationDrawerItemListener drawerItemListener) {
        mDrawerItemListener = drawerItemListener;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_POSITION, mSelectedPosition);
    }

    public void notifyDataSetChanged(){
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }
}
