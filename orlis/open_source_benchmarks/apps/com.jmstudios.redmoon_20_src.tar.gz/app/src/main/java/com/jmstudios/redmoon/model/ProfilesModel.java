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
package com.jmstudios.redmoon.model;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.ArrayList;

/**
 * This class manages the SharedPreference that store all custom
 * filter profiles added by the user.
 *
 * The profiles are stored in a seperate SharedPreference, with per
 * profile a key given by "$PROFILE_NAME_$ID", where $PROFILE_NAME is
 * the name given to the profile by the user and $ID is the position
 * of the list of profiles, starting with 0 for the first custom
 * profile created by the user. A string is associated with every key
 * with the format "$PROGRESS_COLOR,$PROGRESS_INTENSITY,$PROGRESS_DIM"
 */
public class ProfilesModel {
    private static final String preferenceName = "com.jmstudios.redmoon.PROFILES_PREFERENCE";
    private static final int mode = Context.MODE_PRIVATE;

    private static final String TAG = "ProfilesModel";
    private static final boolean DEBUG = true;

    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;
    private Map<String, ?> mPrefsContentsMap;
    private ArrayList<Profile> mParsedProfiles;

    public ProfilesModel(@NonNull Context context) {
        if (DEBUG) Log.i(TAG, "Creating ProfilesModel");
        mSharedPrefs = context.getSharedPreferences(preferenceName, mode);

        mPrefsContentsMap = (Map<String, String>) mSharedPrefs.getAll();

        parsePrefsContents();
    }

    public void addProfile(Profile profile) {
        if (DEBUG) Log.i(TAG, "Adding new profile");
        mParsedProfiles.add(profile);

        updateSharedPreferences();
    }

    public ArrayList<Profile> getProfiles() {
        return mParsedProfiles;
    }

    public Profile getProfile(int index) {
        return mParsedProfiles.get(index);
    }

    public void removeProfile(int index) {
        mParsedProfiles.remove(index);

        updateSharedPreferences();
    }

    private void parsePrefsContents() {
        if (DEBUG) Log.i(TAG, "Parsing preference contents");

        mParsedProfiles = new ArrayList<Profile>();

        int amProfiles = mPrefsContentsMap.entrySet().size();
        if (DEBUG) Log.d(TAG, "Allocating " + amProfiles);
        mParsedProfiles.ensureCapacity(amProfiles);

        if (DEBUG) Log.d(TAG, "Allocated " + amProfiles);

        for (int i = 0; i < amProfiles; i++) {
            if (DEBUG) Log.d(TAG, "Parsing " + i);
            String profileEntry = findProfileEntry(i);
            mParsedProfiles.add(parseProfile(profileEntry));
        }

        if (DEBUG) Log.d(TAG, "Done parsing preference contents. Parsed " + amProfiles + " profiles.");
    }

    private String findProfileEntry(int index) {
        if (DEBUG) Log.i(TAG, "Finding entry at " + index);
        for (Map.Entry<String, ?> entry : mPrefsContentsMap.entrySet()) {
            if (getIndexFromString(entry.getKey()) == index)
                return entry.getKey() + "@" + ((String) entry.getValue());
        }
        return "Profile not found_0,0,0";
    }

    private int getIndexFromString(String keyString) {
        if (DEBUG) Log.i(TAG, "Parsing index from string: " + keyString);
        int length = keyString.length();
        int idIndex = keyString.lastIndexOf('_') + 1;
        String idString = keyString.substring(idIndex, length);

        if (DEBUG) Log.i(TAG, "Found idString: " + idString);

        return Integer.parseInt(idString);
    }

    private Profile parseProfile(String entry) {
        if (DEBUG) Log.i(TAG, "Parsing entry: " + entry);
        String key = entry.substring(0, entry.lastIndexOf("@"));
        String values = entry.substring(entry.lastIndexOf("@") + 1, entry.length());

        String profileName = getProfileNameFromString(key);

        String progressValues = values;
        int firstComma = progressValues.indexOf(',');
        int colorProgress = Integer.parseInt
            (progressValues.substring(0, firstComma));

        int secondComma = progressValues.indexOf(',', firstComma + 1);
        int intensityProgress = Integer.parseInt
            (progressValues.substring(firstComma + 1, secondComma));

        int dimProgress = Integer.parseInt
            (progressValues.substring(secondComma + 1, progressValues.length()));

        Profile profile = new Profile(profileName, colorProgress, intensityProgress, dimProgress);
        return profile;
    }

    private String getProfileNameFromString(String keyString) {
        int nameEndIndex = keyString.lastIndexOf('_');
        String profileNameString = keyString.substring(0, nameEndIndex);

        return profileNameString;
    }

    private void updateSharedPreferences() {
        if (DEBUG) Log.i(TAG, "Updating SharedPreferences");
        mEditor = mSharedPrefs.edit();
        mEditor.clear();

        int i = 0;
        for (Profile profile : mParsedProfiles) {
            mEditor.putString(profile.getKey(i), profile.getValues());

            i++;
        }

        mEditor.apply();
        if (DEBUG) Log.d(TAG, "Done updating SharedPreferences");
    }

    public static class Profile {
        public String mProfileName;
        public int mColorProgress;
        public int mIntensityProgress;
        public int mDimProgress;

        public Profile(String profileName, int colorProgress,
                       int intensityProgress, int dimProgress) {
            mProfileName = profileName;
            mColorProgress = colorProgress;
            mIntensityProgress = intensityProgress;
            mDimProgress = dimProgress;
        }

        public String getKey(int index) {
            String id = Integer.toString(index);
            return mProfileName + "_" + id;
        }

        public String getValues() {
            return Integer.toString(mColorProgress) + "," +
                Integer.toString(mIntensityProgress) + "," +
                Integer.toString(mDimProgress);
        }
    }
}
