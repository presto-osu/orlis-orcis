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
import android.os.Build;
import android.os.Bundle;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the Zmanim header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected int getPreferencesXml() {
        return R.xml.zmanim_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initList(ZmanimSettings.KEY_OPINION_OMER);
    }
}
