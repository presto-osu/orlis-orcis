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


package org.berlin_vegan.bvapp;

import android.app.Application;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.berlin_vegan.bvapp.acra.ACRAPostSender;

@ReportsCrashes(
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.something_went_wrong
)

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Log.i(TAG, "initialize application crash reporting");
            ACRA.init(this);
            ACRA.getErrorReporter().setReportSender(new ACRAPostSender());
        }
    }
}
