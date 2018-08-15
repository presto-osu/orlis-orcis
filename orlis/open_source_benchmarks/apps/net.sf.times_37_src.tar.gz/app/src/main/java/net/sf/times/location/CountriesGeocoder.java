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
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.text.format.DateUtils;

import net.sf.times.R;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Maintains the lists of countries.
 *
 * @author Moshe Waisberg
 */
public class CountriesGeocoder extends GeocoderBase {

    /** The time zone location provider. */
    public static final String TIMEZONE_PROVIDER = "timezone";

    /** Degrees per time zone hour. */
    private static final double TZ_HOUR = 360 / 24;

    /** Factor to convert a fixed-point integer to double. */
    private static final double RATIO = 1e+6;

    /**
     * Not physically possible for more than 20 countries to overlap each other.
     */
    private static final int MAX_COUNTRIES_OVERLAP = 20;

    /** Maximum radius for which a zman is the same (20 kilometres). */
    private static final float CITY_RADIUS = 20000f;

    private static CountryPolygon[] countryBorders;
    private String[] citiesNames;
    private static String[] citiesCountries;
    private static double[] citiesLatitudes;
    private static double[] citiesLongitudes;
    private static double[] citiesElevations;

    /**
     * Constructs a new cities provider.
     *
     * @param context
     *         the context.
     */
    public CountriesGeocoder(Context context) {
        super(context);
    }

    /**
     * Constructs a new cities provider.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public CountriesGeocoder(Context context, Locale locale) {
        super(context, locale);

        // Populate arrays from "countries.xml"
        Resources res = context.getResources();
        if (countryBorders == null) {
            String[] countryCodes = res.getStringArray(R.array.countries);
            int countriesCount = countryCodes.length;
            countryBorders = new CountryPolygon[countriesCount];
            int[] verticesCounts = res.getIntArray(R.array.vertices_count);
            int[] latitudes = res.getIntArray(R.array.latitudes);
            int[] longitudes = res.getIntArray(R.array.longitudes);
            int verticesCount;
            CountryPolygon country;
            int i = 0;

            for (int c = 0; c < countriesCount; c++) {
                verticesCount = verticesCounts[c];
                country = new CountryPolygon(countryCodes[c]);
                for (int v = 0; v < verticesCount; v++, i++) {
                    country.addPoint(latitudes[i], longitudes[i]);
                }
                countryBorders[c] = country;
            }
        }
        if (citiesCountries == null) {
            citiesCountries = res.getStringArray(R.array.cities_countries);
            int citiesCount = citiesCountries.length;
            String[] latitudes = res.getStringArray(R.array.cities_latitudes);
            String[] longitudes = res.getStringArray(R.array.cities_longitudes);
            String[] elevations = res.getStringArray(R.array.cities_elevations);
            citiesLatitudes = new double[citiesCount];
            citiesLongitudes = new double[citiesCount];
            citiesElevations = new double[citiesCount];
            for (int i = 0; i < citiesCount; i++) {
                citiesLatitudes[i] = Double.parseDouble(latitudes[i]);
                citiesLongitudes[i] = Double.parseDouble(longitudes[i]);
                citiesElevations[i] = Double.parseDouble(elevations[i]);
            }
        }
        citiesNames = res.getStringArray(R.array.cities);
    }

    /**
     * Find the nearest city to the location.
     *
     * @param location
     *         the location.
     * @return the city - {@code null} otherwise.
     */
    public Address findCountry(Location location) {
        return findCountry(location.getLatitude(), location.getLongitude());
    }

    /**
     * Find the nearest city to the location.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     * @return the city - {@code null} otherwise.
     */
    public Address findCountry(double latitude, double longitude) {
        final int fixedpointLatitude = (int) Math.rint(latitude * RATIO);
        final int fixedpointLongitude = (int) Math.rint(longitude * RATIO);
        double distanceToBorder;
        double distanceMin = Double.MAX_VALUE;
        int found = -1;
        final int countriesSize = countryBorders.length;
        CountryPolygon country;
        int[] matches = new int[MAX_COUNTRIES_OVERLAP];
        int matchesCount = 0;

        for (int c = 0; (c < countriesSize) && (matchesCount < MAX_COUNTRIES_OVERLAP); c++) {
            country = countryBorders[c];
            if (country.containsBox(fixedpointLatitude, fixedpointLongitude))
                matches[matchesCount++] = c;
        }
        if (matchesCount == 0) {
            // Find the nearest border.
            for (int c = 0; c < countriesSize; c++) {
                country = countryBorders[c];
                distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
                if (distanceToBorder < distanceMin) {
                    distanceMin = distanceToBorder;
                    found = c;
                }
            }

            if (found < 0)
                return null;
        } else if (matchesCount == 1) {
            found = matches[0];
        } else {
            // Case 1: Smaller country inside a larger country.
            CountryPolygon other;
            country = countryBorders[matches[0]];
            int matchCountryIndex;
            for (int m = 1; m < matchesCount; m++) {
                matchCountryIndex = matches[m];
                other = countryBorders[matchCountryIndex];
                if (country.containsBox(other)) {
                    country = other;
                    found = matchCountryIndex;
                } else if ((found < 0) && other.containsBox(country)) {
                    found = matches[0];
                }
            }

            // Case 2: Country rectangle intersects another country's rectangle.
            if (found < 0) {
                // Only include countries foe which the location is actually
                // inside the defined borders.
                for (int m = 0; m < matchesCount; m++) {
                    matchCountryIndex = matches[m];
                    country = countryBorders[matchCountryIndex];
                    if (country.contains(fixedpointLatitude, fixedpointLongitude)) {
                        distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
                        if (distanceToBorder < distanceMin) {
                            distanceMin = distanceToBorder;
                            found = matchCountryIndex;
                        }
                    }
                }

                if (found < 0) {
                    // Find the nearest border.
                    for (int m = 0; m < matchesCount; m++) {
                        matchCountryIndex = matches[m];
                        country = countryBorders[matchCountryIndex];
                        distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
                        if (distanceToBorder < distanceMin) {
                            distanceMin = distanceToBorder;
                            found = matchCountryIndex;
                        }
                    }
                }
            }
        }

        Locale locale = new Locale(getLanguage(), countryBorders[found].countryCode);
        ZmanimAddress city = new ZmanimAddress(locale);
        city.setId(-found);
        city.setLatitude(latitude);
        city.setLongitude(longitude);
        city.setCountryCode(locale.getCountry());
        city.setCountryName(locale.getDisplayCountry());

        return city;
    }

    /**
     * Find the first corresponding location for the time zone.
     *
     * @param tz
     *         the time zone.
     * @return the location - {@code null} otherwise.
     */
    public Location findLocation(TimeZone tz) {
        Location loc = new Location(TIMEZONE_PROVIDER);
        if (tz != null) {
            int offset = tz.getRawOffset() % 43200000;
            double longitude = (TZ_HOUR * offset) / DateUtils.HOUR_IN_MILLIS;
            loc.setLongitude(longitude);
        }
        return loc;
    }

    /**
     * Find the nearest valid city for the location.
     *
     * @param location
     *         the location.
     * @return the city - {@code null} otherwise.
     */
    public Address findCity(Location location) {
        ZmanimAddress city = null;
        final int citiesCount = citiesNames.length;
        double searchLatitude = location.getLatitude();
        double searchLongitude = location.getLongitude();
        double latitude;
        double longitude;
        float distanceMin = Float.MAX_VALUE;
        float[] distances = new float[1];
        Locale cityLocale;
        int nearestCityIndex = -1;

        for (int i = 0; i < citiesCount; i++) {
            latitude = citiesLatitudes[i];
            longitude = citiesLongitudes[i];
            Location.distanceBetween(searchLatitude, searchLongitude, latitude, longitude, distances);
            if (distances[0] <= distanceMin) {
                distanceMin = distances[0];
                if (distanceMin <= CITY_RADIUS) {
                    nearestCityIndex = i;
                }
            }
        }
        if (nearestCityIndex >= 0) {
            cityLocale = new Locale(getLanguage(), citiesCountries[nearestCityIndex]);

            city = new ZmanimAddress(locale);
            city.setId(-nearestCityIndex - 1);
            city.setLatitude(citiesLatitudes[nearestCityIndex]);
            city.setLongitude(citiesLongitudes[nearestCityIndex]);
            city.setElevation(citiesElevations[nearestCityIndex]);
            city.setCountryCode(cityLocale.getCountry());
            city.setCountryName(cityLocale.getDisplayCountry());
            city.setLocality(citiesNames[nearestCityIndex]);
        }

        return city;
    }

    /**
     * Get the list of cities.
     *
     * @return the list of addresses.
     */
    public List<ZmanimAddress> getCities() {
        final int citiesCount = citiesNames.length;
        List<ZmanimAddress> cities = new ArrayList<ZmanimAddress>(citiesCount);
        double latitude;
        double longitude;
        double elevation;
        String cityName;
        Locale locale = this.locale;
        Locale cityLocale;
        String languageCode = locale.getLanguage();
        ZmanimAddress city;

        for (int i = 0, j = -1; i < citiesCount; i++, j--) {
            latitude = citiesLatitudes[i];
            longitude = citiesLongitudes[i];
            elevation = citiesElevations[i];
            cityName = citiesNames[i];
            cityLocale = new Locale(languageCode, citiesCountries[i]);

            city = new ZmanimAddress(locale);
            city.setId(j);
            city.setLatitude(latitude);
            city.setLongitude(longitude);
            city.setElevation(elevation);
            city.setCountryCode(cityLocale.getCountry());
            city.setCountryName(cityLocale.getDisplayCountry());
            city.setLocality(cityName);

            cities.add(city);
        }

        return cities;
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);

        List<Address> cities = new ArrayList<Address>(maxResults);
        ZmanimAddress city = null;
        final int citiesCount = citiesNames.length;
        double cityLatitude;
        double cityLongitude;
        float[] distances = new float[1];
        Locale cityLocale;

        for (int i = 0; i < citiesCount; i++) {
            cityLatitude = citiesLatitudes[i];
            cityLongitude = citiesLongitudes[i];
            Location.distanceBetween(latitude, longitude, cityLatitude, cityLongitude, distances);
            if (distances[0] <= CITY_RADIUS) {
                cityLocale = new Locale(getLanguage(), citiesCountries[i]);

                city = new ZmanimAddress(locale);
                city.setId(-i - 1);
                city.setLatitude(cityLatitude);
                city.setLongitude(cityLongitude);
                city.setElevation(citiesElevations[i]);
                city.setCountryCode(cityLocale.getCountry());
                city.setCountryName(cityLocale.getDisplayCountry());
                city.setLocality(citiesNames[i]);

                cities.add(city);
            }
        }

        return cities;
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return null;
    }

    @Override
    public ZmanimLocation getElevation(double latitude, double longitude) throws IOException {
        if (latitude < -90.0 || latitude > 90.0)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < -180.0 || longitude > 180.0)
            throw new IllegalArgumentException("longitude == " + longitude);

        List<ZmanimAddress> cities = getCities();
        int citiesCount = cities.size();

        float distance;
        float[] distanceCity = new float[1];
        double d;
        double distancesSum = 0;
        int n = 0;
        double[] distances = new double[citiesCount];
        double[] elevations = new double[citiesCount];
        ZmanimLocation elevated;
        ZmanimAddress cityNearest = null;
        double distanceCityMin = SAME_CITY;

        for (ZmanimAddress city : cities) {
            if (!city.hasElevation())
                continue;
            Location.distanceBetween(latitude, longitude, city.getLatitude(), city.getLongitude(), distanceCity);
            distance = distanceCity[0];
            if (distance <= SAME_PLATEAU) {
                if (distance < distanceCityMin) {
                    cityNearest = city;
                    distanceCityMin = distance;
                }
                elevations[n] = city.getElevation();
                d = distance * distance;
                distances[n] = d;
                distancesSum += d;
                n++;
            }
        }

        if ((n == 1) && (cityNearest != null)) {
            elevated = new ZmanimLocation(USER_PROVIDER);
            elevated.setTime(System.currentTimeMillis());
            elevated.setLatitude(cityNearest.getLatitude());
            elevated.setLongitude(cityNearest.getLongitude());
            elevated.setAltitude(cityNearest.getElevation());
            elevated.setId(cityNearest.getId());
            return elevated;
        }
        if (n <= 1)
            return null;

        double weightSum = 0;
        for (int i = 0; i < n; i++) {
            weightSum += (1 - (distances[i] / distancesSum)) * elevations[i];
        }

        elevated = new ZmanimLocation(USER_PROVIDER);
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
