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


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DateUtil {

    public static final List<String> FIXED_HOLIDAYS = Arrays.asList("1.1", "1.5", "3.10", "25.12", "26.12");
    public static final List<String> DYNAMIC_HOLIDAYS = Arrays.asList("25.3.2016", "28.3.2016", "5.5.2016", "16.5.2016", "14.4.2017", "17.4.2017", "25.5.2017", "05.6.2017");
    public static final int ONE_MINUTE_IN_MILLISECONDS = 60000;
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR;

    /**
     * return the current day of week, starting with monday
     */
    static public int getDayOfWeek(Date date) {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) { // set sunday to end
            dayOfWeek = 8;
        }
        dayOfWeek = dayOfWeek - 2;
        return dayOfWeek;
    }

    static public boolean isPublicHoliday(Date date) {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int year = calendar.get(Calendar.YEAR);

        String dateStr = String.valueOf(day) + "." + String.valueOf(month);
        String dateStrWithYear = dateStr + "." + String.valueOf(year);
        return FIXED_HOLIDAYS.contains(dateStr) || DYNAMIC_HOLIDAYS.contains(dateStrWithYear);
    }

    public static int inMinutes(int hours, int minutes) {
        return (hours * 60) + minutes;
    }

    public static Date addMinutesToDate(Date date, int minutes) {
        final long currentTime = date.getTime();
        return new Date(currentTime + (minutes * ONE_MINUTE_IN_MILLISECONDS));
    }

    /**
     * return a typical opening times string, for the input minutes
     * some example
     * minutes = 120 -> 2:00
     * minutes = 570 -> 9:30
     */
    static public String formatTimeFromMinutes(int minutes) {
        if (minutes == 0) {
            return "0";
        }
        int hours = minutes / MINUTES_PER_HOUR;
        if (hours == 24) { // set 24 to 0, its more common in germany
            hours = 0;
        }
        int restMinutes = minutes % MINUTES_PER_HOUR;
        if (restMinutes != 0) {
            return hours + ":" + String.format("%02d", restMinutes); // format minutes with 2 digits
        }
        return String.valueOf(hours);
    }
}
