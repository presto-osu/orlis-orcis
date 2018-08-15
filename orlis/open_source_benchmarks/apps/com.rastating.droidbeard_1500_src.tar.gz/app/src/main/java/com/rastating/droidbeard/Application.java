/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard;

import android.content.Context;
import android.content.pm.PackageManager;

public class Application extends android.app.Application{
    private static Application mInstance;

    public static Application getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance;
    }

    public static String getVersionName() {
        Context context = getContext();
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String versionName = "Unknown";

        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
    }
}