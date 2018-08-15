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

package com.samsung.srpol.ui.tabpager;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.samsung.srpol.R;
import com.samsung.srpol.data.Category;
import com.samsung.srpol.data.Subcategory;
import com.samsung.srpol.loader.AppListLoader;
import com.samsung.srpol.parallax.ParallaxListView;
import com.samsung.srpol.ui.AppInfoActivity;
import com.samsung.srpol.ui.PopupActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PageFragment extends Fragment {

    private AppListArrayAdapter mAppListArrayAdapter;
    private Category mCategory;
    private int mPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments() != null ? getArguments().getInt("position") : 0;
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        
        LinearLayout headerLayout = (LinearLayout) inflater.inflate(R.layout.header_layout, container, false);
        headerLayout.setLayoutParams(new ListView.LayoutParams(
                ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        if (mCategory == null) {
            if (AppListLoader.getCategories() != null) {
                mCategory = AppListLoader.getCategories().get(mPosition);
            } else {
                return rootView;
            }
        }
        TextView header = (TextView) headerLayout.findViewById(R.id.section_label);
        if (header != null) {
            header.setText(mCategory.getDescription());
        }
        
        TextView headerLabel = (TextView) rootView.findViewById(R.id.header_label);
        if (headerLabel != null) {
            String titleSufix = getActivity().getString(R.string.apps_that);
            headerLabel.setText(titleSufix.concat(mCategory.getHeader()));
        }
        
        //Header icons Click
        LinearLayout groupIcons = (LinearLayout) headerLayout.findViewById(R.id.group_icons);
        for (Subcategory subgroup : mCategory.getSubCategories()) {
            ImageView icon = new ImageView(getActivity());
            icon.setImageDrawable(subgroup.getIconDrawable());
            groupIcons.addView(icon);
        }

        //Header More click
        ImageButton moreButton = (ImageButton) headerLayout.findViewById(R.id.legend_btn);
        moreButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_CATEGORY, mPosition);
                getActivity().startActivity(intent);
            }
        });

        // List with applications
        ParallaxListView listview = (ParallaxListView) rootView.findViewById(R.id.listview);
        listview.addParallaxedHeaderView(headerLayout);
        mAppListArrayAdapter = new AppListArrayAdapter(getActivity(), mCategory);
        AnimationAdapter adapter = new AlphaInAnimationAdapter(mAppListArrayAdapter);
        adapter.setAbsListView(listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int positionInAdapter, long id) {
                Intent intent = new Intent(getActivity(), AppInfoActivity.class);
                intent.putExtra(AppInfoActivity.APP_PACKAGE_NAME,
                        mAppListArrayAdapter.getItem(positionInAdapter - 1).getAppPackageName());
                getActivity().startActivity(intent);
            }
        });
        
        return rootView;
    }

    public void notifyDataSetChanged() {
        mAppListArrayAdapter.notifyDataSetChanged();
    }

}
