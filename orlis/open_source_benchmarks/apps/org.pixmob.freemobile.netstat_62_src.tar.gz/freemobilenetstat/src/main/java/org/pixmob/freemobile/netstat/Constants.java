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
package org.pixmob.freemobile.netstat;

import android.content.SharedPreferences;

/**
 * Application constants.
 * @author Pixmob
 */
public final class Constants {
    /**
     * Logging tag.
     */
    public static final String TAG = "FreeMobileNetstat";

    /**
     * Global {@link SharedPreferences} name.
     */
    public static final String SP_NAME = "netstat";
    /**
     * {@link SharedPreferences} key: start the monitor service at boot.
     */
    public static final String SP_KEY_ENABLE_AT_BOOT = "pref_enable_at_boot";
    /**
     * {@link SharedPreferences} key: play a sound when the mobile operator is
     * updated, on Free Mobile network.
     */
    public static final String SP_KEY_STAT_NOTIF_SOUND_FREE_MOBILE = "pref_notif_sound_free_mobile";
    /**
     * {@link SharedPreferences} key: play a sound when the mobile operator is
     * updated, on Orange network.
     */
    public static final String SP_KEY_STAT_NOTIF_SOUND_ORANGE = "pref_notif_sound_orange";
    /**
     * {@link SharedPreferences} key: play a sound when the mobile operator is
     * updated, on 4G (Free) network.
     */
    public static final String SP_KEY_STAT_NOTIF_SOUND_4G = "pref_notif_sound_4g";
    /**
     * {@link SharedPreferences} key: play a sound when the mobile operator is
     * updated, on Femtocell Free network.
     */
    public static final String SP_KEY_STAT_NOTIF_SOUND_FEMTO = "pref_notif_sound_femto";
    /**
     * {@link SharedPreferences} key: enable a workaround for KitKat START_STICKY bug.
     */
    public static final String SP_KEY_ENABLE_AUTO_RESTART_SERVICE = "pref_enable_auto_restart_service";
    /**
     * {@link SharedPreferences} key: enable a workaround for OnePlusTwo not update notification.
     */
    public static final String SP_KEY_ENABLE_AUTO_SEND_PHONE_LISTENER_EVENTS = "pref_enable_auto_send_phone_listener_events";
    /**
     * {@link SharedPreferences} key: set the time interval for displayed data.
     */
    public static final String SP_KEY_TIME_INTERVAL = "pref_time_interval";
    /**
     * {@link SharedPreferences} key: upload statistics.
     */
    public static final String SP_KEY_UPLOAD_STATS = "pref_upload_stats";
    /**
     * {@link SharedPreferences} key: set the notification action.
     */
    public static final String SP_KEY_NOTIF_ACTION = "pref_notif_action";
    /**
     * {@link SharedPreferences} key: set the theme.
     */
    public static final String SP_KEY_THEME = "pref_theme";
    /**
     * {@link SharedPreferences} key: set whether notification actions should be
     * displayed with Jelly Bean.
     */
    public static final String SP_KEY_ENABLE_NOTIF_ACTIONS = "pref_enable_notif_actions";
    /**
     * {@link SharedPreferences} key: detect manual mode selection when starting the application
     */
    public static final String SP_KEY_MANUAL_MODE_DETECTION_ENABLED = "pref_manual_mode_detection_enabled";
    /**
     * {@link SharedPreferences} key: the one plus two incompatibility message has been dismissed
     */
    public static final String SP_KEY_ONE_PLUS_TWO_MESSAGE_SEEN = "pref_one_plus_two_message_seen";
    /**
     * Notification action: open network operator settings.
     */
    public static final String NOTIF_ACTION_NETWORK_OPERATOR_SETTINGS = "network_operator_settings";
    /**
     * Notification action: open statistics.
     */
    public static final String NOTIF_ACTION_STATISTICS = "statistics";
    /**
     * Time interval value: show data since the device boot time.
     */
    public static final int INTERVAL_SINCE_BOOT = 0;
    /**
     * Time interval value: show data from one day.
     */
    public static final int INTERVAL_ONE_DAY = 1;
    /**
     * Time interval value: show data from one week.
     */
    public static final int INTERVAL_ONE_WEEK = 2;
    /**
     * Time interval value: show data from one month.
     */
    public static final int INTERVAL_ONE_MONTH = 3;
    /**
     * Notification intent action.
     */
    public static final String ACTION_NOTIFICATION = "org.pixmob.freemobile.netstat.notif";
    /**
     * Default theme.
     */
    public static final String THEME_COLOR = "color";
    /**
     * Black & white theme.
     */
    public static final String THEME_DEFAULT = "bw";
    /**
     * Pie theme.
     */
    public static final String THEME_PIE = "pie";

    private Constants() {
    }
}
