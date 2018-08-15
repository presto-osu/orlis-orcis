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

package com.samsung.srpol.ui;

import java.util.List;

import com.samsung.srpol.R;
import com.samsung.srpol.loader.AppDetails;
import com.samsung.srpol.loader.AppListLoader;
import com.samsung.srpol.ui.drawer.MenuFragment;
import com.samsung.srpol.ui.drawer.NavigationDrawerItemListener;
import com.samsung.srpol.ui.tabpager.PageAdapter;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<List<AppDetails>> {
    public static final String TAG = "MainActivity";

    private static final String STATE_WELCOME_DIALOG_IS_OPEN = "welcome_dialog_is_open";
    private static final String STATE_WELCOME_DIALOG_OPENED_FROM_MENU = "welcome_dialog_opened_from_menu";
    private static final String STATE_DRAWER_MENU_IS_OPEN = "state_drawer_menu_is_open";

    private MenuFragment mMenuFragment;

    private WelcomeDialog mWelcomeDialog;
    private ViewPager mViewPager;
    private PageAdapter mFramePagerAdapter;
    private boolean mDrawerOpenState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            boolean isWelcomeDialogOpen = savedInstanceState.getBoolean(
                    STATE_WELCOME_DIALOG_IS_OPEN, false);
            if (isWelcomeDialogOpen) {
                boolean isOpenedFromMenu = savedInstanceState.getBoolean(
                        STATE_WELCOME_DIALOG_OPENED_FROM_MENU, false);
                showWelcomeDialog(isOpenedFromMenu);
            }
            mDrawerOpenState = savedInstanceState.getBoolean(STATE_DRAWER_MENU_IS_OPEN, false);
        } else {
            showWelcomeDialog(false);
        }

        getSupportLoaderManager().initLoader(0, null, this);

        // if Loader was created then we have all data to create
        // NavigationDrawer otherwise wait for loader to be created and then
        // create NavigationDrawer
        if (getSupportLoaderManager().getLoader(0) != null) {
            initNavigationDrawer();
        }

        mFramePagerAdapter = new PageAdapter(getSupportFragmentManager(), getApplicationContext());
        mViewPager = (ViewPager) findViewById(R.id.myviewpager);
        mViewPager.setAdapter(mFramePagerAdapter);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.titlestrip);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.tab_underline_color));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWelcomeDialog != null) {
            outState.putBoolean(STATE_WELCOME_DIALOG_IS_OPEN,
                    mWelcomeDialog.isShowing());
            outState.putBoolean(STATE_WELCOME_DIALOG_OPENED_FROM_MENU,
                    mWelcomeDialog.ismOpenedFromMenu());
        }
        if (isDrawerOpen() || (mDrawerOpenState && mDrawerLayout == null)) {
            outState.putBoolean(STATE_DRAWER_MENU_IS_OPEN, true);
        }
    }

    protected void onDestroy() {
        if (mWelcomeDialog != null) {
            if (mWelcomeDialog.isShowing()) {
                mWelcomeDialog.dismiss();
            }
        }
        super.onDestroy();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean showSystemApp = sp.getBoolean(
                AppListLoader.PREF_INCLUDE_SYSTEM_APPS,
                true);
        MenuItem menuItemSystem = menu.findItem(R.id.action_toggle_system_visibility);
        if (menuItemSystem != null) {
            if (showSystemApp) {
                menuItemSystem.setTitle(R.string.hide_system_visibility);
            } else {
                menuItemSystem.setTitle(R.string.show_system_visibility);
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle != null
                && mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                showWelcomeDialog(true);
                return true;
            case R.id.action_licence:
                showLicense();
                return true;
            case R.id.action_toggle_system_visibility:
                toggleSharedPrefences(AppListLoader.PREF_INCLUDE_SYSTEM_APPS);
                refreshAfterChanges();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleSharedPrefences(String preferenceName) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean actualValue = sp.getBoolean(preferenceName, true);
        sp.edit().putBoolean(preferenceName, !actualValue).commit();
    }

    public void refreshAfterChanges() {
        supportInvalidateOptionsMenu();
        if (mFramePagerAdapter != null) {
            mFramePagerAdapter.refreshAdapterNotify();
            mFramePagerAdapter.notifyDataSetChanged();
        }
        if (mMenuFragment != null) {
            mMenuFragment.notifyDataSetChanged();
        }
    }

    @Override
    public Loader<List<AppDetails>> onCreateLoader(int id, Bundle args) {
        AppListLoader loader = new AppListLoader(this.getApplicationContext());
        initNavigationDrawer();

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<AppDetails>> loader,
            List<AppDetails> data) {
        Log.d(TAG, "onLoadFinished");

        AppListLoader appLoader = (AppListLoader) loader;
        if (appLoader.wasDataReloaded()) {
            appLoader.resetWasDataReloaded();
            mViewPager.setOffscreenPageLimit(AppListLoader.getCategories().size());
            mFramePagerAdapter.refreshPages();
        } else {
            mFramePagerAdapter.refreshAdapterNotify();
        }
        if (mWelcomeDialog != null && mWelcomeDialog.isShowing()) {
            mWelcomeDialog.loadingDone();
        }
        if (mMenuFragment != null) {
            mMenuFragment.notifyDataSetChanged();
        }
    }

    public void onLoaderReset(Loader<List<AppDetails>> arg0) {
        mFramePagerAdapter.refreshAdapterNotify();
    }

    private void showWelcomeDialog(boolean fromMenu) {
        Log.d(TAG, "showWelcomeActivity flag: " + fromMenu);
        if (mWelcomeDialog == null) {
            mWelcomeDialog = new WelcomeDialog(this, fromMenu);
        }
        if (!mWelcomeDialog.isShowing()) {
            mWelcomeDialog.show(fromMenu);
        }
        if (!fromMenu) {
            mWelcomeDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    openDrawerAnimate();
                }
            });
        }
    }

    private void showLicense() {
        Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private void initNavigationDrawer() {
        mMenuFragment = (MenuFragment) getSupportFragmentManager()
                .findFragmentById(R.id.menu_container);
        mMenuFragment.setDrawerItemListener(mDrawerItemListener);
        mFragmentContainerView = findViewById(R.id.menu_container);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setUp();
        mMenuFragment.refresh();
    }
    
    private NavigationDrawerItemListener mDrawerItemListener = new NavigationDrawerItemListener() {

        public void onNavigationDrawerItemSelected(int position) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position, true);
                closeDrawer();
            }
        }
    };

    public void closeDrawer() {
        if(mDrawerLayout != null){
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            mDrawerOpenState=false;
        }
    }
    
    public boolean isDrawerOpen() {
        return mDrawerLayout != null
                && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    
    public void setUp() {
        if (mDrawerLayout == null)
            return;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu(); 
            }
        };
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });
        if (mDrawerOpenState && !isDrawerOpen()) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void openDrawerAnimate() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (mDrawerLayout != null) {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                    mDrawerOpenState = true;
                }
            }
        });

        if (mWelcomeDialog != null) {
            mWelcomeDialog.setOnDismissListener(null);
        }
    }
}
