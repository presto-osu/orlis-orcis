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

package com.jarsilio.android.waveup;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.jarsilio.android.waveup.receivers.OrientationReceiver;
import com.jarsilio.android.waveup.receivers.ScreenReceiver;

public class WaveUpService extends Service {
    private static final String TAG = "WakeUpService";
    private ProximitySensorManager proximitySensorManager;
    private OrientationReceiver orientationReceiver;
    private ScreenReceiver screenReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(orientationReceiver);
        unregisterReceiver(screenReceiver);
        proximitySensorManager.stop();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        proximitySensorManager = ProximitySensorManager.getInstance(getApplicationContext());
        registerScreenReceiver();
        registerOrientationReceiver();
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        if (screenReceiver == null) {
            screenReceiver = new ScreenReceiver();
        }
        registerReceiver(screenReceiver, filter);
    }

    private void registerOrientationReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        if (orientationReceiver == null) {
            orientationReceiver = new OrientationReceiver();
        }
        registerReceiver(orientationReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        proximitySensorManager.startOrStopListeningDependingOnConditions();
        return super.onStartCommand(intent, flags, startId);
    }
}
