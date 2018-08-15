/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.util;

import android.content.Context;
import android.text.format.DateFormat;

import org.pixmob.freemobile.netstat.R;

/**
 * Date & time utilities.
 * @author Pixmob
 */
public final class DateUtils {
    private DateUtils() {
    }

    /**
     * Format a duration in milliseconds.
     */
    public static CharSequence formatDuration(long duration, Context context, CharSequence defaultValue) {
        if (duration <= 0) {
            return defaultValue;
        }
        final long ds = duration / 1000;
        final StringBuilder buf = new StringBuilder(32);
        if (ds < 60) {
            buf.append(ds).append(context.getString(R.string.seconds));
        } else if (ds < 3600) {
            final long m = ds / 60;
            buf.append(m).append(context.getString(R.string.minutes));
        } else if (ds < 86400) {
            final long h = ds / 3600;
            buf.append(h).append(context.getString(R.string.hours));

            final long m = (ds - h * 3600) / 60;
            if (m != 0) {
                if (m < 10) {
                    buf.append("0");
                }
                buf.append(m);
            }
        } else {
            final long d = ds / 86400;
            buf.append(d).append(context.getString(R.string.days));

            final long h = (ds - d * 86400) / 3600;
            if (h != 0) {
                buf.append(" ").append(h).append(context.getString(R.string.hours));
            }

            final long m = (ds - d * 86400 - h * 3600) / 60;
            if (m != 0) {
                if (h == 0) {
                    buf.append(" ");
                } else if (m < 10) {
                    buf.append("0");
                }
                buf.append(m);
                if (h == 0) {
                    buf.append(context.getString(R.string.minutes));
                }
            }
        }

        return buf;
    }

    /**
     * Format a date.
     */
    public static CharSequence formatDate(long d) {
        return DateFormat.format("dd/MM/yyyy", d);
    }
}
