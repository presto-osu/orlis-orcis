/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp;

import com.google.gson.reflect.TypeToken;

import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;
import org.berlin_vegan.bvapp.data.GastroLocation;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.data.OpeningHoursInterval;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GastroLocationTest {

    private static final String GASTRO_LOCATIONS_JSON = "GastroLocations.json";
    private GastroLocation mLocation;
    private GastroLocation mLocationAlwaysClosed;

    @Before
    public void setUp() throws Exception {
        mLocation = new GastroLocation();
        mLocation.setOtMon("9 - 1");
        mLocation.setOtTue("9 - 1");
        mLocation.setOtWed("");
        mLocation.setOtThu("7 - 19");
        mLocation.setOtFri("10 - 14");
        mLocation.setOtSat("10 - 22");
        mLocation.setOtSun("10 - 22");

        mLocationAlwaysClosed = new GastroLocation();
        mLocationAlwaysClosed.setOtMon("");
        mLocationAlwaysClosed.setOtTue("");
        mLocationAlwaysClosed.setOtWed("");
        mLocationAlwaysClosed.setOtThu("");
        mLocationAlwaysClosed.setOtFri("");
        mLocationAlwaysClosed.setOtSat("");
        mLocationAlwaysClosed.setOtSun("");
    }

    @Test
    public void testParseLocationsWithGSON() throws Exception {
        final InputStream inputStream = getClass().getResourceAsStream(GASTRO_LOCATIONS_JSON);
        final List<Location> locationList = LocationsOverviewActivity.createList(inputStream, new TypeToken<ArrayList<GastroLocation>>() {
        }.getType());
        final int numLocations = locationList.size();

        assertTrue(numLocations > 300);

        final GastroLocation location = (GastroLocation)locationList.get(3);
        final String name = location.getName();
        final int numPictures = location.getPictures().size();

        assertEquals("Amaranth", name);
        assertEquals(2, numPictures);
    }

    @Test
    public void testCondensedOpeningHours() throws Exception {
        final List<OpeningHoursInterval> openingHours = mLocation.getCondensedOpeningHours();
        assertEquals(5, openingHours.size());
    }

    @Test
    public void testCondensedOpeningHoursCompleteClosed() throws Exception {

        final List<OpeningHoursInterval> openingHours = mLocationAlwaysClosed.getCondensedOpeningHours();
        assertEquals(1, openingHours.size());
    }

    @Test
    public void testIsOpenLocationAlwaysClosed() throws Exception {
        assertFalse(mLocationAlwaysClosed.isOpen(Calendar.getInstance().getTime()));
    }

    @Test
    public void testIsOpen() throws Exception {
        Date date = new GregorianCalendar(2015, 6, 27, 20, 32).getTime();// monday 20:32, 27 July 2015
        assertTrue(mLocation.isOpen(date));

        date = new GregorianCalendar(2015, 6, 28, 0, 32).getTime();// tuesday 0:32, after midnight, so take the opening hours from "monday"
        assertTrue(mLocation.isOpen(date));

        date = new GregorianCalendar(2015, 6, 29, 1, 32).getTime();// wednesday
        assertFalse(mLocation.isOpen(date));

        date = new GregorianCalendar(2015, 4, 1, 21, 0).getTime();// friday, but as 1 may is public holiday, take opening hours from sunday
        assertTrue(mLocation.isOpen(date));

    }

    @Test
    // test for ISSUE #30
    public void testIsOpenForLocationAlwaysOpen() throws Exception {
        final GastroLocation location = new GastroLocation();
        location.setOtMon("0 - 24");
        Date date = new GregorianCalendar(2015, 7, 10, 20, 32).getTime();// monday 20:32, 10 August 2015
        assertTrue(location.isOpen(date));
    }
}