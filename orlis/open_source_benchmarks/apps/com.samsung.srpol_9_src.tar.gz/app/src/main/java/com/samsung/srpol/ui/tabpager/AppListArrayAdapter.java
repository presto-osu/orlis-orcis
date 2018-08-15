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

import java.util.ArrayList;

import com.samsung.srpol.R;
import com.samsung.srpol.data.Category;
import com.samsung.srpol.data.Subcategory;
import com.samsung.srpol.loader.AppDetails;
import com.samsung.srpol.loader.AppListLoader;
import com.samsung.srpol.utils.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppListArrayAdapter extends ArrayAdapter<AppDetails> {

    private static SharedPreferences mSp;
    
    private final Context mContext;
    private final Category mCategory;
    private ArrayList<AppDetails> mDisplayedList;

    private static class ViewHolder {
        public TextView appName;
        public TextView appPackageName;
        public ImageView appIcon;
        public ImageView systemAppIcon;
        public LinearLayout groupIcons;
        public ImageView[] groupIconsView;
    }

    public AppListArrayAdapter(Context context, Category category) {
        super(context, R.layout.app_list_item_view);
        mContext = context;
        mCategory = category;
        mDisplayedList = new ArrayList<AppDetails>();
        if (mSp == null)
            mSp = PreferenceManager.getDefaultSharedPreferences(mContext);
        refreshVisibleList();
    }

    private void refreshVisibleList() {
        boolean includeSystemApps = mSp.getBoolean(
                AppListLoader.PREF_INCLUDE_SYSTEM_APPS, true);
        
        mDisplayedList.clear();
        for (AppDetails app : mCategory.getRelatedApps()) {
            if (isVisible(app, includeSystemApps))
                mDisplayedList.add(app);
        }
        mCategory.updateVisibleCount(mDisplayedList.size());
    }

    @Override
    public void notifyDataSetChanged() {
        refreshVisibleList();
        super.notifyDataSetChanged();
    }
    
    @Override
    public AppDetails getItem(int position) {
        return mDisplayedList.get(position);
    }
    
    @Override
    public int getCount() {
        return mDisplayedList.size();
    }
    
    private boolean isVisible(AppDetails item, boolean includeSystemApps) {

        return !item.isSystemApp() 
                || (includeSystemApps && item.isSystemApp());
    }
    
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_item_view, parent,
                    false);
            holder = new ViewHolder();
            holder.appName = (TextView) convertView
                    .findViewById(R.id.list_app_name);
            holder.appPackageName = (TextView) convertView
                    .findViewById(R.id.list_app_package);
            holder.appIcon = (ImageView) convertView
                    .findViewById(R.id.list_app_icon);
            holder.systemAppIcon = (ImageView) convertView
                    .findViewById(R.id.list_system_app_icon);
            holder.groupIcons = (LinearLayout) convertView
                    .findViewById(R.id.app_list_group_icons);
            
            int i = 0;
            holder.groupIconsView = new ImageView[mCategory.getSubCategories().size()];
            for (Subcategory category : mCategory.getSubCategories()){
                ImageView icon = new ImageView(mContext);
                icon.setImageDrawable(category.getDarkIcon());
                holder.groupIconsView[i++] = icon;
                holder.groupIcons.addView(icon);
            }
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        AppDetails details = getItem(position);
        holder.appName.setText(details.getAppName());
        holder.appPackageName.setText(details.getAppPackageName());
        holder.appIcon.setImageDrawable(details.getAppIcon());

        // disabling and enabling apps in system application manager is available since API 4.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (details.isEnabled()) {
                holder.appName.setTextColor(mContext.getResources().getColor(android.R.color.black));
            } else {
                holder.appName.setTextColor(mContext.getResources().getColor(R.color.disabled_app_name));
            }
        }
        if(details.isSystemApp()){
            holder.systemAppIcon.setVisibility(View.VISIBLE);
            if(details.isEnabled()){
                holder.systemAppIcon.setImageDrawable(Utils.getmSystemIcon(mContext));
            } else {
                holder.systemAppIcon.setImageDrawable(Utils.getmSystemIconDisable(mContext));
            }
        } else {
            holder.systemAppIcon.setVisibility(View.INVISIBLE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            convertView.setBackgroundDrawable(mContext
                    .getResources()
                    .getDrawable(
                            details.isSystemApp() ? R.color.grayout_list_item_bg
                                    : android.R.color.white));
        else {
            convertView.setBackground(mContext
                    .getResources()
                    .getDrawable(
                            details.isSystemApp() ? R.color.grayout_list_item_bg
                                    : android.R.color.white));
        }
        for (int i = 0; i < mCategory.getSubCategories().size(); ++i) {
            if (details.isInSubcategory(mCategory.getSubCategories().get(i).getId())) {
                // disabling and enabling apps in system application manager is available since API 4.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (details.isEnabled())
                        holder.groupIconsView[i].setImageDrawable(mCategory.getSubCategories().get(i).getDarkIcon());
                    else
                        holder.groupIconsView[i].setImageDrawable(mCategory.getSubCategories().get(i).getDisabledIcon());
                }
                holder.groupIconsView[i].setVisibility(View.VISIBLE);
            } else {
                holder.groupIconsView[i].setVisibility(View.GONE);
            }
        }
        return convertView;
    }
    
}
