package com.jparkie.aizoban.views.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.MainPresenter;
import com.jparkie.aizoban.presenters.MainPresenterImpl;
import com.jparkie.aizoban.views.MainView;

public class MainActivity extends BaseActivity implements MainView {
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String POSITION_ARGUMENT_KEY = TAG + ":" + "PositionArgumentKey";

    private MainPresenter mMainPresenter;

    private Toolbar mToolbar;
    private FrameLayout mMainLayout;
    private FrameLayout mNavigationLayout;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainPresenter = new MainPresenterImpl(this);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.mainToolbar);
        mMainLayout = (FrameLayout)findViewById(R.id.mainFragmentContainer);
        mNavigationLayout = (FrameLayout)findViewById(R.id.navigationFragmentContainer);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);

        mMainPresenter.initializeViews();

        if (savedInstanceState != null) {
            mMainPresenter.restoreState(savedInstanceState);
        } else {
            mMainPresenter.initializeMainLayout(getIntent());
            mMainPresenter.initializeNavigationLayout();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMainPresenter.registerForEvents();
    }

    @Override
    protected void onStop() {
        mMainPresenter.unregisterForEvents();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMainPresenter.destroyAllSubscriptions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMainPresenter.saveState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(mNavigationLayout)) {
                menu.clear();
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(mNavigationLayout)) {
                mDrawerLayout.closeDrawer(mNavigationLayout);

                return;
            }
        }

        super.onBackPressed();
    }

    // MainView:

    @Override
    public void initializeToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.app_name);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.primaryBlue500));

            setSupportActionBar(mToolbar);
        }
    }

    @Override
    public void initializeDrawerLayout() {
        if (mDrawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    mToolbar,
                    R.string.action_drawer_open,
                    R.string.action_drawer_close
            ){
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);

                    invalidateOptionsMenu();
                    // Do Nothing.
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);

                    invalidateOptionsMenu();
                    // Do Nothing.
                }
            };

            mDrawerToggle.setHomeAsUpIndicator(android.R.drawable.menu_frame);

            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }

    @Override
    public void closeDrawerLayout() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mNavigationLayout);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public int getNavigationLayoutId() {
        return R.id.navigationFragmentContainer;
    }

    @Override
    public int getMainLayoutId() {
        return R.id.mainFragmentContainer;
    }
}
