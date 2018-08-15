/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import org.pixmob.freemobile.netstat.content.NetstatContract.Events;
import org.pixmob.freemobile.netstat.ui.Netstat;
import org.pixmob.freemobile.netstat.util.IntentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.pixmob.freemobile.netstat.BuildConfig.DEBUG;
import static org.pixmob.freemobile.netstat.Constants.ACTION_NOTIFICATION;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_AUTO_RESTART_SERVICE;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_AUTO_SEND_PHONE_LISTENER_EVENTS;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_NOTIF_ACTIONS;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_4G;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_FEMTO;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_FREE_MOBILE;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_STAT_NOTIF_SOUND_ORANGE;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_THEME;
import static org.pixmob.freemobile.netstat.Constants.SP_NAME;
import static org.pixmob.freemobile.netstat.Constants.TAG;
import static org.pixmob.freemobile.netstat.Constants.THEME_COLOR;
import static org.pixmob.freemobile.netstat.Constants.THEME_DEFAULT;
import static org.pixmob.freemobile.netstat.Constants.THEME_PIE;

/**
 * This foreground service is monitoring phone state and battery level. A notification shows which mobile network is the
 * phone is connected to.
 * 
 * @author Pixmob
 */
public class MonitorService extends Service implements OnSharedPreferenceChangeListener {
    /**
     * Notification themes.
     */
    private static final Map< String, Theme> THEMES = new HashMap<>(3);
    /**
     * Match network types from {@link TelephonyManager} with the corresponding string.
     */
    private static final SparseIntArray NETWORK_TYPE_STRINGS = new SparseIntArray(10);
    /**
     * Special data used for terminating the PendingInsert worker thread.
     */
    private static final Event STOP_PENDING_CONTENT_MARKER = new Event();
    /**
     * Femtocell's LAC start with this code.
     */
    private static final String FREE_MOBILE_FEMTOCELL_LAC_CODE = "98";
    /**
     * SDK Versions concerned with service auto-kill issue.
     */
    public static final String[] ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE = new String[] { "4.4", "4.4.1", "4.4.2" };
    /**
     * One+2
     */
    public static final String ONE_PLUS_TWO_MANUFACTURER = "OnePlus";
    public static final String ONE_PLUS_TWO_MODEL = "ONE A2003";

    /**
     * Intent extra when requesting service restart after died
     */
    private static final String INTENT_ALARM_RESTART_SERVICE_DIED = "ALARM_RESTART_SERVICE_DIED";

    /**
     * Intent extra when requesting service restart after died
     */
    public static final String INTENT_UPDATE_NOTIF_ON_LOCKSCREEN = "UPDATE_NOTIF_ON_LOCKSCREEN";

    private static final List<Integer> FEMTOCELL_AVAILABLE_NETWORK_TYPE = new ArrayList<>();

    private static final String[] POWEROFF_INTENT_FILTER_ACTIONS = {
            Intent.ACTION_SHUTDOWN,
            // HTC devices use a different Intent action:
            // http://stackoverflow.com/q/5076410/422906
            "android.intent.action.QUICKBOOT_POWEROFF",
    };

    /**
     * This intent will open the main UI.
     */
    private PendingIntent openUIPendingIntent;
    private PendingIntent networkOperatorSettingsPendingIntent;
    private PendingIntent wirelessSettingsPendingIntent;
    private PowerManager pm;
    private TelephonyManager tm;
    private int telephonyManagerEvents;
    private ConnectivityManager cm;
    private BroadcastReceiver broadcastReceiver;
    private PhoneStateListener phoneMonitor;
    private Boolean lastWifiConnected;
    private Boolean lastMobileNetworkConnected;
    private boolean powerOn = true;
    private int batteryLevel;
    private boolean firstInsert = true;
    private String lastMobileOperatorId;
    private String mobileOperatorId;
    private boolean isFemtocell;
    private Boolean lastIsFemtocell;
    private boolean mobileNetworkConnected;
    private Integer lastMobileNetworkType;
    private Integer lastMobileNetworkTypeForLTEDetect;
    private int mobileNetworkType;
    private BlockingQueue< Event> pendingInsert;
    private SharedPreferences prefs;
    private Bitmap freeLargeIcon;
    private Bitmap freeFemtoLargeIcon;
    private Bitmap orangeLargeIcon;

	static {
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_EDGE, R.string.network_type_edge);
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_GPRS, R.string.network_type_gprs);
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_HSDPA, R.string.network_type_hsdpa);
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_HSPA, R.string.network_type_hspa);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_HSPAP, R.string.network_type_hspap);
        }
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_HSUPA, R.string.network_type_hsupa);
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_UMTS, R.string.network_type_umts);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_LTE, R.string.network_type_lte);
        }
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_CDMA, R.string.network_type_cdma);
        NETWORK_TYPE_STRINGS.put(TelephonyManager.NETWORK_TYPE_UNKNOWN, R.string.network_type_unknown);

        FEMTOCELL_AVAILABLE_NETWORK_TYPE.add(TelephonyManager.NETWORK_TYPE_HSDPA);
        FEMTOCELL_AVAILABLE_NETWORK_TYPE.add(TelephonyManager.NETWORK_TYPE_HSPA);
        FEMTOCELL_AVAILABLE_NETWORK_TYPE.add(TelephonyManager.NETWORK_TYPE_HSUPA);
        FEMTOCELL_AVAILABLE_NETWORK_TYPE.add(TelephonyManager.NETWORK_TYPE_UMTS);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            FEMTOCELL_AVAILABLE_NETWORK_TYPE.add(TelephonyManager.NETWORK_TYPE_HSPAP);
        }

        THEMES.put(THEME_DEFAULT, new Theme(R.drawable.ic_stat_notify_service_free,
            R.drawable.ic_stat_notify_service_free_femto, R.drawable.ic_stat_notify_service_orange));
        THEMES.put(THEME_COLOR, new Theme(R.drawable.ic_stat_notify_service_free_color,
            R.drawable.ic_stat_notify_service_free_femto_color, R.drawable.ic_stat_notify_service_orange_color));
        THEMES.put(THEME_PIE, new Theme(R.drawable.ic_stat_notify_service_free_pie,
        		R.drawable.ic_stat_notify_service_free_femto_pie, R.drawable.ic_stat_notify_service_orange_pie));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SP_KEY_THEME.equals(key) || SP_KEY_ENABLE_NOTIF_ACTIONS.equals(key)) {
            updateNotification(false, false);
        }
    }

    private boolean checkRequiredPermissions() {
        if (PermissionsManager.checkRequiredPermissions(this) != PackageManager.PERMISSION_GRANTED) {
            Intent mainActivityIntent = new Intent(this, Netstat.class);
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivityIntent);
            stopSelf();
            return false;
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate() {
        super.onCreate();

        if (!checkRequiredPermissions()) {
            return;
        }

        pm = (PowerManager) getSystemService(POWER_SERVICE);
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        
        prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final int largeIconWidth =
                getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            final int largeIconHeight =
                getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            if ((largeIconWidth > 0) && (largeIconHeight > 0)) {
                Bitmap freeLargeIconTmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notify_service_free_large);
                if ((freeLargeIconTmp != null) && (freeLargeIconTmp.getWidth() > 0) && (freeLargeIconTmp.getHeight() > 0)) {
                    freeLargeIcon = Bitmap.createScaledBitmap(freeLargeIconTmp, largeIconWidth, largeIconHeight, true);
                }

                Bitmap freeFemtoLargeIconTmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notify_service_free_femto_large);
                if ((freeFemtoLargeIconTmp != null) && (freeFemtoLargeIconTmp.getHeight() > 0) && (freeFemtoLargeIconTmp.getWidth() > 0)) {
                    freeFemtoLargeIcon = Bitmap.createScaledBitmap(freeFemtoLargeIconTmp, largeIconWidth, largeIconHeight, true);
                }

                Bitmap orangeLargeIconTmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notify_service_orange_large);
                if ((orangeLargeIconTmp != null) && (orangeLargeIconTmp.getHeight() > 0) && (orangeLargeIconTmp.getWidth() > 0)) {
                    orangeLargeIcon = Bitmap.createScaledBitmap(orangeLargeIconTmp, largeIconWidth, largeIconHeight, true);
                }
            }
        }

        // Initialize and start a worker thread for inserting rows into the
        // application database.
        final Context c = getApplicationContext();
        pendingInsert = new ArrayBlockingQueue<>(8);
        new PendingInsertWorker(c, pendingInsert).start();

        // This intent is fired when the application notification is clicked.
        openUIPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_NOTIFICATION), PendingIntent.FLAG_CANCEL_CURRENT);

        // This intent is only available as a Jelly Bean notification action in
        // order to open network operator settings.
        Intent networkSettingsIntent = IntentFactory.networkOperatorSettings(this);
        if (networkSettingsIntent != null) {
            networkOperatorSettingsPendingIntent = PendingIntent.getActivity(this, 0, networkSettingsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        Intent wirelessSettingsIntent = IntentFactory.wirelessSettings(this);
        if (wirelessSettingsIntent != null) {
            wirelessSettingsPendingIntent = PendingIntent.getActivity(this, 0, wirelessSettingsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        // Initialize battery level. Do this before initializing events, so that we don't risk to put wrong value for battery.
        batteryLevel = getBatteryLevel();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (Arrays.asList(POWEROFF_INTENT_FILTER_ACTIONS).contains(action)) {
                    onDeviceShutdown();
                    return;
                }

                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
                    if (!onConnectivityUpdated())
                        return;

                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    if (level != -1 && scale != -1) {
                        batteryLevel = parseBatteryLevel(level, scale);
                    }
                }

                updateEventDatabase();
            }
        };

        final IntentFilter intentFilter = new IntentFilter();

        // Watch screen light: is the screen on?
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        // Watch Wi-Fi connections
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        // Watch battery level.
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        // Watch shutdown
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction("android.intent.action.QUICKBOOT_POWEROFF");

        registerReceiver(broadcastReceiver, intentFilter);


        // Watch mobile connections.
        phoneMonitor = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                updateService();
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                if (stopServiceIfSimOperatorIsNotFreeMobile())
                    return;

                mobileNetworkConnected =
                        (serviceState != null) && (serviceState.getState() == ServiceState.STATE_IN_SERVICE);

                updateService();
            }

            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                updateService();
            }

            private void updateService() {
                if (tm != null) {
                    mobileNetworkType = tm.getNetworkType(); //update the network type to have the latest
                }
                final int phoneStateUpdated = onPhoneStateUpdated();
                if (phoneStateUpdated >= 0)
                    updateEventDatabase();

                updateNotification(true, phoneStateUpdated == 1);
            }
        };
        telephonyManagerEvents = PhoneStateListener.LISTEN_SERVICE_STATE |
                                 PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            telephonyManagerEvents |= PhoneStateListener.LISTEN_CELL_INFO;

        tm.listen(phoneMonitor, telephonyManagerEvents);

        if (prefs.getBoolean(SP_KEY_ENABLE_AUTO_RESTART_SERVICE, false) &&
                Arrays.asList(ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE).contains(Build.VERSION.RELEASE)) {
            // Kitkat and JellyBean auto-kill service workaround
            // http://stackoverflow.com/a/20735519/1527491
            ensureServiceStaysRunning();
        }

        if (prefs.getBoolean(SP_KEY_ENABLE_AUTO_SEND_PHONE_LISTENER_EVENTS, false) &&
                ONE_PLUS_TWO_MANUFACTURER.equals(Build.MANUFACTURER) && ONE_PLUS_TWO_MODEL.equals(Build.MODEL)) {
            // One+2 (OxygenOS) does not send PhoneState events when the application is in foreground.
            // This is a bug. We are still waiting for a solution. This workaround will force PhoneState to refresh.
            refreshPhoneStatePeriodically();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!checkRequiredPermissions()) {
            return;
        }

        // Tell the PendingInsert worker thread to stop.
        try {
            pendingInsert.put(STOP_PENDING_CONTENT_MARKER);
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to stop PendingInsert worker thread", e);
        }

        // Stop listening to system events.
        unregisterReceiver(broadcastReceiver);
        tm.listen(phoneMonitor, PhoneStateListener.LISTEN_NONE);

        tm = null;
        cm = null;
        pm = null;

        // Remove the status bar notification.
        stopForeground(true);

        prefs.unregisterOnSharedPreferenceChangeListener(this);
        prefs = null;

        if (freeLargeIcon != null) {
            freeLargeIcon.recycle();
            freeLargeIcon = null;
        }
        if (orangeLargeIcon != null) {
            orangeLargeIcon.recycle();
            orangeLargeIcon = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!checkRequiredPermissions()) {
            return START_NOT_STICKY;
        }

        if (intent != null) //we have intent to handle
        {
            if (intent.getBooleanExtra(INTENT_ALARM_RESTART_SERVICE_DIED, false)) { //intent to check service is alive
                if (DEBUG)
                    Log.d(TAG, "onStartCommand > after ALARM_RESTART_SERVICE_DIED [ Kitkat START_STICKY bug ]");
                if (isRunning()) {
                    if (DEBUG)
                        Log.d(TAG, "onStartCommand > Service already running - return immediately... [ Kitkat START_STICKY bug ]");
                    ensureServiceStaysRunning();
                    return START_STICKY;
                }
            }

            if (intent.getBooleanExtra(INTENT_UPDATE_NOTIF_ON_LOCKSCREEN, false)) { //intent to update the notification on lockscreen (hide / show)
                if (DEBUG) {
                    Log.d(TAG, "onStartCommand > update the notification on lockscreen (hide / show)");
                }
                updateNotification(false, false);
            }
        }

        if (stopServiceIfSimOperatorIsNotFreeMobile()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        
        // Update with current state.
        onConnectivityUpdated();
        onPhoneStateUpdated();
        updateNotification(false, false);

        return START_STICKY;
    }

    /**
     * Stops the service if sim operator is not free mobile.
     *
     * @return true if the service was killed
     */
    private boolean stopServiceIfSimOperatorIsNotFreeMobile() {
        if (!DEBUG && MobileOperator.FREE_MOBILE.isCurrentSimOwner(getApplicationContext()) == 0) {
            stopSelf();
            return true;
        }

        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (prefs.getBoolean(SP_KEY_ENABLE_AUTO_RESTART_SERVICE, false) &&
                Arrays.asList(ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE).contains(Build.VERSION.RELEASE)) {
            // If task was removed, we should launch the service again.
            if (DEBUG)
                Log.d(TAG, "onTaskRemoved > setting alarm to restart service [ Kitkat START_STICKY bug ]");
            Intent restartService = new Intent(getApplicationContext(),
                    this.getClass());
            restartService.setPackage(getPackageName());
            PendingIntent restartServicePI = PendingIntent.getService(
                    getApplicationContext(), 1, restartService,
                    PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
        }

    }
    
    private boolean isRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (MonitorService.class.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }
    
    private void ensureServiceStaysRunning() {
        // KitKat appears to have (in some cases) forgotten how to honor START_STICKY
        // and if the service is killed, it doesn't restart.  On an emulator & AOSP device, it restarts...
        // on my CM device, it does not - WTF?  So, we'll make sure it gets back
        // up and running in a minimum of 10 minutes.  We reset our timer on a handler every
        // 2 minutes...but since the handler runs on uptime vs. the alarm which is on realtime,
        // it is entirely possible that the alarm doesn't get reset.  So - we make it a noop,
        // but this will still count against the app as a wakelock when it triggers.  Oh well,
        // it should never cause a device wakeup.  We're also at SDK 19 preferred, so the alarm
        // mgr set algorithm is better on memory consumption which is good.
    	// http://stackoverflow.com/a/20735519/1527491
        if (prefs.getBoolean(SP_KEY_ENABLE_AUTO_RESTART_SERVICE, false) &&
        		Arrays.asList(ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE).contains(Build.VERSION.RELEASE))
        {
        	if (DEBUG)
        		Log.d(TAG, "ensureServiceStaysRunning > setting alarm. [ Kitkat START_STICKY bug ]");
        	// A restart intent - this never changes...        
            final int restartAlarmInterval = 10*60*1000;
            final int resetAlarmTimer = 1*60*1000;
            final Intent restartIntent = new Intent(this, MonitorService.class);
            restartIntent.putExtra(INTENT_ALARM_RESTART_SERVICE_DIED, true);
            final AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Handler restartServiceHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    // Create a pending intent
                    PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, restartIntent, 0);
                    alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + restartAlarmInterval, pintent);
                    sendEmptyMessageDelayed(0, resetAlarmTimer);
                }            
            };
            restartServiceHandler.sendEmptyMessageDelayed(0, 0); 
        }
    }

    private void refreshPhoneStatePeriodically() {
        if (DEBUG) Log.d(TAG, "refreshPhoneStatePeriodically > Setting service restart");
        final Handler h = new Handler();
        final int delay = 60 * 1000; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){
                if (tm == null) {
                    return;
                }

                tm.listen(phoneMonitor, PhoneStateListener.LISTEN_NONE);
                tm.listen(phoneMonitor, telephonyManagerEvents);
                refreshPhoneStatePeriodically();
            }
        }, delay);
    }

    /**
     * Update the status bar notification.
     * @param playSound play notification sound
     * @param phoneStateUpdated if phone state has been updated
     */
	private void updateNotification(boolean playSound, boolean phoneStateUpdated) {
        String tickerText, contentText;
        int smallIcon;
        final int notificationPriority = NotificationCompat.PRIORITY_LOW;
        Bitmap largeIcon;
        final PendingIntent contentIntent = openUIPendingIntent;
        boolean airplaneModeOn = false;

        MobileOperator mobOp = MobileOperator.fromString(mobileOperatorId);

        if (mobOp == null) { // Not Free Mobile nor Orange
        	if (airplaneModeOn = isAirplaneModeOn()) { // Airplane mode
	            tickerText = getString(R.string.stat_airplane_mode_on);
	            contentText = getString(R.string.notif_monitoring_disabled);
        	} else if (mobileOperatorId == null) { // No signal
	            tickerText = getString(R.string.stat_no_signal);
	            contentText = getString(R.string.notif_action_open_network_operator_settings);
        	} else { // Foreign operator
	            tickerText = getString(R.string.stat_connected_to_foreign_mobile_network);
	            contentText = getString(R.string.notif_action_open_network_operator_settings);
        	}

            smallIcon = android.R.drawable.stat_sys_warning;
            largeIcon = null; // Use small icon as large icon.

        } else { // Free Mobile or Orange detected
            tickerText = String.format(getString(R.string.stat_connected_to_mobile_network), mobOp.toName(this));

            final Integer networkTypeRes = NETWORK_TYPE_STRINGS.get(mobileNetworkType, R.string.network_type_unknown);
            contentText = String.format(getString(R.string.mobile_network_type), getString(networkTypeRes));
            if (MobileOperator.FREE_MOBILE.equals(mobOp) && isFemtocell) {
                contentText = getString(R.string.network_free_femtocell, contentText);
            }

            smallIcon = getStatIcon(mobOp);
            largeIcon = getStatLargeIcon(mobOp);
        }

        final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext());
        nBuilder.setSmallIcon(smallIcon)
                .setContentTitle(tickerText).setContentText(contentText).setTicker(tickerText)
                .setContentIntent(contentIntent) // always set the content intent - exception fired on GB if null
                .setPriority(notificationPriority)
                .setWhen(0);

        //see : http://stackoverflow.com/questions/15642900/bad-notification-posted-from-package-couldnt-expand-remoteviews
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            nBuilder.setLargeIcon(largeIcon);
        }

        if  ((prefs != null) && (prefs.getBoolean(SP_KEY_ENABLE_NOTIF_ACTIONS, true))) {
            if (airplaneModeOn && wirelessSettingsPendingIntent != null) {
                nBuilder.addAction(android.R.drawable.ic_menu_preferences,
                        getString(R.string.notif_action_open_wireless_settings),
                        wirelessSettingsPendingIntent);
            }
            else if (networkOperatorSettingsPendingIntent != null) {
                nBuilder.addAction(android.R.drawable.ic_menu_preferences,
                        getString(R.string.notif_action_open_network_operator_settings),
                        networkOperatorSettingsPendingIntent);
            }
        }

        if (playSound) {
            Log.d(TAG, "Play notification sound");

            // check if we need to trigger LTE alarm
            // network type changed from 3G to LTE
            boolean lteAlarm = ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    && (lastMobileNetworkTypeForLTEDetect != null)
                    && (lastMobileNetworkTypeForLTEDetect != TelephonyManager.NETWORK_TYPE_LTE)
                    && (mobileNetworkType == TelephonyManager.NETWORK_TYPE_LTE));

            // we have just connected on a femtocell, trigger sound
            boolean femtocellConnection = ((isFemtocell) && (lastIsFemtocell != null)
                    && (!lastIsFemtocell) && MobileOperator.FREE_MOBILE.equals(mobOp));

            // other case : trigger FreeMobile 3G alarm if we changed network type from LTE to 3G
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // first, are we using a compatible android version ?
                    && (mobOp == MobileOperator.FREE_MOBILE) // second, are we on FreeMobile network ?
                    && (!lteAlarm) // third, are not we on LTE network
                    && (lastMobileNetworkTypeForLTEDetect != null)
                    && (lastMobileNetworkTypeForLTEDetect.equals(TelephonyManager.NETWORK_TYPE_LTE))) { // fourth, and the last mobile network type is LTE
                phoneStateUpdated = true; // trigger 3G alarm
            }

            if ((phoneStateUpdated || lteAlarm || femtocellConnection) && (prefs != null)) {
                String rawSoundUri = null;
                if (lteAlarm) { // we are in LTE alarm case
                    Log.d(TAG, "Try to play LTE alarm");
                    rawSoundUri = prefs.getString(SP_KEY_STAT_NOTIF_SOUND_4G, null);
                } else if (femtocellConnection) { //we are in fetmocell connection alarm case
                    Log.d(TAG, "Try to play Femtocell alarm");
                    rawSoundUri = prefs.getString(SP_KEY_STAT_NOTIF_SOUND_FEMTO, null);
                } else { // we are in operator change case
                    Log.d(TAG, "Try to play normal operator alarm");
                    rawSoundUri = prefs.getString((mobOp == MobileOperator.FREE_MOBILE) ? SP_KEY_STAT_NOTIF_SOUND_FREE_MOBILE : SP_KEY_STAT_NOTIF_SOUND_ORANGE, null);
                }
                if (rawSoundUri != null) {
                    final Uri soundUri = Uri.parse(rawSoundUri);
                    nBuilder.setSound(soundUri);
                }
            } else if (BuildConfig.DEBUG) {
                Log.d(TAG, "No notification sound to play");
            }
        }

        startForeground(R.string.stat_connected_to_mobile_network, nBuilder.build());
    }
    
    /**
     * Gets the state of Airplane Mode.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isAirplaneModeOn() {        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;          
        } else {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }       
    }

    private int getStatIcon(MobileOperator op) {
        final String themeKey = prefs.getString(SP_KEY_THEME, THEME_DEFAULT);
        Theme theme = THEMES.get(themeKey);
        if (theme == null) {
            theme = THEMES.get(THEME_DEFAULT);
        }

        if (MobileOperator.FREE_MOBILE.equals(op) && isFemtocell) {
            return theme.freeFemtoIcon;
        } else if (MobileOperator.FREE_MOBILE.equals(op)) {
            return theme.freeIcon;
        } else if (MobileOperator.ORANGE.equals(op)) {
            return theme.orangeIcon;
        }
        return android.R.drawable.ic_dialog_alert;
    }

    private Bitmap getStatLargeIcon(MobileOperator op) {
        if (MobileOperator.FREE_MOBILE.equals(op) && isFemtocell) {
            return freeFemtoLargeIcon;
        } else if (MobileOperator.FREE_MOBILE.equals(op)) {
            return freeLargeIcon;
        } else if (MobileOperator.ORANGE.equals(op)) {
            return orangeLargeIcon;
        }
        return null;
    }

    private void onDeviceShutdown() {
        Log.i(TAG, "Device is about to shut down");
        powerOn = false;
        updateEventDatabase();
    }

    /**
     * This method is called when the phone data connectivity is updated.
     */
    private boolean onConnectivityUpdated() {
        // Get the Wi-Fi connectivity state.
        final NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final boolean wifiNetworkConnected = ni != null && ni.isConnected();

        // Prevent duplicated inserts.
        if (lastWifiConnected != null && lastWifiConnected == wifiNetworkConnected) {
            return false;
        }
        lastWifiConnected = wifiNetworkConnected;

        Log.i(TAG, "Wifi state updated: connected=" + wifiNetworkConnected);
        return true;
    }

    /**
     * This method is called when the phone service state is updated.
     * @return -1 : no update ; 0 : minor update ; 1 : major update
     * It is a major update if mobile operator changes or phone connects a network.
     */
	private int onPhoneStateUpdated() {		
        mobileOperatorId = tm != null ? tm.getNetworkOperator() : null;
        if (TextUtils.isEmpty(mobileOperatorId)) {
            mobileOperatorId = null;
        }

        updateFemtocellStatus();
        
        // Prevent duplicated inserts.
        if (lastMobileNetworkConnected != null && lastMobileOperatorId != null
            && lastIsFemtocell != null && lastMobileNetworkType != null
            && lastMobileNetworkConnected.equals(mobileNetworkConnected)
            && lastIsFemtocell.equals(isFemtocell)
            && lastMobileOperatorId.equals(mobileOperatorId)
            && lastMobileNetworkType.equals(mobileNetworkType)) {
            return -1;
        }
        
		int ret = 0;
        
        if (lastMobileNetworkConnected != null && lastMobileNetworkConnected != mobileNetworkConnected
        		|| lastMobileOperatorId != null && !lastMobileOperatorId.equals(mobileOperatorId))
        	ret = 1;
        
        lastMobileNetworkConnected = mobileNetworkConnected;
        lastMobileOperatorId = mobileOperatorId;
        lastIsFemtocell = isFemtocell;
        lastMobileNetworkTypeForLTEDetect = lastMobileNetworkType; // save previous network type for LTE detection
        lastMobileNetworkType = mobileNetworkType;
        
        Log.i(TAG, "Phone state updated: operator=" + mobileOperatorId + "; connected=" + mobileNetworkConnected + "; femtocell=" + isFemtocell);
        return ret;
    }

	/**
	 * Check if we are connected on a Free Mobile femtocell
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void updateFemtocellStatus() {
        // getAllCellInfo requires ACCESS_COARSE_LOCATION
        if (!checkRequiredPermissions()) {
            return;
        }

        // No need to check LAC if current operator is not free mobile
        // And no need to check if network type is not femtocell supported network
        if ((!MobileOperator.FREE_MOBILE.equals(MobileOperator.fromString(mobileOperatorId)))
            || ((MobileOperator.FREE_MOBILE.equals(MobileOperator.fromString(mobileOperatorId)))
                && (!FEMTOCELL_AVAILABLE_NETWORK_TYPE.contains(mobileNetworkType)))) {
            isFemtocell = false;
            return;
        }

		Integer lac = null;
        if (tm != null) {
        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        	
        		//get the cell list
        		List<CellInfo> cellInfos = tm.getAllCellInfo();
        		if (cellInfos != null) {
	        		for (CellInfo cellInfo : cellInfos) {
	        			
	        			if (cellInfo.isRegistered()) { //we use only registered cells
		        			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) 
		        					&& (cellInfo instanceof CellInfoWcdma)) { //manage the wcdma cell case
		        				Log.d(TAG, "We got a WCDMA cell");
		        				CellIdentityWcdma ci = ((CellInfoWcdma) cellInfo).getCellIdentity();
		        				if (ci != null) { //save the LAC and exit loop
		        					lac = ci.getLac();
		        					Log.d(TAG, "We got the LAC - exit loop");
		        					break;
		        				}
		        				
		        			} else if (cellInfo instanceof CellInfoGsm) { //test the gsm case
		        				CellIdentityGsm ci = ((CellInfoGsm) cellInfo).getCellIdentity();
		        				Log.d(TAG, "We got a CDMA cell");
		        				if (ci != null) { //save the LAC and exit loop
		        					lac = ci.getLac();
		        					Log.d(TAG, "We got the LAC - exit loop");
		        					break;
		        				}
		        			}
		        			
	        			} else
	        				Log.d(TAG, "Unregistered cell - skipping");
	        		}
        		} else
    				Log.d(TAG, "No cell infos available");
        		
        	}
        	if (lac == null) { //use old API if LAC was not found with the new method (useful for buggy devices such as Samsung Galaxy S5) or if SDK is too old
	    		CellLocation cellLocation = tm.getCellLocation(); //cell location might be null... handle with care
	    		if ((cellLocation != null) && (cellLocation instanceof GsmCellLocation)) {
	    			Log.d(TAG, "We got a old GSM cell with LAC");
	    			lac = ((GsmCellLocation) cellLocation).getLac();
	    		}
        	}
        }
        if (DEBUG) Log.d(TAG, "LAC value : " + lac);
        
        Log.i(TAG, "Femtocell value : " + isFemtocell);
        
        if (lac != null) {
        	String lacAsString = String.valueOf(lac);
        	isFemtocell = (lacAsString.length() == 4) && (lacAsString.subSequence(1, 3).equals(FREE_MOBILE_FEMTOCELL_LAC_CODE));
        }
	}

    private void updateEventDatabase() {
        final Event e = new Event();
        e.timestamp = System.currentTimeMillis();
        e.screenOn = pm != null && pm.isScreenOn();
        e.batteryLevel = this.batteryLevel;
        e.wifiConnected = Boolean.TRUE.equals(lastWifiConnected);
        e.mobileConnected = powerOn && Boolean.TRUE.equals(lastMobileNetworkConnected);
        e.mobileOperator = lastMobileOperatorId;
        e.mobileNetworkType = lastMobileNetworkType != null ?
        		lastMobileNetworkType : TelephonyManager.NETWORK_TYPE_UNKNOWN;
        e.powerOn = powerOn;
        e.femtocell  = Boolean.TRUE.equals(lastIsFemtocell);
        e.firstInsert = firstInsert;
        firstInsert = false;
        
        try {
            pendingInsert.put(e);
        } catch (InterruptedException ex) {
            Log.w(TAG, "Failed to schedule event insertion", ex);
        }
    }

    private int getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent == null) {
            return 100;
        }

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if(level == -1 || scale == -1) {
            return 100;
        }

        return parseBatteryLevel(level, scale);
    }

    private int parseBatteryLevel(int level, int scale) {
        return scale == 0 ? 100 : (int) Math.round(level * 100d / scale);
    }

    /**
     * This internal thread is responsible for inserting data into the application database. This thread will prevent
     * the main loop from being used for interacting with the database, which could cause "Application Not Responding"
     * dialogs.
     */
    private static class PendingInsertWorker extends Thread {
        private final Context context;
        private final BlockingQueue< Event> pendingInsert;

        public PendingInsertWorker(final Context context, final BlockingQueue< Event> pendingInsert) {
            super("FreeMobileNetstat/PendingInsert");
            setDaemon(true);
            this.context = context;
            this.pendingInsert = pendingInsert;
        }

        @Override
        public void run() {
            if (DEBUG) {
                Log.d(TAG, "PendingInsert worker thread is started");
            }

            // Set a lower priority to prevent UI from lagging.
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            final ContentValues cv = new ContentValues(7);
            final ContentResolver cr = context.getContentResolver();

            final ContentValues lastCV = new ContentValues(7);
            long lastEventHashCode = 0;

            boolean running = true;
            while (running) {
                try {
                    final Event e = pendingInsert.take();
                    if (STOP_PENDING_CONTENT_MARKER == e) {
                        running = false;
                    } else {
                        e.write(cv);

                        // Check the last inserted event hash code:
                        // if the hash code is the same, the event is not
                        // inserted.
                        lastCV.putAll(cv);
                        lastCV.remove(Events.TIMESTAMP);
                        if (e.powerOn && lastCV.hashCode() == lastEventHashCode) {
                            if (DEBUG) {
                                Log.d(TAG, "Skip event insertion: " + e);
                            }
                        } else {
                            if (DEBUG) {
                                Log.d(TAG, "Inserting new event into database: " + e);
                            }
                            cr.insert(Events.CONTENT_URI, cv);
                        }
                        lastEventHashCode = lastCV.hashCode();
                        lastCV.clear();
                    }
                    cv.clear();
                } catch (InterruptedException e) {
                    running = false;
                } catch (Exception e) {
                    Log.e(TAG, "Pending insert failed", e);
                }
            }

            if (DEBUG) {
                Log.d(TAG, "PendingInsert worker thread is terminated");
            }
        }
    }

    /**
     * Notification theme.
     * 
     * @author Pixmob
     */
    private static class Theme {
        public final int freeIcon;
        public final int freeFemtoIcon;
        public final int orangeIcon;

        public Theme(final int freeIcon, final int freeFemtoIcon, final int orangeIcon) {
            this.freeIcon = freeIcon;
            this.freeFemtoIcon = freeFemtoIcon;
            this.orangeIcon = orangeIcon;
        }
    }
}
