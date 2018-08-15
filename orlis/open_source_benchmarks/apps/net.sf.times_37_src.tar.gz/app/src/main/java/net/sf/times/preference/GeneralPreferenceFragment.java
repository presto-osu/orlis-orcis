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
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the General header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends AbstractPreferenceFragment {

    private RingtonePreference reminderRingtonePreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.general_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reminderRingtonePreference = initRingtone(ZmanimSettings.KEY_REMINDER_RINGTONE);

        initList(ZmanimSettings.KEY_REMINDER_STREAM);
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        super.onListPreferenceChange(preference, newValue);

        String key = preference.getKey();
        if (ZmanimSettings.KEY_REMINDER_STREAM.equals(key) && (reminderRingtonePreference != null)) {
            String value = newValue.toString();
            int audioStreamType = TextUtils.isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
            int ringType;
            if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                ringType = RingtoneManager.TYPE_NOTIFICATION;
            } else {
                ringType = RingtoneManager.TYPE_ALARM;
            }
            reminderRingtonePreference.setRingtoneType(ringType);
        }
    }
}
