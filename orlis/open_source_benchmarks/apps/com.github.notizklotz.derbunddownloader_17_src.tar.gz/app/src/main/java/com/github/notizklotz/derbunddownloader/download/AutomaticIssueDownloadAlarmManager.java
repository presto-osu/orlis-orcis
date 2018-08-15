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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.github.notizklotz.derbunddownloader.settings.Settings;
import com.github.notizklotz.derbunddownloader.settings.TimePickerPreference;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.springframework.util.StringUtils;

import java.util.Calendar;

@EBean
public class AutomaticIssueDownloadAlarmManager {

    @SystemService
    AlarmManager alarmManager;

    @RootContext
    Context context;

    public void updateAlarm() {
        //Update enforces reusing of an existing PendingIntent instance so AlarmManager.cancel(pi)
        //actually cancels the alarm. FLAG_CANCEL_CURRENT cancels the PendingIndent but then there's no
        //way to cancel the alarm programmatically. However, the Intent won't be executed anyway because
        //the fired alarm can't executed the cancelled PendingIntent.
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, AutomaticIssueDownloadAlarmReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        cancelAlarm(pendingIntent, sharedPreferences);
        if (Settings.isAutoDownloadEnabled(context)) {
            registerAlarm(pendingIntent);
        }
    }

    private void registerAlarm(PendingIntent pendingIntent) {
        String autoDownloadTime = Settings.getAutoDownloadTime(context);
        if (!StringUtils.hasText(autoDownloadTime)) {
            throw new IllegalStateException("AutoDownloadTime is not set");
        }

        final Integer[] hourMinute = TimePickerPreference.toHourMinuteIntegers(autoDownloadTime);
        final Calendar nextAlarm = calculateNextAlarm(hourMinute[0], hourMinute[1]);

        // Make sure wakeup trigger is exact across all API versions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), pendingIntent);
        }

        Settings.updateNextWakeup(context, nextAlarm.getTime());
    }

    private Calendar calculateNextAlarm(int hourOfDay, int minute) {
        final Calendar nextAlarm = Calendar.getInstance();
        nextAlarm.clear();
        final Calendar now = Calendar.getInstance();

        nextAlarm.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

        //Make sure trigger is in the future
        if (now.after(nextAlarm)) {
            nextAlarm.add(Calendar.DAY_OF_MONTH, 1);
        }
        //Do not schedule Sundays as the newspaper is not issued on Sundays
        if ((nextAlarm.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
            nextAlarm.add(Calendar.DAY_OF_MONTH, 1);
        }
        return nextAlarm;
    }

    private void cancelAlarm(PendingIntent pendingIntent, SharedPreferences sharedPreferences) {
        alarmManager.cancel(pendingIntent);
        sharedPreferences.edit().remove(Settings.KEY_NEXT_WAKEUP).apply();
    }
}
