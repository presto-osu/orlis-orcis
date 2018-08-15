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
package net.sf.geonames;

import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 * Country region.
 *
 * @author Moshe
 */
public class CountryRegion extends Polygon {

    /** Factor to convert coordinate value to a fixed-point integer. */
    public static final double FACTOR_TO_INT = 1e+6;
    /**
     * Factor to convert coordinate value to a fixed-point integer for city
     * limits.
     */
    private static final double CITY_BOUNDARY = 1e+5;

    private final String countryCode;

    /**
     * Constructs a new region.
     */
    public CountryRegion(String countryCode) {
        super();
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Add a location.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     */
    public void addLocation(double latitude, double longitude) {
        int x = (int) (longitude * FACTOR_TO_INT);
        int y = (int) (latitude * FACTOR_TO_INT);
        addPoint(x, y);
    }

    /**
     * Find the main vertices that represent the border.
     *
     * @param vertexCount
     *         the number of vertices.
     * @return an array of indexes.
     */
    public int[] findMainVertices(final int vertexCount) {
        int[] indexes = new int[vertexCount];
        int r = 0;
        int n = npoints;

        // Find centre.
        long tx = 0;
        long ty = 0;
        for (int i = 0; i < n; i++) {
            tx += xpoints[i];
            ty += ypoints[i];
        }
        double cx = ((double) tx) / n;
        double cy = ((double) ty) / n;

        final double sweepAngle = (2f * Math.PI) / vertexCount;
        double angleStart = -(sweepAngle / 2f);
        double angleEnd;
        double x, y, a, d;
        int farIndex;
        double farDist;

        for (int v = 0; v < vertexCount; v++) {
            angleEnd = angleStart + sweepAngle;
            farDist = Double.MIN_VALUE;
            farIndex = -1;

            for (int i = 0; i < n; i++) {
                x = xpoints[i];
                y = ypoints[i];
                a = Math.atan2(y - cy, x - cx) + Math.PI;
                if ((angleStart <= a) && (a < angleEnd)) {
                    d = Point2D.distanceSq(cx, cy, x, y);
                    if (farDist < d) {
                        farDist = d;
                        farIndex = i;
                    }
                }
            }

            if (farIndex >= 0)
                indexes[r++] = farIndex;

            angleStart += sweepAngle;
        }

        if (r < vertexCount) {
            switch (r) {
                case 0:
                    addPoint((int) (xpoints[0] - CITY_BOUNDARY), (int) (ypoints[0] - CITY_BOUNDARY));
                    addPoint((int) (xpoints[0] + CITY_BOUNDARY), (int) (ypoints[0] + CITY_BOUNDARY));
                case 1:
                    addPoint((int) (xpoints[1] - CITY_BOUNDARY), (int) (ypoints[1] - CITY_BOUNDARY));
                    addPoint((int) (xpoints[1] + CITY_BOUNDARY), (int) (ypoints[1] + CITY_BOUNDARY));
                    return findMainVertices(vertexCount);
            }

            for (int i = r; i < vertexCount; i++)
                indexes[i] = -1;
        }

        return indexes;
    }
}
