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
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.github.notizklotz.derbunddownloader.settings.Settings;

import org.mockito.Mockito;

public class AutomaticIssueDownloadAlarmManagerTest extends AndroidTestCase {

    private AutomaticIssueDownloadAlarmManager automaticIssueDownloadAlarmManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        automaticIssueDownloadAlarmManager = AutomaticIssueDownloadAlarmManager_.getInstance_(getContext());
        automaticIssueDownloadAlarmManager.alarmManager = Mockito.mock(AlarmManager.class);
    }

    public void testUpdateAlarmAutoDownloadDisabled() throws Exception {
        //Prepare
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Settings.KEY_AUTO_DOWNLOAD_ENABLED, false).apply();

        //Execute
        automaticIssueDownloadAlarmManager.updateAlarm();

        //Test
        Mockito.verify(automaticIssueDownloadAlarmManager.alarmManager).cancel(Mockito.<PendingIntent>any());
        assertNull(Settings.getNextWakeup(getContext()));
    }

    public void testUpdateAlarmAutoDownloadEnabledButNoTimeGiven() throws Exception {
        //Prepare
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Settings.KEY_AUTO_DOWNLOAD_ENABLED, true).apply();

        //Execute
        try {
            automaticIssueDownloadAlarmManager.updateAlarm();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            //Expected
        }
    }
}