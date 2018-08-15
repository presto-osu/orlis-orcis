package com.luk.timetable2.listeners.SettingsActivity.v7;

import android.content.SharedPreferences;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by luk on 9/22/15.
 */
public class URLChangeListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String correctURL = fixURL((String) newValue);
        SharedPreferences.Editor editor = preference.getSharedPreferences().edit();
        PreferenceManager preferenceManager = preference.getPreferenceManager();

        editor.putString("school", correctURL).commit();
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
