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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Location that is partially stored in the local database.
 *
 * @author Moshe Waisberg
 */
public class ZmanimLocation extends Location {

    /** Double subtraction error. */
    private static final double EPSILON = 1e-6;

    private long id;

    /**
     * Constructs a new location.
     *
     * @param provider
     *         the name of the provider that generated this location.
     */
    public ZmanimLocation(String provider) {
        super(provider);
    }

    /**
     * Construct a new location that is copied from an existing one.
     *
     * @param location
     *         the source location.
     */
    public ZmanimLocation(Location location) {
        super(location);
    }

    /**
     * Get the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id
     *         the id.
     */
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(id);
    }

    public static final Parcelable.Creator<ZmanimLocation> CREATOR = new Parcelable.Creator<ZmanimLocation>() {
        @Override
        public ZmanimLocation createFromParcel(Parcel source) {
            Location l = Location.CREATOR.createFromParcel(source);
            ZmanimLocation zl = new ZmanimLocation(l);
            zl.id = source.readLong();
            return zl;
        }

        @Override
        public ZmanimLocation[] newArray(int size) {
            return new ZmanimLocation[size];
        }
    };

    /**
     * Compare two locations by latitude and longitude only.
     *
     * @param l1
     *         the first location.
     * @param l2
     *         the second location.
     * @return the comparison as per {@link Comparable}.
     */
    public static int compareTo(Location l1, Location l2) {
        if (l1 == l2)
            return 0;
        if (l1 == null) {
            if (l2 == null)
                return 0;
            return -1;
        }
        if (l2 == null) {
            return 1;
        }

        double lat1 = l1.getLatitude();
        double lat2 = l2.getLatitude();
        double latD = lat1 - lat2;
        if (latD >= EPSILON)
            return 1;
        if (latD <= -EPSILON)
            return -1;

        double lng1 = l1.getLongitude();
        double lng2 = l2.getLongitude();
        double lngD = lng1 - lng2;
        if (lngD >= EPSILON)
            return 1;
        if (lngD <= -EPSILON)
            return -1;

        return 0;
    }

    /**
     * Compare two locations by latitude and then longitude, and then altitude,
     * and then time.
     *
     * @param l1
     *         the first location.
     * @param l2
     *         the second location.
     * @return the comparison as per {@link Comparable}.
     */
    public static int compareAll(Location l1, Location l2) {
        if (l1 == l2)
            return 0;
        if (l1 == null) {
            if (l2 == null)
                return 0;
            return -1;
        }
        if (l2 == null) {
            return 1;
        }

        double lat1 = l1.getLatitude();
        double lat2 = l2.getLatitude();
        double latD = lat1 - lat2;
        if (latD >= EPSILON)
            return 1;
        if (latD <= -EPSILON)
            return -1;

        double lng1 = l1.getLongitude();
        double lng2 = l2.getLongitude();
        double lngD = lng1 - lng2;
        if (lngD >= EPSILON)
            return 1;
        if (lngD <= -EPSILON)
            return -1;

        double ele1 = l1.hasAltitude() ? l1.getAltitude() : 0;
        double ele2 = l2.hasAltitude() ? l2.getAltitude() : 0;
        double eleD = ele1 - ele2;
        if (eleD >= EPSILON)
            return 1;
        if (eleD <= -EPSILON)
            return -1;

        long t1 = l1.getTime();
        long t2 = l2.getTime();
        return (t1 > t2) ? 1 : (t1 < t2 ? -1 : 0);
    }
}
