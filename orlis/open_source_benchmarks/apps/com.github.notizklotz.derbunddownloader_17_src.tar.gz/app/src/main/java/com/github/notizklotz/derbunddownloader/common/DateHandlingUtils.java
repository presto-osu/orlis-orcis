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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHandlingUtils {

    private static final String FORMAT_HH_MM = "%02d:%02d";
    private static final Locale SERVER_LOCALE = new Locale("de", "CH");
    private static final TimeZone SERVER_TIMEZONE = TimeZone.getTimeZone("Europe/Zurich");

    private DateHandlingUtils() {
    }

    /**
     * Formats time as in this example: "12:59".
     */
    public static String toHH_MMString(int hours, int minutes) {
        return String.format(FORMAT_HH_MM, hours, minutes);
    }

    /**
     * Creates a formatted full date/time string as in this example: "31.12.2014 12:59:59 GMT+2".
     * The device's current default timezone is used for timezone calculations.
     */
    public static String toFullStringDefaultTimezone(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss ZZZZ", SERVER_LOCALE).format(date);
    }

    /**
     * Creates a {@link java.util.Calendar} instance using the server's timezone and locale (Switzerland).
     */
    public static Calendar createServerCalendar() {
        return Calendar.getInstance(SERVER_TIMEZONE, SERVER_LOCALE);
    }
}
