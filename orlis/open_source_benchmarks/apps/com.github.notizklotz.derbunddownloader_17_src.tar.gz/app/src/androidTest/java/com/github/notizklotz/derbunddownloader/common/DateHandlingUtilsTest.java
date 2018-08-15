/*
 * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
 * Copyright (C) 2013 Adrian Gygax
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see {http://www.gnu.org/licenses/}.
 */

package com.github.notizklotz.derbunddownloader.common;

import android.test.AndroidTestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateHandlingUtilsTest extends AndroidTestCase {

    public void testToFullStringDefaultTimezone() {
        assertEquals("Unexpected device timezone", "America/New_York", TimeZone.getDefault().getID());
        assertEquals("31.12.1969 19:00:00 GMT-05:00", DateHandlingUtils.toFullStringDefaultTimezone(new Date(0)));
    }

    public void testCreateServerCalendar() {
        Calendar serverCalendar = DateHandlingUtils.createServerCalendar();
        assertEquals("Europe/Zurich", serverCalendar.getTimeZone().getID());
        assertEquals(Calendar.MONDAY, serverCalendar.getFirstDayOfWeek());
    }
}
