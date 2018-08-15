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
package com.jmstudios.redmoon.helper;

import android.content.Context;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.preference.ProfileSelectorPreference;
import com.jmstudios.redmoon.model.ProfilesModel;

public class ProfilesHelper {
    public static String getProfileName(ProfilesModel model, int profile, Context context) {
        if (profile < ProfileSelectorPreference.DEFAULT_OPERATIONS_AM) {
            return context.getResources().getStringArray(R.array.standard_profiles_array)[profile];
        } else {
            return model.getProfile(profile - ProfileSelectorPreference.DEFAULT_OPERATIONS_AM).mProfileName;
        }
    }

    public static ProfilesModel.Profile getProfile(ProfilesModel model, int profile, Context context) {
        if (profile < ProfileSelectorPreference.DEFAULT_OPERATIONS_AM) {
            String name = context.getResources().getStringArray(R.array.standard_profiles_array)[profile];
            int color, intensity, dim;
            switch (profile) {
            case 1:
                color = 10;
                intensity = 30;
                dim = 40;
                break;
            case 2:
                color = 20;
                intensity = 60;
                dim = 78;
                break;
            default:
                color = 0;
                intensity = 0;
                dim = 0;
                break;
            }
            return new ProfilesModel.Profile(name, color, intensity, dim);
        } else {
            return model.getProfile(profile - ProfileSelectorPreference.DEFAULT_OPERATIONS_AM);
        }
    }
}
