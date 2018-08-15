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

import net.sf.net.HTTPReader;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * A class for handling geocoding and reverse geocoding.
 *
 * @author Moshe Waisberg
 */
public abstract class GeocoderBase {

    /** The "user pick a city" location provider. */
    public static final String USER_PROVIDER = "user";

    /** Maximum radius to consider two locations in the same vicinity. */
    protected static final float SAME_LOCATION = 250f;// 250 metres.
    /**
     * Maximum radius to consider a location near the same city.
     * <p/>
     * New York city, USA, is <tt>8,683 km<sup>2</sup></tt>, thus radius is
     * about <tt>37.175 km</tt>.<br>
     * Johannesburg/East Rand, ZA, is <tt>2,396 km<sup>2</sup></tt>, thus radius
     * is about <tt>19.527 km</tt>..<br>
     * Cape Town, ZA, is <tt>686 km<sup>2</sup></tt>, thus radius is about
     * <tt>10.449 km</tt>.
     */
    protected static final float SAME_CITY = 15000f;// 15 kilometres.
    /**
     * Maximum radius to consider a location near the same plateau with similar
     * terrain.
     */
    protected static final float SAME_PLATEAU = 50000f;// 50 kilometres.
    /**
     * Maximum radius to consider a location near the same planet.
     */
    protected static final float SAME_PLANET = 5000000f;// 5000 kilometres.

    protected final Context context;
    protected final Locale locale;
    private static SAXParserFactory parserFactory;
    private static SAXParser parser;

    /**
     * Creates a new geocoder.
     *
     * @param context
     *         the context.
     */
    public GeocoderBase(Context context) {
        this(context, Locale.getDefault());
    }

    /**
     * Creates a new geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public GeocoderBase(Context context, Locale locale) {
        this.context = context;
        this.locale = locale;
    }

    protected SAXParserFactory getParserFactory() {
        if (parserFactory == null)
            parserFactory = SAXParserFactory.newInstance();
        return parserFactory;
    }

    protected SAXParser getParser() throws ParserConfigurationException, SAXException {
        if (parser == null)
            parser = getParserFactory().newSAXParser();
        return parser;
    }

    /**
     * Returns an array of Addresses that are known to describe the area
     * immediately surrounding the given latitude and longitude.
     *
     * @param latitude
     *         the latitude a point for the search.
     * @param longitude
     *         the longitude a point for the search.
     * @param maxResults
     *         maximum number of addresses to return. Smaller numbers (1 to
     *         5) are recommended.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    public abstract List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException;

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     * <p/>
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName
     *         a user-supplied description of a location.
     * @param maxResults
     *         max number of addresses to return. Smaller numbers (1 to 5)
     *         are recommended.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        return null;
    }

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     * <p/>
     * You may specify a bounding box for the search results by including the
     * Latitude and Longitude of the Lower Left point and Upper Right point of
     * the box.
     * <p/>
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName
     *         a user-supplied description of a location.
     * @param maxResults
     *         max number of addresses to return. Smaller numbers (1 to 5)
     *         are recommended.
     * @param lowerLeftLatitude
     *         the latitude of the lower left corner of the bounding box.
     * @param lowerLeftLongitude
     *         the longitude of the lower left corner of the bounding box.
     * @param upperRightLatitude
     *         the latitude of the upper right corner of the bounding box.
     * @param upperRightLongitude
     *         the longitude of the upper right corner of the bounding box.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
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
        return null;
    }

    /**
     * Get the address by parsing the XML results.
     *
     * @param queryUrl
     *         the URL.
     * @param maxResults
     *         the maximum number of results.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    protected List<Address> getAddressXMLFromURL(String queryUrl, int maxResults) throws IOException {
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url, HTTPReader.CONTENT_XML);
        try {
            return parseLocations(data, maxResults);
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce.getMessage());
        } catch (SAXException se) {
            throw new IOException(se.getMessage());
        }
    }

    /**
     * Parse the XML response for addresses.
     *
     * @param data
     *         the XML data.
     * @param maxResults
     *         the maximum number of results.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws ParserConfigurationException
     *         if an XML error occurs.
     * @throws SAXException
     *         if an XML error occurs.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    protected List<Address> parseLocations(byte[] data, int maxResults) throws ParserConfigurationException, SAXException, IOException {
        // Minimum length for "<X/>"
        if ((data == null) || (data.length <= 4))
            return null;

        List<Address> results = new ArrayList<Address>(maxResults);
        InputStream in = new ByteArrayInputStream(data);
        SAXParser parser = getParser();
        DefaultHandler handler = createAddressResponseHandler(results, maxResults, locale);
        parser.parse(in, handler);

        return results;
    }

    /**
     * Create an SAX XML handler for addresses.
     *
     * @param results
     *         the list of results to populate.
     * @param maxResults
     *         the maximum number of results.
     * @param locale
     *         the locale.
     * @return the XML handler.
     */
    protected abstract DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale);

    /**
     * Get the ISO 639 language code.
     *
     * @return the language code.
     */
    protected String getLanguage() {
        String language = locale.getLanguage();
        if ("in".equals(language))
            return "id";
        if ("iw".equals(language))
            return "he";
        if ("ji".equals(language))
            return "yi";
        return language;
    }

    /**
     * Get the location with elevation.
     *
     * @param latitude
     *         the latitude a point for the search.
     * @param longitude
     *         the longitude a point for the search.
     * @return the location - {@code null} otherwise.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    public abstract ZmanimLocation getElevation(double latitude, double longitude) throws IOException;

    /**
     * Get the elevation by parsing the XML results.
     *
     * @param queryUrl
     *         the URL.
     * @return the location - {@code null} otherwise.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    protected ZmanimLocation getElevationXMLFromURL(String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url, HTTPReader.CONTENT_XML);
        try {
            return parseElevation(data);
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce.getMessage());
        } catch (SAXException se) {
            throw new IOException(se.getMessage());
        }
    }

    /**
     * Parse the XML response for an elevation.
     *
     * @param data
     *         the XML data.
     * @param maxResults
     *         the maximum number of results.
     * @return the location - {@code null} otherwise.
     * @throws ParserConfigurationException
     *         if an XML error occurs.
     * @throws SAXException
     *         if an XML error occurs.
     * @throws IOException
     *         if the network is unavailable or any other I/O problem
     *         occurs.
     */
    protected ZmanimLocation parseElevation(byte[] data) throws ParserConfigurationException, SAXException, IOException {
        // Minimum length for "<X/>"
        if ((data == null) || (data.length <= 4))
            return null;

        List<ZmanimLocation> results = new ArrayList<ZmanimLocation>(1);
        InputStream in = new ByteArrayInputStream(data);
        SAXParser parser = getParser();
        DefaultHandler handler = createElevationResponseHandler(results);
        parser.parse(in, handler);

        if (results.isEmpty())
            return null;

        return results.get(0);
    }

    /**
     * Create an SAX XML handler for elevations.
     *
     * @param results
     *         the list of results to populate.
     * @return the XML handler.
     */
    protected abstract DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results);

}
