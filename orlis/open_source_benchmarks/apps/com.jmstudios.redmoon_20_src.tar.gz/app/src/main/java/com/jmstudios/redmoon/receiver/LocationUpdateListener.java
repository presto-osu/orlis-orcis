/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.receiver;

import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import com.jmstudios.redmoon.preference.LocationPreference;

import com.jmstudios.redmoon.R;

public class LocationUpdateListener implements LocationListener {
    private static final String TAG = "LocationUpdate";
    private static final boolean DEBUG = true;

    private Context mContext;
    private LocationPreference mPreference;

    public LocationUpdateListener(Context context) {
        this(context, null);
    }

    public LocationUpdateListener(Context context, LocationPreference preference) {
        mContext = context;
        mPreference = preference;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (DEBUG) Log.i(TAG, "Location search succeeded");
        LocationManager locationManager = (LocationManager)
            mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);

        String prefKey = mContext.getString(R.string.pref_key_location);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(prefKey, location.getLatitude() + "," + location.getLongitude());
        editor.apply();

        if (mPreference != null) mPreference.handleLocationFound(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (DEBUG) Log.i(TAG, "Status changed for " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {
        if (DEBUG) Log.i(TAG, "Location search failed");
        LocationManager locationManager = (LocationManager)
            mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);

        if (mPreference != null) mPreference.handleLocationSearchFailed();
    }
}
