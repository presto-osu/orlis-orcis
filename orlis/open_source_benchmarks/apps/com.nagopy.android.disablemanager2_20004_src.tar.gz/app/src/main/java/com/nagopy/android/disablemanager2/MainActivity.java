/*
 * Copyright (C) 2015 75py
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nagopy.android.disablemanager2;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.nagopy.android.disablemanager2.support.DebugUtil;
import com.nagopy.android.disablemanager2.support.Logic;
import com.viewpagerindicator.PageIndicator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener, AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    PageIndicator pageIndicator;
    ActionMode actionMode;
    AppData reloadAppData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ツールバーをアクションバーとしてセット
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        pageIndicator = (PageIndicator) findViewById(R.id.indicator);
        pageIndicator.setViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pageIndicator.setOnPageChangeListener(this);
    }

    @Override
    protected void onPause() {
        pageIndicator.setOnPageChangeListener(null);
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (reloadAppData != null) {
            ApplicationListAdapter applicationListAdapter = getApplicationListAdapter();

            String packageName = reloadAppData.packageName.split(":")[0];
            PackageManager packageManager = getPackageManager();
            ApplicationInfo packageInfo = Logic.getApplicationInfo(packageManager, packageName);
            if (packageInfo == null) {
                // パッケージが存在しない場合
                applicationListAdapter.removeApplication(reloadAppData);
            } else {
                reloadAppData.isEnabled = packageInfo.enabled;
            }
            applicationListAdapter.doFilter();

            reloadAppData = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionMode != null) {
            // 非表示になっているメニューは何もしない
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_share:
                ApplicationListAdapter listAdapter = getApplicationListAdapter();
                Logic.sendIntent(this, getString(listAdapter.filterType.titleId), Logic.makeShareString(listAdapter.filteredData));
                break;
            case R.id.action_about:
                Intent aboutActivity = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutActivity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ViewPagerで表示中のFragmentを取得する.
     *
     * @return ApplicationListFragment
     */
    private ApplicationListFragment getApplicationListFragment() {
        PagerAdapter adapter = mViewPager.getAdapter();
        return (ApplicationListFragment) adapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }

    /**
     * ViewPagerで表示中のListFragmentのアダプターを取得する.
     *
     * @return ApplicationListAdapter
     */
    private ApplicationListAdapter getApplicationListAdapter() {
        ApplicationListFragment listFragment = getApplicationListFragment();
        return (ApplicationListAdapter) listFragment.getListAdapter();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ApplicationListFragment listFragment = getApplicationListFragment();
        AppData appData = (AppData) listFragment.getListAdapter().getItem(position);
        String packageName = appData.packageName.split(":")[0];
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + packageName));
        reloadAppData = appData;
        startActivityForResult(intent, 1);
    }

    // =============================================================================================================
    // ViewPager.OnPageChangeListener
    // ここから

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ApplicationListFragment fragment = (ApplicationListFragment) mSectionsPagerAdapter.getItem(position);
        ApplicationListAdapter adapter = (ApplicationListAdapter) fragment.getListAdapter();
        if (adapter != null) {
            adapter.doFilter();
        }

        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    // ViewPager.OnPageChangeListener
    // ここまで
    // =============================================================================================================


    // =============================================================================================================
    // AbsListView.MultiChoiceModeListener
    // ここから

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        DebugUtil.debugLog("onCreateActionMode");
        actionMode = mode;
        mode.getMenuInflater().inflate(R.menu.menu_main_multi, menu);
        return true;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // 検索は一つだけ選択されている場合のみ表示
        ApplicationListFragment listFragment = getApplicationListFragment();
        int checkedItemCount = listFragment.getListView().getCheckedItemCount();
        mode.getMenu().findItem(R.id.action_search).setVisible(checkedItemCount == 1);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ApplicationListFragment listFragment = getApplicationListFragment();
        // 選択されたリストアイテム取得
        List<AppData> checkedItemList = Logic.getCheckedItemList(listFragment.getListView());
        switch (item.getItemId()) {
            case R.id.action_search:
                if (checkedItemList.isEmpty()) {
                    throw new RuntimeException("Checked item is empty!");
                }
                AppData selected = checkedItemList.get(0);
                if (Logic.canLaunchImplicitIntent(getApplicationContext(), Intent.ACTION_WEB_SEARCH)) {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, Logic.makeSearchQuery(selected, getApplicationContext()));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(Logic.makeSearchUrl(selected, getApplicationContext())));
                    startActivity(intent);
                }
                break;
            case R.id.action_share:
                Logic.sendIntent(this, Logic.makeShareString(checkedItemList));
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        DebugUtil.verboseLog("onDestroyActionMode");
        actionMode = null;
    }

    // AbsListView.MultiChoiceModeListener
    // ここまで
    // =============================================================================================================

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Map<FilterType, WeakReference<ApplicationListFragment>> cache;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            cache = new EnumMap<>(FilterType.class);
        }


        @Override
        public Fragment getItem(int position) {
            DebugUtil.verboseLog("SectionsPagerAdapter#getItem(" + position + ")");
            FilterType filterType = FilterType.indexOf(position);
            WeakReference<ApplicationListFragment> weakReference = cache.get(filterType);
            if (weakReference == null || weakReference.get() == null) {
                DebugUtil.verboseLog("create new instance");
                ApplicationListFragment fragment = ApplicationListFragment.newInstance(filterType);
                cache.put(filterType, new WeakReference<>(fragment));
                return fragment;
            } else {
                DebugUtil.verboseLog("return from cache");
                return weakReference.get();
            }
        }

        @Override
        public int getCount() {
            return FilterType.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            FilterType filterType = FilterType.values()[position];
            return getString(filterType.titleId);
        }
    }

    public static class ApplicationListFragment extends ListFragment {

        private static final String ARG_FILTER_TYPE = "filter_type";

        public static ApplicationListFragment newInstance(FilterType filterType) {
            ApplicationListFragment fragment = new ApplicationListFragment();
            Bundle args = new Bundle();
            args.putString(ARG_FILTER_TYPE, filterType.name());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return View.inflate(getActivity().getApplicationContext(), R.layout.fragment_application_list, null);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            ListView listView = getListView();
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener((AbsListView.MultiChoiceModeListener) getActivity());

            Context context = getActivity().getApplicationContext();
            FilterType filterType = FilterType.valueOf(getArguments().getString(ARG_FILTER_TYPE));
            final ApplicationListAdapter applicationListAdapter = new ApplicationListAdapter(context, filterType);
            setListAdapter(applicationListAdapter);
            final Handler handler = new Handler();

            final ApplicationList applicationList = ApplicationList.getInstance();
            applicationList.loadApplicationList(context, new ApplicationList.ApplicationLoadListener() {
                @Override
                public void onLoaded(final List<AppData> appList) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            applicationListAdapter.updateApplicationList(appList);
                            applicationListAdapter.doFilter();
                            hideProgressBar();
                        }
                    });
                }
            });
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            ((AdapterView.OnItemClickListener) getActivity()).onItemClick(l, v, position, id);
        }

        public void hideProgressBar() {
            View view = getView();
            if (view != null) {
                view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                    }
                }, 3000);
            }
        }
    }


    public static class ApplicationListAdapter extends BaseAdapter implements Filterable {

        private List<AppData> originalData = Collections.emptyList();
        private List<AppData> filteredData = Collections.emptyList();
        private LayoutInflater mInflater;
        private ItemFilter mFilter = new ItemFilter();
        private final FilterType filterType;
        private final PackageManager packageManager;
        private final int iconSize;

        public ApplicationListAdapter(Context context, FilterType filterType) {
            mInflater = LayoutInflater.from(context);
            this.filterType = filterType;
            this.packageManager = context.getPackageManager();
            this.iconSize = Logic.getIconSize(context);
        }

        public void updateApplicationList(List<AppData> data) {
            this.originalData = data;
            this.filteredData = data;
        }

        public void removeApplication(AppData appData) {
            this.originalData.remove(appData);
            this.filteredData.remove(appData);
        }

        public void doFilter() {
            mFilter.filter(filterType.name());
        }

        public int getCount() {
            return filteredData.size();
        }

        public AppData getItem(int position) {
            return filteredData.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_list_row, null);
                convertView.setMinimumHeight(iconSize * 7 / 4);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.list_title);
                holder.title.setTag(new Object()); // 同期のために使用するオブジェクト
                holder.process = (TextView) convertView.findViewById(R.id.list_process);
                holder.packageName = (TextView) convertView.findViewById(R.id.list_package_name);
                holder.disabled = (TextView) convertView.findViewById(R.id.list_disabled);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppData appData = filteredData.get(position);
            synchronized (holder.title.getTag()) {
                holder.title.setText(appData.label);
                holder.title.setTag(R.id.tag_package_name, appData.packageName);

                Drawable icon = appData.icon == null ? null : appData.icon.get();
                if (icon == null) {
                    DebugUtil.verboseLog("create loader :" + appData.packageName);
                    Logic.setIcon(holder.title, R.drawable.icon_transparent);
                    new ApplicationIconLoader(appData.packageName, packageManager, iconSize, holder.title).execute(appData);
                } else {
                    DebugUtil.verboseLog("use cache onCreateView() :" + appData.packageName);
                    Logic.setIcon(holder.title, icon, iconSize);
                }

                if (appData.process.isEmpty()) {
                    holder.process.setVisibility(View.GONE);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String str : appData.process) {
                        sb.append(str);
                        sb.append(Constants.LINE_SEPARATOR);
                    }
                    sb.setLength(sb.length() - 1);
                    holder.process.setText(sb.toString());
                    holder.process.setVisibility(View.VISIBLE);
                }

                if (appData.isInstalled) {
                    holder.disabled.setVisibility(View.GONE);
                } else {
                    holder.disabled.setText(R.string.not_installed);
                    holder.disabled.setVisibility(View.VISIBLE);
                }

                holder.packageName.setText(appData.packageName);
            }

            return convertView;
        }

        static class ViewHolder {
            TextView title;
            TextView process;
            TextView packageName;
            TextView disabled;
        }

        public Filter getFilter() {
            return mFilter;
        }

        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterType filterType = FilterType.valueOf(constraint.toString());
                FilterResults results = new FilterResults();
                final List<AppData> list = originalData;

                int count = list.size();
                final List<AppData> nlist = new ArrayList<>(count);

                for (AppData appData : list) {
                    if (filterType.isTarget(appData)) {
                        nlist.add(appData);
                    }
                }
                results.values = nlist;
                results.count = nlist.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                filteredData = (ArrayList<AppData>) results.values;
                notifyDataSetChanged();
            }

        }
    }

}
