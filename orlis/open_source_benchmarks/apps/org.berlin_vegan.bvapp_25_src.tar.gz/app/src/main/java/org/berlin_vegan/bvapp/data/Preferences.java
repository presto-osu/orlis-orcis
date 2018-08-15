/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;


public class Preferences {
    static final String KEY_FAVORITES = "key_favorites";
    private static final String KEY_UNITS = "key_units";
    private static final String KEY_GASTRO_FILTER = "key_gastro_filter";
    private static final String KEY_GASTRO_LAST_MODIFIED = "key_gastro_last_modified";
    private static final String KEY_SHOPPING_LAST_MODIFIED = "key_shopping_last_modified";

    public static boolean isMetricUnit(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Preferences.KEY_UNITS, true);
    }


    public static void removeGastroFilter(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(Preferences.KEY_GASTRO_FILTER);
        editor.apply();

    }


    public static void saveGastroFilter(Context context, GastroLocationFilter filter) {
        String stringFilter = new Gson().toJson(filter);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Preferences.KEY_GASTRO_FILTER, stringFilter);
        editor.apply();
    }

    public static GastroLocationFilter getGastroFilter(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String filterJson = prefs.getString(Preferences.KEY_GASTRO_FILTER, null);
        if (filterJson == null) {
            return new GastroLocationFilter();
        }
        return new Gson().fromJson(filterJson, GastroLocationFilter.class);
    }


    public static void saveFavorites(Context context, Set<String> favoriteIDs) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet(KEY_FAVORITES, favoriteIDs);
        editor.apply();
    }

    public static Set<String> getFavorites(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // returns a new cloned instance, because its not allowed to "work" on getStringSet result directly
        // see https://stackoverflow.com/questions/14034803/misbehavior-when-trying-to-store-a-string-set-using-sharedpreferences/14034804#14034804
        return new HashSet<>(prefs.getStringSet(Preferences.KEY_FAVORITES, new HashSet<String>()));
    }

    /**
     * return last modified date of the gastro location database, in milliseconds
     *
     * @return date in milliseconds or 0 if not set
     */
    public static long getGastroLastModified(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Preferences.KEY_GASTRO_LAST_MODIFIED, 0);
    }

    public static void saveGastroLastModified(Context context, long lastModified) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(Preferences.KEY_GASTRO_LAST_MODIFIED, lastModified);
        editor.apply();
    }

    /**
     * return last modified date of the shopping location database, in milliseconds
     *
     * @return date in milliseconds or 0 if not set
     */
    public static long getShoppingLastModified(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Preferences.KEY_SHOPPING_LAST_MODIFIED, 0);
    }

    public static void saveShoppingLastModified(Context context, long lastModified) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(Preferences.KEY_SHOPPING_LAST_MODIFIED, lastModified);
        editor.apply();
    }
}
