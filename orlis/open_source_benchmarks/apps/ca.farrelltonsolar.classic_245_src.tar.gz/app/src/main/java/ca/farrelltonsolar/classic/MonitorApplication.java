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

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Graham on 26/12/13.
 */
public class MonitorApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static Context context;

    static Map<Integer, String> chargeStates = new HashMap<Integer, String>();
    static Map<Integer, String> chargeStateTitles = new HashMap<Integer, String>();
    static Map<Integer, Pair<Severity, String>> messages = new HashMap<Integer, Pair<Severity, String>>();
    static UDPListener UDPListenerService;
    static boolean isUDPListenerServiceBound = false;
    private static ChargeControllers chargeControllers;
    private static Gson GSON = new Gson();
    ComplexPreferences configuration;
    WifiManager.WifiLock wifiLock;
    ModbusService modbusService;
    static boolean isModbusServiceBound = false;
    private Timer disconnectTimer;

    @Override
    protected void finalize() throws Throwable {
        try {
            if (isUDPListenerServiceBound) {
                UDPListenerService.stopListening();
                unbindService(UDPListenerServiceConnection);
            }
            if (isModbusServiceBound) {
                unbindService(modbusServiceConnection);
            }
        } catch (Exception ex) {
            Log.w(getClass().getName(), "onActivityDestroyed exception ex: " + ex);
        }
        super.finalize();
    }

    public void onCreate() {
        super.onCreate();

        MonitorApplication.context = getApplicationContext();
        if (Constants.DEVELOPER_MODE) {
            StrictMode.enableDefaults();
        }
        InitializeChargeStateLookup();
        InitializeChargeStateTitleLookup();
        InitializeMessageLookup();
        try {
            configuration = ComplexPreferences.getComplexPreferences(this, null, Context.MODE_PRIVATE);
            chargeControllers = configuration.getObject("devices", ChargeControllers.class);
        }
        catch (Exception ex) {
            Log.w(getClass().getName(), "getComplexPreferences failed to load");
            chargeControllers = null;
        }
        if (chargeControllers == null) { // save empty collection
            chargeControllers = new ChargeControllers(context);
        }
        if (chargeControllers.uploadToPVOutput()) {
            startService(new Intent(this, PVOutputService.class)); // start PVOutputService intent service
        }
        this.registerActivityLifecycleCallbacks(this);
        bindService(new Intent(this, UDPListener.class), UDPListenerServiceConnection, Context.BIND_AUTO_CREATE);
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            wifiLock = wifi.createWifiLock("ClassicMonitor");
        }
        bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(getClass().getName(), "onCreate complete");
    }

    public static Pair<Severity, String> getMessage(int cs) {
        if (messages.containsKey(cs)) {
            return messages.get(cs);
        }
        return null;
    }

    private void InitializeMessageLookup() {
        messages.put(0x00000001, new Pair(Severity.alert, getString(R.string.info_message_1)));
        messages.put(0x00000002, new Pair(Severity.alert, getString(R.string.info_message_2)));
        messages.put(0x00000100, new Pair(Severity.info, getString(R.string.info_message_100)));
        messages.put(0x00000200, new Pair(Severity.warning, getString(R.string.info_message_200)));
        messages.put(0x00000400, new Pair(Severity.warning, getString(R.string.info_message_400)));
        messages.put(0x00004000, new Pair(Severity.info, getString(R.string.info_message_4000)));
        messages.put(0x00008000, new Pair(Severity.info, getString(R.string.info_message_8000)));
        messages.put(0x00010000, new Pair(Severity.alert, getString(R.string.info_message_10000)));
        messages.put(0x00020000, new Pair(Severity.alert, getString(R.string.info_message_20000)));
        messages.put(0x00040000, new Pair(Severity.alert, getString(R.string.info_message_40000)));
        messages.put(0x00100000, new Pair(Severity.alert, getString(R.string.info_message_100000)));
        messages.put(0x00400000, new Pair(Severity.warning, getString(R.string.info_message_400000)));
        messages.put(0x08000000, new Pair(Severity.warning, getString(R.string.info_message_8000000)));
    }

    public static Context getAppContext() {
        return MonitorApplication.context;
    }

    public static ChargeControllers chargeControllers() {
        return chargeControllers;
    }

    public static String getChargeStateText(int cs) {
        if (chargeStates.containsKey(cs)) {
            return chargeStates.get(cs);
        }
        return "";
    }

    public static String getChargeStateTitleText(int cs) {
        if (chargeStateTitles.containsKey(cs)) {
            return chargeStateTitles.get(cs);
        }
        return "";
    }

    private void InitializeChargeStateLookup() {
        chargeStates.put(-1, getString(R.string.NoConnection));
        chargeStates.put(0, getString(R.string.RestingDescription));
        chargeStates.put(3, getString(R.string.AbsorbDescription));
        chargeStates.put(4, getString(R.string.BulkMPPTDescription));
        chargeStates.put(5, getString(R.string.FloatDescription));
        chargeStates.put(6, getString(R.string.FloatMPPTDescription));
        chargeStates.put(7, getString(R.string.EqualizeDescription));
        chargeStates.put(10, getString(R.string.HyperVocDescription));
        chargeStates.put(18, getString(R.string.EqMPPTDescription));
    }

    private void InitializeChargeStateTitleLookup() {
        chargeStateTitles.put(-1, "");
        chargeStateTitles.put(0, getString(R.string.RestingTitle));
        chargeStateTitles.put(3, getString(R.string.AbsorbTitle));
        chargeStateTitles.put(4, getString(R.string.BulkMPPTTitle));
        chargeStateTitles.put(5, getString(R.string.FloatTitle));
        chargeStateTitles.put(6, getString(R.string.FloatMPPTTitle));
        chargeStateTitles.put(7, getString(R.string.EqualizeTitle));
        chargeStateTitles.put(10, getString(R.string.HyperVocTitle));
        chargeStateTitles.put(18, getString(R.string.EqMpptTitle));
    }

    private ServiceConnection UDPListenerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            UDPListener.UDPListenerServiceBinder binder = (UDPListener.UDPListenerServiceBinder) service;
            UDPListenerService = binder.getService();
            isUDPListenerServiceBound = true;
            if (chargeControllers().autoDetectClassic()) {
                UDPListenerService.listen(chargeControllers);
            }
            Log.d(getClass().getName(), "UDPListener ServiceConnected");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isUDPListenerServiceBound = false;
            UDPListenerService = null;
            Log.d(getClass().getName(), "UDPListener ServiceDisconnected");
        }
    };

    private ServiceConnection modbusServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(getClass().getName(), "ModbusService ServiceConnected");
            ModbusService.ModbusServiceBinder binder = (ModbusService.ModbusServiceBinder) service;
            modbusService = binder.getService();
            isModbusServiceBound = true;
            modbusService.monitorChargeControllers(MonitorApplication.chargeControllers());
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isModbusServiceBound = false;
            modbusService = null;
            Log.d(getClass().getName(), "ModbusService ServiceDisconnected");
        }
    };

    // Our handler for received Intents.
    private BroadcastReceiver addChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChargeControllerInfo cc = GSON.fromJson(intent.getStringExtra("ChargeController"), ChargeController.class);
            Log.d(getClass().getName(), String.format("adding new controller to list (%s)", cc.toString()));
            chargeControllers.add(cc);
            modbusService.monitorChargeControllers(chargeControllers());
        }
    };

    private BroadcastReceiver removeChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConfigurationChanged();
        }
    };

    public static void ConfigurationChanged() {
        if (UDPListenerService != null) {
            UDPListenerService.stopListening();
            if (chargeControllers().autoDetectClassic()) {
                UDPListenerService.listen(chargeControllers);
            }
        }
    }

    public static void monitorChargeController(int device) {
        if (device < 0 || device >= chargeControllers.count()) {
            return;
        }
        if (chargeControllers.setCurrent(device)) {
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
            Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER);
            pkg.putExtra("DifferentController", true);
            broadcaster.sendBroadcast(pkg); //notify activity
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (wifiLock != null) {
            wifiLock.acquire();
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (wifiLock != null) {
            wifiLock.release();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity.getLocalClassName().compareTo("MonitorActivity") == 0) {
            LocalBroadcastManager.getInstance(this).registerReceiver(addChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER));
            LocalBroadcastManager.getInstance(this).registerReceiver(removeChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER));
            if (disconnectTimer != null) {
                disconnectTimer.cancel();
                disconnectTimer.purge();
            }
            if (isModbusServiceBound && modbusService != null){
                modbusService.monitorChargeControllers(chargeControllers());
            }
            else {
                bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity.getLocalClassName().compareTo("MonitorActivity") == 0) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(addChargeControllerReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(removeChargeControllerReceiver);
            disconnectTimer = new Timer();
            disconnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 10 seconds unless Activity Resumed
                    modbusService.stopMonitoringChargeControllers();
                }
            }, 10000);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(getClass().getName(), "saving chargeController settings");
        if (configuration != null) {
            configuration.putObject("devices", chargeControllers);
            configuration.commit();
        }
    }

    public MonitorApplication() {
        super();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

}