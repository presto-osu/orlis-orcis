package com.luk.timetable2.listeners.SettingsActivity.v7;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.preference.Preference;

import com.luk.timetable2.services.RegisterReceivers;

/**
 * Created by luk on 9/22/15.
 */
public class NotificationsLengthChangeListener implements Preference.OnPreferenceChangeListener {
    private final Activity mActivity;

    public NotificationsLengthChangeListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue.equals("")) {
            return false;
        }

        preference.setSummary((String) newValue);
        mActivity.sendBroadcast(new Intent(mActivity, RegisterReceivers.class));

        return true;
    }
}
