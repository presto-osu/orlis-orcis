/*
 * Copyright (c) 2016 Juan García Basilio
 *
 * This file is part of WaveUp.
 *
 * WaveUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WaveUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WaveUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jarsilio.android.waveup;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.jarsilio.android.waveup.receivers.LockScreenAdminReceiver;


public class Settings {
    public static final String ENABLED = "pref_enable";
    public static final String INITIAL_DIALOG_SHOWN = "pref_initial_dialog_shown";
    public static final String WAVE_MODE = "pref_wave_mode";
    public static final String POCKET_MODE = "pref_pocket_mode";
    public static final String LOCK_SCREEN = "pref_lock_screen";
    public static final String LOCK_SCREEN_WHEN_LANDSCAPE = "pref_lock_screen_when_landscape";
    public static final String LOCK_SCREEN_WITH_POWER_BUTTON = "pref_lock_screen_with_power_button_as_root";
    public static final String VIBRATE_ON_LOCK = "pref_lock_screen_vibrate_on_lock";

    private static volatile Settings instance;
    private final Context context;
    private PreferenceActivity preferenceActivity = null;

    public static Settings getInstance(Context context) {
        if (instance == null ) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = new Settings(context);
                }
            }
        }

        return instance;
    }

    private Settings(Context context) {
        this.context = context;
    }
    
    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isServiceEnabled() {
        return getPreferences().getBoolean(ENABLED, false);
    }

    public boolean isWaveMode() {
        return getPreferences().getBoolean(WAVE_MODE, false);
    }

    public boolean isPocketMode() {
        return getPreferences().getBoolean(POCKET_MODE, false);
    }

    public boolean isLockScreen() {
        return getPreferences().getBoolean(LOCK_SCREEN, false);
    }

    public void setLockScreen(boolean lockScreen) {
        setPreference(LOCK_SCREEN, lockScreen);
    }

    public boolean isLockScreenWhenLandscape() {
        return getPreferences().getBoolean(LOCK_SCREEN_WHEN_LANDSCAPE, false);
    }

    public boolean isLockScreenWithPowerButton() {
        return getPreferences().getBoolean(LOCK_SCREEN_WITH_POWER_BUTTON, false);
    }

    public void setLockScreenWithPowerButton(boolean lockScreenWithPowerButton) {
        setPreference(LOCK_SCREEN_WITH_POWER_BUTTON, lockScreenWithPowerButton);
    }

    public boolean isVibrateWhileLocking() {
        return getPreferences().getBoolean(VIBRATE_ON_LOCK, false);
    }

    private void setPreference(String key, boolean value) {
        if (preferenceActivity != null) { // This changes the GUI, but it needs the MainActivity to have started
            CheckBoxPreference checkBox = (CheckBoxPreference) preferenceActivity.findPreference(key);
            checkBox.setChecked(value);
        } else { // This doesn't change the GUI
            getPreferences().edit().putBoolean(key, value).commit();
        }
        /* onSharedPreferenceChanged is not called sometimes when status of a preference is changed manually.
         * Call startOrStop here to check if proximity sensor listener should be registered or not. */
        ProximitySensorManager.getInstance(context).startOrStopListeningDependingOnConditions();
    }

    public boolean isLockScreenAdmin() {
        ComponentName adminReceiver = new ComponentName(context, LockScreenAdminReceiver.class);
        return getPolicyManager().isAdminActive(adminReceiver);
    }

    public boolean isInitialDialogShown() {
        return getPreferences().getBoolean(INITIAL_DIALOG_SHOWN, false);
    }

    public void setInitialDialogShown(boolean initialDialogShown) {
        getPreferences().edit().putBoolean(INITIAL_DIALOG_SHOWN, initialDialogShown).commit();
    }

    private DevicePolicyManager getPolicyManager() {
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public void setPreferenceActivity(PreferenceActivity preferenceActivity) {
        /* If a Preference is updated using getPreferences().edit().putBoolean(key, value).commit(),
         * the GUI doesn't update automatically.
         * If it is changed using a CheckBox, then it does work. In order to get a CheckBox object,
         * we need to have the preferenceActivity, which is the MainActivity so we set it the moment
         * it is launched so that we can use it afterwards.
         */
        this.preferenceActivity = preferenceActivity;
    }
}
