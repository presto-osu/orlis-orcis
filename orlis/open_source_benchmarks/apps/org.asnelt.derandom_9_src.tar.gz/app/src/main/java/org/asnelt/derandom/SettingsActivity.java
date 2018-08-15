/*
 * Copyright (C) 2015 Arno Onken
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

package org.asnelt.derandom;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A PreferenceActivity that presents a set of application settings.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    /** Key to identify the auto-detect preference. */
    public static final String KEY_PREF_AUTO_DETECT = "pref_auto_detect";
    /** Key to identify the predictions length preference. */
    public static final String KEY_PREF_PREDICTION_LENGTH = "pref_prediction_length";
    /** Key to identify the colored past preference. */
    public static final String KEY_PREF_COLORED_PAST = "pref_colored_past";
    /** Key to identify the history length preference. */
    public static final String KEY_PREF_HISTORY_LENGTH = "pref_history_length";
    /** Key to identify the parameter base preference. */
    public static final String KEY_PREF_PARAMETER_BASE = "pref_parameter_base";
    /** Key to identify the socket port preference. */
    public static final String KEY_PREF_SOCKET_PORT = "pref_socket_port";

    /** Delegate for enabling an action bar. */
    private AppCompatDelegate delegate;
    /** Reference to a SettingsFragment if on Honeycomb or newer. */
    private Object fragment;

    /**
     * Initializes the activity.
     * @param savedInstanceState Bundle to recover the state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            fragment = null;
            //noinspection deprecation
            addPreferencesFromResource(R.xml.preferences);
        } else {
            createPreferenceFragment();
        }
    }

    /**
     * Called after the activity was created.
     * @param savedInstanceState Bundle to recover the state
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    /**
     * Registers a listener for preference changes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //noinspection deprecation
            preferences = getPreferenceScreen().getSharedPreferences();
        } else {
            preferences = getFragmentSharedPreferences();
        }
        preferences.registerOnSharedPreferenceChangeListener(this);
        // Initialize summaries
        onSharedPreferenceChanged(preferences, KEY_PREF_PREDICTION_LENGTH);
        onSharedPreferenceChanged(preferences, KEY_PREF_HISTORY_LENGTH);
        onSharedPreferenceChanged(preferences, KEY_PREF_PARAMETER_BASE);
        onSharedPreferenceChanged(preferences, KEY_PREF_SOCKET_PORT);
    }

    /**
     * Unregisters a listener for preference changes.
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //noinspection deprecation
            preferences = getPreferenceScreen().getSharedPreferences();
        } else {
            preferences = getFragmentSharedPreferences();
        }
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Updates the preference summaries.
     * @param preferences shared preferences
     * @param key the key of the preference that changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        Preference preference;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //noinspection deprecation
            preference = findPreference(key);
        } else {
            preference = findFragmentPreference(key);
        }
        switch (key) {
            case KEY_PREF_PREDICTION_LENGTH:
            case KEY_PREF_HISTORY_LENGTH:
            case KEY_PREF_SOCKET_PORT:
                EditTextPreference numberPreference = (EditTextPreference) preference;
                String numberString = numberPreference.getText();
                try {
                    int numberInteger = Integer.parseInt(numberString);
                    // Check that numbers fit into a single string
                    if (numberInteger > Integer.MAX_VALUE
                            / (Long.toString(Long.MAX_VALUE).length()+1)) {
                        throw new NumberFormatException();
                    }
                    if (key.equals(SettingsActivity.KEY_PREF_SOCKET_PORT)
                            && numberInteger > 0xFFFF) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    // Correct to default value
                    String defaultValue = "";
                    switch (key) {
                        case SettingsActivity.KEY_PREF_PREDICTION_LENGTH:
                            defaultValue = getResources().getString(
                                    R.string.pref_prediction_length_default_value);
                            break;
                        case SettingsActivity.KEY_PREF_HISTORY_LENGTH:
                            defaultValue = getResources().getString(
                                    R.string.pref_history_length_default_value);
                            break;
                        case SettingsActivity.KEY_PREF_SOCKET_PORT:
                            defaultValue = getResources().getString(
                                    R.string.pref_socket_port_default_value);
                            break;
                    }
                    numberPreference.setText(defaultValue);
                    String errorMessage = getResources().getString(R.string.number_error_message);
                    Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
                String summary = numberPreference.getText();
                if (key.equals(SettingsActivity.KEY_PREF_SOCKET_PORT)) {
                    summary = getResources().getString(R.string.pref_socket_port_summary) + " "
                            + summary;
                } else if (summary != null && summary.equals("1")) {
                    if (key.equals(SettingsActivity.KEY_PREF_HISTORY_LENGTH)) {
                        summary += " " + getResources().getString(
                                R.string.pref_history_length_summary_singular);
                    } else {
                        summary += " " + getResources().getString(
                                R.string.pref_prediction_length_summary_singular);
                    }
                } else {
                    if (key.equals(SettingsActivity.KEY_PREF_HISTORY_LENGTH)) {
                        summary += " " + getResources().getString(
                                R.string.pref_history_length_summary_plural);
                    } else {
                        summary += " " + getResources().getString(
                                R.string.pref_prediction_length_summary_plural);
                    }
                }
                numberPreference.setSummary(summary);
                break;
            case KEY_PREF_PARAMETER_BASE:
                ListPreference parameterBasePreference = (ListPreference) preference;
                parameterBasePreference.setSummary(parameterBasePreference.getEntry());
                break;
        }
    }

    /**
     * Set the activity content from a layout resource.
     * @param layoutResID the id of the layout resource
     */
    @Override
    public void setContentView(int layoutResID) {
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.activity_settings, rootView, false);
        ViewGroup preferencesLayout = (ViewGroup) view.findViewById(R.id.preferences_layout);
        inflater.inflate(layoutResID, preferencesLayout, true);
        getDelegate().setContentView(view);
    }

    /**
     * Callback method for item selected.
     * @param item the selected item
     * @return false to allow normal menu processing to proceed, true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Returns a menu inflater.
     * @return the menu inflater
     */
    @NonNull
    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    /**
     * Callback method for options menu creations.
     * @param menu the menu to inflate
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    /**
     * Trigger recreation of options menu.
     */
    @Override
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    /**
     * Set the activity content to a view.
     * @param view the view to set the content to
     */
    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    /**
     * Set the activity content to a view with given layout parameters.
     * @param view the view to set the content to
     * @param params the layout parameters
     */
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    /**
     * Add an additional view to the activity.
     * @param view the view to add
     * @param params the layout parameters
     */
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    /**
     * Called when the configuration changed.
     * @param newConfig the new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    /**
     * Called after the activity is resumed.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    /**
     * Called when the title of the activity changes.
     * @param title the new title
     * @param color the color of the title
     */
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    /**
     * Called when the activity is stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    /**
     * Returns and eventually creates a delegate for enabling an action bar.
     * @return the delegate
     */
    private AppCompatDelegate getDelegate() {
        if (delegate == null) {
            delegate = AppCompatDelegate.create(this, null);
        }
        return delegate;
    }

    /**
     * Populates the activity based on a SettingsFragment.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void createPreferenceFragment() {
        fragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.preferences_layout,
                (SettingsFragment) fragment).commit();
    }

    /**
     * Returns the shared preferences by means of a preference fragment.
     * @return the shared preferences
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private SharedPreferences getFragmentSharedPreferences() {
        return ((SettingsFragment) fragment).getPreferenceScreen().getSharedPreferences();
    }

    /**
     * Finds a preference by means of a preference fragment.
     * @param key the key of the preference
     * @return the preference
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Preference findFragmentPreference(String key) {
        return ((SettingsFragment) fragment).findPreference(key);
    }

    /**
     * This class implements a preference fragment that adds all preferences from a common resource.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragment {
        /**
         * Initializes the fragment by adding all preference items from a resource.
         * @param savedInstanceState Bundle to restore a state
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
