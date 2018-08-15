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
import android.preference.PreferenceActivity;

import net.sf.times.R;

import java.util.List;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimPreferenceActivity extends PreferenceActivity {

    private final String packageName;

    /**
     * Constructs a new preferences.
     */
    public ZmanimPreferenceActivity() {
        packageName = getClass().getPackage().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.startsWith(packageName);
    }
}
