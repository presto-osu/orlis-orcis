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
import android.location.Address;
import android.text.TextUtils;
import android.util.Log;

import net.sf.net.HTTPReader;
import net.sf.times.BuildConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * GeoNames WebServices API.
 * <p/>
 * <a
 * href="http://www.geonames.org/export/web-services.html">http://www.geonames
 * .org/export/web-services.html</a>
 *
 * @author Moshe Waisberg
 */
public class GeoNamesGeocoder extends GeocoderBase {

    private static final String TAG = "GeoNamesGeocoder";

    /** GeoNames user name. */
    private static final String USERNAME = BuildConfig.GEONAMES_USERNAME;

    /** URL that accepts latitude and longitude coordinates as parameters. */
    private static final String URL_LATLNG = "http://api.geonames.org/extendedFindNearby?lat=%f&lng=%f&lang=%s&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Shuttle Radar Topography Mission (SRTM) elevation data.
     */
    private static final String URL_ELEVATION_SRTM3 = "http://api.geonames.org/srtm3?lat=%f&lng=%f&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Aster Global Digital Elevation Model data.
     */
    private static final String URL_ELEVATION_AGDEM = "http://api.geonames.org/astergdem?lat=%f&lng=%f&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * GTOPO30 is a global digital elevation model (DEM) with a horizontal grid
     * spacing of 30 arc seconds (approximately 1 kilometre).
     */
    private static final String URL_ELEVATION_GTOPO30 = "http://api.geonames.org/gtopo3?lat=%f&lng=%f&username=%s";

    /**
     * Creates a new GeoNames geocoder.
     *
     * @param context
     *         the context.
     */
    public GeoNamesGeocoder(Context context) {
        super(context);
    }

    /**
     * Creates a new GeoNames geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public GeoNamesGeocoder(Context context, Locale locale) {
        super(context, locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), USERNAME);
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return new AddressResponseHandler(results, maxResults, locale);
    }

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected static class AddressResponseHandler extends DefaultHandler2 {

        /** Parse state. */
        private enum State {
            START, ROOT, GEONAME, TOPONYM, TOPONYM_NAME, COUNTRY_CODE, COUNTRY, LATITUDE, LONGITUDE, STREET, STREET_NUMBER, MTFCC, LOCALITY, POSTAL_CODE, ADMIN_CODE, ADMIN, SUBADMIN_CODE, SUBADMIN, FINISH
        }

        private static final String TAG_ROOT = "geonames";
        private static final String TAG_LATITUDE = "lat";
        private static final String TAG_LONGITUDE = "lng";

        private static final String TAG_GEONAME = "geoname";
        private static final String TAG_TOPONYM = "toponymName";
        private static final String TAG_NAME = "name";
        private static final String TAG_CC = "countryCode";
        private static final String TAG_COUNTRY = "countryName";

        private static final String TAG_ADDRESS = "address";
        private static final String TAG_STREET = "street";
        private static final String TAG_MTFCC = "mtfcc";
        private static final String TAG_STREET_NUMBER = "streetNumber";
        private static final String TAG_POSTAL_CODE = "postalcode";
        private static final String TAG_LOCALITY = "placename";
        private static final String TAG_SUBADMIN_CODE = "adminCode2";
        private static final String TAG_SUBADMIN = "adminName2";
        private static final String TAG_ADMIN_CODE = "adminCode1";
        private static final String TAG_ADMIN = "adminName1";

        private State state = State.START;
        private final List<Address> results;
        private final int maxResults;
        private final Locale locale;
        private Address address;

        /**
         * Constructs a new parse handler.
         *
         * @param results
         *         the destination results.
         * @param maxResults
         *         the maximum number of results.
         */
        public AddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
            this.results = results;
            this.maxResults = maxResults;
            this.locale = locale;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (TextUtils.isEmpty(localName))
                localName = qName;

            switch (state) {
                case START:
                    if (TAG_ROOT.equals(localName))
                        state = State.ROOT;
                    else
                        throw new SAXException("Unexpected root element " + localName);
                    break;
                case ROOT:
                    if (TAG_GEONAME.equals(localName)) {
                        state = State.GEONAME;
                        address = new Address(locale);
                    } else if (TAG_ADDRESS.equals(localName)) {
                        state = State.GEONAME;
                        address = new Address(locale);
                    }
                    break;
                case GEONAME:
                    if (TAG_TOPONYM.equals(localName))
                        state = State.TOPONYM;
                    else if (TAG_NAME.equals(localName))
                        state = State.TOPONYM_NAME;
                    else if (TAG_LATITUDE.equals(localName))
                        state = State.LATITUDE;
                    else if (TAG_LONGITUDE.equals(localName))
                        state = State.LONGITUDE;
                    else if (TAG_CC.equals(localName))
                        state = State.COUNTRY_CODE;
                    else if (TAG_COUNTRY.equals(localName))
                        state = State.COUNTRY;
                    else if (TAG_STREET.equals(localName))
                        state = State.STREET;
                    else if (TAG_MTFCC.equals(localName))
                        state = State.MTFCC;
                    else if (TAG_STREET_NUMBER.equals(localName))
                        state = State.STREET_NUMBER;
                    else if (TAG_POSTAL_CODE.equals(localName))
                        state = State.POSTAL_CODE;
                    else if (TAG_LOCALITY.equals(localName))
                        state = State.LOCALITY;
                    else if (TAG_ADMIN.equals(localName))
                        state = State.ADMIN;
                    else if (TAG_ADMIN_CODE.equals(localName))
                        state = State.ADMIN_CODE;
                    else if (TAG_SUBADMIN.equals(localName))
                        state = State.SUBADMIN;
                    else if (TAG_SUBADMIN_CODE.equals(localName))
                        state = State.SUBADMIN_CODE;
                    break;
                case FINISH:
                    return;
                default:
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (TextUtils.isEmpty(localName))
                localName = qName;

            switch (state) {
                case ROOT:
                    if (TAG_ROOT.equals(localName))
                        state = State.FINISH;
                    break;
                case GEONAME:
                    if (TAG_GEONAME.equals(localName)) {
                        state = State.ROOT;
                        if (address != null) {
                            if ((results.size() < maxResults) && address.hasLatitude() && address.hasLongitude())
                                results.add(address);
                            else
                                state = State.FINISH;
                            address = null;
                        }
                    } else if (TAG_ADDRESS.equals(localName)) {
                        state = State.ROOT;
                        if (address != null) {
                            if (results.size() < maxResults)
                                results.add(address);
                            else
                                state = State.FINISH;
                            address = null;
                        }
                        state = State.ROOT;
                    }
                    break;
                case ADMIN:
                    if (TAG_ADMIN.equals(localName))
                        state = State.GEONAME;
                    break;
                case ADMIN_CODE:
                    if (TAG_ADMIN_CODE.equals(localName))
                        state = State.GEONAME;
                    break;
                case COUNTRY:
                    if (TAG_COUNTRY.equals(localName))
                        state = State.GEONAME;
                    break;
                case COUNTRY_CODE:
                    if (TAG_CC.equals(localName))
                        state = State.GEONAME;
                    break;
                case LATITUDE:
                    if (TAG_LATITUDE.equals(localName))
                        state = State.GEONAME;
                    break;
                case LOCALITY:
                    if (TAG_LOCALITY.equals(localName))
                        state = State.GEONAME;
                    break;
                case LONGITUDE:
                    if (TAG_LONGITUDE.equals(localName))
                        state = State.GEONAME;
                    break;
                case MTFCC:
                    if (TAG_MTFCC.equals(localName))
                        state = State.GEONAME;
                    break;
                case POSTAL_CODE:
                    if (TAG_POSTAL_CODE.equals(localName))
                        state = State.GEONAME;
                    break;
                case STREET:
                    if (TAG_STREET.equals(localName))
                        state = State.GEONAME;
                    break;
                case STREET_NUMBER:
                    if (TAG_STREET_NUMBER.equals(localName))
                        state = State.GEONAME;
                    break;
                case SUBADMIN:
                    if (TAG_SUBADMIN.equals(localName))
                        state = State.GEONAME;
                    break;
                case SUBADMIN_CODE:
                    if (TAG_SUBADMIN_CODE.equals(localName))
                        state = State.GEONAME;
                    break;
                case TOPONYM:
                    if (TAG_TOPONYM.equals(localName))
                        state = State.GEONAME;
                    break;
                case TOPONYM_NAME:
                    if (TAG_NAME.equals(localName))
                        state = State.GEONAME;
                    break;
                case FINISH:
                    return;
                default:
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (length == 0)
                return;
            String s = new String(ch, start, length).trim();
            if (s.length() == 0)
                return;

            switch (state) {
                case LATITUDE:
                    if (address != null) {
                        try {
                            address.setLatitude(Double.parseDouble(s));
                        } catch (NumberFormatException nfe) {
                            throw new SAXException(nfe);
                        }
                    }
                    break;
                case LONGITUDE:
                    if (address != null) {
                        try {
                            address.setLongitude(Double.parseDouble(s));
                        } catch (NumberFormatException nfe) {
                            throw new SAXException(nfe);
                        }
                    }
                    break;
                case ADMIN:
                    if (address != null)
                        address.setAdminArea(s);
                    break;
                case ADMIN_CODE:
                    if ((address != null) && (address.getAdminArea() == null))
                        address.setAdminArea(s);
                    break;
                case COUNTRY:
                    if (address != null)
                        address.setCountryName(s);
                    break;
                case COUNTRY_CODE:
                    if (address != null) {
                        address.setCountryCode(s);
                        if (address.getCountryName() == null)
                            address.setCountryName(new Locale(locale.getLanguage(), s).getDisplayCountry());
                    }
                    break;
                case LOCALITY:
                    if (address != null)
                        address.setLocality(s);
                    break;
                case MTFCC:
                    break;
                case POSTAL_CODE:
                    if (address != null)
                        address.setPostalCode(s);
                    break;
                case STREET:
                    if (address != null)
                        address.setAddressLine(1, s);
                    break;
                case STREET_NUMBER:
                    if (address != null)
                        address.setAddressLine(0, s);
                    break;
                case SUBADMIN:
                    if (address != null)
                        address.setSubAdminArea(s);
                    break;
                case SUBADMIN_CODE:
                    if ((address != null) && (address.getSubAdminArea() == null))
                        address.setSubAdminArea(s);
                    break;
                case TOPONYM:
                    if ((address != null) && (address.getFeatureName() == null))
                        address.setFeatureName(s);
                    break;
                case TOPONYM_NAME:
                    if (address != null)
                        address.setFeatureName(s);
                    break;
                case FINISH:
                    return;
                default:
                    break;
            }
        }
    }

    @Override
    public ZmanimLocation getElevation(double latitude, double longitude) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_ELEVATION_AGDEM, latitude, longitude, USERNAME);
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url);
        if (data == null)
            return null;
        String text = new String(data);
        double elevation;
        ZmanimLocation elevated;
        try {
            elevation = Double.parseDouble(text);
            if (elevation <= -9999)
                return null;
            elevated = new ZmanimLocation(USER_PROVIDER);
            elevated.setTime(System.currentTimeMillis());
            elevated.setLatitude(latitude);
            elevated.setLongitude(longitude);
            elevated.setAltitude(elevation);
            return elevated;
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Bad elevation: [" + text + "] at " + latitude + "," + longitude, nfe);
        }
        return null;
    }

    @Override
    protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
        return null;
    }

}
