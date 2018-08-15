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
import android.util.Log;

import com.jarsilio.android.waveup.ProximitySensorManager;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { // I am leaving this if just for the logging
            Log.d(TAG, "Screen off");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d(TAG, "Screen on");
        }
        ProximitySensorManager.getInstance(context).startOrStopListeningDependingOnConditions();
    }
}