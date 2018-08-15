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

package com.rastating.droidbeard.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.TVShowSummary;
import com.rastating.droidbeard.ui.ExpandableImageView;
import com.rastating.droidbeard.ui.ListViewSectionHeader;

import java.util.ArrayList;

public class TVShowSummaryAdapter extends ArrayAdapter<Object> {
    private Context mContext;
    private int mLayoutResourceId;
    private Object[] mObjects;
    private LayoutInflater mInflater;

    private TVShowSummaryAdapter(Context context, LayoutInflater inflater, int layoutResourceId, Object[] objects) {
        super(context, layoutResourceId, objects);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mObjects = objects;
        mInflater = inflater;
    }

    public static TVShowSummaryAdapter createInstance(Context context, LayoutInflater inflater, int layoutResourceId, TVShowSummary[] objects) {
        Preferences preferences = new Preferences(context);
        Object[] list = preferences.getGroupInactiveShows() ? createSectionedList(objects) : createStandardList(objects);
        return new TVShowSummaryAdapter(context, inflater, layoutResourceId, list);
    }

    private static Object[] createStandardList(TVShowSummary[] objects) {
        ArrayList<Object> list = new ArrayList<Object>(objects.length);
        for (int i = 0; i < objects.length; i++) {
            list.add(objects[i]);
        }

        return list.toArray(new Object[list.size()]);
    }

    private static Object[] createSectionedList(TVShowSummary[] objects) {
        ArrayList<Object> sectionedList = new ArrayList<Object>();
        ArrayList<TVShowSummary> activeShows = new ArrayList<TVShowSummary>();
        ArrayList<TVShowSummary> inactiveShows = new ArrayList<TVShowSummary>();

        for (int i = 0; i < objects.length; i++) {
            if (objects[i].getPaused() || objects[i].getStatus().equalsIgnoreCase("Ended")) {
                inactiveShows.add(objects[i]);
            }
            else {
                activeShows.add(objects[i]);
            }
        }

        if (activeShows.size() > 0 && inactiveShows.size() > 0) {
            sectionedList.add(new ListViewSectionHeader("Active Shows"));
            sectionedList.addAll(activeShows);
            sectionedList.add(new ListViewSectionHeader("Inactive Shows"));
            sectionedList.addAll(inactiveShows);
        }
        else {
            sectionedList.addAll(activeShows);
            sectionedList.addAll(inactiveShows);
        }

        return sectionedList.toArray(new Object[sectionedList.size()]);
    }

    @Override
    public TVShowSummary getItem(int position) {
        return mObjects[position] instanceof TVShowSummary ? (TVShowSummary) mObjects[position] : null;
    }

    private View getTVShowSummaryRowView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TVShowHolder holder;

        if (row == null || row.getTag() == null) {
            row = mInflater.inflate(mLayoutResourceId, parent, false);
            holder = new TVShowHolder();
            holder.showName = (TextView) row.findViewById(R.id.show_name);
            holder.airs = (TextView) row.findViewById(R.id.airs);

            View bannerView = row.findViewById(R.id.banner);
            if (bannerView != null) {
                holder.banner = (ExpandableImageView) bannerView;
            }

            row.setTag(holder);
        }
        else {
            holder = (TVShowHolder) row.getTag();
        }

        if (position % 2 == 0) {
            row.setBackgroundResource(R.drawable.alternate_list_item_bg);
        }
        else {
            row.setBackgroundColor(Color.TRANSPARENT);
        }

        TVShowSummary show = (TVShowSummary) mObjects[position];
        holder.showName.setText(show.getName());

        if (show.getNextAirDate() != null) {
            holder.airs.setText(String.format("Next episode on %tB %te, %tY on %s", show.getNextAirDate(), show.getNextAirDate(), show.getNextAirDate(), show.getNetwork()));
        }
        else {
            holder.airs.setText("No upcoming episodes scheduled");
        }

        if (holder.banner != null) {
            holder.banner.setImageBitmap(show.getBanner());
        }

        return row;
    }

    private View getSectionHeaderView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.list_view_section_header_item, parent, false);
        ListViewSectionHeader header = (ListViewSectionHeader) mObjects[position];
        ((TextView) convertView.findViewById(R.id.title)).setText(header.getTitle());

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mObjects[position] instanceof TVShowSummary) {
            return getTVShowSummaryRowView(position, convertView, parent);
        }
        else {
            return getSectionHeaderView(position, convertView, parent);
        }
    }

    private class TVShowHolder {
        public TextView showName;
        public TextView airs;
        public ExpandableImageView banner;
    }
}