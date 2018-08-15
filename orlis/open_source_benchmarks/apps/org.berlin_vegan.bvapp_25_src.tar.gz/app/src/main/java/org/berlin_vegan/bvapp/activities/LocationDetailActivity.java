/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.fragments.LocationDetails.LocationActionsFragment;
import org.berlin_vegan.bvapp.fragments.LocationDetails.LocationDescriptionFragment;
import org.berlin_vegan.bvapp.fragments.LocationDetails.LocationDetailsFragment;
import org.berlin_vegan.bvapp.fragments.LocationDetails.LocationHeadFragment;
import org.berlin_vegan.bvapp.fragments.LocationMapFragment;
import org.berlin_vegan.bvapp.helpers.DividerFragment;
import org.berlin_vegan.bvapp.helpers.UiUtils;

/**
 * Activity for the detail view of a gastro location.
 */
public class LocationDetailActivity extends BaseActivity {

    public static final String EXTRA_LOCATION = "LOCATION";

    private Location mGastroLocation;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_detail_activity);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mGastroLocation = (Location) extras.getSerializable(EXTRA_LOCATION);
            }
        } else {
            mGastroLocation = (Location) savedInstanceState.getSerializable(EXTRA_LOCATION);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // -->> Disable scrolling in the Map fragment (for allowing undisturbed interaction with the map)
        AppBarLayout mapAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mapAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
        // <<-- Disable scrolling in the Map fragment

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

        final LocationHeadFragment gastroHeadFragment = new LocationHeadFragment();
        getSupportFragmentManager().beginTransaction().add(linearLayout.getId(), gastroHeadFragment).commit();

        final LocationActionsFragment locationActionsFragment = new LocationActionsFragment();
        getSupportFragmentManager().beginTransaction().add(linearLayout.getId(), locationActionsFragment).commit();

        DividerFragment dividerFragment = new DividerFragment();
        getSupportFragmentManager().beginTransaction().add(linearLayout.getId(), dividerFragment).commit();


        LocationDescriptionFragment gastroDescriptionFragment = new LocationDescriptionFragment();
        getSupportFragmentManager().beginTransaction().add(linearLayout.getId(), gastroDescriptionFragment).commit();

        dividerFragment = new DividerFragment();
        getSupportFragmentManager().beginTransaction().add(linearLayout.getId(), dividerFragment).commit();

        LocationDetailsFragment locationDetailsFragment = new LocationDetailsFragment();
        getSupportFragmentManager().beginTransaction().add(linearLayout.getId(), locationDetailsFragment).commit();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mGastroLocation == null) { // TODO: clarify why it is null
            return;
        }

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        final String title = mGastroLocation.getName();
        collapsingToolbar.setTitle(title);

        // otherwise the backdrop is not fully visible
        final int transparent = getResources().getColor(android.R.color.transparent);
        toolbar.setBackgroundColor(transparent);

        LocationMapFragment mapFragment = (LocationMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.backdrop);
        mapFragment.setLocation(mGastroLocation);

        final FloatingActionButton navButton = (FloatingActionButton) findViewById(R.id.fab);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationButtonClicked();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gastro_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // respond to the action bar's up button
                NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(EXTRA_LOCATION, mGastroLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void navigationButtonClicked() {
        // tested with oeffi, google maps,citymapper and osmand
        final String uriString = "geo:" + mGastroLocation.getLatCoord() + "," + mGastroLocation.getLongCoord() + "?q=" + mGastroLocation.getStreet() + ", " + mGastroLocation.getCityCode() + ", " + mGastroLocation.getCity();

        Uri geoIntentUri = Uri.parse(uriString);
        Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoIntentUri);
        if (geoIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(geoIntent);
        } else {
            UiUtils.showMaterialDialog(this, getString(R.string.error),
                    getString(R.string.gastro_details_no_navigation_found));
        }
    }
}
