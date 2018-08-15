package com.luk.timetable2.listeners.SettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.luk.timetable2.services.RegisterReceivers;

/**
 * Created by luk on 9/22/15.
 */
public class NotificationsChangeListener implements Preference.OnPreferenceChangeListener {
    private final Activity mActivity;

    public NotificationsChangeListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (boolean) newValue;
        PreferenceManager preferenceManager = preference.getPreferenceManager();

        preferenceManager.findPreference("notifications_vibrate_length").setEnabled(isEnabled);
        preferenceManager.findPreference("notifications_vibrate_time").setEnabled(isEnabled);
        mActivity.sendBroadcast(new Intent(mActivity, RegisterReceivers.class));

        return true;
    }
}