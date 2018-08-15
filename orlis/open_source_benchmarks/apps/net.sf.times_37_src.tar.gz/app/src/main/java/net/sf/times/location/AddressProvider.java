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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

import net.sf.times.database.CursorFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 *
 * @author Moshe Waisberg
 */
public class AddressProvider {
    private static final String TAG = "AddressProvider";

    public interface OnFindAddressListener {

        /**
         * Called when an address is found.
         *
         * @param provider
         *         the address provider.
         * @param location
         *         the requested location.
         * @param address
         *         the found address.
         */
        void onFindAddress(AddressProvider provider, Location location, Address address);

        /**
         * Called when a location with an elevation is found.
         *
         * @param provider
         *         the address provider.
         * @param location
         *         the requested location.
         * @param elevated
         *         the location with elevation.
         */
        void onFindElevation(AddressProvider provider, Location location, ZmanimLocation elevated);

    }

    /** Database provider. */
    public static final String DB_PROVIDER = "db";

    private static final String[] COLUMNS = {BaseColumns._ID, AddressColumns.LOCATION_LATITUDE, AddressColumns.LOCATION_LONGITUDE, AddressColumns.LATITUDE,
            AddressColumns.LONGITUDE, AddressColumns.ADDRESS, AddressColumns.LANGUAGE, AddressColumns.FAVORITE};
    static final int INDEX_ID = 0;
    static final int INDEX_LOCATION_LATITUDE = 1;
    static final int INDEX_LOCATION_LONGITUDE = 2;
    static final int INDEX_LATITUDE = 3;
    static final int INDEX_LONGITUDE = 4;
    static final int INDEX_ADDRESS = 5;
    static final int INDEX_LANGUAGE = 6;
    static final int INDEX_FAVORITE = 7;

    private static final String[] COLUMNS_ELEVATIONS = {BaseColumns._ID, ElevationColumns.LATITUDE, ElevationColumns.LONGITUDE, ElevationColumns.ELEVATION,
            ElevationColumns.TIMESTAMP};
    static final int INDEX_ELEVATIONS_ID = 0;
    static final int INDEX_ELEVATIONS_LATITUDE = 1;
    static final int INDEX_ELEVATIONS_LONGITUDE = 2;
    static final int INDEX_ELEVATIONS_ELEVATION = 3;
    static final int INDEX_ELEVATIONS_TIMESTAMP = 4;

    private static final String[] COLUMNS_CITIES = {BaseColumns._ID, CitiesColumns.TIMESTAMP, CitiesColumns.FAVORITE};
    static final int INDEX_CITIES_ID = 0;
    static final int INDEX_CITIES_TIMESTAMP = 1;
    static final int INDEX_CITIES_FAVORITE = 2;

    private static final String WHERE_ID = BaseColumns._ID + "=?";

    private final Context context;
    private final Locale locale;
    private SQLiteOpenHelper openHelper;
    /** The list of countries. */
    private CountriesGeocoder countriesGeocoder;
    private Geocoder geocoder;
    private GeocoderBase googleGeocoder;
    private GeocoderBase bingGeocoder;
    private GeocoderBase geonamesGeocoder;
    private GeocoderBase databaseGeocoder;

    /**
     * Constructs a new provider.
     *
     * @param context
     *         the context.
     */
    public AddressProvider(Context context) {
        this(context, Locale.getDefault());
    }

    /**
     * Constructs a new provider.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public AddressProvider(Context context, Locale locale) {
        this.context = context;
        this.locale = locale;
        countriesGeocoder = new CountriesGeocoder(context, locale);
    }

    /**
     * Find the nearest address of the location.
     *
     * @param location
     *         the location.
     * @param listener
     *         the listener.
     * @return the address - {@code null} otherwise.
     */
    public Address findNearestAddress(Location location, OnFindAddressListener listener) {
        if (location == null)
            return null;
        final double latitude = location.getLatitude();
        if ((latitude > 90) || (latitude < -90))
            return null;
        final double longitude = location.getLongitude();
        if ((longitude > 180) || (longitude < -180))
            return null;

        List<Address> addresses;
        Address best = null;
        Address bestCountry;
        Address bestCity;

        if (listener != null)
            listener.onFindAddress(this, location, best);

        addresses = findNearestCountry(location);
        best = findBestAddress(location, addresses, GeocoderBase.SAME_PLANET);
        if ((best != null) && (listener != null))
            listener.onFindAddress(this, location, best);
        bestCountry = best;

        addresses = findNearestCity(location);
        best = findBestAddress(location, addresses, GeocoderBase.SAME_PLATEAU);
        if ((best != null) && (listener != null))
            listener.onFindAddress(this, location, best);
        bestCity = best;

        addresses = findNearestAddressDatabase(location);
        best = findBestAddress(location, addresses);
        if ((best != null) && (listener != null))
            listener.onFindAddress(this, location, best);

        if (best == null) {
            addresses = findNearestAddressGeocoder(location);
            best = findBestAddress(location, addresses);
            if ((best != null) && (listener != null))
                listener.onFindAddress(this, location, best);
        }
        if (best == null) {
            addresses = findNearestAddressGoogle(location);
            best = findBestAddress(location, addresses);
            if ((best != null) && (listener != null))
                listener.onFindAddress(this, location, best);
        }
        if (best == null) {
            addresses = findNearestAddressBing(location);
            best = findBestAddress(location, addresses);
            if ((best != null) && (listener != null))
                listener.onFindAddress(this, location, best);
        }
        if (best == null) {
            addresses = findNearestAddressGeoNames(location);
            best = findBestAddress(location, addresses);
            if ((best != null) && (listener != null))
                listener.onFindAddress(this, location, best);
        }
        if (best == null) {
            best = bestCity;
        }
        if (best == null) {
            best = bestCountry;
        }

        return best;
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the built-in Android {@link Geocoder} API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressGeocoder(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        Geocoder geocoder = this.geocoder;
        if (geocoder == null) {
            geocoder = new Geocoder(context);
            this.geocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (IOException e) {
            Log.e(TAG, "Geocoder: " + e.getLocalizedMessage(), e);
        }
        return addresses;
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the Google Maps API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressGoogle(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = googleGeocoder;
        if (geocoder == null) {
            geocoder = new GoogleGeocoder(context, locale);
            googleGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (IOException e) {
            Log.e(TAG, "Google geocoder: " + e.getLocalizedMessage(), e);
        }
        return addresses;
    }

    /**
     * Finds the nearest street and address for a given lat/lng pair.
     * <p/>
     * Uses the GeoNames API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressGeoNames(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = geonamesGeocoder;
        if (geocoder == null) {
            geocoder = new GeoNamesGeocoder(context, locale);
            geonamesGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (IOException e) {
            Log.e(TAG, "GeoNames geocoder: " + e.getLocalizedMessage(), e);
        }
        return addresses;
    }

    /**
     * Finds the nearest street and address for a given lat/lng pair.
     * <p/>
     * Uses the Bing API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressBing(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = bingGeocoder;
        if (geocoder == null) {
            geocoder = new BingGeocoder(context, locale);
            bingGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (IOException e) {
            Log.e(TAG, "Bing geocoder: " + e.getLocalizedMessage(), e);
        }
        return addresses;
    }

    /**
     * Find the best address by checking relevant fields.
     *
     * @param location
     *         the location.
     * @param addresses
     *         the list of addresses.
     * @return the best address - {@code null} otherwise.
     */
    private Address findBestAddress(Location location, List<Address> addresses) {
        return findBestAddress(location, addresses, GeocoderBase.SAME_CITY);
    }

    /**
     * Find the best address by checking relevant fields.
     *
     * @param location
     *         the location.
     * @param addresses
     *         the list of addresses.
     * @param radius
     *         the maximum radius.
     * @return the best address - {@code null} otherwise.
     */
    private Address findBestAddress(Location location, List<Address> addresses, float radius) {
        if ((addresses == null) || addresses.isEmpty())
            return null;

        // First, find the closest location.
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        float distanceMin = radius;
        Address addrMin = null;
        float[] distances = new float[1];
        List<Address> near = new ArrayList<Address>(addresses.size());

        for (Address a : addresses) {
            if (!a.hasLatitude() || !a.hasLongitude())
                continue;
            Location.distanceBetween(latitude, longitude, a.getLatitude(), a.getLongitude(), distances);
            if (distances[0] <= radius) {
                near.add(a);
                if (distances[0] <= distanceMin) {
                    distanceMin = distances[0];
                    addrMin = a;
                }
            }
        }

        if (addrMin != null)
            return addrMin;
        if (near.isEmpty())
            return null;
        if (near.size() == 1)
            return near.get(0);

        // Next, find the best address part.
        for (Address a : near) {
            if (a.getFeatureName() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getLocality() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getSubLocality() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getAdminArea() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getSubAdminArea() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getCountryName() != null)
                return a;
        }
        return near.get(0);
    }

    /**
     * Format the address.
     *
     * @param a
     *         the address.
     * @return the formatted address name.
     */
    public static String formatAddress(ZmanimAddress a) {
        return a.getFormatted();
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the local database.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressDatabase(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = databaseGeocoder;
        if (geocoder == null) {
            geocoder = new DatabaseGeocoder(context, locale);
            databaseGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (IOException e) {
            Log.e(TAG, "Database geocoder: " + e.getLocalizedMessage(), e);
        }
        return addresses;
    }

    /**
     * Get the readable addresses database.
     *
     * @return the database - {@code null} otherwise.
     */
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null)
            openHelper = new AddressOpenHelper(context);
        try {
            return openHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "no readable db", e);
        }
        return null;
    }

    /**
     * Get the writable addresses database.
     *
     * @return the database - {@code null} otherwise.
     */
    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null)
            openHelper = new AddressOpenHelper(context);
        try {
            return openHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "no writable db", e);
        }
        return null;
    }

    /**
     * Insert or update the address in the local database. The local database is
     * supposed to reduce redundant network requests.
     *
     * @param location
     *         the location.
     * @param address
     *         the address.
     */
    public void insertOrUpdateAddress(Location location, ZmanimAddress address) {
        if (address == null)
            return;
        long id = address.getId();
        if (id < 0L)
            return;
        boolean insert = id == 0L;

        ContentValues values = new ContentValues();
        if (insert) {
            if (location == null) {
                values.put(AddressColumns.LOCATION_LATITUDE, address.getLatitude());
                values.put(AddressColumns.LOCATION_LONGITUDE, address.getLongitude());
            } else {
                values.put(AddressColumns.LOCATION_LATITUDE, location.getLatitude());
                values.put(AddressColumns.LOCATION_LONGITUDE, location.getLongitude());
            }
        }
        values.put(AddressColumns.ADDRESS, formatAddress(address));
        values.put(AddressColumns.LANGUAGE, address.getLocale().getLanguage());
        values.put(AddressColumns.LATITUDE, address.getLatitude());
        values.put(AddressColumns.LONGITUDE, address.getLongitude());
        values.put(AddressColumns.TIMESTAMP, System.currentTimeMillis());
        values.put(AddressColumns.FAVORITE, address.isFavorite());

        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        if (insert) {
            id = db.insert(AddressOpenHelper.TABLE_ADDRESSES, null, values);
            address.setId(id);
        } else {
            String[] whereArgs = {Long.toString(id)};
            db.update(AddressOpenHelper.TABLE_ADDRESSES, values, WHERE_ID, whereArgs);
        }
    }

    /** Close database resources. */
    public void close() {
        if (openHelper != null)
            openHelper.close();
    }

    /**
     * Find the nearest country to the latitude and longitude.
     * <p/>
     * Uses the pre-compiled array of countries from GeoNames.
     *
     * @param location
     *         the location.
     * @return the list of addresses with at most 1 entry.
     */
    private List<Address> findNearestCountry(Location location) {
        List<Address> countries = null;
        Address country = countriesGeocoder.findCountry(location);
        if (country != null) {
            countries = new ArrayList<Address>();
            countries.add(country);
        }
        return countries;
    }

    /**
     * Find the nearest city to the latitude and longitude.
     * <p/>
     * Uses the pre-compiled array of cities from GeoNames.
     *
     * @param location
     *         the location.
     * @return the list of addresses with at most 1 entry.
     */
    private List<Address> findNearestCity(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = countriesGeocoder;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (IOException e) {
            Log.e(TAG, "City: " + e.getLocalizedMessage(), e);
        }
        return addresses;
    }

    /**
     * Fetch addresses from the database.
     *
     * @param filter
     *         a cursor filter.
     * @return the list of addresses.
     */
    public List<ZmanimAddress> query(CursorFilter filter) {
        final String language = locale.getLanguage();
        final String country = locale.getCountry();

        List<ZmanimAddress> addresses = new ArrayList<ZmanimAddress>();
        SQLiteDatabase db = getReadableDatabase();
        if (db == null)
            return addresses;
        Cursor cursor = db.query(AddressOpenHelper.TABLE_ADDRESSES, COLUMNS, null, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return addresses;
        }

        try {
            if (cursor.moveToFirst()) {
                long id;
                double addressLatitude;
                double addressLongitude;
                String formatted;
                String locationLanguage;
                Locale locale;
                ZmanimAddress address;
                boolean favorite;

                do {
                    locationLanguage = cursor.getString(INDEX_LANGUAGE);
                    if ((locationLanguage == null) || locationLanguage.equals(language)) {
                        if ((filter != null) && !filter.accept(cursor))
                            continue;

                        addressLatitude = cursor.getDouble(INDEX_LATITUDE);
                        addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
                        id = cursor.getLong(INDEX_ID);
                        formatted = cursor.getString(INDEX_ADDRESS);
                        favorite = cursor.getShort(INDEX_FAVORITE) != 0;
                        if (locationLanguage == null)
                            locale = this.locale;
                        else
                            locale = new Locale(locationLanguage, country);

                        address = new ZmanimAddress(locale);
                        address.setFormatted(formatted);
                        address.setId(id);
                        address.setLatitude(addressLatitude);
                        address.setLongitude(addressLongitude);
                        address.setFavorite(favorite);
                        addresses.add(address);
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Query addresses: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }

        return addresses;
    }

    /**
     * Find the elevation (altitude).
     *
     * @param location
     *         the location.
     * @param listener
     *         the listener.
     * @return the elevated location - {@code null} otherwise.
     */
    public Location findElevation(Location location, OnFindAddressListener listener) {
        ZmanimLocation elevated;

        if (location.hasAltitude()) {
            elevated = findElevationCities(location);
            if (elevated == null) {
                elevated = findElevationDatabase(location);
                if (elevated == null)
                    elevated = new ZmanimLocation(location);
                else if (ZmanimLocation.compareTo(location, elevated) == 0)
                    elevated.setAltitude(location.getAltitude());
            } else if (ZmanimLocation.compareTo(location, elevated) == 0)
                elevated.setAltitude(location.getAltitude());
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationCities(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationDatabase(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationGoogle(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationBing(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationGeoNames(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        return null;
    }

    /**
     * Find elevation of nearest cities. Calculates the average elevation of
     * neighbouring cities if more than {@code 1} is found.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationCities(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        try {
            return countriesGeocoder.getElevation(latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "Countries geocoder: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Find elevation according to Google Maps.
     *
     * @param location
     *         the location.
     * @return the location with elevation - {@code null} otherwise.
     */
    private ZmanimLocation findElevationGoogle(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = googleGeocoder;
        if (geocoder == null) {
            geocoder = new GoogleGeocoder(context, locale);
            googleGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "Google geocoder: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Find elevation according to GeoNames.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationGeoNames(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = geonamesGeocoder;
        if (geocoder == null) {
            geocoder = new GeoNamesGeocoder(context, locale);
            geonamesGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "GeoNames geocoder: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Find elevation according to Bing.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationBing(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = bingGeocoder;
        if (geocoder == null) {
            geocoder = new BingGeocoder(context, locale);
            bingGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "Bing geocoder: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Find elevation of nearest locations cached in the database. Calculates
     * the average elevation of neighbouring locations if more than {@code 1} is
     * found.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationDatabase(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = databaseGeocoder;
        if (geocoder == null) {
            geocoder = new DatabaseGeocoder(context, locale);
            databaseGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "Database geocoder: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Insert or update the location with elevation in the local database. The
     * local database is supposed to reduce redundant network requests.
     *
     * @param location
     *         the location.
     */
    public void insertOrUpdateElevation(ZmanimLocation location) {
        if ((location == null) || !location.hasAltitude())
            return;
        long id = location.getId();
        if (id < 0L)
            return;

        ContentValues values = new ContentValues();
        values.put(ElevationColumns.LATITUDE, location.getLatitude());
        values.put(ElevationColumns.LONGITUDE, location.getLongitude());
        values.put(ElevationColumns.ELEVATION, location.getAltitude());
        values.put(ElevationColumns.TIMESTAMP, System.currentTimeMillis());

        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        if (id == 0L) {
            id = db.insert(AddressOpenHelper.TABLE_ELEVATIONS, null, values);
            location.setId(id);
        } else {
            String[] whereArgs = {Long.toString(id)};
            db.update(AddressOpenHelper.TABLE_ELEVATIONS, values, WHERE_ID, whereArgs);
        }
    }

    /**
     * Fetch elevations from the database.
     *
     * @param filter
     *         a cursor filter.
     * @return the list of locations with elevations.
     */
    public List<ZmanimLocation> queryElevations(CursorFilter filter) {
        List<ZmanimLocation> locations = new ArrayList<ZmanimLocation>();
        SQLiteDatabase db = getReadableDatabase();
        if (db == null)
            return locations;
        Cursor cursor = db.query(AddressOpenHelper.TABLE_ELEVATIONS, COLUMNS_ELEVATIONS, null, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return locations;
        }

        try {
            if (cursor.moveToFirst()) {
                ZmanimLocation location;

                do {
                    if ((filter != null) && !filter.accept(cursor))
                        continue;

                    location = new ZmanimLocation(DB_PROVIDER);
                    location.setId(cursor.getLong(INDEX_ELEVATIONS_ID));
                    location.setLatitude(cursor.getDouble(INDEX_ELEVATIONS_LATITUDE));
                    location.setLongitude(cursor.getDouble(INDEX_ELEVATIONS_LONGITUDE));
                    location.setAltitude(cursor.getDouble(INDEX_ELEVATIONS_ELEVATION));
                    location.setTime(cursor.getLong(INDEX_ELEVATIONS_TIMESTAMP));
                    locations.add(location);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Query elevations: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }

        return locations;
    }

    /**
     * Populate the cities with data from the table.
     *
     * @param cities
     *         the list of cities to populate.
     */
    public void populateCities(Collection<ZmanimAddress> cities) {
        SQLiteDatabase db = getReadableDatabase();
        if (db == null)
            return;
        Cursor cursor = db.query(AddressOpenHelper.TABLE_CITIES, COLUMNS_CITIES, null, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return;
        }

        Map<Long, ZmanimAddress> citiesById = new HashMap<Long, ZmanimAddress>();
        long id;
        for (ZmanimAddress city : cities) {
            id = -city.getId();
            citiesById.put(id, city);
        }

        try {
            if (cursor.moveToFirst()) {
                boolean favorite;
                ZmanimAddress city;

                do {
                    id = cursor.getLong(INDEX_CITIES_ID);
                    favorite = cursor.getShort(INDEX_CITIES_FAVORITE) != 0;

                    city = citiesById.get(id);
                    city.setFavorite(favorite);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Populate cities: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }
    }

    /**
     * Insert or update the city in the local database.
     *
     * @param city
     *         the city.
     */
    public void insertOrUpdateCity(ZmanimAddress city) {
        if (city == null)
            return;
        long id = city.getId();
        if (id >= 0L)
            return;

        id = -id;

        ContentValues values = new ContentValues();
        values.put(CitiesColumns.TIMESTAMP, System.currentTimeMillis());
        values.put(CitiesColumns.FAVORITE, city.isFavorite());

        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        String[] whereArgs = {Long.toString(id)};
        db.update(AddressOpenHelper.TABLE_CITIES, values, WHERE_ID, whereArgs);
    }

    /**
     * Delete the list of cached addresses.
     */
    public void deleteAddresses() {
        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        db.delete(AddressOpenHelper.TABLE_ADDRESSES, null, null);
    }
}
