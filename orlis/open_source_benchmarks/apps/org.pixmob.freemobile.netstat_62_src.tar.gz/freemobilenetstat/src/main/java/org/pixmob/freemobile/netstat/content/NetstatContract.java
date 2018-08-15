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
package org.pixmob.freemobile.netstat.content;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the content provider and applications.
 * @author Pixmob
 */
public class NetstatContract {
    /**
     * The authority for the content provider.
     */
    public static final String AUTHORITY = "org.pixmob.freemobile.netstat";
    
    protected static interface EventsColumns {
        String TIMESTAMP = "timestamp";
        String MOBILE_CONNECTED = "mobile_connected";
        String MOBILE_OPERATOR = "mobile_operator";
        String WIFI_CONNECTED = "wifi_connected";
        String BATTERY_LEVEL = "battery_level";
		String MOBILE_NETWORK_TYPE = "mobile_network_type";
        String SCREEN_ON = "screen_on";
        String POWER_ON = "power_on";
        String FEMTOCELL = "femtocell";
        String FIRST_INSERT = "first_insert";
    }
    
    /**
     * Table for events.
     * @author Pixmob
     */
    public static class Events implements BaseColumns, EventsColumns {
        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath("events").build();
        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/event";
        /**
         * The MIME type of a {@link #CONTENT_URI} providing a directory of
         * entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/event";
    }
}
