/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class ModbusService extends Service {


    private final IBinder mBinder = new ModbusServiceBinder();

    private List<ModbusTask> tasks = new ArrayList<>();
    public ModbusService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(getClass().getName(), "onBind");
        return mBinder;
    }

    @Override
    public File getCacheDir() {
        return super.getCacheDir();
    }

    public class ModbusServiceBinder extends Binder {
        ModbusService getService() {
            return ModbusService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(getClass().getName(), "onCreate");
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(removeChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER));
    }

    private BroadcastReceiver removeChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uniqueId = intent.getStringExtra("uniqueId");
            for (ModbusTask task : tasks) {
                if (task.chargeController().uniqueId().compareTo(uniqueId) == 0) {
                    Disconnector d = new Disconnector(task);
                    d.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(removeChargeControllerReceiver);
        stopMonitoringChargeControllers();
        super.onDestroy();
    }

    public void monitorChargeControllers(ChargeControllers controllers) {
        if (controllers == null || controllers.count() == 0) {
            return;
        }
        if (controllers.systemViewEnabled()) {
            int count = controllers.count();
            for (int i = 0; i < count; i++) {
                ChargeController controller = controllers.get(i);
                if (isBeingMonitored(controller) == false) {
                    ModbusTask task = new ModbusTask(controller, this.getBaseContext());
                    tasks.add(task);
                    Timer pollTimer = new Timer();
                    pollTimer.schedule(task, 100, Constants.MODBUS_POLL_TIME);
                    Log.d(getClass().getName(), String.format("Monitor running on: %s this thread is %s", controller.toString(), Thread.currentThread().getName()));
                }
            }
        }
        else {
            ChargeController controller = controllers.getCurrentChargeController();
            if (controller != null) {
                if (isBeingMonitored(controller) == false) {
                    stopMonitoringChargeControllers();
                    ModbusTask task = new ModbusTask(controller, this.getBaseContext());
                    tasks.add(task);
                    Timer pollTimer = new Timer();
                    pollTimer.schedule(task, 100, Constants.MODBUS_POLL_TIME);
                    Log.d(getClass().getName(), String.format("Monitor running on: %s this thread is %s", controller.toString(), Thread.currentThread().getName()));
                }
            }
        }
    }

    private boolean isBeingMonitored(ChargeController controller) {
        boolean rVal = false;
        for (ModbusTask task : tasks) {
            if (task.chargeController().equals(controller)) {
                rVal = true;
                break;
            }
        }
        return rVal;
    }


    public void stopMonitoringChargeControllers() {
        try {
            if (!tasks.isEmpty()) {
                for (ModbusTask task : tasks) {
                    Log.d(getClass().getName(), String.format("stopMonitoringChargeController: %s this thread is %s", task.chargeController().toString(), Thread.currentThread().getName()));
                    Disconnector d = new Disconnector(task);
                    d.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                tasks.clear();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isInService() {
        return !tasks.isEmpty();
    }

    private class Disconnector extends AsyncTask<String, Void, String> {

        private Disconnector(ModbusTask task) {
            this.task = task;
        }

        ModbusTask task;

        @Override
        protected String doInBackground(String... params) {
            Log.d(getClass().getName(), String.format("Cancelling task for: %s this thread is %s", task.chargeController().toString(), Thread.currentThread().getName()));
            task.cancel();
            task = null;
            return null;
        }
    }

}
