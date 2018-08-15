package com.luk.timetable2.listeners.SettingsActivity.v7;

import android.app.Activity;
import android.support.v7.preference.Preference;

/**
 * Created by luk on 9/22/15.
 */
public class ThemeChangeListener implements Preference.OnPreferenceChangeListener {
    private final Activity mActivity;

    public ThemeChangeListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            mActivity.recreate();
        } else {
            mActivity.startActivity(mActivity.getIntent());
            mActivity.finish();
        }

        return true;
    }
}
