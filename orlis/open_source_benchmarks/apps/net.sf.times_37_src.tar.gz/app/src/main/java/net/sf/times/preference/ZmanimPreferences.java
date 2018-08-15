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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.View;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.R;
import net.sf.times.ZmanimApplication;
import net.sf.times.ZmanimReminder;
import net.sf.times.ZmanimWidget;
import net.sf.times.location.AddressProvider;

import java.util.Calendar;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPreferences extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String REMINDER_SUNDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.SUNDAY;
    private static final String REMINDER_MONDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.MONDAY;
    private static final String REMINDER_TUESDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.TUESDAY;
    private static final String REMINDER_WEDNESDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.WEDNESDAY;
    private static final String REMINDER_THURSDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.THURSDAY;
    private static final String REMINDER_FRIDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.FRIDAY;
    private static final String REMINDER_SATURDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.SATURDAY;

    private SeekBarDialogPreference candles;
    private ZmanimSettings settings;
    private ZmanimReminder reminder;
    private Preference clearHistory;
    private RingtonePreference reminderRingtonePreference;
    private Runnable remindRunner;

    /**
     * Constructs a new preferences.
     */
    public ZmanimPreferences() {
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);

        reminderRingtonePreference = initRingtone(ZmanimSettings.KEY_REMINDER_RINGTONE);
        initList(ZmanimSettings.KEY_REMINDER_STREAM);

        initList(ZmanimSettings.KEY_COORDS_FORMAT);
        initList(ZmanimSettings.KEY_THEME);

        candles = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
        candles.setSummaryFormat(R.plurals.candles_summary);
        candles.setOnPreferenceChangeListener(this);
        onCandlesPreferenceChange(candles, null);

        initList(ZmanimSettings.KEY_OPINION_HOUR);
        initList(ZmanimSettings.KEY_OPINION_DAWN);
        initList(ZmanimSettings.KEY_OPINION_TALLIS);
        initList(ZmanimSettings.KEY_OPINION_SUNRISE);
        initList(ZmanimSettings.KEY_OPINION_SHEMA);
        initList(ZmanimSettings.KEY_OPINION_TFILA);
        initList(ZmanimSettings.KEY_OPINION_NOON);
        initList(ZmanimSettings.KEY_OPINION_EARLIEST_MINCHA);
        initList(ZmanimSettings.KEY_OPINION_MINCHA);
        initList(ZmanimSettings.KEY_OPINION_PLUG_MINCHA);
        initList(ZmanimSettings.KEY_OPINION_SUNSET);
        initList(ZmanimSettings.KEY_OPINION_TWILIGHT);
        initList(ZmanimSettings.KEY_OPINION_NIGHTFALL);
        initList(ZmanimSettings.KEY_OPINION_MIDNIGHT);
        initList(ZmanimSettings.KEY_OPINION_EARLIEST_LEVANA);
        initList(ZmanimSettings.KEY_OPINION_LATEST_LEVANA);
        initList(ZmanimSettings.KEY_OPINION_OMER);

        initList(ZmanimSettings.KEY_REMINDER_DAWN);
        initList(ZmanimSettings.KEY_REMINDER_TALLIS);
        initList(ZmanimSettings.KEY_REMINDER_SUNRISE);
        initList(ZmanimSettings.KEY_REMINDER_SHEMA);
        initList(ZmanimSettings.KEY_REMINDER_TFILA);
        initList(ZmanimSettings.KEY_REMINDER_NOON);
        initList(ZmanimSettings.KEY_REMINDER_EARLIEST_MINCHA);
        initList(ZmanimSettings.KEY_REMINDER_MINCHA);
        initList(ZmanimSettings.KEY_REMINDER_PLUG_MINCHA);
        initList(ZmanimSettings.KEY_REMINDER_CANDLES);
        initList(ZmanimSettings.KEY_REMINDER_SUNSET);
        initList(ZmanimSettings.KEY_REMINDER_TWILIGHT);
        initList(ZmanimSettings.KEY_REMINDER_NIGHTFALL);
        initList(ZmanimSettings.KEY_REMINDER_MIDNIGHT);
        initList(ZmanimSettings.KEY_REMINDER_EARLIEST_LEVANA);
        initList(ZmanimSettings.KEY_REMINDER_LATEST_LEVANA);

        initReminderDays(ZmanimSettings.KEY_REMINDER_DAWN);
        initReminderDays(ZmanimSettings.KEY_REMINDER_TALLIS);
        initReminderDays(ZmanimSettings.KEY_REMINDER_SUNRISE);
        initReminderDays(ZmanimSettings.KEY_REMINDER_SHEMA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_TFILA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_NOON);
        initReminderDays(ZmanimSettings.KEY_REMINDER_EARLIEST_MINCHA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_MINCHA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_PLUG_MINCHA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_CANDLES);
        initReminderDays(ZmanimSettings.KEY_REMINDER_SUNSET);
        initReminderDays(ZmanimSettings.KEY_REMINDER_TWILIGHT);
        initReminderDays(ZmanimSettings.KEY_REMINDER_NIGHTFALL);
        initReminderDays(ZmanimSettings.KEY_REMINDER_MIDNIGHT);
        initReminderDays(ZmanimSettings.KEY_REMINDER_EARLIEST_LEVANA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_LATEST_LEVANA);

        clearHistory = findPreference("clear_history");
        clearHistory.setOnPreferenceClickListener(this);

        Preference version = findPreference("about.version");
        try {
            version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            // Never should happen with our own package!
        }

        // Other preferences that affect the app widget.
        findPreference(ZmanimSettings.KEY_PAST).setOnPreferenceChangeListener(this);
        findPreference(ZmanimSettings.KEY_SECONDS).setOnPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    protected ListPreference initList(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        Preference pref = findPreference(name);
        if (pref != null) {
            ListPreference list = (ListPreference) pref;
            list.setOnPreferenceChangeListener(this);
            onListPreferenceChange(list, list.getValue());
            return list;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    protected RingtonePreference initRingtone(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        Preference pref = findPreference(name);
        if (pref != null) {
            RingtonePreference ring = (RingtonePreference) pref;
            ring.setOnPreferenceChangeListener(this);
            onRingtonePreferenceChange(ring, ring.getSharedPreferences().getString(name, null));
            return ring;
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == candles) {
            return onCandlesPreferenceChange(candles, newValue);
        }
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

    private boolean onCandlesPreferenceChange(SeekBarDialogPreference preference, Object newValue) {
        int minutes = preference.getProgress();
        Resources res = getResources();
        CharSequence summary = res.getQuantityString(R.plurals.candles_summary, minutes, minutes);
        preference.setSummary(summary);
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
    private void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();
        String value = (newValue == null) ? null : newValue.toString();
        updateSummary(preference, value);

        if (!oldValue.equals(newValue)) {
            String key = preference.getKey();

            if (ZmanimSettings.KEY_REMINDER_STREAM.equals(key) && (reminderRingtonePreference != null)) {
                int audioStreamType = TextUtils.isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
                int ringType;
                if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                    ringType = RingtoneManager.TYPE_NOTIFICATION;
                } else {
                    ringType = RingtoneManager.TYPE_ALARM;
                }
                reminderRingtonePreference.setRingtoneType(ringType);
            } else if (key.endsWith(ZmanimSettings.REMINDER_SUFFIX)) {
                // Explicitly disable dependencies?
                preference.notifyDependencyChange(preference.shouldDisableDependents());

                remind();
            }
        }
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
        String key = preference.getKey();
        if (key.endsWith(REMINDER_SUNDAY_SUFFIX)
                || key.endsWith(REMINDER_MONDAY_SUFFIX)
                || key.endsWith(REMINDER_TUESDAY_SUFFIX)
                || key.endsWith(REMINDER_WEDNESDAY_SUFFIX)
                || key.endsWith(REMINDER_THURSDAY_SUFFIX)
                || key.endsWith(REMINDER_FRIDAY_SUFFIX)
                || key.endsWith(REMINDER_SATURDAY_SUFFIX)) {
            remind();
        }
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

        Context context = this;
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
        if (preference == clearHistory) {
            preference.setEnabled(false);
            deleteHistory();
            preference.setEnabled(true);
            return true;
        }
        return false;
    }

    /**
     * Clear the history of addresses.
     */
    private void deleteHistory() {
        ZmanimApplication app = (ZmanimApplication) getApplication();
        AddressProvider provider = app.getAddresses();
        provider.deleteAddresses();
    }

    private void notifyAppWidgets() {
        Context context = this;
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

    /**
     * Run the reminder service.
     * Tries to postpone the reminder until after any preferences have changed.
     */
    private void remind() {
        if (remindRunner == null) {
            remindRunner = new Runnable() {
                @Override
                public void run() {
                    Context context = ZmanimPreferences.this;
                    if (context != null) {
                        if (settings == null)
                            settings = new ZmanimSettings(context);
                        if (reminder == null)
                            reminder = new ZmanimReminder(context);
                        reminder.remind(settings);
                    }
                }
            };
        }
        View view = getWindow().getDecorView();
        if (view == null) {
            remindRunner.run();
        } else {
            view.post(remindRunner);
        }
    }

    protected void initReminderDays(String reminderKey) {
        if (TextUtils.isEmpty(reminderKey)) {
            return;
        }

        initReminderDay(reminderKey + ZmanimSettings.REMINDER_SUNDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_MONDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_TUESDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_WEDNESDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_THURSDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_FRIDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_SATURDAY_SUFFIX);
    }

    protected CheckBoxPreference initReminderDay(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if (pref != null) {
            CheckBoxPreference checkBox = (CheckBoxPreference) pref;
            checkBox.setOnPreferenceChangeListener(this);
            return checkBox;
        }
        return null;
    }
}
