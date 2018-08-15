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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Locale;

public class ProximitySensorManager implements SensorEventListener {
    private static final String TAG = "ProximitySensorManager";

    private final SensorManager sensorManager;
    private final Sensor proximitySensor;

    private enum Distance { NEAR, FAR }
    private Distance lastDistance = Distance.FAR;
    private long lastTime = 0;
    private static final long WAVE_THRESHOLD = 2000;
    private static final long MIN_TIME_BETWEEN_SCREEN_ON_AND_OFF = 1500;

    private final Context context;

    private static volatile ProximitySensorManager instance;
    private final ScreenHandler screenHandler;
    private final Settings settings;

    private boolean listening = false;

    public static ProximitySensorManager getInstance(Context context) {
        if (instance == null ) {
            synchronized (ProximitySensorManager.class) {
                if (instance == null) {
                    instance = new ProximitySensorManager(context);
                }
            }
        }

        return instance;
    }

    private ProximitySensorManager(Context context) {
        this.context = context;
        this.screenHandler = ScreenHandler.getInstance(context);
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        this.settings = Settings.getInstance(context);
        start();
    }

    private void start() {
        if (!listening) {
            Log.d(TAG, "Registering proximity sensor listener.");
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            listening = true;
        } else {
            Log.d(TAG, "Proximity sensor listener is already registered. There is no need to register it again.");
        }
    }

    public final void startOrStopListeningDependingOnConditions() {
        WaveUpWorldState waveUpWorldState = new WaveUpWorldState(context);
        boolean startAllowedByWaveOrLockModes =
                (!waveUpWorldState.isScreenOn() && (settings.isPocketMode() || settings.isWaveMode())) ||
                (waveUpWorldState.isScreenOn() && settings.isLockScreen() && settings.isLockScreenAdmin());
        boolean startAllowedByOrientation = settings.isLockScreenWhenLandscape() || waveUpWorldState.isPortrait();
        boolean startAllowedByNoOngoingCall = !waveUpWorldState.isOngoingCall();

        Log.v(TAG, String.format(
                "start because of wave or lock modes: %s\n" +
                "start because of orientation: %s\n" +
                "start because of no ongoing call: %s" ,
                startAllowedByWaveOrLockModes,
                startAllowedByOrientation,
                startAllowedByNoOngoingCall));

        boolean start = startAllowedByWaveOrLockModes && startAllowedByOrientation && startAllowedByNoOngoingCall;

        if (start) {
            Log.d(TAG, "Starting because an event happened and the world in combination with the settings say I should start listening");
            start();
        } else {
            Log.d(TAG, "Stopping because an event happened and the world in combination with the settings say I should stop listening");
            stop();
        }
    }

    public final void stop() {
        ScreenHandler.getInstance(context).cancelTurnOff();
        if (listening) {
            Log.d(TAG, "Unregistering proximity sensor listener");
            sensorManager.unregisterListener(this);
            listening = false;
        } else {
            Log.d(TAG, "Proximity sensor listener is already unregistered. There is no need to unregister it again.");
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        Distance currentDistance = event.values[0] >= event.sensor.getMaximumRange() ? Distance.FAR : Distance.NEAR;
        Log.v(TAG, String.format(Locale.ENGLISH, "Proximity sensor changed: %s (current sensor value: %f - max. sensor value: %f)", currentDistance, event.values[0], event.sensor.getMaximumRange()));

        // If the sensor gets uncovered, there is possibly a thread waiting to turn off the screen. It needs to be interrupted.
        if (currentDistance == Distance.FAR) {
            screenHandler.cancelTurnOff();
        }

        boolean uncovered = lastDistance == Distance.NEAR && currentDistance == Distance.FAR;
        boolean covered = lastDistance == Distance.FAR && currentDistance == Distance.NEAR;

        long timeBetweenFarAndNear = currentTime - lastTime;

        boolean waved = timeBetweenFarAndNear <= WAVE_THRESHOLD;
        boolean tookOutOfPocket = timeBetweenFarAndNear > WAVE_THRESHOLD;

        if (uncovered && ((waved && settings.isWaveMode()) || (tookOutOfPocket && settings.isPocketMode()))) {
            long timeSinceLastScreenOnOrOff = currentTime - screenHandler.getLastTimeScreenOnOrOff();
            if (timeSinceLastScreenOnOrOff > MIN_TIME_BETWEEN_SCREEN_ON_AND_OFF) { // Don't do anything if it turned on or off 1.5 seconds ago
                screenHandler.turnOnScreen();
            } else {
                Log.d(TAG, "Time since last screen off: " + timeSinceLastScreenOnOrOff + ". Not switching it on");
            }
        } else if (covered) {
            screenHandler.turnOffScreen();
        }

        lastDistance = currentDistance;
        lastTime = currentTime;
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int i) {
    }
}