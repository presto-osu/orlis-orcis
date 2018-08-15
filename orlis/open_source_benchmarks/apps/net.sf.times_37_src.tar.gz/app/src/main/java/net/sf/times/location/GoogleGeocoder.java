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
import android.os.Bundle;
import android.text.TextUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Google Geocoding API.
 * <p/>
 * <a href="http://code.google.com/apis/maps/documentation/geocoding/">http://
 * code.google.com/apis/maps/documentation/geocoding/</a>
 *
 * @author Moshe Waisberg
 */
public class GoogleGeocoder extends GeocoderBase {

    /** URL that accepts latitude and longitude coordinates as parameters. */
    private static final String URL_LATLNG = "http://maps.googleapis.com/maps/api/geocode/xml?latlng=%f,%f&language=%s&sensor=true";
    /** URL that accepts an address as parameters. */
    private static final String URL_ADDRESS = "http://maps.googleapis.com/maps/api/geocode/xml?address=%s&language=%s&sensor=true";
    /** URL that accepts a bounded address as parameters. */
    private static final String URL_ADDRESS_BOUNDED = "http://maps.googleapis.com/maps/api/geocode/xml?address=%s&bounds=%f,%f|%f,%f&language=%s&sensor=true";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.
     */
    private static final String URL_ELEVATION = "http://maps.googleapis.com/maps/api/elevation/xml?locations=%f,%f";

    /**
     * Creates a new Google geocoder.
     *
     * @param context
     *         the context.
     */
    public GoogleGeocoder(Context context) {
        super(context);
    }

    /**
     * Creates a new Google geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public GoogleGeocoder(Context context, Locale locale) {
        super(context, locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage());
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        if (locationName == null)
            throw new IllegalArgumentException("locationName == null");
        String queryUrl = String.format(Locale.US, URL_ADDRESS, locationName, getLanguage());
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    public List<Address> getFromLocationName(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
                                             double upperRightLongitude) throws IOException {
        if (locationName == null)
            throw new IllegalArgumentException("locationName == null");
        if (lowerLeftLatitude < -90.0 || lowerLeftLatitude > 90.0)
            throw new IllegalArgumentException("lowerLeftLatitude == " + lowerLeftLatitude);
        if (lowerLeftLongitude < -180.0 || lowerLeftLongitude > 180.0)
            throw new IllegalArgumentException("lowerLeftLongitude == " + lowerLeftLongitude);
        if (upperRightLatitude < -90.0 || upperRightLatitude > 90.0)
            throw new IllegalArgumentException("upperRightLatitude == " + upperRightLatitude);
        if (upperRightLongitude < -180.0 || upperRightLongitude > 180.0)
            throw new IllegalArgumentException("upperRightLongitude == " + upperRightLongitude);
        String queryUrl = String
                .format(Locale.US, URL_ADDRESS_BOUNDED, locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, getLanguage());
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return new GeocodeResponseHandler(results, maxResults, locale);
    }

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected static class GeocodeResponseHandler extends DefaultHandler2 {

        /** Parse state. */
        private enum State {
            START, ROOT, STATUS, RESULT, RESULT_TYPE, RESULT_FORMATTED, ADDRESS, ADDRESS_TYPE, ADDRESS_LONG, ADDRESS_SHORT, GEOMETRY, LOCATION, LATITUDE, LONGITUDE, FINISH
        }

        private static final String STATUS_OK = "OK";

        private static final String TAG_ROOT = "GeocodeResponse";
        private static final String TAG_STATUS = "status";
        private static final String TAG_RESULT = "result";
        private static final String TAG_TYPE = "type";
        private static final String TAG_FORMATTED = "formatted_address";
        private static final String TAG_ADDRESS = "address_component";
        private static final String TAG_LONG_NAME = "long_name";
        private static final String TAG_SHORT_NAME = "short_name";
        private static final String TAG_GEOMETRY = "geometry";
        private static final String TAG_LOCATION = "location";
        private static final String TAG_LATITUDE = "lat";
        private static final String TAG_LONGITUDE = "lng";

        private static final String TYPE_ADMIN = "administrative_area_level_1";
        private static final String TYPE_COUNTRY = "country";
        private static final String TYPE_FEATURE = "feature_name";
        private static final String TYPE_LOCALITY = "locality";
        private static final String TYPE_POLITICAL = "political";
        private static final String TYPE_POSTAL_CODE = "postal_code";
        private static final String TYPE_ROUTE = "route";
        private static final String TYPE_STREET = "street_address";
        private static final String TYPE_STREET_NUMBER = "street_number";
        private static final String TYPE_SUBADMIN = "administrative_area_level_2";
        private static final String TYPE_SUBLOCALITY = "sublocality";

        private State state = State.START;
        private final List<Address> results;
        private final int maxResults;
        private final Locale locale;
        private Address address;
        private String longName;
        private String shortName;
        private String addressType;

        /**
         * Constructs a new parse handler.
         *
         * @param results
         *         the destination results.
         * @param maxResults
         *         the maximum number of results.
         */
        public GeocodeResponseHandler(List<Address> results, int maxResults, Locale locale) {
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
                    if (TAG_STATUS.equals(localName))
                        state = State.STATUS;
                    else if (TAG_RESULT.equals(localName))
                        state = State.RESULT;
                    break;
                case RESULT:
                    if (TAG_TYPE.equals(localName))
                        state = State.RESULT_TYPE;
                    else if (TAG_FORMATTED.equals(localName))
                        state = State.RESULT_FORMATTED;
                    else if (TAG_ADDRESS.equals(localName))
                        state = State.ADDRESS;
                    else if (TAG_GEOMETRY.equals(localName))
                        state = State.GEOMETRY;
                    break;
                case ADDRESS:
                    if (TAG_LONG_NAME.equals(localName))
                        state = State.ADDRESS_LONG;
                    else if (TAG_SHORT_NAME.equals(localName))
                        state = State.ADDRESS_SHORT;
                    else if (TAG_TYPE.equals(localName))
                        state = State.ADDRESS_TYPE;
                    break;
                case GEOMETRY:
                    if (TAG_LOCATION.equals(localName))
                        state = State.LOCATION;
                    break;
                case LOCATION:
                    if (TAG_LATITUDE.equals(localName))
                        state = State.LATITUDE;
                    else if (TAG_LONGITUDE.equals(localName))
                        state = State.LONGITUDE;
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
                case STATUS:
                    if (TAG_STATUS.equals(localName))
                        state = State.ROOT;
                    break;
                case RESULT:
                    if (TAG_RESULT.equals(localName)) {
                        if (address != null) {
                            if ((results.size() < maxResults) && address.hasLatitude() && address.hasLongitude())
                                results.add(address);
                            else
                                state = State.FINISH;
                            address = null;
                        }
                        longName = null;
                        shortName = null;
                        addressType = null;
                        state = State.ROOT;
                    }
                    break;
                case RESULT_TYPE:
                    if (TAG_TYPE.equals(localName))
                        state = State.RESULT;
                    break;
                case RESULT_FORMATTED:
                    if (TAG_FORMATTED.equals(localName))
                        state = State.RESULT;
                    break;
                case ADDRESS:
                    if (TAG_ADDRESS.equals(localName)) {
                        if (address != null) {
                            if (TYPE_ADMIN.equals(addressType)) {
                                address.setAdminArea(longName);
                            } else if (TYPE_SUBADMIN.equals(addressType)) {
                                address.setSubAdminArea(longName);
                            } else if (TYPE_COUNTRY.equals(addressType)) {
                                address.setCountryCode(shortName);
                                address.setCountryName(longName);
                            } else if (TYPE_FEATURE.equals(addressType)) {
                                address.setFeatureName(longName);
                            } else if (TYPE_LOCALITY.equals(addressType)) {
                                address.setLocality(longName);
                            } else if (TYPE_POSTAL_CODE.equals(addressType)) {
                                address.setPostalCode(longName);
                            } else if (TYPE_ROUTE.equals(addressType) || TYPE_STREET.equals(addressType) || TYPE_STREET_NUMBER.equals(addressType)) {
                                address.setAddressLine(address.getMaxAddressLineIndex() + 1, longName);
                            } else if (TYPE_SUBLOCALITY.equals(addressType)) {
                                address.setSubLocality(longName);
                            }
                            longName = null;
                            shortName = null;
                            addressType = null;
                        }
                        state = State.RESULT;
                    }
                    break;
                case ADDRESS_LONG:
                    if (TAG_LONG_NAME.equals(localName))
                        state = State.ADDRESS;
                    break;
                case ADDRESS_SHORT:
                    if (TAG_SHORT_NAME.equals(localName))
                        state = State.ADDRESS;
                    break;
                case ADDRESS_TYPE:
                    if (TAG_TYPE.equals(localName))
                        state = State.ADDRESS;
                    break;
                case GEOMETRY:
                    if (TAG_GEOMETRY.equals(localName))
                        state = State.RESULT;
                    break;
                case LOCATION:
                    if (TAG_LOCATION.equals(localName))
                        state = State.GEOMETRY;
                    break;
                case LATITUDE:
                    if (TAG_LATITUDE.equals(localName))
                        state = State.LOCATION;
                    break;
                case LONGITUDE:
                    if (TAG_LONGITUDE.equals(localName))
                        state = State.LOCATION;
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
                case STATUS:
                    if (!STATUS_OK.equals(s))
                        state = State.FINISH;
                    break;
                case RESULT_TYPE:
                    if (TYPE_POLITICAL.equals(s))
                        break;
                    address = new Address(locale);
                    break;
                case RESULT_FORMATTED:
                    if (address != null) {
                        Bundle extras = address.getExtras();
                        if (extras == null) {
                            extras = new Bundle();
                            address.setExtras(extras);
                            extras = address.getExtras();
                        }
                        extras.putString(ZmanimAddress.KEY_FORMATTED, s);
                    }
                    break;
                case ADDRESS_LONG:
                    longName = s;
                    break;
                case ADDRESS_SHORT:
                    shortName = s;
                    break;
                case ADDRESS_TYPE:
                    if (TYPE_POLITICAL.equals(s))
                        break;
                    if (addressType == null)
                        addressType = s;
                    break;
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
        String queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude);
        return getElevationXMLFromURL(queryUrl);
    }

    @Override
    protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
        return new ElevationResponseHandler(results);
    }

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected static class ElevationResponseHandler extends DefaultHandler2 {

        /** Parse state. */
        private enum State {
            START, ROOT, STATUS, RESULT, LOCATION, FINISH
        }

        private static final String STATUS_OK = "OK";

        private static final String TAG_ROOT = "ElevationResponse";
        private static final String TAG_STATUS = "status";
        private static final String TAG_RESULT = "result";
        private static final String TAG_LOCATION = "location";
        private static final String TAG_LATITUDE = "lat";
        private static final String TAG_LONGITUDE = "lng";
        private static final String TAG_ELEVATION = "elevation";

        private State mState = State.START;
        private final List<ZmanimLocation> mResults;
        private ZmanimLocation mLocation;
        private String mTag;

        /**
         * Constructs a new parse handler.
         *
         * @param results
         *         the destination results.
         * @param maxResults
         *         the maximum number of results.
         */
        public ElevationResponseHandler(List<ZmanimLocation> results) {
            mResults = results;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (TextUtils.isEmpty(localName))
                localName = qName;

            mTag = localName;

            switch (mState) {
                case START:
                    if (TAG_ROOT.equals(localName))
                        mState = State.ROOT;
                    else
                        throw new SAXException("Unexpected root element " + localName);
                    break;
                case ROOT:
                    if (TAG_STATUS.equals(localName))
                        mState = State.STATUS;
                    else if (TAG_RESULT.equals(localName))
                        mState = State.RESULT;
                    break;
                case RESULT:
                    if (TAG_LOCATION.equals(localName)) {
                        mLocation = new ZmanimLocation(USER_PROVIDER);
                        mLocation.setTime(System.currentTimeMillis());
                        mState = State.LOCATION;
                    }
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

            switch (mState) {
                case ROOT:
                    if (TAG_ROOT.equals(localName))
                        mState = State.FINISH;
                    break;
                case STATUS:
                    if (TAG_STATUS.equals(localName))
                        mState = State.ROOT;
                    break;
                case RESULT:
                    if (TAG_RESULT.equals(localName)) {
                        if (mLocation != null) {
                            if (mLocation.hasAltitude())
                                mResults.add(mLocation);
                            mLocation = null;
                        }
                        mState = State.ROOT;
                    }
                    break;
                case LOCATION:
                    if (TAG_LOCATION.equals(localName))
                        mState = State.RESULT;
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

            switch (mState) {
                case STATUS:
                    if (!STATUS_OK.equals(s))
                        mState = State.FINISH;
                    break;
                case RESULT:
                    if (mLocation != null) {
                        if (TAG_ELEVATION.equals(mTag)) {
                            try {
                                mLocation.setAltitude(Double.parseDouble(s));
                            } catch (NumberFormatException nfe) {
                                throw new SAXException(nfe);
                            }
                        }
                    }
                    break;
                case LOCATION:
                    if (mLocation != null) {
                        if (TAG_LATITUDE.equals(mTag)) {
                            try {
                                mLocation.setLatitude(Double.parseDouble(s));
                            } catch (NumberFormatException nfe) {
                                throw new SAXException(nfe);
                            }
                        } else if (TAG_LONGITUDE.equals(mTag)) {
                            try {
                                mLocation.setLongitude(Double.parseDouble(s));
                            } catch (NumberFormatException nfe) {
                                throw new SAXException(nfe);
                            }
                        }
                    }
                    break;
                case FINISH:
                    return;
                default:
                    break;
            }
        }
    }

}
