/*
 * Copyright (c) 2016 Juan García Basilio
 *
 * This file is part of WaveUp.
 *
 * WaveUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WaveUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WaveUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jarsilio.android.waveup.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.jarsilio.android.waveup.ProximitySensorManager;

public class OrientationReceiver extends BroadcastReceiver {
    private static final String TAG = "OrientationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int orientation = context.getResources().getConfiguration().orientation;
        // Leaving this here only for logging purposes
        if (orientation == Configuration.ORIENTATION_PORTRAIT ||
                orientation == Configuration.ORIENTATION_UNDEFINED) {
            Log.d(TAG, "Changed orientation: portrait (or undefined).");
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "Changed orientation: landscape.");
        }

        ProximitySensorManager.getInstance(context).startOrStopListeningDependingOnConditions();
    }
}
