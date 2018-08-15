/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.utils;

import com.samsung.srpol.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class Utils {

    private static final String SCHEME = "package";

    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";

    private static final String APP_PKG_NAME_22 = "pkg";

    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";

    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    private static final String MY_PREFERENCES = "my_preferences";

    private static Drawable mSystemIcon;
    private static Drawable mSystemIconDisable;

    /**
     * @return the mSystemIcon
     */
    public static Drawable getmSystemIcon(Context context) {
        if (mSystemIcon == null) {
            return mSystemIcon = context.getResources().getDrawable(
                    R.drawable.ic_system_red_dark);
        } else {
            return mSystemIcon;
        }
    }

    /**
     * @return the mSystemIcon
     */
    public static Drawable getmSystemIconDisable(Context context) {
        if (mSystemIconDisable == null) {
            return mSystemIconDisable = context.getResources().getDrawable(
                    R.drawable.ic_system_red_disable);
        } else {
            return mSystemIconDisable;
        }
    }

    /**
     * Show Platform's Settings app Window with details about given packageName
     * app
     * 
     * @param context
     *            App context
     * @param packageName
     *            Package name of requested app details
     */
    public static void showInstalledAppDetails(Context context,
            String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            // TODO not tested
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    public static void unistallApp(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 14) { // above 4.0
            intent.setAction(Intent.ACTION_DELETE);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 4.0
            // TODO not tested
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String getAppVersionCode(Context context) {
        String versionName;
        try {
            versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "not found";
            e.printStackTrace();
        }
        return versionName;
    }

    public static boolean isFirstRun(Context context) {
        final SharedPreferences reader = context.getSharedPreferences(
                MY_PREFERENCES, Context.MODE_PRIVATE);
        final boolean first = reader.getBoolean("is_first", true);
        if (first) {
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", false);
            editor.commit();
        }
        return first;
    }

    public static void startBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
