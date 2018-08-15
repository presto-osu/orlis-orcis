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
package net.sf.times.location;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;

import net.sf.times.R;
import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Location provider.
 *
 * @author Moshe Waisberg
 */
public class ZmanimLocations implements ZmanimLocationListener {

    private static final String TAG = "ZmanimLocations";

    /** ISO 639 language code for "Hebrew". */
    public static final String ISO639_HEBREW_FORMER = "he";
    /** ISO 639 language code for "Hebrew" (Java compatibility). */
    public static final String ISO639_HEBREW = "iw";
    /** ISO 639 language code for "Yiddish" (Java compatibility). */
    public static final String ISO639_YIDDISH_FORMER = "ji";
    /** ISO 639 language code for "Yiddish". */
    public static final String ISO639_YIDDISH = "yi";

    /** The minimum time interval between location updates, in milliseconds. */
    private static final long UPDATE_TIME = DateUtils.SECOND_IN_MILLIS;
    /** The maximum time interval between location updates, in milliseconds. */
    private static final long UPDATE_TIME_MAX = 2 * DateUtils.HOUR_IN_MILLIS;
    /** The time interval between requesting location updates, in milliseconds. */
    private static final long UPDATE_TIME_START = 30 * DateUtils.SECOND_IN_MILLIS;
    /**
     * The duration to receive updates, in milliseconds.<br>
     * Should be enough time to get a sufficiently accurate location.
     */
    private static final long UPDATE_DURATION = 30 * DateUtils.SECOND_IN_MILLIS;
    /** The minimum distance between location updates, in metres. */
    private static final int UPDATE_DISTANCE = 100;

    /** Time zone ID for Jerusalem. */
    private static final String TZ_JERUSALEM = "Asia/Jerusalem";
    /** Time zone ID for Israeli Standard Time. */
    private static final String TZ_IST = "IST";
    /** Time zone ID for Israeli Daylight Time. */
    private static final String TZ_IDT = "IDT";
    /** Time zone ID for Jerusalem Standard Time. */
    private static final String TZ_JST = "JST";
    /** Time zone ID for Beirut (patch for Israeli law of DST 2013). */
    private static final String TZ_BEIRUT = "Asia/Beirut";
    /**
     * The offset in milliseconds from UTC of Israeli time zone's standard time.
     */
    private static final int TZ_OFFSET_ISRAEL = (int) (2 * DateUtils.HOUR_IN_MILLIS);
    /** Israeli time zone offset with daylight savings time. */
    private static final int TZ_OFFSET_DST_ISRAEL = (int) (TZ_OFFSET_ISRAEL + DateUtils.HOUR_IN_MILLIS);

    /** Northern-most latitude for Israel. */
    private static final double ISRAEL_NORTH = 33.289212;
    /** Southern-most latitude for Israel. */
    private static final double ISRAEL_SOUTH = 29.489218;
    /** Eastern-most longitude for Israel. */
    private static final double ISRAEL_EAST = 35.891876;
    /** Western-most longitude for Israel. */
    private static final double ISRAEL_WEST = 34.215317;

    /** Start seeking locations. */
    private static final int WHAT_START = 0;
    /** Stop seeking locations. */
    private static final int WHAT_STOP = 1;
    /** Location has changed. */
    private static final int WHAT_CHANGED = 2;
    /** Found an elevation. */
    private static final int WHAT_ELEVATION = 3;
    /** Found an address. */
    private static final int WHAT_ADDRESS = 4;

    /** If the current location is older than 1 second, then it is stale. */
    private static final long LOCATION_EXPIRATION = DateUtils.SECOND_IN_MILLIS;

    /**
     * Constant used to specify formatting of a latitude or longitude in the
     * form "[+-]DDD.DDDDD" where D indicates degrees.
     */
    private static final String FORMAT_DEGREES = "%1$.6f";

    /** The context. */
    private final Context context;
    /** The owner location listeners. */
    private final List<ZmanimLocationListener> locationListeners = new ArrayList<ZmanimLocationListener>();
    /** The owner location listeners for dispatching events. */
    private List<ZmanimLocationListener> locationListenersLoop = locationListeners;
    /** Service provider for locations. */
    private LocationManager locationManager;
    /** The location. */
    private Location location;
    /** The settings and preferences. */
    private ZmanimSettings settings;
    /** The list of countries. */
    private CountriesGeocoder countriesGeocoder;
    /** The coordinates format. */
    private String coordsFormat;
    /** The time zone. */
    private TimeZone timeZone;
    /** The handler thread. */
    private HandlerThread handlerThread;
    /** The handler. */
    private Handler handler;
    /** The next time to start update locations. */
    private long startTaskDelay = UPDATE_TIME_START;
    /** The next time to stop update locations. */
    private final long stopTaskDelay = UPDATE_DURATION;
    /** The address receiver. */
    private final BroadcastReceiver addressReceiver;
    /** The location is externally set? */
    private boolean manualLocation;

    /**
     * Constructs a new provider.
     *
     * @param context
     *         the context.
     */
    public ZmanimLocations(Context context) {
        Context app = context.getApplicationContext();
        if (app != null)
            context = app;
        this.context = context;
        settings = new ZmanimSettings(context);
        countriesGeocoder = new CountriesGeocoder(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        coordsFormat = context.getString(R.string.location_coords);
        timeZone = TimeZone.getDefault();

        addressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ADDRESS_ACTION.equals(action)) {
                    Location location = intent.getParcelableExtra(PARAMETER_LOCATION);
                    ZmanimAddress address = intent.getParcelableExtra(PARAMETER_ADDRESS);
                    if (address != null) {
                        Bundle extras = address.getExtras();
                        if (extras == null) {
                            extras = new Bundle();
                            address.setExtras(extras);
                        }
                        extras.putParcelable(PARAMETER_LOCATION, location);
                        handler.obtainMessage(WHAT_ADDRESS, address).sendToTarget();
                    } else {
                        handler.obtainMessage(WHAT_ADDRESS, location).sendToTarget();
                    }
                } else if (ELEVATION_ACTION.equals(action)) {
                    Location location = intent.getParcelableExtra(PARAMETER_LOCATION);
                    handler.obtainMessage(WHAT_ELEVATION, location).sendToTarget();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ADDRESS_ACTION);
        context.registerReceiver(addressReceiver, filter);
        filter = new IntentFilter(ELEVATION_ACTION);
        context.registerReceiver(addressReceiver, filter);

        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new UpdatesHandler(handlerThread.getLooper());
    }

    /**
     * Add a location listener.
     *
     * @param listener
     *         the listener.
     */
    private void addLocationListener(ZmanimLocationListener listener) {
        if (!locationListeners.contains(listener) && (listener != this)) {
            locationListeners.add(listener);
            locationListenersLoop = new ArrayList<ZmanimLocationListener>(locationListeners);
        }
    }

    /**
     * Remove a location listener.
     *
     * @param listener
     *         the listener.
     */
    private void removeLocationListener(ZmanimLocationListener listener) {
        locationListeners.remove(listener);
        locationListenersLoop = new ArrayList<ZmanimLocationListener>(locationListeners);
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationChanged(location, true, true);
    }

    private void onLocationChanged(Location location, boolean findAddress, boolean findElevation) {
        if (!isValid(location))
            return;

        boolean keepLocation = true;
        if ((this.location != null) && (ZmanimLocation.compareTo(this.location, location) != 0)) {
            // Ignore old locations.
            if (this.location.getTime() + LOCATION_EXPIRATION > location.getTime()) {
                keepLocation = false;
            }
            // Ignore manual locations.
            if (manualLocation) {
                location = this.location;
                keepLocation = false;
            }
        }

        if (keepLocation) {
            this.location = location;
            settings.putLocation(location);
        }

        List<ZmanimLocationListener> listeners = locationListenersLoop;
        for (ZmanimLocationListener listener : listeners)
            listener.onLocationChanged(location);

        if (findElevation && !location.hasAltitude())
            findElevation(location);
        else if (findAddress)
            findAddress(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        List<ZmanimLocationListener> listeners = locationListenersLoop;
        for (ZmanimLocationListener listener : listeners)
            listener.onProviderDisabled(provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        List<ZmanimLocationListener> listeners = locationListenersLoop;
        for (ZmanimLocationListener listener : listeners)
            listener.onProviderEnabled(provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        List<ZmanimLocationListener> listeners = locationListenersLoop;
        for (ZmanimLocationListener listener : listeners)
            listener.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        List<ZmanimLocationListener> listeners = locationListenersLoop;
        for (ZmanimLocationListener listener : listeners)
            listener.onAddressChanged(location, address);
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location, true, false);
    }

    /**
     * Get a location from GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationGPSEclair() {
        if (locationManager == null)
            return null;

        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "GPS: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocationGPS() {
        if (locationManager == null)
            return null;

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "GPS: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from the GSM network.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationNetworkEclair() {
        if (locationManager == null)
            return null;

        try {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Network: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from the GSM network.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocationNetwork() {
        if (locationManager == null)
            return null;

        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Network: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a passive location from other application's GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public Location getLocationPassiveFroyo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO)
            return null;
        if (locationManager == null)
            return null;
        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Passive: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a passive location from other application's GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocationPassive() {
        if (locationManager == null)
            return null;

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Passive: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from the time zone.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationTZ() {
        return getLocationTZ(timeZone);
    }

    /**
     * Get a location from the time zone.
     *
     * @param timeZone
     *         the time zone.
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationTZ(TimeZone timeZone) {
        return countriesGeocoder.findLocation(timeZone);
    }

    /**
     * Get a location from the saved preferences.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationSaved() {
        return settings.getLocation();
    }

    /**
     * Get the best location.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocation() {
        Location loc = location;
        if (isValid(loc))
            return loc;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            loc = getLocationGPSEclair();
            if (isValid(loc))
                return loc;
            loc = getLocationNetworkEclair();
            if (isValid(loc))
                return loc;
            loc = getLocationPassiveFroyo();
            if (isValid(loc))
                return loc;
        } else {
            loc = getLocationGPS();
            if (isValid(loc))
                return loc;
            loc = getLocationNetwork();
            if (isValid(loc))
                return loc;
            loc = getLocationPassive();
            if (isValid(loc))
                return loc;
        }
        loc = getLocationSaved();
        if (isValid(loc))
            return loc;
        loc = getLocationTZ();
        return loc;
    }

    /**
     * Is the location valid?
     *
     * @param location
     *         the location to check.
     * @return {@code false} if location is invalid.
     */
    public boolean isValid(Location location) {
        if (location == null)
            return false;
        final double latitude = location.getLatitude();
        if ((latitude > 90) || (latitude < -90))
            return false;
        final double longitude = location.getLongitude();
        if ((longitude > 180) || (longitude < -180))
            return false;
        return true;
    }

    /**
     * Stop listening.
     *
     * @param listener
     *         the listener who wants to stop listening.
     */
    public void stop(ZmanimLocationListener listener) {
        if (listener != null)
            removeLocationListener(listener);

        if (locationListeners.isEmpty()) {
            removeUpdates();
            handler.removeMessages(WHAT_START);
        }
    }

    /**
     * Start or resume listening.
     *
     * @param listener
     *         the listener who wants to resume listening.
     */
    public void start(ZmanimLocationListener listener) {
        if (listener != null)
            addLocationListener(listener);

        startTaskDelay = UPDATE_TIME_START;
        handler.sendEmptyMessage(WHAT_START);

        // Give the listener our latest known location, and address.
        if (listener != null) {
            Location location = getLocation();
            handler.obtainMessage(WHAT_CHANGED, location).sendToTarget();
        }
    }

    /**
     * Is the location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param location
     *         the location.
     * @param timeZone
     *         the time zone.
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean inIsrael(Location location, TimeZone timeZone) {
        if (location == null) {
            if (timeZone == null)
                timeZone = this.timeZone;
            String id = timeZone.getID();
            if (TZ_JERUSALEM.equals(id) || TZ_BEIRUT.equals(id))
                return true;
            // Check offsets because "IST" could be "Ireland ST", "JST" could be
            // "Japan ST".
            int offset = timeZone.getRawOffset() + timeZone.getDSTSavings();
            if ((offset >= TZ_OFFSET_ISRAEL) && (offset <= TZ_OFFSET_DST_ISRAEL)) {
                if (TZ_IDT.equals(id) || TZ_IST.equals(id) || TZ_JST.equals(id))
                    return true;
            }
            return false;
        }

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        return (latitude <= ISRAEL_NORTH) && (latitude >= ISRAEL_SOUTH) && (longitude >= ISRAEL_WEST) && (longitude <= ISRAEL_EAST);
    }

    /**
     * Is the current location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param timeZone
     *         the time zone.
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean inIsrael(TimeZone timeZone) {
        return inIsrael(getLocation(), timeZone);
    }

    /**
     * Is the current location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean inIsrael() {
        return inIsrael(timeZone);
    }

    /**
     * Format the coordinates.
     *
     * @return the coordinates text.
     */
    public String formatCoordinates() {
        return formatCoordinates(getLocation());
    }

    /**
     * Format the coordinates.
     *
     * @param location
     *         the location.
     * @return the coordinates text.
     */
    public String formatCoordinates(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        return formatCoordinates(latitude, longitude);
    }

    /**
     * Format the coordinates.
     *
     * @param address
     *         the address.
     * @return the coordinates text.
     */
    public String formatCoordinates(Address address) {
        final double latitude = address.getLatitude();
        final double longitude = address.getLongitude();
        return formatCoordinates(latitude, longitude);
    }

    /**
     * Format the coordinates.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     * @return the coordinates text.
     */
    public String formatCoordinates(double latitude, double longitude) {
        final String notation = settings.getCoordinatesFormat();
        final String latitudeText;
        final String longitudeText;
        if (ZmanimSettings.FORMAT_SEXIGESIMAL.equals(notation)) {
            latitudeText = Location.convert(latitude, Location.FORMAT_SECONDS);
            longitudeText = Location.convert(longitude, Location.FORMAT_SECONDS);
        } else {
            latitudeText = String.format(Locale.US, FORMAT_DEGREES, latitude);
            longitudeText = String.format(Locale.US, FORMAT_DEGREES, longitude);
        }
        return String.format(Locale.US, coordsFormat, latitudeText, longitudeText);
    }

    /**
     * Format the coordinates.
     *
     * @param coord
     *         the coordinate.
     * @return the coordinate text.
     */
    public String formatCoordinate(double coord) {
        final String notation = settings.getCoordinatesFormat();
        if (ZmanimSettings.FORMAT_SEXIGESIMAL.equals(notation)) {
            return Location.convert(coord, Location.FORMAT_SECONDS);
        }
        return String.format(Locale.US, FORMAT_DEGREES, coord);
    }

    /**
     * Get the location.
     *
     * @param timeZone
     *         the time zone.
     * @return the location - {@code null} otherwise.
     */
    public GeoLocation getGeoLocation(TimeZone timeZone) {
        Location loc = getLocation();
        if (loc == null)
            return null;
        final String locationName = loc.getProvider();
        final double latitude = loc.getLatitude();
        final double longitude = loc.getLongitude();
        final double elevation = loc.hasAltitude() ? Math.max(0, loc.getAltitude()) : 0;

        return new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
    }

    /**
     * Get the location.
     *
     * @return the location - {@code null} otherwise.
     */
    public GeoLocation getGeoLocation() {
        return getGeoLocation(timeZone);
    }

    /**
     * Get the time zone.
     *
     * @return the time zone.
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Set the location.
     *
     * @param location
     *         the location.
     */
    public void setLocation(Location location) {
        this.location = null;
        manualLocation = location != null;
        onLocationChanged(location);
    }

    /**
     * Is the default locale right-to-left?
     *
     * @return {@code true} if the locale is either Hebrew or Yiddish.
     */
    public static boolean isLocaleRTL() {
        final String iso639 = Locale.getDefault().getLanguage();
        return ISO639_HEBREW.equals(iso639) || ISO639_HEBREW_FORMER.equals(iso639) || ISO639_YIDDISH.equals(iso639) || ISO639_YIDDISH_FORMER.equals(iso639);
    }

    private void requestUpdatesEclair() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Log.w(TAG, "No location provider");
            return;
        }
        try {
            locationManager.requestLocationUpdates(provider, UPDATE_TIME, UPDATE_DISTANCE, this);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "request updates: " + e.getLocalizedMessage(), e);
        }

        // Let the updates run for only a small while to save battery.
        handler.sendEmptyMessageDelayed(WHAT_STOP, stopTaskDelay);
        startTaskDelay = Math.min(UPDATE_TIME_MAX, startTaskDelay << 1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestUpdates() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Log.w(TAG, "No location provider");
            return;
        }

        if ((context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(provider, UPDATE_TIME, UPDATE_DISTANCE, this);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "request updates: " + e.getLocalizedMessage(), e);
        }

        // Let the updates run for only a small while to save battery.
        handler.sendEmptyMessageDelayed(WHAT_STOP, stopTaskDelay);
        startTaskDelay = Math.min(UPDATE_TIME_MAX, startTaskDelay << 1);
    }

    private void removeUpdates() {
        locationManager.removeUpdates(this);

        if (!locationListeners.isEmpty()) {
            handler.sendEmptyMessageDelayed(WHAT_START, startTaskDelay);
        }
    }

    /**
     * Quit updating locations.
     */
    public void quit() {
        manualLocation = false;
        locationListeners.clear();
        removeUpdates();
        handler.removeMessages(WHAT_START);

        context.unregisterReceiver(addressReceiver);

        Looper looper = handlerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        handlerThread.interrupt();
    }

    private class UpdatesHandler extends Handler {

        public UpdatesHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Location location = null;
            ZmanimAddress address = null;

            switch (msg.what) {
                case WHAT_START:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        requestUpdatesEclair();
                    } else {
                        requestUpdates();
                    }
                    break;
                case WHAT_STOP:
                    removeUpdates();
                    break;
                case WHAT_CHANGED:
                    location = getLocation();
                    onLocationChanged(location);
                    break;
                case WHAT_ADDRESS:
                    if (msg.obj instanceof ZmanimAddress) {
                        address = (ZmanimAddress) msg.obj;
                        if (address != null)
                            location = address.getExtras().getParcelable(PARAMETER_LOCATION);
                    } else {
                        location = (Location) msg.obj;
                    }
                    onAddressChanged(location, address);
                    break;
                case WHAT_ELEVATION:
                    location = (Location) msg.obj;
                    onElevationChanged(location);
                    break;
            }
        }
    }

    private void findAddress(Location location) {
        Intent findAddress = new Intent(context, AddressService.class);
        findAddress.setAction(ADDRESS_ACTION);
        findAddress.putExtra(PARAMETER_LOCATION, location);
        context.startService(findAddress);
    }

    private void findElevation(Location location) {
        Intent findElevation = new Intent(context, AddressService.class);
        findElevation.setAction(ELEVATION_ACTION);
        findElevation.putExtra(PARAMETER_LOCATION, location);
        context.startService(findElevation);
    }
}
