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


package org.berlin_vegan.bvapp.listeners;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;
import org.berlin_vegan.bvapp.data.Locations;

public class CustomLocationListener implements LocationListener {

    private static final String TAG = "CustomLocationListener";

    private final LocationsOverviewActivity mLocationListActivity;
    private final Locations mLocations;

    public CustomLocationListener(LocationsOverviewActivity locationListActivity, Locations locations) {
        mLocationListActivity = locationListActivity;
        mLocations = locations;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location found: " + location.toString());
        //remove to preserve battery
        mLocationListActivity.removeGpsLocationUpdates();
        mLocationListActivity.setLocationFound(location);
        mLocations.updateLocationAdapter(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // nothing to do
    }

    @Override
    public void onProviderEnabled(String provider) {
        // nothing to do
    }

    @Override
    public void onProviderDisabled(String provider) {
        // nothing to do
    }
}
