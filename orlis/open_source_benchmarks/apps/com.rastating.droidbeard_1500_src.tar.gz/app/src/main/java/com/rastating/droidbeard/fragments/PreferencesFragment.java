/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rastating.droidbeard.MainActivity;
import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;
import com.rastating.droidbeard.net.HttpClientManager;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    MainActivity mMainActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preference executePreference;

        Preferences droidbeardPreferences = new Preferences(getActivity());
        if (!droidbeardPreferences.getSelectedProfileName().equals(Preferences.DEFAULT_PROFILE_NAME)) {
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(droidbeardPreferences.getSelectedProfileName());
        }

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences preferences = droidbeardPreferences.getSharedPreferences();
        EditTextPreference preference = (EditTextPreference) findPreference("address");
        String value = preferences.getString("address", null);
        if (value != null && !value.equals("")) {
            preference.setSummary(value);
        }

        preference = (EditTextPreference) findPreference("port");
        value = preferences.getString("port", null);
        if (value != null && !value.equals("")) {
            preference.setSummary(value);
        }

        preference = (EditTextPreference) findPreference("extension_path");
        value = preferences.getString("extension_path", null);
        if (value != null && !value.equals("")) {
            preference.setSummary(value);
        }

        preference = (EditTextPreference) findPreference("api_key");
        value = preferences.getString("api_key", null);
        if (value != null && !value.equals("")) {
            preference.setSummary(value);
        }

        preference = (EditTextPreference) findPreference("http_username");
        value = preferences.getString("http_username", null);
        if (value != null && !value.equals("")) {
            preference.setSummary(value);
        }

        preference = (EditTextPreference) findPreference("http_password");
        value = preferences.getString("http_password", null);
        if (value != null && !value.equals("")) {
            int n = value.length();
            if (n > 0) {
                preference.setSummary(String.format(String.format("%%0%dd", n), 0).replace("0", "*"));
            }
            else {
                preference.setSummary("");
            }
        }

        /*executePreference = findPreference("shutdown");
        executePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                shutdownSickbeard();
                return false;
            }
        });

        executePreference = findPreference("restart");
        executePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restartSickbeard(true);
                return false;
            }
        });

        executePreference = findPreference("about");
        executePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return false;
            }
        });*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preferences, container, false);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (mMainActivity != null) {
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mMainActivity);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        if (mMainActivity != null) {
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mMainActivity);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            if (key.equals(Preferences.HTTP_PASSWORD)) {
                int n = editTextPreference.getText().length();
                if (n > 0) {
                    preference.setSummary(String.format(String.format("%%0%dd", n), 0).replace("0", "*"));
                }
                else {
                    preference.setSummary("");
                }
            }
            else {
                preference.setSummary(editTextPreference.getText());
            }
        }

        HttpClientManager.INSTANCE.invalidateClient();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.setCurrentFragment(this);
            mMainActivity = mainActivity;
        }
    }
}
