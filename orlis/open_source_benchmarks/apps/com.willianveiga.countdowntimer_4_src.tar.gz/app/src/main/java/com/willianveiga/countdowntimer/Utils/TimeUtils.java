/**
 * Copyright 2015 Willian Gustavo Veiga
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.willianveiga.countdowntimer.Utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    private static final int TIME_UNIT = 1;

    public static long toMilliseconds(int hours, int minutes, int seconds) {
        return (TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds));
    }

    public static long millisecondsToHours(long milliseconds) {
        return TimeUnit.MILLISECONDS.toHours(milliseconds);
    }

    public static long millisecondsToMinutes(long milliseconds) {
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds) % TimeUnit.HOURS.toMinutes(TIME_UNIT);
    }

    public static long millisecondsToSeconds(long milliseconds) {
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds) % TimeUnit.MINUTES.toSeconds(TIME_UNIT);
    }
}