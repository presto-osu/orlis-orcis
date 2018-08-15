/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.receiver;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.model.ProfilesModel;
import com.jmstudios.redmoon.helper.ProfilesHelper;

public class NextProfileCommandReceiver extends BroadcastReceiver {
    public static final boolean DEBUG = true;
    public static final String TAG = "NextProfileCommandRcv";

    private SettingsModel mSettingsModel;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) Log.i(TAG, "Next profile requested");

        SharedPreferences standardSp = PreferenceManager.getDefaultSharedPreferences(context);
        mSettingsModel = new SettingsModel(context.getResources(), standardSp);

        // Here we just change the profile (cycles back to default
        // when it reaches the max).
        int profile = mSettingsModel.getProfile();
        int amProfiles = mSettingsModel.getAmmountProfiles();
        int newProfile = (profile + 1) >= amProfiles ?
            1 : (profile + 1);
        mSettingsModel.setProfile(newProfile);

        // Next update the other settings that are based on the
        // profile
        if (newProfile != 0) {
            // We need a ProfilesModel to get the properties of the
            // profile from the index
            ProfilesModel profilesModel = new ProfilesModel(context);
            ProfilesModel.Profile profileObject = ProfilesHelper.getProfile
                (profilesModel, newProfile, context);

            mSettingsModel.setShadesDimLevel(profileObject.mDimProgress);
            mSettingsModel.setShadesIntensityLevel(profileObject.mIntensityProgress);
            mSettingsModel.setShadesColor(profileObject.mColorProgress);
        }
    }
}
