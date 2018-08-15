/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.times.preference;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import net.sf.times.ZmanimWidget;

/**
 * This fragment shows the preferences for a header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class AbstractPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    protected Context context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int xmlId = getPreferencesXml();
        PreferenceManager.setDefaultValues(getActivity(), xmlId, false);
        addPreferencesFromResource(xmlId);
    }

    protected abstract int getPreferencesXml();

    protected ListPreference initList(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if (pref != null) {
            ListPreference list = (ListPreference) pref;
            list.setOnPreferenceChangeListener(this);
            onListPreferenceChange(list, list.getValue());
            return list;
        }
        return null;
    }

    protected RingtonePreference initRingtone(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if (pref != null) {
            RingtonePreference ring = (RingtonePreference) pref;
            ring.setOnPreferenceChangeListener(this);
            onRingtonePreferenceChange(ring, ring.getSharedPreferences().getString(key, null));
            return ring;
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference) preference;
            return onCheckBoxPreferenceChange(checkBox, newValue);
        }
        if (preference instanceof ListPreference) {
            ListPreference list = (ListPreference) preference;
            onListPreferenceChange(list, newValue);
            notifyAppWidgets();
            return false;
        }
        if (preference instanceof RingtonePreference) {
            RingtonePreference ring = (RingtonePreference) preference;
            return onRingtonePreferenceChange(ring, newValue);
        }
        notifyAppWidgets();
        return true;
    }

    /**
     * Called when a list preference has probably changed its value.
     * <br>Updates the summary to the new value.
     *
     * @param preference
     *         the  preference.
     * @param newValue
     *         the possibly new value.
     */
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String value = (newValue == null) ? null : newValue.toString();
        updateSummary(preference, value);
    }

    /**
     * Called when a ringtone preference has changed its value.
     * <br>Updates the summary to the new ringtone title.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     * @return {@code true} if the user value should be set as the preference value (and persisted).
     */
    protected boolean onRingtonePreferenceChange(RingtonePreference preference, Object newValue) {
        String value = (newValue == null) ? null : newValue.toString();
        updateSummary(preference, value);
        return true;
    }

    /**
     * Called when a check box preference has changed its value.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     * @return {@code true} if the user value should be set as the preference value (and persisted).
     */
    protected boolean onCheckBoxPreferenceChange(CheckBoxPreference preference, Object newValue) {
        return true;
    }

    /**
     * Update the summary that was selected from the list.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     */
    private void updateSummary(ListPreference preference, String newValue) {
        preference.setValue(newValue);

        CharSequence[] values = preference.getEntryValues();
        CharSequence[] entries = preference.getEntries();
        int length = values.length;

        for (int i = 0; i < length; i++) {
            if (newValue.equals(values[i])) {
                preference.setSummary(entries[i]);
                return;
            }
        }
        preference.setSummary(null);
    }

    /**
     * Update the summary that was selected from the list.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     */
    private void updateSummary(RingtonePreference preference, String newValue) {
        Uri ringtoneUri;
        if (newValue == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(preference.getRingtoneType());
        } else if (TextUtils.isEmpty(newValue)) {
            ringtoneUri = Uri.EMPTY;
        } else {
            ringtoneUri = Uri.parse(newValue);
        }

        Context context = this.context;
        Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
        if (ringtone != null) {
            String title = ringtone.getTitle(context);
            preference.setSummary(title);
        } else {
            preference.setSummary(null);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    protected void notifyAppWidgets() {
        Context context = this.context;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final Class<?> clazz = ZmanimWidget.class;
        ComponentName provider = new ComponentName(context, clazz);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        if ((appWidgetIds == null) || (appWidgetIds.length == 0))
            return;

        Intent intent = new Intent(context, ZmanimWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
}
