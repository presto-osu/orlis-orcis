/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.functional;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.gabm.fancyplaces.ui.TabItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp1 on 21-01-2015.
 */


public class MainWindowViewpagerAdapter extends FragmentStatePagerAdapter {

    private List<TabItem> tabItemList = new ArrayList<>();
    private Context curContext = null;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public MainWindowViewpagerAdapter(Context context, FragmentManager fm, List<TabItem> newTabItemList) {
        super(fm);

        tabItemList = newTabItemList;
        curContext = context;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        return tabItemList.get(position);
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return tabItemList.get(position).getTitle(curContext);
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return tabItemList.size();
    }
}
