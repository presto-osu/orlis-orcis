/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.uicomponents;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;


public class TabStripAdapter extends FragmentPagerAdapter {

    private final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();

    private final Context context;

    private final FragmentManager fragmentManager;

    private final ViewPager viewPager;

    private final SlidingTabLayout tabLayout;

    static final class TabInfo {

        private final Class<?> mClass;

        private final Bundle mArgs;

        private final int mTitleRes;

        public final int mPosition;

        public String mBookmarkName;

        @Override
        protected void finalize() throws Throwable {
            Log.d(getClass().getName(), "TabStripAdapter finalized");
            super.finalize();
        }

        TabInfo(String bookmark, int position, Class<?> fragmentClass, Bundle args, int titleRes) {
            mPosition = position;
            mClass = fragmentClass;
            mArgs = args;
            mTitleRes = titleRes;
            mBookmarkName = bookmark;
        }
    }

    public TabStripAdapter(FragmentManager fm, Context context, ViewPager pager, SlidingTabLayout tabs, ViewPager.OnPageChangeListener pageChangeListener) {
        this(fm, context, pager, tabs);
        if (pageChangeListener != null) {
            tabLayout.setOnPageChangeListener(pageChangeListener);
        }
    }

    public TabStripAdapter(FragmentManager fm, Context context, ViewPager pager, SlidingTabLayout tabs) {
        super(fm);
        fragmentManager = fm;
        this.context = context;

        // setup view pager
        viewPager = pager;
        viewPager.setAdapter(this);

        // setup tabs
        tabLayout = tabs;
//        tabLayout.setCustomTabView(R.layout.tabstrip_item_allcaps, R.id.textViewTabStripItem);
//        tabLayout.setSelectedIndicatorColors(context.getResources().getColor(R.color.white));
        tabLayout.setViewPager(viewPager);
    }

    public void addTab(String bookmark, int titleRes, Class<?> fragmentClass, Bundle args) {
        tabs.add(new TabInfo(bookmark, tabs.size(), fragmentClass, args, titleRes));
    }

    @Override
    public long getItemId(int position) {
        TabInfo tab = tabs.get(position);
        return tab.mPosition;
    }

    public String getItemBookmark(int position) {
        TabInfo tab = tabs.get(position);
        return tab.mBookmarkName;
    }

    /**
     * Notifies the adapter and tab strip that the tabs have changed.
     */
    public void notifyTabsChanged() {
        super.notifyDataSetChanged();
        tabLayout.setViewPager(viewPager);
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo tab = tabs.get(position);
        return Fragment.instantiate(context, tab.mClass.getName(), tab.mArgs);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TabInfo tabInfo = tabs.get(position);
        if (tabInfo != null) {
            return context.getString(tabInfo.mTitleRes).toUpperCase(Locale.getDefault());
        }
        return "";
    }

}
