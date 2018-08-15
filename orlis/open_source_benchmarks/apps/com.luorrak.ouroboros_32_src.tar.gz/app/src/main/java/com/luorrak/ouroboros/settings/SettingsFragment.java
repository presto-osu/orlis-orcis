package com.luorrak.ouroboros.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogActivity;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment(){
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        getActivity().setTitle("Settings");
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() != null) { //Messy fix, problem is old settings fragments are not being destroyed no matter what method I call. It's even surviving new activities.
            switch (key){
                case "theme_preference": {
                    Intent intent = new Intent(getContext(), CatalogActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
                case "pref_reply_checker": {
                    if (SettingsHelper.getReplyCheckerStatus(getActivity())){
                        Util.startReplyCheckerService(getActivity());
                    } else {
                        Util.stopReplyCheckerService(getActivity());
                    }
                    break;
                }
            }
        }
    }
}