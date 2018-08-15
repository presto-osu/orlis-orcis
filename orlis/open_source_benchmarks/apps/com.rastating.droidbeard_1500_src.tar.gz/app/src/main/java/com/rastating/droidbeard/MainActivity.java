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

package com.rastating.droidbeard;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.rastating.droidbeard.fragments.ComingEpisodesFragment;
import com.rastating.droidbeard.fragments.DroidbeardFragment;
import com.rastating.droidbeard.fragments.HistoryFragment;
import com.rastating.droidbeard.fragments.InvalidAddressFragment;
import com.rastating.droidbeard.fragments.LogFragment;
import com.rastating.droidbeard.fragments.NavigationDrawerFragment;
import com.rastating.droidbeard.fragments.PreferencesFragment;
import com.rastating.droidbeard.fragments.ProfilesFragment;
import com.rastating.droidbeard.fragments.SetupFragment;
import com.rastating.droidbeard.fragments.ShowFragment;
import com.rastating.droidbeard.fragments.ShowsFragment;
import com.rastating.droidbeard.net.ApiResponseListener;
import com.rastating.droidbeard.net.RestartTask;
import com.rastating.droidbeard.net.ShutdownTask;
import com.rastating.droidbeard.net.SickbeardAsyncTask;

import java.net.URI;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, SharedPreferences.OnSharedPreferenceChangeListener {

    private Fragment mCurrentFragment;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ShowsFragment mShowsFragment;
    private ComingEpisodesFragment mComingEpisodesFragment;
    private CharSequence mTitle;

    private FloatingActionMenu floatingActionsMenu;

    private Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public void setCurrentFragment(Fragment value) {
        mCurrentFragment = value;
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFragment() == null || !(getCurrentFragment() instanceof DroidbeardFragment)) {
            if (getCurrentFragment() instanceof PreferencesFragment) {
                mNavigationDrawerFragment.selectItem(0, true);
            }
            else {
                super.onBackPressed();
            }
        }
        else {
            DroidbeardFragment fragment = (DroidbeardFragment) getCurrentFragment();
            if (fragment.onBackPressed()) {
                if (fragment instanceof ShowFragment) {
                    if (((ShowFragment) fragment).shouldReturnToUpcomingEpisodes()) {
                        mNavigationDrawerFragment.selectItem(1, true);
                    }
                    else {
                        mNavigationDrawerFragment.selectItem(0, true);
                    }
                }
                else {
                    super.onBackPressed();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setFloatingActionButton();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        if (mTitle == null) {
            mTitle = getTitle();
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        if(floatingActionsMenu != null) {
            if (mCurrentFragment instanceof ShowsFragment) {
                floatingActionsMenu.setVisibility(View.VISIBLE);
                ((ShowsFragment) mCurrentFragment).setFloatingActionMenu(floatingActionsMenu);
            } else {
                floatingActionsMenu.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean isUrlValid(String url) {
        try {
            URI uri = URI.create(url);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        Preferences preferences = new Preferences(this);

        String sickbeardUrl = preferences.getSickbeardUrl();
        boolean hasUrl = sickbeardUrl != null && sickbeardUrl.length() > 0;
        boolean hasApiKey = preferences.getApiKey() != null && preferences.getApiKey().length() > 0;

        if ((!hasUrl || !hasApiKey) && position != 4 && position != 99) {
            fragment = new SetupFragment();
        }
        else if (!isUrlValid(sickbeardUrl) && position != 99 && position != 4) {
            fragment = new InvalidAddressFragment();
        }
        else if (position == 0) {
            if (mShowsFragment == null) {
                mShowsFragment = new ShowsFragment();
            }

            fragment = mShowsFragment;
        }
        else if (position == 1) {
            if (mComingEpisodesFragment == null) {
                mComingEpisodesFragment = new ComingEpisodesFragment();
            }

            fragment = mComingEpisodesFragment;
        }
        else if (position == 2) {
            fragment = new HistoryFragment();
        }
        else if (position == 3) {
            fragment = new LogFragment();
        }
        else if (position == 4) {
            fragment = new ProfilesFragment();
        }
        else if (position == 5) {
            onNavigationDrawerItemSelected(99);
            setTitle(getString(R.string.action_settings));
        }
        else if (position == 99) {
            fragment = new PreferencesFragment();
        }

        if (fragment != null) {
            FragmentManager manager = this.getFragmentManager();
            manager.beginTransaction().replace(R.id.container, fragment).commit();
            setCurrentFragment(fragment);

            if(floatingActionsMenu != null) {
                if (fragment instanceof ShowsFragment) {
                    floatingActionsMenu.setVisibility(View.VISIBLE);
                    ((ShowsFragment) fragment).setFloatingActionMenu(floatingActionsMenu);
                } else {
                    floatingActionsMenu.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        if (mTitle != null) {
            actionBar.setTitle(mTitle);
        }
    }

    public void setTitle(String value) {
        mTitle = value;
        restoreActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_power) {
            shutdownSickbeard();
        }
        else if (id == R.id.action_restart) {
            restartSickbeard(true);
        }
        else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void shutdownSickbeard() {
        shutdownSickbeard(true);
    }

    private void shutdownSickbeard(boolean prompt) {
        if (prompt) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation")
                    .setMessage("Are you sure you want to shutdown SickBeard?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            shutdownSickbeard(false);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
        else {
            ShutdownTask task = new ShutdownTask(this);
            task.addResponseListener(new ApiResponseListener<Boolean>() {
                @Override
                public void onApiRequestFinished(SickbeardAsyncTask sender, Boolean result) {
                    finish();
                }
            });
            task.start();
        }
    }

    private void restartSickbeard(boolean prompt) {
        if (prompt) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation")
                    .setMessage("Are you sure you want to restart SickBeard?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            restartSickbeard(false);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
        else {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Restarting");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();

            RestartTask task = new RestartTask(this);
            task.addResponseListener(new ApiResponseListener<Boolean>() {
                @Override
                public void onApiRequestFinished(SickbeardAsyncTask sender, Boolean result) {
                    dialog.dismiss();
                }
            });
            task.start();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        invalidateFragmentCache();
    }

    public void invalidateFragmentCache() {
        mShowsFragment = null;
        mComingEpisodesFragment = null;
    }


    public void setFloatingActionButton() {
        floatingActionsMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        floatingActionsMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean b) {
                floatingActionsMenu.close(true);
                if(b) {
                    startActivity(new Intent(getApplicationContext(), ShowSearch.class));
                }
            }
        });
    }


    public void displayAndRefreshShowsFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("resetAdapter", true);

        mShowsFragment = new ShowsFragment();
        mShowsFragment.setArguments(bundle);

        FragmentManager manager = this.getFragmentManager();
        manager.beginTransaction().replace(R.id.container, mShowsFragment).commit();
        setCurrentFragment(mShowsFragment);
    }
}
