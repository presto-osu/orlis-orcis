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
package com.jmstudios.redmoon.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import com.jmstudios.redmoon.receiver.LocationUpdateListener;

import com.jmstudios.redmoon.R;

public class LocationPreference extends Preference {
    private static final String TAG = "LocationPreference";
    private static final boolean DEBUG = true;

    public static final String DEFAULT_VALUE = "not set";

    // Location in the form "$LAT,$LONG"
    private String mLocation;

    private Context mContext;
    private boolean mIsSearchingLocation;
    private boolean mIsSearchExplicit;

    private OnLocationChangedListener mLocationChangeListener;

    public LocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mIsSearchingLocation = false;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        mLocationChangeListener = listener;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mLocation = getPersistedString(DEFAULT_VALUE);
        } else {
            mLocation = defaultValue.toString();
            persistString(mLocation);
        }
        if (DEBUG) Log.i(TAG, "LocationPreference set to: " + mLocation);

        updateSummary();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
    }

    @Override
    protected void onClick() {
        searchLocation(true);
    }

    private void updateSummary() {
        if (mLocation.equals("not set")) {
            setSummary(mContext.getString(R.string.location_not_set));
        } else {
            String shortLatitude = mContext.getString(R.string.latitude_short);
            String shortLongitude = mContext.getString(R.string.longitude_short);

            double latitude = Double.parseDouble
                (mLocation.substring(0, mLocation.indexOf(",")));
            double longitude = Double.parseDouble
                (mLocation.substring(mLocation.indexOf(",") + 1, mLocation.length()));

            String summary = String.format("%s: %.2f %s: %.2f",
                                           shortLatitude, latitude,
                                           shortLongitude, longitude);
            setSummary(summary);
        }
    }

    public void searchLocation(boolean explicitRequest) {
        if (mIsSearchingLocation)
            mIsSearchExplicit = explicitRequest || mIsSearchExplicit;
        else
            mIsSearchExplicit = explicitRequest;
        if (!mIsSearchingLocation) {
            if (DEBUG) Log.i(TAG, explicitRequest ?
                             "Searching location on explicit request" :
                             "Searching location automatically");
            mIsSearchingLocation = true;

            LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationListener listener = new LocationUpdateListener(mContext, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                                       0, 0, listener);
            } else {
                handleLocationSearchFailed();
            }
        }

        if (mIsSearchExplicit && mIsSearchingLocation) {
            setSummary(mContext.getString(R.string.searching_location));
        }
    }

    public void handleLocationFound(Location location) {
        mIsSearchingLocation = false;

        mLocation = location.getLatitude() + "," + location.getLongitude();
        persistString(mLocation);
        updateSummary();

        if (mLocationChangeListener != null)
            mLocationChangeListener.onLocationChange();
    }

    public void handleLocationSearchFailed() {
        mIsSearchingLocation = false;

        if (mIsSearchExplicit) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText
                (mContext, mContext.getString
                 (R.string.toast_warning_no_location), duration);
            toast.show();
        }

        updateSummary();
    }

    // LocationChangedListener
    public interface OnLocationChangedListener {
        public abstract void onLocationChange();
    }
}
