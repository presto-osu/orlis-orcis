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
package org.pixmob.freemobile.netstat.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import org.pixmob.freemobile.netstat.MonitorService;
import org.pixmob.freemobile.netstat.R;
import org.pixmob.freemobile.netstat.feature.BackupManagerFeature;
import org.pixmob.freemobile.netstat.feature.Features;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.pixmob.freemobile.netstat.Constants.INTERVAL_ONE_DAY;
import static org.pixmob.freemobile.netstat.Constants.INTERVAL_ONE_MONTH;
import static org.pixmob.freemobile.netstat.Constants.INTERVAL_ONE_WEEK;
import static org.pixmob.freemobile.netstat.Constants.INTERVAL_SINCE_BOOT;
import static org.pixmob.freemobile.netstat.Constants.NOTIF_ACTION_NETWORK_OPERATOR_SETTINGS;
import static org.pixmob.freemobile.netstat.Constants.NOTIF_ACTION_STATISTICS;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_AUTO_RESTART_SERVICE;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_AUTO_SEND_PHONE_LISTENER_EVENTS;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_NOTIF_ACTIONS;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_NOTIF_ACTION;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_4G;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_FEMTO;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_FREE_MOBILE;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_ORANGE;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_THEME;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_TIME_INTERVAL;
import static org.pixmob.freemobile.netstat.Constants.SP_NAME;
import static org.pixmob.freemobile.netstat.Constants.TAG;
import static org.pixmob.freemobile.netstat.Constants.THEME_COLOR;
import static org.pixmob.freemobile.netstat.Constants.THEME_DEFAULT;
import static org.pixmob.freemobile.netstat.Constants.THEME_PIE;

/**
 * Application preferences screen.
 * @author Pixmob
 */
public class Preferences extends PreferenceActivity implements OnPreferenceClickListener,
        OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String SP_KEY_VERSION = "pref_version";
    private static final String SP_KEY_CHANGELOG = "pref_changelog";
    private static final String SP_KEY_LICENSE = "pref_license";
    private static final String SP_KEY_HOMESITE = "pref_homesite";
    private static final String SP_KEY_DISABLE_ALL_SOUNDS = "pref_disable_all_sounds";
    private static final String[] SOUND_NOTIFICATIONS_SP_KEYS = {
            SP_KEY_STAT_NOTIF_SOUND_FREE_MOBILE, SP_KEY_STAT_NOTIF_SOUND_ORANGE, SP_KEY_STAT_NOTIF_SOUND_4G,
            SP_KEY_STAT_NOTIF_SOUND_FEMTO
    };
    private final SparseArray<CharSequence> timeIntervals = new SparseArray<>(4);
    private final Map<String, String> notifActions = new HashMap<>(2);
    private final Map<String, Integer> themes = new HashMap<>(3);

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timeIntervals.clear();
        timeIntervals.append(INTERVAL_SINCE_BOOT, getString(R.string.interval_since_boot));
        timeIntervals.append(INTERVAL_ONE_DAY, getString(R.string.interval_one_day));
        timeIntervals.append(INTERVAL_ONE_WEEK, getString(R.string.interval_one_week));
        timeIntervals.append(INTERVAL_ONE_MONTH, getString(R.string.interval_one_month));

        themes.clear();
        themes.put(THEME_DEFAULT, R.string.theme_default);
        themes.put(THEME_COLOR, R.string.theme_color);
        themes.put(THEME_PIE, R.string.theme_pie);

        notifActions.clear();
        notifActions.put(NOTIF_ACTION_STATISTICS, getString(R.string.pref_notif_action_summary_stats));
        notifActions.put(NOTIF_ACTION_NETWORK_OPERATOR_SETTINGS,
                getString(R.string.pref_notif_action_summary_netop));

        final PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesMode(MODE_PRIVATE);
        pm.setSharedPreferencesName(SP_NAME);

        addPreferencesFromResource(R.xml.prefs);

        String version = "0";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot get application version", e);
        }

        Preference p = findPreference(SP_KEY_VERSION);
        p.setSummary(version);

        pm.findPreference(SP_KEY_VERSION).setOnPreferenceClickListener(this);
        pm.findPreference(SP_KEY_CHANGELOG).setOnPreferenceClickListener(this);
        pm.findPreference(SP_KEY_LICENSE).setOnPreferenceClickListener(this);
        pm.findPreference(SP_KEY_HOMESITE).setOnPreferenceClickListener(this);
        pm.findPreference(SP_KEY_DISABLE_ALL_SOUNDS).setOnPreferenceClickListener(this);

        pm.findPreference(SP_KEY_STAT_NOTIF_SOUND_FREE_MOBILE).setOnPreferenceChangeListener(this); // RingtonePreference does not trigger otherwise
        pm.findPreference(SP_KEY_STAT_NOTIF_SOUND_ORANGE).setOnPreferenceChangeListener(this); // RingtonePreference does not trigger otherwise
        pm.findPreference(SP_KEY_STAT_NOTIF_SOUND_4G).setOnPreferenceChangeListener(this); // RingtonePreference does not trigger otherwise
        pm.findPreference(SP_KEY_STAT_NOTIF_SOUND_FEMTO).setOnPreferenceChangeListener(this); // RingtonePreference does not trigger otherwise

        final IntListPreference lp = (IntListPreference) pm.findPreference(SP_KEY_TIME_INTERVAL);
        lp.setEntries(getValues(timeIntervals));
        lp.setEntryValues(getKeys(timeIntervals));

        final int currentInterval = pm.getSharedPreferences().getInt(SP_KEY_TIME_INTERVAL, 0);
        lp.setSummary(timeIntervals.get(currentInterval));
        lp.setValue(currentInterval);
        lp.setOnPreferenceChangeListener(this);

        final String currentTheme = pm.getSharedPreferences().getString(SP_KEY_THEME, THEME_DEFAULT);
        final ListPreference themePref = (ListPreference) pm.findPreference(SP_KEY_THEME);
        Integer themePrefSummary = themes.get(currentTheme);
        if (themePrefSummary == null) {
            themePrefSummary = themes.get(THEME_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        	// Colors no longer supported in Lollipop notification bar
        	themePref.setEntries(R.array.lollipop_theme_titles);
        	themePref.setEntryValues(R.array.lollipop_theme_values);

           // Set default theme if we are using colors (after android upgrade)
           if (THEME_COLOR.equals(currentTheme)) {
               pm.getSharedPreferences().edit().putString(SP_KEY_THEME, THEME_DEFAULT).apply(); //save in the shared pref
               themePrefSummary = themes.get(THEME_DEFAULT); //also update the widget
               themePref.setValueIndex(0);
           }
        }
        themePref.setSummary(themePrefSummary);
        themePref.setOnPreferenceChangeListener(this);

        final String currentNotifAction = pm.getSharedPreferences().getString(SP_KEY_NOTIF_ACTION,
                NOTIF_ACTION_STATISTICS);
        p = findPreference(SP_KEY_NOTIF_ACTION);
        p.setSummary(notifActions.get(currentNotifAction));

        pm.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Disable notification customization on Jelly Bean, as we are using
            // notification actions.
            final PreferenceGroup g = (PreferenceGroup) pm.findPreference("notif_category");
            g.removePreference(pm.findPreference(SP_KEY_NOTIF_ACTION));
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // Disable notification actions display preference before Jelly
            // Bean.
            final PreferenceGroup g = (PreferenceGroup) pm.findPreference("notif_category");
            g.removePreference(pm.findPreference(SP_KEY_ENABLE_NOTIF_ACTIONS));
        }

        if (!Arrays.asList(MonitorService.ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE).contains(Build.VERSION.RELEASE)) {
        	// Disable auto restart service workaround if not on KitKat nor Jelly Bean
            final PreferenceGroup g = (PreferenceGroup) pm.findPreference("notif_category");
        	g.removePreference(pm.findPreference(SP_KEY_ENABLE_AUTO_RESTART_SERVICE));
        }


        if (!(MonitorService.ONE_PLUS_TWO_MANUFACTURER.equals(Build.MANUFACTURER) && MonitorService.ONE_PLUS_TWO_MODEL.equals(Build.MODEL))) {
            // Disable auto refresh TelephonyListener workaround if not OnePlusTwo
            final PreferenceGroup g = (PreferenceGroup) pm.findPreference("notif_category");
            g.removePreference(pm.findPreference(SP_KEY_ENABLE_AUTO_SEND_PHONE_LISTENER_EVENTS));
        }
    }

    @Override
    protected void onDestroy() {
        findPreference(SP_KEY_VERSION).setOnPreferenceClickListener(null);
        findPreference(SP_KEY_CHANGELOG).setOnPreferenceClickListener(null);
        findPreference(SP_KEY_LICENSE).setOnPreferenceClickListener(null);
        findPreference(SP_KEY_HOMESITE).setOnPreferenceClickListener(null);
        findPreference(SP_KEY_TIME_INTERVAL).setOnPreferenceChangeListener(null);
        findPreference(SP_KEY_THEME).setOnPreferenceChangeListener(null);
        findPreference(SP_KEY_DISABLE_ALL_SOUNDS).setOnPreferenceChangeListener(null);

        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceChange(Preference p, Object value) {
        final String k = p.getKey();

        switch (k) {
            case SP_KEY_TIME_INTERVAL:
                final IntListPreference lp = (IntListPreference) p;
                final int intValue = Integer.parseInt((String) value);
                lp.setSummary(timeIntervals.get(intValue));
                break;
            case SP_KEY_THEME:
                Integer themePrefSummary = themes.get(value);
                if (themePrefSummary == null) {
                    themePrefSummary = themes.get(THEME_DEFAULT);
                }
                p.setSummary(themePrefSummary);
                break;
        }

        for (final String soundNotification : SOUND_NOTIFICATIONS_SP_KEYS) {
            if (soundNotification.equals(k) && "".equals(value)) {
                getPreferenceManager().getSharedPreferences().edit().putString(soundNotification, null).commit();
                return false;
            }
        }

        return true;
    }

    private static <T> int[] getKeys(SparseArray<T> a) {
        final int s = a.size();
        final int[] keys = new int[s];
        for (int i = 0; i < s; ++i) {
            keys[i] = a.keyAt(i);
        }
        return keys;
    }

    private static CharSequence[] getValues(SparseArray<CharSequence> a) {
        final int s = a.size();
        final CharSequence[] values = new CharSequence[s];
        for (int i = 0; i < s; ++i) {
            values[i] = a.get(i);
        }
        return values;
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        final String k = p.getKey();
        switch (k) {
            case SP_KEY_CHANGELOG:
                openDocument("CHANGELOG.html");
                break;
            case SP_KEY_LICENSE:
                openDocument("LICENSE.html");
                break;
            case SP_KEY_VERSION:
                final String appName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }
                break;
            case SP_KEY_HOMESITE:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://fm.netstat.fr")));
                break;
            case SP_KEY_DISABLE_ALL_SOUNDS:
                for (final String soundNotification : SOUND_NOTIFICATIONS_SP_KEYS) {
                    getPreferenceManager().getSharedPreferences().edit().putString(soundNotification, null).commit();
                }
                Toast.makeText(this, getResources().getString(R.string.all_alerts_removed), Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    private void openDocument(String path) {
        startActivity(new Intent(this, DocumentBrowser.class)
                .putExtra(DocumentBrowser.INTENT_EXTRA_URL, path).putExtra(
                        DocumentBrowser.INTENT_EXTRA_HIDE_BUTTON_BAR, true));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SP_KEY_NOTIF_ACTION.equals(key)) {
            final String notifAction = sharedPreferences.getString(key, null);
            findPreference(key).setSummary(notifActions.get(notifAction));
        }

        Log.d(TAG, "Application preferences updated: " + "calling BackupManager.dataChanged()");
        Features.getFeature(BackupManagerFeature.class).dataChanged(this);
    }
}
