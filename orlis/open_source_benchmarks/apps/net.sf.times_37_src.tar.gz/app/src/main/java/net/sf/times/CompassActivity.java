/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimSettings;

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassActivity extends Activity implements ZmanimLocationListener, SensorEventListener {

    /** Latitude of the Holy of Holies, according to Google. */
    private static final double HOLIEST_LATITUDE = 31.778122;
    /** Longitude of the Holy of Holies, according to Google. */
    private static final double HOLIEST_LONGITUDE = 35.235345;
    /** Elevation of the Holy of Holies, according to Google. */
    private static final double HOLIEST_ELEVATION = 744.5184937;

    /** The sensor manager. */
    private SensorManager sensorManager;
    /** The accelerometer sensor. */
    private Sensor accelerometer;
    /** The magnetic field sensor. */
    private Sensor magnetic;
    /** Provider for locations. */
    private ZmanimLocations locations;
    /** Location of the Holy of Holies. */
    private Location holiest;
    /** The main view. */
    private CompassView view;
    /** The gravity values. */
    private final float[] gravity = new float[3];
    /** The geomagnetic field. */
    private final float[] geomagnetic = new float[3];
    /** Rotation matrix. */
    private final float[] matrixR = new float[9];
    /** Orientation matrix. */
    private final float[] orientation = new float[3];
    /** The settings and preferences. */
    private ZmanimSettings settings;
    /** The address location. */
    private Location addressLocation;
    /** The address. */
    private ZmanimAddress address;
    /** Populate the header in UI thread. */
    private Runnable populateHeader;
    /** Update the location in UI thread. */
    private Runnable updateLocation;

    /**
     * Constructs a new compass.
     */
    public CompassActivity() {
        holiest = new Location(LocationManager.GPS_PROVIDER);
        holiest.setLatitude(HOLIEST_LATITUDE);
        holiest.setLongitude(HOLIEST_LONGITUDE);
        holiest.setAltitude(HOLIEST_ELEVATION);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        view = (CompassView) findViewById(R.id.compass);

        settings = new ZmanimSettings(this);
        if (!settings.isSummaries()) {
            View summary = findViewById(android.R.id.summary);
            summary.setVisibility(View.GONE);
        }

        ZmanimApplication app = (ZmanimApplication) getApplication();
        locations = app.getLocations();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locations.start(this);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        locations.stop(this);
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ZmanimLocation.compareTo(addressLocation, location) != 0) {
            address = null;
        }
        addressLocation = location;
        if (updateLocation == null) {
            updateLocation = new Runnable() {
                @Override
                public void run() {
                    // Have we been destroyed?
                    Location loc = addressLocation;
                    if (loc == null)
                        return;
                    CompassView cv = view;
                    if (cv == null)
                        return;

                    populateHeader();
                    cv.setHoliest(loc.bearingTo(holiest));
                }
            };
        }
        runOnUiThread(updateLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, gravity, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, geomagnetic, 0, 3);
                break;
            default:
                return;
        }
        if (SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(matrixR, orientation);
            view.setAzimuth(orientation[0]);
        }
    }

    /** Populate the header item. */
    private void populateHeader() {
        // Have we been destroyed?
        Location loc = (addressLocation == null) ? locations.getLocation() : addressLocation;
        if (loc == null)
            return;

        final String coordsText = locations.formatCoordinates(loc);
        final String locationName = formatAddress();

        // Update the location.
        TextView address = (TextView) findViewById(R.id.address);
        address.setText(locationName);
        TextView coordinates = (TextView) findViewById(R.id.coordinates);
        coordinates.setText(coordsText);
        coordinates.setVisibility(settings.isCoordinates() ? View.VISIBLE : View.GONE);
    }

    /**
     * Format the address for the current location.
     *
     * @return the formatted address.
     */
    private String formatAddress() {
        if (address != null)
            return address.getFormatted();
        return getString(R.string.location_unknown);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        addressLocation = location;
        this.address = address;
        if (populateHeader == null) {
            populateHeader = new Runnable() {
                @Override
                public void run() {
                    populateHeader();
                }
            };
        }
        runOnUiThread(populateHeader);
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }
}
