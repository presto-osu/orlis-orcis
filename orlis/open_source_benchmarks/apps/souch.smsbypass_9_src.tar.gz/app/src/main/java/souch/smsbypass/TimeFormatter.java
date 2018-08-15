/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
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
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

public class TimeFormatter
{
    public static final int SHORT_FORMAT = 0x00000001;
    public static final int FULL_FORMAT  = 0x00000002;

    public static String f(Context context, long timestamp, int flags)
    {
        Time timestampTime = new Time();
        timestampTime.set(timestamp);

        Time now = new Time();
        now.setToNow();

        int dateUtilFlags =
            DateUtils.FORMAT_NO_NOON_MIDNIGHT
            | DateUtils.FORMAT_ABBREV_ALL
            | DateUtils.FORMAT_CAP_AMPM;

        if (timestampTime.year != now.year)
            dateUtilFlags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        else if (timestampTime.yearDay != now.yearDay)
            dateUtilFlags |= DateUtils.FORMAT_SHOW_DATE;
        else
            dateUtilFlags |= DateUtils.FORMAT_SHOW_TIME;

        if ((flags & FULL_FORMAT) != 0)
            dateUtilFlags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

        return DateUtils.formatDateTime(context, timestamp, dateUtilFlags);
    }
}
