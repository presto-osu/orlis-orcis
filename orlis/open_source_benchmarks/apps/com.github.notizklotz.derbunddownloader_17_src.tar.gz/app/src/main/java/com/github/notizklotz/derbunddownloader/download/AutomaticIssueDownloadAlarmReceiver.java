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

package com.github.notizklotz.derbunddownloader.download;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.github.notizklotz.derbunddownloader.common.DateHandlingUtils;
import com.github.notizklotz.derbunddownloader.settings.Settings;

import java.util.Calendar;
import java.util.Date;

/**
 * Triggered by an alarm to automatically download the issue of today.
 */
public class AutomaticIssueDownloadAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String LOG_TAG = AutomaticIssueDownloadAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "I woke up this morning and got ready to start the service");
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        updateLastWakeupTimestamp(sharedPref);
        scheduleNextWakeup(context);
        callDownloadService(context);
    }

    private void scheduleNextWakeup(Context context) {
        AutomaticIssueDownloadAlarmManager_.getInstance_(context).updateAlarm();
    }

    private void callDownloadService(Context context) {
        final Calendar c = DateHandlingUtils.createServerCalendar();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        startWakefulService(context, IssueDownloadService_.intent(context).downloadIssue(day, month, year).get());
    }

    private void updateLastWakeupTimestamp(SharedPreferences sharedPref) {
        sharedPref.edit().putString(Settings.KEY_LAST_WAKEUP, DateHandlingUtils.toFullStringDefaultTimezone(new Date())).apply();
    }
}
