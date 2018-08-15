/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.functional;

import android.app.Activity;
import android.app.Application;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabm on 29.12.14.
 */
public class LocationHandler implements LocationListener, Application.ActivityLifecycleCallbacks {

    private static final int ONE_MINUTE = 1000 * 60;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private List<String> relevantLocationProviders = new ArrayList<>();

    private Location curLocation = null;
    private Boolean searchingForLocation = false;
    private android.location.LocationManager curLocationManager = null;
    private List<OnLocationUpdatedListener> onLocationUpdatedListeners = new ArrayList<>();

    public LocationHandler(android.location.LocationManager locationManager) {
        curLocationManager = locationManager;


        List<String> availableProviders = locationManager.getAllProviders();

        if (availableProviders.contains(LocationManager.NETWORK_PROVIDER))
            relevantLocationProviders.add(LocationManager.NETWORK_PROVIDER);

        if (availableProviders.contains(LocationManager.GPS_PROVIDER))
            relevantLocationProviders.add(LocationManager.GPS_PROVIDER);

        initLocation();
    }

    public void updateLocation(boolean force) {
        if (!isValidLocation(curLocation) || force)
            startLocationUpdate();
        else
            notifyLocationUpdated();
    }


    protected void initLocation() {
        for (int i = 0; i < relevantLocationProviders.size(); i++) {
            Location lastKnownLoc = curLocationManager.getLastKnownLocation(relevantLocationProviders.get(i));

            if (isValidLocation(lastKnownLoc))
                curLocation = lastKnownLoc;
        }
    }

    public void addOnLocationUpdatedListener(OnLocationUpdatedListener locationUpdatedListener) {
        onLocationUpdatedListeners.add(locationUpdatedListener);
    }

    public void removeOnLocationUpdatedListener(OnLocationUpdatedListener locationUpdatedListener) {
        onLocationUpdatedListeners.remove(locationUpdatedListener);
    }

    protected void notifyLocationUpdating() {
        for (int i = 0; i < onLocationUpdatedListeners.size(); i++)
            onLocationUpdatedListeners.get(i).onLocationUpdating();
    }

    protected void notifyLocationUpdated() {
        for (int i = 0; i < onLocationUpdatedListeners.size(); i++)
            onLocationUpdatedListeners.get(i).onLocationUpdated(curLocation);
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);

    }

    @Override
    public void onLocationChanged(Location location) {

        if (isBetterLocation(location, curLocation)) {
            curLocation = location;

            notifyLocationUpdated();
        }

        if (isValidLocation(curLocation))
            stopLocationUpdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void startLocationUpdate() {
        if (relevantLocationProviders.isEmpty())
            return;

        notifyLocationUpdating();

        // search for new location
        for (int i = 0; i < relevantLocationProviders.size(); i++)
            curLocationManager.requestLocationUpdates(relevantLocationProviders.get(i), 0, 0, LocationHandler.this);


        searchingForLocation = true;
    }

    private void stopLocationUpdate() {
        curLocationManager.removeUpdates(this);
        searchingForLocation = false;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (bundle != null) {
            searchingForLocation = bundle.getBoolean("searchingForLocation");
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (searchingForLocation)
            startLocationUpdate();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Boolean stillSearching = searchingForLocation;

        stopLocationUpdate();

        searchingForLocation = stillSearching;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        bundle.putBoolean("searchingForLocation", searchingForLocation);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    protected Boolean isValidLocation(Location location) {
        if (location == null)
            return false;

        Time now = new Time();
        now.setToNow();

        return (now.toMillis(true) - location.getTime()) <= TWO_MINUTES;

    }

    public interface OnLocationUpdatedListener {
        void onLocationUpdated(Location location);

        void onLocationUpdating();
    }
}
