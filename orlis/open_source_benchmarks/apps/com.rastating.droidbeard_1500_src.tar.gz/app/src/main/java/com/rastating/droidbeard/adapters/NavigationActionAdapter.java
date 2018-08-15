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
import android.widget.ImageView;
import android.widget.TextView;

import com.rastating.droidbeard.entities.NavigationAction;
import com.rastating.droidbeard.R;

public class NavigationActionAdapter extends ArrayAdapter<NavigationAction> {
    private Context mContext;
    private int mLayoutResourceId;
    private NavigationAction[] mObjects;
    private int mSelectedPosition;
    private LayoutInflater mInflater;
    private int mInactiveTextColor;
    private int mActiveTextColor;

    public NavigationActionAdapter(Context context, LayoutInflater inflater, int layoutResourceId, NavigationAction[] objects) {
        super(context, layoutResourceId, objects);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mObjects = objects;
        mSelectedPosition = 0;
        mInflater = inflater;
        mInactiveTextColor = Color.parseColor("#838383");
        mActiveTextColor = Color.parseColor("#333333");
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NavigationActionHolder holder;

        if (row == null) {
            row = mInflater.inflate(mLayoutResourceId, parent, false);

            holder = new NavigationActionHolder();
            holder.icon = (ImageView) row.findViewById(R.id.icon);
            holder.text = (TextView) row.findViewById(R.id.text);

            row.setTag(holder);
        }
        else {
            holder = (NavigationActionHolder) row.getTag();
        }

        NavigationAction action = mObjects[position];
        holder.text.setText(action.getText());

        if (position == mSelectedPosition) {
            row.setBackgroundColor(mContext.getResources().getColor(R.color.navigation_list_item_selected));
            holder.text.setTextColor(mActiveTextColor);
            holder.icon.setImageResource(action.getActiveIconResourceId());
        }
        else {
            row.setBackgroundColor(Color.TRANSPARENT);
            holder.text.setTextColor(mInactiveTextColor);
            holder.icon.setImageResource(action.getInactiveIconResourceId());
        }

        return row;
    }

    private class NavigationActionHolder {
        public ImageView icon;
        public TextView text;
    }
}