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

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.samsung.srpol.data.Category;
import com.samsung.srpol.loader.AppListLoader;

public class PageAdapter extends FragmentPagerAdapter {

    private static List<Category> mCategories;
    private static PageFragment[] mCreatedFragments = null;
    
    public PageAdapter(FragmentManager fm, Context context) {
        super(fm);
    }
    
    public void refreshPages() {
        if (mCategories == null) {
            mCategories = AppListLoader.getCategories();
            notifyDataSetChanged();
        } else {
            refreshAdapterNotify();
        }
    }

    public void refreshAdapterNotify() {
        for (PageFragment fragment : mCreatedFragments) {
            if (fragment != null)
                fragment.notifyDataSetChanged();
        }
    }
    @Override
    public Fragment getItem(int position) {
        if (mCreatedFragments == null)
            mCreatedFragments = new PageFragment[mCategories.size()];
        PageFragment f = mCreatedFragments[position];
        if (f == null) {
            f = new PageFragment();
            
            Bundle args = new Bundle();
            args.putInt("position", position);
            f.setArguments(args);
            mCreatedFragments[position] = f;
        }
        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCategories.get(position).getTitle();
    }
    
    @Override
    public int getCount() {
        return mCategories == null ? 0 : mCategories.size();
    }
    
}
