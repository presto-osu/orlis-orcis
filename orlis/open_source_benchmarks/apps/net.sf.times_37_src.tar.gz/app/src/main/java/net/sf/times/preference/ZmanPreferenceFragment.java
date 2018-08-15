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
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;

import net.sf.times.ZmanimReminder;

/**
 * This fragment shows the preferences for a zman screen.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanPreferenceFragment extends AbstractPreferenceFragment {

    public static final String EXTRA_XML = "xml";
    public static final String EXTRA_OPINION = "opinion";
    public static final String EXTRA_REMINDER = "reminder";

    private int xmlId;
    private ZmanReminderPreference preferenceReminder;
    private Preference preferenceReminderSunday;
    private Preference preferenceReminderMonday;
    private Preference preferenceReminderTuesday;
    private Preference preferenceReminderWednesday;
    private Preference preferenceReminderThursday;
    private Preference preferenceReminderFriday;
    private Preference preferenceReminderSaturday;
    private ZmanimSettings settings;
    private ZmanimReminder reminder;
    private Runnable remindRunner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String xmlName = args.getString(EXTRA_XML);
        Resources res = getResources();
        this.xmlId = res.getIdentifier(xmlName, "xml", getActivity().getPackageName());
        String opinionKey = args.getString(EXTRA_OPINION);
        String reminderKey = args.getString(EXTRA_REMINDER);

        super.onCreate(savedInstanceState);

        initList(opinionKey);
        this.preferenceReminder = (ZmanReminderPreference) initList(reminderKey);
        if (preferenceReminder != null) {
            initReminderDays(preferenceReminder);
        }
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();

        super.onListPreferenceChange(preference, newValue);

        if (!oldValue.equals(newValue) && (preference == preferenceReminder)) {
            // Explicitly disable dependencies?
            preference.notifyDependencyChange(preference.shouldDisableDependents());

            remind();
        }
    }

    protected void initReminderDays(Preference reminderTime) {
        String namePrefix = reminderTime.getKey();
        this.preferenceReminderSunday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_SUNDAY_SUFFIX);
        this.preferenceReminderMonday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_MONDAY_SUFFIX);
        this.preferenceReminderTuesday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_TUESDAY_SUFFIX);
        this.preferenceReminderWednesday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_WEDNESDAY_SUFFIX);
        this.preferenceReminderThursday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_THURSDAY_SUFFIX);
        this.preferenceReminderFriday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_FRIDAY_SUFFIX);
        this.preferenceReminderSaturday = initReminderDay(namePrefix + ZmanimSettings.REMINDER_SATURDAY_SUFFIX);
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

    protected boolean onCheckBoxPreferenceChange(CheckBoxPreference preference, Object newValue) {
        if (preference.equals(preferenceReminderSunday)
                || preference.equals(preferenceReminderMonday)
                || preference.equals(preferenceReminderTuesday)
                || preference.equals(preferenceReminderWednesday)
                || preference.equals(preferenceReminderThursday)
                || preference.equals(preferenceReminderFriday)
                || preference.equals(preferenceReminderSaturday)) {
            remind();
        }

        return super.onCheckBoxPreferenceChange(preference, newValue);
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
        View view = getView();
        if (view == null) {
            remindRunner.run();
        } else {
            view.post(remindRunner);
        }
    }
}
