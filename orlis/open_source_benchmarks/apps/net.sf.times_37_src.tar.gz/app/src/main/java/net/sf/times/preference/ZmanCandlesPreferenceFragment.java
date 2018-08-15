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
import android.preference.Preference;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.R;

/**
 * This fragment shows the preferences for the Candles zman screen.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanCandlesPreferenceFragment extends ZmanPreferenceFragment {

    private SeekBarDialogPreference candles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        candles = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
        candles.setSummaryFormat(R.plurals.candles_summary);
        candles.setOnPreferenceChangeListener(this);
        onCandlesPreferenceChange(candles, null);
    }


    private boolean onCandlesPreferenceChange(SeekBarDialogPreference preference, Object newValue) {
        int minutes = preference.getProgress();
        Resources res = getResources();
        CharSequence summary = res.getQuantityString(R.plurals.candles_summary, minutes, minutes);
        preference.setSummary(summary);
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == candles) {
            return onCandlesPreferenceChange(candles, newValue);
        }
        return super.onPreferenceChange(preference, newValue);
    }
}
