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

public class RegionComparator implements Comparator<CountryRegion> {

    public RegionComparator() {
        super();
    }

    @Override
    public int compare(CountryRegion region0, CountryRegion region1) {
        String name0 = region0.getCountryCode();
        String name1 = region1.getCountryCode();
        int name = name0.compareTo(name1);
        if (name != 0)
            return name;

        int npoints0 = region0.npoints;
        int npoints1 = region1.npoints;
        int npoints = npoints0 - npoints1;
        if (npoints != 0)
            return npoints;

        int x0;
        int x1;
        int x;
        for (int i = 0; i < npoints0; i++) {
            x0 = region0.xpoints[i];
            x1 = region1.xpoints[i];
            x = x0 - x1;
            if (x != 0)
                return npoints;
        }

        int y0;
        int y1;
        int y;
        for (int i = 0; i < npoints0; i++) {
            y0 = region0.ypoints[i];
            y1 = region1.ypoints[i];
            y = y0 - y1;
            if (y != 0)
                return npoints;
        }

        return 0;
    }

}
