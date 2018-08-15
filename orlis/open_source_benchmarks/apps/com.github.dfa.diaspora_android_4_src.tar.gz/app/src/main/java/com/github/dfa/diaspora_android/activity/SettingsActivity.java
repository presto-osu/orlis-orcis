package com.github.dfa.diaspora_android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;

/**
 * @author vanitas
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("app");
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setPreferenceSummaries();
        setResult(Activity.RESULT_CANCELED);
        sharedPreferences.edit().putBoolean(AppSettings.PREF.PROXY_WAS_ENABLED,
                sharedPreferences.getBoolean(AppSettings.PREF.PROXY_ENABLED, false)).apply();
    }

    private void setPreferenceSummaries() {
        String[] editTextKeys = new String[]{AppSettings.PREF.PROXY_HOST, AppSettings.PREF.PROXY_PORT};
        for(String key : editTextKeys) {
            EditTextPreference p = (EditTextPreference) findPreference(key);
            p.setSummary(p.getText());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key), key);
    }

    private void updatePreference(Preference preference, String key) {
        if (preference == null) return;
        if (preference instanceof EditTextPreference) {
            EditTextPreference textPref = (EditTextPreference) preference;
            textPref.setSummary(textPref.getText());
            return;
        }
        if(preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            listPref.setSummary(listPref.getEntry());
            return;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        Intent results = new Intent();
        String podDomain = ((App)getApplication()).getSettings().getPodDomain();
        switch(preference.getKey()) {
            case "pref_key_personal_settings":
                results.putExtra(MainActivity.URL_MESSAGE, "https://" + podDomain + "/user/edit");
                setResult(Activity.RESULT_OK, results);
                finish();
            case "pref_key_manage_tags":
                results.putExtra(MainActivity.URL_MESSAGE, "https://" + podDomain + "/tag_followings/manage");
                setResult(Activity.RESULT_OK, results);
                finish();
            case "pref_key_manage_contacts":
                results.putExtra(MainActivity.URL_MESSAGE, "https://" + podDomain + "/contacts");
                setResult(Activity.RESULT_OK, results);
                finish();
            case "pref_key_change_account":
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getString(R.string.confirmation))
                        .setMessage(getString(R.string.pref_warning_change_account))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        setResult(MainActivity.RESULT_CODE_CHANGE_ACCOUNT);
                                        finish();
                                    }
                                })
                        .show();
        }
        return super.onPreferenceTreeClick(screen, preference);
    }
}