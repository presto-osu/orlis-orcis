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

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

public class ScreenHandler {
    private static final String TAG = "ScreenHandler";

    private static final long TIME_SCREEN_ON = 5000;

    private final PowerManager powerManager;
    private final PowerManager.WakeLock wakeLock;
    private final DevicePolicyManager policyManager;
    private final WaveUpWorldState waveUpWorldState;

    private final Settings settings;

    private long lastTimeScreenOnOrOff;

    private final Context context;

    private static volatile ScreenHandler instance;

    private Thread turnOffScreenThread;
    private boolean turningOffScreen;

    private static final long MIN_TIME_SENSOR_COVERED_TO_TURN_SCREEN_OFF = 1000;

    public static ScreenHandler getInstance(Context context) {
        if (instance == null ) {
            synchronized (ScreenHandler.class) {
                if (instance == null) {
                    instance = new ScreenHandler(context);
                }
            }
        }

        return instance;
    }

    private ScreenHandler(Context context) {
        this.context = context;
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakeUpWakeLock");
        this.policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.settings = Settings.getInstance(context);
        this.waveUpWorldState = new WaveUpWorldState(context);
    }

    private Thread turnOffScreenThread(final long delay) {
        return new Thread() {
            @Override
            public void run() {
                if (waveUpWorldState.isScreenOn()) {
                    Log.d(TAG, "Creating a thread to turn off display if still covered in " + delay/1000 + " seconds");
                    try {
                        Thread.sleep(delay);
                        doTurnOffScreen();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupted thread: Turning off screen cancelled.");
                    }
                }
            }
        };
    }

    private void doTurnOffScreen() {
        turningOffScreen = true; // Issue #68. Avoid interrupting the thread if screen is already being turned off.
        lastTimeScreenOnOrOff = System.currentTimeMillis();
        if (settings.isVibrateWhileLocking()) {
            vibrate();
        }
        Log.i( TAG, "Switched from 'far' to 'near'.");
        if (settings.isLockScreenWithPowerButton()) {
            Log.i( TAG, "Turning screen off simulating power button press.");
            Root.pressPowerButton();
        } else {
            Log.i( TAG, "Turning screen off.");
            policyManager.lockNow();
        }
        turningOffScreen = false;
    }

    public void turnOffScreen() {
        if (waveUpWorldState.isScreenOn()) {
            if (settings.isVibrateWhileLocking()) {
                vibrate();
            }
            turnOffScreenThread = turnOffScreenThread(MIN_TIME_SENSOR_COVERED_TO_TURN_SCREEN_OFF);
            turnOffScreenThread.start();
        }
    }

    public void cancelTurnOff() {
        if (turnOffScreenThread != null && !turningOffScreen) {
            Log.d(TAG, "Cancelling turning off of display");
            turnOffScreenThread.interrupt();
            turnOffScreenThread = null;
        }
    }

    public long getLastTimeScreenOnOrOff() {
        return lastTimeScreenOnOrOff;
    }

    public void turnOnScreen() {
        if (!waveUpWorldState.isScreenOn()) {
            lastTimeScreenOnOrOff = System.currentTimeMillis();
            Log.i( TAG, "Switched from 'near' to 'far'. Turning screen on");
            wakeLock.acquire(TIME_SCREEN_ON);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
    }
}