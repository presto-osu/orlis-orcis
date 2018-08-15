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

package com.github.notizklotz.derbunddownloader.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import com.github.notizklotz.derbunddownloader.common.DateHandlingUtils;

import java.util.Date;

public class Settings {

    public static final String KEY_AUTO_DOWNLOAD_ENABLED = "auto_download_enabled";
    public static final String KEY_AUTO_DOWNLOAD_TIME = "auto_download_time";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_LAST_WAKEUP = "last_wakeup";
    public static final String KEY_NEXT_WAKEUP = "next_wakeup";
    private static final String KEY_WIFI_ONLY_ENABLED = "wifi_only";

    private Settings() {

    }

    public static String getUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.KEY_USERNAME, null);
    }

    public static String getPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.KEY_PASSWORD, null);
    }

    public static boolean isWifiOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.KEY_WIFI_ONLY_ENABLED, true);
    }

    public static boolean isAutoDownloadEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.KEY_AUTO_DOWNLOAD_ENABLED, false);
    }

    public static String getAutoDownloadTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.KEY_AUTO_DOWNLOAD_TIME, null);
    }

    public static String getNextWakeup(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.KEY_NEXT_WAKEUP, null);
    }

    public static void updateNextWakeup(Context context, Date nextWakeup) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Settings.KEY_NEXT_WAKEUP, DateHandlingUtils.toFullStringDefaultTimezone(nextWakeup)).apply();
    }
}
