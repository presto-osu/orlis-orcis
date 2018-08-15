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

import android.graphics.Point;
import android.graphics.Region;

/**
 * Country borders as a simplified polygon.<br>
 * Loosely based on {@code java.awt.Polygon}.
 * <p/>
 * <em>{@link Region} has bug with large areas.</em>
 *
 * @author Moshe Waisberg
 */
public class CountryPolygon {

    /** Default length for latitudes and longitudes. */
    private static final int MIN_LENGTH = 8;

    public final String countryCode;
    /** The total number of points. */
    public int npoints;
    /** The array of latitudes (Y coordinates). */
    public int[] latitudes;
    /** The array of longitudes (X coordinates). */
    public int[] longitudes;
    private int minLatitude = Integer.MAX_VALUE;
    private int minLongitude = Integer.MAX_VALUE;
    private int maxLatitude = Integer.MIN_VALUE;
    private int maxLongitude = Integer.MIN_VALUE;

    /**
     * Constructs a new country.
     *
     * @param countryCode
     *         the country code.
     */
    public CountryPolygon(String countryCode) {
        this.countryCode = countryCode;
        this.latitudes = new int[MIN_LENGTH];
        this.longitudes = new int[MIN_LENGTH];
    }

    /**
     * Constructs a new country.
     *
     * @param countryCode
     *         the country code.
     * @param latitudes
     *         an array of latitudes.
     * @param longitudes
     *         an array of longitudes.
     * @param npoints
     *         the total number of points.
     * @throws NegativeArraySizeException
     *         if the value of {@code npoints} is negative.
     * @throws IndexOutOfBoundsException
     *         if {@code npoints} is greater than the length of
     *         {@code latitudes} or the length of {@code longitudes}.
     * @throws NullPointerException
     *         if {@code latitudes} or {@code longitudes} is {@code null}.
     */
    public CountryPolygon(String countryCode, int[] latitudes, int[] longitudes, int npoints) {
        this.countryCode = countryCode;
        if (npoints > longitudes.length || npoints > latitudes.length) {
            throw new IndexOutOfBoundsException("npoints > longitutes.length || " + "npoints > latitudes.length");
        }
        if (npoints < 0) {
            throw new NegativeArraySizeException("npoints < 0");
        }
        this.npoints = npoints;
        this.latitudes = new int[npoints];
        this.longitudes = new int[npoints];
        System.arraycopy(latitudes, 0, this.latitudes, 0, npoints);
        System.arraycopy(longitudes, 0, this.longitudes, 0, npoints);
    }

    /**
     * Tests if the specified coordinates are inside the bounding box of the
     * country.
     *
     * @param latitude
     *         the latitude to be tested.
     * @param longitude
     *         the longitude to be tested.
     * @return {@code true} if the specified coordinates are inside the country
     * boundary; {@code false} otherwise.
     */
    public boolean containsBox(int latitude, int longitude) {
        return (latitude >= minLatitude) && (latitude <= maxLatitude) && (longitude >= minLongitude) && (longitude <= maxLongitude);
    }

    /**
     * Tests if the specified country is inside the bounding box of this
     * country.
     *
     * @param other
     *         the other country to be tested.
     * @return {@code true} if the specified country is inside this country
     * boundary; {@code false} otherwise.
     */
    public boolean containsBox(CountryPolygon other) {
        return (other.minLatitude >= this.minLatitude) && (other.minLongitude >= this.minLongitude) && (other.maxLatitude <= this.maxLatitude)
                && (other.maxLongitude <= this.maxLongitude);
    }

    /**
     * Tests if the specified coordinates are inside the borders of the country.
     * <p/>
     * Uses the ray casting algorithm.
     *
     * @param latitude
     *         the latitude to be tested.
     * @param longitude
     *         the longitude to be tested.
     * @return {@code true} if the specified coordinates are inside the country
     * boundary; {@code false} otherwise.
     */
    public boolean contains(int latitude, int longitude) {
        int j = npoints - 1;
        boolean oddNodes = false;
        int latI, lngI;
        int latJ, lngJ;

        for (int i = 0; i < npoints; i++) {
            latI = latitudes[i];
            lngI = longitudes[i];
            latJ = latitudes[j];
            lngJ = longitudes[j];

            if ((((latI < latitude) && (latJ >= latitude)) || ((latJ < latitude) && (latI >= latitude))) && ((lngI <= longitude) || (lngJ <= longitude))) {
                if (lngI + (((latitude - latI) * (lngJ - lngI)) / (latJ - latI)) < longitude) {
                    oddNodes = !oddNodes;
                }
            }
            j = i;
        }

        return oddNodes;
    }

    /**
     * Appends the specified coordinates to this country.
     * <p/>
     *
     * @param latitude
     *         the specified latitude (Y coordinate).
     * @param longitude
     *         the specified longitude (X coordinate).
     */
    public void addPoint(int latitude, int longitude) {
        if (npoints >= latitudes.length || npoints >= longitudes.length) {
            int newLength = npoints << 1;
            // Make sure that newLength will be greater than MIN_LENGTH and
            // aligned to the power of 2
            if (newLength < MIN_LENGTH) {
                newLength = MIN_LENGTH;
            } else if ((newLength & (newLength - 1)) != 0) {
                newLength = Integer.highestOneBit(newLength);
            }

            int[] oldLatitudes = latitudes;
            int[] oldLongitudes = longitudes;
            this.latitudes = new int[newLength];
            this.longitudes = new int[newLength];
            System.arraycopy(oldLatitudes, 0, latitudes, 0, npoints);
            System.arraycopy(oldLongitudes, 0, this.longitudes, 0, npoints);
        }
        latitudes[npoints] = latitude;
        longitudes[npoints] = longitude;
        npoints++;
        updateBounds(latitude, longitude);
    }

    /**
     * Update the rectangular boundary.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     */
    private void updateBounds(int latitude, int longitude) {
        if (latitude < minLatitude)
            minLatitude = latitude;
        if (latitude > maxLatitude)
            maxLatitude = latitude;
        if (longitude < minLongitude)
            minLongitude = longitude;
        if (longitude > maxLongitude)
            maxLongitude = longitude;
    }

    /**
     * Calculate the distance from a point to a line.
     *
     * @param a
     *         a point on the line.
     * @param b
     *         another point on the line.
     * @param p
     *         the point, not on the line.
     * @return the distance.
     * @see <a
     * href="http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line">Distance
     * from a point to a line</a>
     */
    public static double pointToLineDistance(Point a, Point b, Point p) {
        return pointToLineDistance(a.x, a.y, b.x, b.y, p.x, p.y);
    }

    /**
     * Calculate the distance from a point to a line.
     *
     * @param ax
     *         X coordinate of a point on the line.
     * @param ay
     *         Y coordinate of a point on the line.
     * @param bx
     *         X coordinate of another point on the line.
     * @param by
     *         Y coordinate of another point on the line.
     * @param px
     *         X coordinate of the point, not on the line.
     * @param py
     *         Y coordinate of the point, not on the line.
     * @return the distance.
     * @see <a
     * href="http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line">Distance
     * from a point to a line</a>
     */
    public static double pointToLineDistance(double ax, double ay, double bx, double by, double px, double py) {
        final double dxAB = bx - ax;
        final double dyAB = by - ay;
        final double normalLength = Math.hypot(dxAB, dyAB);
        return Math.abs(((px - ax) * dyAB) - ((py - ay) * dxAB)) / normalLength;
    }

    /**
     * Find the minimum distance to any of the borders.
     *
     * @param latitude
     *         the latitude of the point.
     * @param longitude
     *         the longitude of the point.
     * @return the distance.
     */
    public double minimumDistanceToBorders(int latitude, int longitude) {
        int j = npoints - 1;
        double minimum = Double.MAX_VALUE;
        double d;

        for (int i = 0; i < npoints; i++) {
            d = pointToLineDistance(latitudes[i], longitudes[i], latitudes[j], longitudes[j], latitude, longitude);
            if (d < minimum)
                minimum = d;
            j = i;
        }
        return minimum;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(countryCode);
        buf.append('[');
        for (int i = 0; i < npoints; i++) {
            if (i > 0)
                buf.append(',');
            buf.append('(');
            buf.append(latitudes[i]);
            buf.append(',');
            buf.append(longitudes[i]);
            buf.append(')');
        }
        buf.append(']');
        return buf.toString();
    }
}
