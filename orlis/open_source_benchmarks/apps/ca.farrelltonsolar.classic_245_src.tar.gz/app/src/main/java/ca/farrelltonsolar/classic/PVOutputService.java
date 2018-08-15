/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.zip.GZIPOutputStream;

public class PVOutputService extends IntentService {

    PVOutputUploader uploader;
    private Timer pollTimer;
    private boolean isReceiverRegistered = false;
    private boolean isSlaveReceiverRegistered = false;
    Map<String, float[]> slaveControllerTotalEnergy = new HashMap<String, float[]>();

    public PVOutputService() {
        super("PVOutputService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (intent != null) {
                handleActionPVOutputUpload();
            }
        }
        catch (Exception ex) {
            Log.w(getClass().getName(), String.format("onHandleIntent failed ex: %s", ex));
        }
    }

    /**
     * Try to upload logs to PVOutput.
     */
    private void handleActionPVOutputUpload() {
        if (MonitorApplication.chargeControllers().uploadToPVOutput()) {
            String APIKey = MonitorApplication.chargeControllers().aPIKey();
            if (APIKey.length() > 0) {
                registerReceiver();
                pollTimer = new Timer();
                uploader = new PVOutputUploader(this.getBaseContext(), APIKey);
                pollTimer.schedule(uploader, 30000, 300000); // start in 30 seconds, repeat every 5 minutes
            }
        }
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(PVOutputService.this).registerReceiver(mDayLogReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
            isReceiverRegistered = true;
        }
        if (!isSlaveReceiverRegistered) {
            LocalBroadcastManager.getInstance(PVOutputService.this).registerReceiver(mSlaveReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS_SLAVE));
            isSlaveReceiverRegistered = true;
        }
    }

    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(PVOutputService.this).unregisterReceiver(mDayLogReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        if (isSlaveReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(PVOutputService.this).unregisterReceiver(mSlaveReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isSlaveReceiverRegistered = false;
        }
    }

    protected BroadcastReceiver mSlaveReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogEntry logs = (LogEntry) intent.getSerializableExtra("logs");
            if (logs != null) {
                String uniqueId = intent.getStringExtra("uniqueId");
                float[] f = logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY);
                slaveControllerTotalEnergy.put(uniqueId, f);
            }
        }
    };

    // Our handler for received Intents.
    private BroadcastReceiver mDayLogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LogEntry logs = (LogEntry) intent.getSerializableExtra("logs");
                PVOutputSetting setting = MonitorApplication.chargeControllers().getPVOutputSetting();
                if (setting != null) {
                    DateTime logDate = LogDate(setting);
                    if (logDate != null) {
                        DateTime rightNow = DateTime.now().withTimeAtStartOfDay();
                        int daysSinceLastLog = Days.daysBetween(logDate, rightNow).getDays();
                        if (daysSinceLastLog > 0) { // last log file was created before today? update it
                            saveLogs(logs, setting);
                        }
                    } else {
                        saveLogs(logs, setting); // never saved before!
                    }
                }
            } catch (Exception ex) {
                Log.w(getClass().getName(), String.format("SaveLogs failed ex: %s", ex));
            }
        }

        private void saveLogs(LogEntry logs, PVOutputSetting setting) {
            if (MonitorApplication.chargeControllers().showSystemView() == false) {
                unRegisterReceiver();
                float[] highWatts = logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY);
                Bundle toSave = new Bundle();
                toSave.putFloatArray(String.valueOf(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY), highWatts);
                MonitorApplication.chargeControllers().resetCurrentPVOutputLogs();
                setting.setPVOutputLogFilename(getLogDate());
                save(toSave, setting.getPVOutputLogFilename());
                Log.d(getClass().getName(), String.format("PVOutput save logs for upload for %s starting on thread: %s", setting.getPVOutputLogFilename(), Thread.currentThread().getName()));
            }
            else if (slaveControllerTotalEnergy.size() == (MonitorApplication.chargeControllers().classicCount() - 1)) { // received broadcasts from all other classic controllers
                unRegisterReceiver();
                float[] highWatts = logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY);
                for (float[] f : slaveControllerTotalEnergy.values()) {
                    int length = Math.min(f.length, highWatts.length);
                    for (int i = 0; i < length; i++) {
                        highWatts[i] += f[i];
                    }
                }
                Bundle toSave = new Bundle();
                toSave.putFloatArray(String.valueOf(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY), highWatts);
                MonitorApplication.chargeControllers().resetCurrentPVOutputLogs();
                setting.setPVOutputLogFilename(getLogDate());
                save(toSave, setting.getPVOutputLogFilename());
                Log.d(getClass().getName(), String.format("PVOutput save logs for upload for %s starting on thread: %s", setting.getPVOutputLogFilename(), Thread.currentThread().getName()));
            }
        }
    };

    public static DateTime LogDate(PVOutputSetting setting) {
        DateTime logDate = null;
        String fName = setting.getPVOutputLogFilename();
        if (fName != null && fName.length() > 0) {
            try {
                //this.logDate = String.format("PVOutput_%s.log", logDate) ;
                String logDateSubstring = fName.substring(9, 19);
                logDate = DateTime.parse(logDateSubstring, DateTimeFormat.forPattern("yyyy-MM-dd"));
            } catch (Exception ex) {
                Log.w("PVOutputService", String.format("LogDate parse filename failed ex: %s", ex));
            }
        }
        return logDate;
    }

    private String getLogDate() {
        DateTime today = DateTime.now().withTimeAtStartOfDay();
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(today);
    }

    public void save(final Bundle bundle, String file) {
        try {
            FileOutputStream fOut = MonitorApplication.getAppContext().openFileOutput(file, Context.MODE_PRIVATE);
            fOut.write(serializeBundle(bundle));
            fOut.close();
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("save failed ex: %s", ex));
        }
    }

    private byte[] serializeBundle(final Bundle bundle) {
        byte[] rval = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            rval = bos.toByteArray();
        } catch (IOException ex) {
            Log.w(getClass().getName(), String.format("serializeBundle failed ex: %s", ex));

        } finally {
            parcel.recycle();
        }
        return rval;
    }
}
