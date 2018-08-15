/*
 * Copyright (C) 2015 Matthias Gabriel
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

package com.gabm.fancyplaces;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.os.Environment;

import com.gabm.fancyplaces.functional.LocationHandler;

import java.io.File;

/**
 * Created by gabm on 19/05/15.
 */
public class FancyPlacesApplication extends Application {
    public static final int TARGET_PIX_SIZE = 1000;
    public static final String TMP_IMAGE_FILENAME = "tmpFancyPlacesImg.png";
    static public final int MAP_DEFAULT_ZOOM_NEAR = 16;
    static public final int MAP_DEFAULT_ZOOM_FAR = 13;
    static public final int MAP_DEFAULT_DURATION = 3000;

    public static String TMP_IMAGE_FULL_PATH = "";
    public static String EXTERNAL_EXPORT_DIR = "";
    private static LocationHandler locationHandler = null;

    @Override
    public void onCreate() {


        // tmp file dir
        TMP_IMAGE_FULL_PATH = getExternalCacheDir() + File.separator + com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FILENAME;

        // external export dir
        EXTERNAL_EXPORT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getResources().getString(R.string.app_name) + File.separator;
        (new File(EXTERNAL_EXPORT_DIR)).mkdirs();

        // attach lifecycle callbacks to location handler
        locationHandler = new LocationHandler((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        registerActivityLifecycleCallbacks(locationHandler);
    }

    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
