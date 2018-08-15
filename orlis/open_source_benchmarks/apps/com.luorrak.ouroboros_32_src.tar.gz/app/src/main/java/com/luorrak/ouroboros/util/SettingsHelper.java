package com.luorrak.ouroboros.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class SettingsHelper {
    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeValue = sharedPreferences.getString("theme_preference", "0");
        return Integer.valueOf(themeValue);
    }

    public static int getThreadView(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String threadView = sharedPreferences.getString("thread_view", "0");
        return Integer.valueOf(threadView);
    }

    public static void setThreadView(Context context, int layoutValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("thread_view", String.valueOf(layoutValue));
        editor.apply();
    }

    public static int getCatalogView(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String catalogView = sharedPreferences.getString("catalog_view", "0");
        return Integer.valueOf(catalogView);
    }

    public static void setCatalogView(Context context, int layoutValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("catalog_view", String.valueOf(layoutValue));
        editor.apply();
    }

    public static int getCatalogColumns(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String catalogGridColumns = sharedPreferences.getString("catalog_grid_columns", "3");
        return Integer.valueOf(catalogGridColumns);
    }

    public final static String BUMP_ORDER = "0";
    public final static String CREATION_DATE = "1";
    public final static String  REPLY_COUNT = "3";

    public static void setSortByMethod(Context context, String sortMethod) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sort_by", sortMethod);
        editor.apply();
    }

    public static String getSortByMethod(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sort_by = sharedPreferences.getString("sort_by", BUMP_ORDER);
        return sort_by;
    }

    public static String getDefaultName(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("default_name", "");
    }

    public static String getDefaultEmail(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("default_email", "");
    }

    public static int getImageOptions(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(sharedPreferences.getString("image_options", "1"));
    }

    public static String getPostPassword(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("post_password", "");
    }

    public static void setPostPassword(Context context, String postPassword){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("post_password", postPassword);
        editor.apply();
    }

    public static boolean getReplyCheckerStatus(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("pref_reply_checker", true);
    }
}
