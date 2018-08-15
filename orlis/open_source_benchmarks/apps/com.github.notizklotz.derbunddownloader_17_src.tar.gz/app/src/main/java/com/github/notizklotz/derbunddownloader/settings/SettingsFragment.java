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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.notizklotz.derbunddownloader.R;
import com.github.notizklotz.derbunddownloader.download.AutomaticIssueDownloadAlarmManager_;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        updateSummaries(getPreferenceScreen().getSharedPreferences());
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        assert sharedPreferences != null;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        assert sharedPreferences != null;
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummaries(sharedPreferences);

        if (Settings.KEY_AUTO_DOWNLOAD_ENABLED.equals(key) || Settings.KEY_AUTO_DOWNLOAD_TIME.equals(key)) {
            AutomaticIssueDownloadAlarmManager_.getInstance_(this.getActivity()).updateAlarm();
        }
    }

    private void updateSummaries(SharedPreferences sharedPreferences) {
        updateAutoDownloadTime(sharedPreferences);
        updateUsername(sharedPreferences);
        updateLogin(sharedPreferences);
        updateLastWakeup(sharedPreferences);
        updateNextWakeup(sharedPreferences);
    }

    private void updateNextWakeup(SharedPreferences sharedPreferences) {
        getPreferenceScreen().findPreference(Settings.KEY_NEXT_WAKEUP).setSummary(sharedPreferences.getString(Settings.KEY_NEXT_WAKEUP, this.getString(R.string.last_wakeup_never)));
    }

    private void updateLastWakeup(SharedPreferences sharedPreferences) {
        getPreferenceScreen().findPreference(Settings.KEY_LAST_WAKEUP).setSummary(sharedPreferences.getString(Settings.KEY_LAST_WAKEUP,
                this.getString(R.string.last_wakeup_never)));
    }

    private void updateLogin(SharedPreferences sharedPreferences) {
        Preference passwordPreference = getPreferenceScreen().findPreference(Settings.KEY_PASSWORD);
        assert passwordPreference != null;
        if (sharedPreferences.contains(Settings.KEY_PASSWORD)) {
            passwordPreference.setSummary("****");
        } else {
            passwordPreference.setSummary(this.getString(R.string.password_summary));
        }
    }

    private void updateUsername(SharedPreferences sharedPreferences) {
        Preference usernamePreference = getPreferenceScreen().findPreference(Settings.KEY_USERNAME);
        assert usernamePreference != null;
        usernamePreference.setSummary(sharedPreferences.getString(Settings.KEY_USERNAME, this.getString(R.string.username_summary)));
    }

    private void updateAutoDownloadTime(SharedPreferences sharedPreferences) {
        String auto_download_time = sharedPreferences.getString(Settings.KEY_AUTO_DOWNLOAD_TIME, null);
        Preference auto_download_time_preference = getPreferenceScreen().findPreference(Settings.KEY_AUTO_DOWNLOAD_TIME);
        if (auto_download_time_preference != null) {
            auto_download_time_preference.setSummary(auto_download_time);
        }
    }
}
