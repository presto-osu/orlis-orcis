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


package org.berlin_vegan.bvapp.helpers;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;


public class DateUtilTest {

    @Test
    public void testIsPublicHoliday() throws Exception {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(2015, 0, 1);
        assertTrue(DateUtil.isPublicHoliday(calendar.getTime()));

        calendar.set(2016, 2, 28); //28 March is "Ostermontag"
        assertTrue(DateUtil.isPublicHoliday(calendar.getTime()));

        calendar.set(2015, 5, 5); // 5 June
        assertFalse(DateUtil.isPublicHoliday(calendar.getTime()));

    }
}