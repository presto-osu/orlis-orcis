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

import java.util.Comparator;

public class LocationComparator implements Comparator<GeoName> {

    public LocationComparator() {
        super();
    }

    @Override
    public int compare(GeoName geo0, GeoName geo1) {
        // West < East
        double lng0 = geo0.getLongitude();
        double lng1 = geo1.getLongitude();
        if (lng0 > lng1)
            return +1;
        if (lng0 < lng1)
            return -1;

        // North < South
        double lat0 = geo0.getLatitude();
        double lat1 = geo1.getLatitude();
        if (lat0 > lat1)
            return +1;
        if (lat0 < lat1)
            return -1;

        int alt0 = geo0.getElevation();
        int alt1 = geo1.getElevation();
        int alt = alt0 - alt1;
        if (alt != 0)
            return alt;

        String name0 = geo0.getName();
        String name1 = geo1.getName();
        int name = name0.compareTo(name1);
        if (name != 0)
            return name;

        return (int) (geo0.getGeoNameId() - geo1.getGeoNameId());
    }

}
