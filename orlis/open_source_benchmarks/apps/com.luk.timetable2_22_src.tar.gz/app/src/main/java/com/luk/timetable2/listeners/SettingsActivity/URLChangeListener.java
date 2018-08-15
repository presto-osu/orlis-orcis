package com.luk.timetable2.listeners.SettingsActivity;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by luk on 9/22/15.
 */
public class URLChangeListener implements Preference.OnPreferenceChangeListener {
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String correctURL = fixURL((String) newValue);
        SharedPreferences.Editor editor = preference.getSharedPreferences().edit();
        PreferenceManager preferenceManager = preference.getPreferenceManager();

        editor.putString("school", correctURL).apply();
        ((EditTextPreference) preferenceManager.findPreference("school")).setText(correctURL);
        preferenceManager.findPreference("school").setSummary(correctURL);

        return false;
    }

    private String fixURL(String url) {
        if (!(url.startsWith("http://") || url.startsWith("https://")) && url.length() > 0) {
            url = "http://" + url;
        }

        if (FilenameUtils.getExtension(url).length() == 0) {
            return FilenameUtils.getFullPath(url) + FilenameUtils.getBaseName(url);
        }

        if (FilenameUtils.getFullPath(url).equals("http:/") ||
                FilenameUtils.getFullPath(url).equals("http://")) {
            return FilenameUtils.getFullPath(url + "/");
        }

        return FilenameUtils.getFullPath(url);
    }
}
