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

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;

import net.sf.times.ZmanimApplication;
import net.sf.times.database.CursorFilter;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Android SQLite database.
 *
 * @author Moshe Waisberg
 */
public class DatabaseGeocoder extends GeocoderBase {

    /**
     * Creates a new database geocoder.
     *
     * @param context
     *         the context.
     */
    public DatabaseGeocoder(Context context) {
        super(context);
    }

    /**
     * Creates a new database geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public DatabaseGeocoder(Context context, Locale locale) {
        super(context, locale);
    }

    @Override
    public List<Address> getFromLocation(final double latitude, final double longitude, int maxResults) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);

        CursorFilter filter = new CursorFilter() {

            private final float[] mDistance = new float[1];

            @Override
            public boolean accept(Cursor cursor) {
                double locationLatitude = cursor.getDouble(AddressProvider.INDEX_LOCATION_LATITUDE);
                double locationLongitude = cursor.getDouble(AddressProvider.INDEX_LOCATION_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, mDistance);
                if (mDistance[0] <= SAME_LOCATION)
                    return true;

                double addressLatitude = cursor.getDouble(AddressProvider.INDEX_LATITUDE);
                double addressLongitude = cursor.getDouble(AddressProvider.INDEX_LONGITUDE);
                Location.distanceBetween(latitude, longitude, addressLatitude, addressLongitude, mDistance);
                return (mDistance[0] <= SAME_LOCATION);
            }
        };
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        AddressProvider provider = app.getAddresses();
        List<ZmanimAddress> q = provider.query(filter);
        List<Address> addresses = new ArrayList<Address>(q);

        return addresses;
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return null;
    }

    @Override
    public ZmanimLocation getElevation(final double latitude, final double longitude) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);


        CursorFilter filter = new CursorFilter() {
            private final float[] mDistance = new float[1];

            @Override
            public boolean accept(Cursor cursor) {
                double locationLatitude = cursor.getDouble(AddressProvider.INDEX_ELEVATIONS_LATITUDE);
                double locationLongitude = cursor.getDouble(AddressProvider.INDEX_ELEVATIONS_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, mDistance);
                return (mDistance[0] <= SAME_PLATEAU);
            }
        };
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        AddressProvider provider = app.getAddresses();
        List<ZmanimLocation> locations = provider.queryElevations(filter);

        int locationsCount = locations.size();
        if (locationsCount == 0)
            return null;

        float distance;
        float[] distanceLoc = new float[1];
        double d;
        double distancesSum = 0;
        int n = 0;
        double[] distances = new double[locationsCount];
        double[] elevations = new double[locationsCount];

        for (ZmanimLocation loc : locations) {
            Location.distanceBetween(latitude, longitude, loc.getLatitude(), loc.getLongitude(), distanceLoc);
            distance = distanceLoc[0];
            elevations[n] = loc.getAltitude();
            d = distance * distance;
            distances[n] = d;
            distancesSum += d;
            n++;
        }

        if ((n == 1) && (distanceLoc[0] <= SAME_CITY))
            return locations.get(0);
        if (n <= 1)
            return null;

        double weightSum = 0;
        for (int i = 0; i < n; i++) {
            weightSum += (1 - (distances[i] / distancesSum)) * elevations[i];
        }

        ZmanimLocation elevated = new ZmanimLocation(AddressProvider.DB_PROVIDER);
        elevated.setTime(System.currentTimeMillis());
        elevated.setLatitude(latitude);
        elevated.setLongitude(longitude);
        elevated.setAltitude(weightSum / (n - 1));
        elevated.setId(-1);
        return elevated;
    }

    @Override
    protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
        return null;
    }

}
