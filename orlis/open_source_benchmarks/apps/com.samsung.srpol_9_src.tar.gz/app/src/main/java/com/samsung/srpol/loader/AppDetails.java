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

package com.samsung.srpol.loader;

import java.io.File;
import java.util.Comparator;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Helper class containing details about given permission
 * 
 * This Class contains : String mPermissionName; String
 *         mPermissionLabel; String mPermissionDetails;
 */
public class AppDetails {
    public static final String TAG = "AppDetails";
    private static final ColorMatrixColorFilter mGrayscaleFilter;

    private String mAppName;
    private String mPackageName;
    private Drawable mAppIcon;
    private boolean mSystemApp;

    private int mSubCategoriesMask;

    private static AppListLoader mLoader;
    private boolean mEnabled;
    private boolean mMounted;
    private final File mApkFile;

    static {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        mGrayscaleFilter = new ColorMatrixColorFilter(matrix);
    }

    public AppDetails(AppListLoader loader, PackageInfo packageinfo) {
        mLoader = loader;
        mPackageName = packageinfo.applicationInfo.packageName;
        mEnabled = packageinfo.applicationInfo.enabled;
        mApkFile = new File(packageinfo.applicationInfo.sourceDir);
        if (!mApkFile.exists()) {
            mMounted = false;
            mAppName = packageinfo.applicationInfo.packageName;
        } else {
            mMounted = true;
            CharSequence label = packageinfo.applicationInfo.loadLabel(mLoader.getPm());
            mAppName = label != null ? label.toString()
                    : packageinfo.applicationInfo.packageName;
        }
        mSystemApp = (packageinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    /**
     * @return the mAppName
     */
    public String getAppName() {
        return mAppName;
    }

    /**
     * @return the mAppPackageName
     */
    public String getAppPackageName() {
        return mPackageName;
    }

    /**
     * @return the mAppIcon
     */
    public Drawable getAppIcon() {
        if (mAppIcon == null || !mMounted) {
            if (mApkFile.exists()) {
                try {
                    mAppIcon = mLoader.getPm().getApplicationIcon(mPackageName);
                } catch (NameNotFoundException e) {
                    mAppIcon = mLoader.getContext().getResources()
                            .getDrawable(android.R.drawable.sym_def_app_icon);
                }
                mMounted = true;
                return updateAppIconColor(mAppIcon);
            } else {
                mMounted = false;
                return updateAppIconColor(mLoader.getContext().getResources()
                        .getDrawable(android.R.drawable.sym_def_app_icon));
            }
        } else
            return mAppIcon;
    }

    public int getSubcategoriesMask() {
        return mSubCategoriesMask;
    }

    public boolean isInSubcategory(int subcategoryId) {
        return (mSubCategoriesMask & subcategoryId) > 0;
    }
    
    public boolean isInAllSubcategories(int subcategoryIds) {
        return (mSubCategoriesMask & subcategoryIds) == subcategoryIds;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean state) {
        mEnabled = state;
        updateAppIconColor(mAppIcon);
    }

    private Drawable updateAppIconColor(Drawable icon) {
        // disabling and enabling apps in system application manager is
        // available since API 4.0
        if (icon != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mEnabled) {
                icon.setColorFilter(null);
            } else {
                icon.setColorFilter(mGrayscaleFilter);
            }
        }
        return icon;
    }

    /**
     * @return the mSystemApp
     */
    public boolean isSystemApp() {
        return mSystemApp;
    }

    public void addSubcategory(int id) {
        mSubCategoriesMask |= id;
    }

    /**
     * @return the mAppPackageName
     */
    @Override
    public String toString() {
        return mPackageName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AppDetails) {
            AppDetails appDetails = (AppDetails) o;
            if (appDetails.getAppPackageName().equals(mPackageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform inteligent comparison of application entry objects.
     */
    public static final Comparator<AppDetails> SMART_COMPARATOR = new Comparator<AppDetails>() {
        @Override
        public int compare(AppDetails object1, AppDetails object2) {
            if (object1.mSystemApp) {
                if (object2.mSystemApp)
                    return object1.mAppName.compareTo(object2.mAppName);
                return 1;
            } else {
                if (object2.mSystemApp)
                    return -1;
                return object1.mAppName.compareTo(object2.mAppName);
            }
        }
    };
}
