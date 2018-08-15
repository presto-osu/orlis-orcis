/**
 *   This file is part of Ophasenkistenstapeln.
 *
 *   Ophasenkistenstapeln is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Ophasenkistenstapeln is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Ophasenkistenstapeln.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.d120.ophasekistenstapeln;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import de.d120.ophasekistenstapeln.highscore.Highscore;

/**
 * @author exploide
 * @author bhaettasch
 * @author saibot2013
 */
public class MainActivity extends Activity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Highscore highscore = new Highscore();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity1);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.app_name);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        // Load Highscore
        SharedPreferences userDetails = this.getSharedPreferences(
                "ophase-kistenstapel", MODE_PRIVATE);
        this.highscore = new Gson().fromJson(
                userDetails.getString("HIGHSCORE", ""), Highscore.class);
        if (this.highscore == null) {
            this.highscore = new Highscore();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Log.d("MainActivity1", "navigation drawer item selected at position: "
                + position);
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            // Countdown
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new CountdownFragment()).commit();
                break;
            // calculate
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new CalculateFragment()).commit();
                break;
            // highscore
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new HighscoreFragment()).commit();
                break;
            default:
                break;
        }
        onActionBarTitleUpdated(position);
    }

    /**
     * Sets the title to actionbar
     *
     * @param num The Position of Menu-Entry
     */
    public void onActionBarTitleUpdated(int num) {
        switch (num) {
            case 0:
                mTitle = getString(R.string.title_section_countdown);
                break;
            case 1:
                mTitle = getString(R.string.title_section_calculate);
                break;
            case 2:
                mTitle = getString(R.string.title_section_highscore);
                break;
            default:
                Log.w("MainActivity1", "Undefined number " + num);
        }
        restoreActionBar();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section_countdown);
                break;
            case 2:
                mTitle = getString(R.string.title_section_calculate);
                break;
            case 3:
                mTitle = getString(R.string.title_section_highscore);
                break;
            default:
                Log.w("MainActivity1", "Undefined number " + number);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_activity1, menu);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Highscore to shared preferences
        SharedPreferences.Editor editor = this.getSharedPreferences(
                "ophase-kistenstapel", MODE_PRIVATE).edit();
        Log.d("MainActivity1",
                "Saving highscore: " + new Gson().toJson(this.highscore));
        editor.putString("HIGHSCORE", new Gson().toJson(this.highscore));
        editor.commit();
    }

    ;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // load highscore
        SharedPreferences userDetails = this.getSharedPreferences(
                "ophase-kistenstapel", MODE_PRIVATE);
        this.highscore = new Gson().fromJson(
                userDetails.getString("HIGHSCORE", ""), Highscore.class);
    }

    ;

    @Override
    protected void onPause() {
        super.onPause();
        // Save Highscore to shared preferences
        SharedPreferences.Editor editor = this.getSharedPreferences(
                "ophase-kistenstapel", MODE_PRIVATE).edit();
        Log.d("MainActivity1",
                "Saving highscore: " + new Gson().toJson(this.highscore));
        editor.putString("HIGHSCORE", new Gson().toJson(this.highscore));
        editor.commit();
    }

    ;

    /**
     * Returns the highscore-instance
     *
     * @return global highscore
     */
    public Highscore getHighscore() {
        return this.highscore;
    }

}
