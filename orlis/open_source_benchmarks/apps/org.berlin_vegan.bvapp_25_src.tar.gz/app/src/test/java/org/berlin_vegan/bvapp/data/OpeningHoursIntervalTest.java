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


package org.berlin_vegan.bvapp.data;

import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class OpeningHoursIntervalTest {

    @Test
    public void testIsDateInInterval() throws Exception {
        final Date date = new GregorianCalendar(2015, GregorianCalendar.JULY, 26).getTime(); // sunday

        OpeningHoursInterval interval = new OpeningHoursInterval(5, 6, "9 - 10");// saturday - sunday
        assertTrue(interval.isDateInInterval(date));

        interval = new OpeningHoursInterval(6, "9 - 10");// only sunday
        assertTrue(interval.isDateInInterval(date));

        interval = new OpeningHoursInterval(0, 2, "9 - 10");// monday - wednesday
        assertFalse(interval.isDateInInterval(date));
    }

    @Test
    public void testNumberOfDays() throws Exception {
        OpeningHoursInterval interval = new OpeningHoursInterval(5, 6, "9 - 10");// saturday - sunday
        assertEquals(2,interval.getNumberOfDays());

        interval = new OpeningHoursInterval(0, 6, "9 - 10");// monday - sunday
        assertEquals(7,interval.getNumberOfDays());

        interval = new OpeningHoursInterval(0, "9 - 10");// monday
        assertEquals(1,interval.getNumberOfDays());
    }
}