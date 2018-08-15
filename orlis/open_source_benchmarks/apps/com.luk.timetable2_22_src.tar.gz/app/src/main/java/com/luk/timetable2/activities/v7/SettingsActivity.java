package com.luk.timetable2.activities.v7;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.luk.timetable2.R;
import com.luk.timetable2.Utils;
import com.luk.timetable2.activities.MainActivity;
import com.luk.timetable2.listeners.SettingsActivity.v7.NotificationsChangeListener;
import com.luk.timetable2.listeners.SettingsActivity.v7.NotificationsLengthChangeListener;
import com.luk.timetable2.listeners.SettingsActivity.v7.RestoreLessonsListener;
import com.luk.timetable2.listeners.SettingsActivity.v7.ThemeChangeListener;
import com.luk.timetable2.listeners.SettingsActivity.v7.URLChangeListener;

/**
 * Created by luk on 9/22/15.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getCurrentTheme(this));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, MainActivity.class));
                return true;
        }

        return false;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            this.addPreferencesFromResource(R.xml.preferences);

            bindPreferenceSummaryToValue(findPreference("school"));
            bindPreferenceSummaryToValue(findPreference("theme"));
            bindPreferenceSummaryToValue(findPreference("themeAccent"));
            bindPreferenceSummaryToValue(findPreference("notifications_vibrate_length"));
            bindPreferenceSummaryToValue(findPreference("notifications_vibrate_time"));

            findPreference("school").setOnPreferenceChangeListener(new URLChangeListener());
            findPreference("theme").setOnPreferenceChangeListener(
                    new ThemeChangeListener(getActivity()));
            findPreference("themeAccent").setOnPreferenceChangeListener(
                    new ThemeChangeListener(getActivity()));
            findPreference("notifications_vibrate").setOnPreferenceChangeListener(
                    new NotificationsChangeListener(getActivity()));
            findPreference("notifications_vibrate_length").setOnPreferenceChangeListener(
                    new NotificationsLengthChangeListener(getActivity()));
            findPreference("notifications_vibrate_time").setOnPreferenceChangeListener(
                    new NotificationsLengthChangeListener(getActivity()));
            findPreference("restore_lessons").setOnPreferenceClickListener(
                    new RestoreLessonsListener(getActivity()));

            if (!areNotificationsEnabled()) {
                findPreference("notifications_vibrate_length").setEnabled(false);
                findPreference("notifications_vibrate_time").setEnabled(false);
            }
        }

        private boolean areNotificationsEnabled() {
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());

            return sharedPref.getBoolean("notifications_vibrate", false);
        }

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object value) {
                        String stringValue = value.toString();

                        if (preference instanceof ListPreference) {
                            // For list preferences, look up the correct display value in
                            // the preference's 'entries' list.
                            ListPreference listPreference = (ListPreference) preference;
                            int index = listPreference.findIndexOfValue(stringValue);

                            // Set the summary to reflect the new value.
                            preference.setSummary(
                                    index >= 0
                                            ? listPreference.getEntries()[index]
                                            : null);
                        } else {
                            // For all other preferences, set the summary to the value's
                            // simple string representation.

                            if (value.toString().equals(""))
                                return false;

                            preference.setSummary(stringValue);
                        }
                        return true;
                    }
                };

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see #sBindPreferenceSummaryToValueListener
         */
        private static void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }
}
