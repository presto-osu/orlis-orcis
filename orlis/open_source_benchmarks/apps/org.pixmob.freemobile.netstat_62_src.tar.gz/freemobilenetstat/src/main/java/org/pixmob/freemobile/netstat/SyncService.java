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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.util.LongSparseArray;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.pixmob.freemobile.netstat.content.NetstatContract.Events;
import org.pixmob.freemobile.netstat.util.DateUtils;
import org.pixmob.httpclient.HttpClient;
import org.pixmob.httpclient.HttpClientException;
import org.pixmob.httpclient.HttpResponse;
import org.pixmob.httpclient.HttpResponseHandler;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static org.pixmob.freemobile.netstat.BuildConfig.DEBUG;
import static org.pixmob.freemobile.netstat.Constants.SP_NAME;
import static org.pixmob.freemobile.netstat.Constants.TAG;

/**
 * This background service synchronizes data with a remote server.
 * 
 * @author Pixmob
 */
public class SyncService extends IntentService {
    private static final Random RANDOM = new Random();
    private static final String SERVER_API_URL = "http://fm.netstat.fr/";
    private static final int SERVER_API_VERSION = 2;
    private static final String EXTRA_DEVICE_REG = "org.pixmob.freemobile.netstat.deviceReg";
    private static final long DAY_IN_MILLISECONDS = 86400 * 1000;
    private static final int SYNC_UPLOADED = 1;
    private static final int SYNC_PENDING = 0;
    private static final int MAX_SYNC_ERRORS = 4;
    private static final String INTERNAL_SP_NAME = "sync";
    private static final String INTERNAL_SP_KEY_SYNC_ERRORS = "syncErrors";
    private static String httpUserAgent;
    private SharedPreferences prefs;
    private SharedPreferences internalPrefs;
    private SharedPreferences.Editor internalPrefsEditor;
    private ConnectivityManager cm;
    private PowerManager pm;
    private SQLiteOpenHelper dbHelper;

    public SyncService() {
        super("FreeMobileNetstat/Sync");
    }

    public static void schedule(Context context, boolean enabled) {
        final Context appContext = context.getApplicationContext();
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent syncIntent =
            PendingIntent.getService(appContext, 0, new Intent(appContext, SyncService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(syncIntent);

        if (enabled) {
            // Set the sync period.
            long period = AlarmManager.INTERVAL_HOUR;
            final int syncErrors =
                context.getSharedPreferences(INTERNAL_SP_NAME, MODE_PRIVATE)
                    .getInt(INTERNAL_SP_KEY_SYNC_ERRORS, 0);
            if (syncErrors != 0) {
                // When there was a sync error, the sync period is longer.
                period = AlarmManager.INTERVAL_HOUR * Math.min(syncErrors, MAX_SYNC_ERRORS);
            }

            // Add a random time to prevent concurrent requests for the server.
            final long fuzz = RANDOM.nextInt(1000 * 60 * 30);
            period += fuzz;

            if (DEBUG) {
                Log.d(TAG, "Scheduling synchronization: next in " + (period / 1000 / 60) + " minutes");
            }
            final long syncTime = System.currentTimeMillis() + period;
            am.set(AlarmManager.RTC_WAKEUP, syncTime, syncIntent);
        } else {
            if (DEBUG) {
                Log.d(TAG, "Synchronization schedule canceled");
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        internalPrefs = getSharedPreferences(INTERNAL_SP_NAME, MODE_PRIVATE);
        internalPrefsEditor = internalPrefs.edit();
        internalPrefsEditor.commit();
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        dbHelper = new UploadDatabaseHelper(this);
    }

    @Override
    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Check if statistics upload is enabled.
        if (!prefs.getBoolean(Constants.SP_KEY_UPLOAD_STATS, false)) {
            Log.d(TAG, "Synchronization is disabled: skip sync");
            return;
        }

        // Check if an Internet connection is available.
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isAvailable() || !netInfo.isConnected()) {
            Log.d(TAG, "Network connectivity is not available: skip sync");
            return;
        }

        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        SQLiteDatabase db = null;
        try {
            wl.acquire();
            db = dbHelper.getWritableDatabase();
            run(intent, db);

            // Sync was successful: reset sync error count.
            internalPrefsEditor.remove(INTERNAL_SP_KEY_SYNC_ERRORS).commit();
        } catch (Exception e) {
            Log.e(TAG, "Failed to upload statistics", e);

            // Increment sync errors.
            final int syncErrors = internalPrefs.getInt(INTERNAL_SP_KEY_SYNC_ERRORS, 0);
            internalPrefsEditor.putInt(INTERNAL_SP_KEY_SYNC_ERRORS, syncErrors + 1).commit();
        } finally {
            if (db != null) {
                db.close();
            }
            wl.release();

            // Reschedule this service according to the sync error count.
            schedule(this, true);
        }

        Log.i(TAG, "Statistics upload done");
    }

    private void run(Intent intent, final SQLiteDatabase db) throws Exception {
        final long now = dateAtMidnight(System.currentTimeMillis());

        Log.i(TAG, "Initializing statistics before uploading");

        final LongSparseArray<DailyStat> stats = new LongSparseArray<>(15);
        final Set<Long> uploadedStats = new HashSet<>(15);
        final long statTimestampStart = now - 7 * DAY_IN_MILLISECONDS;

        // Get pending uploads.
        Cursor pendingUploadsCursor = null;
        try {
            pendingUploadsCursor = db.query("daily_stat",
                    new String[] {
                            "stat_timestamp", "orange", "free_mobile",
                            "free_mobile_3g", "free_mobile_4g", "free_mobile_femtocell", "sync"
                    },
                    "stat_timestamp>=? AND stat_timestamp<?", new String[]{String.valueOf(statTimestampStart),
                            String.valueOf(now)}, null, null, null);
            while (pendingUploadsCursor.moveToNext()) {
                final long d = pendingUploadsCursor.getLong(0);
                final int sync = pendingUploadsCursor.getInt(6);
                if (SYNC_UPLOADED == sync) {
                    uploadedStats.add(d);
                } else if (SYNC_PENDING == sync) {
                    final DailyStat s = new DailyStat();
                    s.orange = pendingUploadsCursor.getInt(1);
                    s.freeMobile = pendingUploadsCursor.getInt(2);
                    s.freeMobile3G = pendingUploadsCursor.getInt(3);
                    s.freeMobile4G = pendingUploadsCursor.getInt(4);
                    s.freeMobileFemtocell = pendingUploadsCursor.getInt(5);
                    stats.put(d, s);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            try {
                if (pendingUploadsCursor != null)
                    pendingUploadsCursor.close();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        // Compute missing uploads.
        final ContentValues cv = new ContentValues();
        db.beginTransaction();
        try {
            for (long d = statTimestampStart; d < now; d += DAY_IN_MILLISECONDS) {
                if (stats.get(d) == null && !uploadedStats.contains(d)) {
                    final DailyStat s = computeDailyStat(d);
                    cv.put("stat_timestamp", d);
                    cv.put("orange", s.orange);
                    cv.put("free_mobile", s.freeMobile);
                    cv.put("free_mobile_3g", s.freeMobile3G);
                    cv.put("free_mobile_4g", s.freeMobile4G);
                    cv.put("free_mobile_femtocell", s.freeMobileFemtocell);
                    cv.put("sync", SYNC_PENDING);
                    db.insertOrThrow("daily_stat", null, cv);
                    stats.put(d, s);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Delete old statistics.
        if (DEBUG) {
            Log.d(TAG, "Cleaning up upload database");
        }
        db.delete("daily_stat", "stat_timestamp<?", new String[] {String.valueOf(statTimestampStart) });

        // Check if there are any statistics to upload.
        final int statsLen = stats.size();
        if (statsLen == 0) {
            Log.i(TAG, "Nothing to upload");
            return;
        }

        // Check if the remote server is up.
        final HttpClient client = createHttpClient();
        try {
            client.head(createServerUrl(null)).execute();
        } catch (HttpClientException e) {
            Log.w(TAG, "Remote server is not available: cannot upload statistics", e);
            return;
        }

        // Upload statistics.
        Log.i(TAG, "Uploading statistics");
        final JSONObject json = new JSONObject();
        final String deviceId = getDeviceId();
        final boolean deviceWasRegistered = intent.getBooleanExtra(EXTRA_DEVICE_REG, false);
        for (int i = 0; i < statsLen; ++i) {
            final long d = stats.keyAt(i);
            final DailyStat s = stats.get(d);

            try {
                json.put("timeOnOrange", s.orange);
                json.put("timeOnFreeMobile", s.freeMobile);
                json.put("timeOnFreeMobile3g", s.freeMobile3G);
                json.put("timeOnFreeMobile4g", s.freeMobile4G);
                json.put("timeOnFreeMobileFemtocell", s.freeMobileFemtocell);
            } catch (JSONException e) {
                final IOException ioe = new IOException("Failed to prepare statistics upload");
                ioe.initCause(e);
                throw ioe;
            }

            final String url =
                createServerUrl("/device/" + deviceId + "/daily/" + DateFormat.format("yyyyMMdd", d));
            if (DEBUG) {
                Log.d(TAG, "Uploading statistics for " + DateUtils.formatDate(d) + " to: " + url);
            }

            final byte[] rawJson = json.toString().getBytes("UTF-8");
            try {
                client.post(url).content(rawJson, "application/json").expect(HttpURLConnection.HTTP_OK,
                    HttpURLConnection.HTTP_NOT_FOUND).to(new HttpResponseHandler() {
                    @Override
                    public void onResponse(HttpResponse response) throws Exception {
                        final int sc = response.getStatusCode();
                        if (HttpURLConnection.HTTP_NOT_FOUND == sc) {
                            // Check if the device has just been
                            // registered.
                            if (deviceWasRegistered) {
                                throw new IOException("Failed to upload statistics");
                            } else {
                                // Got 404: the device does not exist.
                                // We need to register this device.
                                registerDevice(deviceId);

                                // Restart this service.
                                startService(new Intent(getApplicationContext(), SyncService.class).putExtra(
                                        EXTRA_DEVICE_REG, true));
                            }
                        } else if (HttpURLConnection.HTTP_OK == sc) {
                            // Update upload database.
                            cv.clear();
                            cv.put("sync", SYNC_UPLOADED);
                            db.update("daily_stat", cv, "stat_timestamp=?", new String[] {String.valueOf(d) });

                            if (DEBUG) {
                                Log.d(TAG, "Upload done for " + DateUtils.formatDate(d));
                            }
                        }
                    }
                }).execute();
            } catch (HttpClientException e) {
                final IOException ioe = new IOException("Failed to send request with statistics");
                ioe.initCause(e);
                throw ioe;
            }
        }
    }

    private DailyStat computeDailyStat(long date) {
        long timeOnOrange = 0;
        long timeOnFreeMobile = 0;
        long timeOnFreeMobile3G = 0;
        long timeOnFemtocell = 0;
        long timeOnFreeMobile4G = 0;

        if (DEBUG) {
            Log.d(TAG, "Computing statistics for " + DateUtils.formatDate(date));
        }

        Cursor computeStatisticsCursor = null;
        try {
            computeStatisticsCursor = getContentResolver().query(Events.CONTENT_URI,
                    new String[]{Events.TIMESTAMP, Events.MOBILE_OPERATOR, Events.MOBILE_NETWORK_TYPE, Events.FEMTOCELL},
                    Events.TIMESTAMP + ">=? AND " + Events.TIMESTAMP + "<=?",
                    new String[]{String.valueOf(date), String.valueOf(date + 86400 * 1000)}, Events.TIMESTAMP);

            long t0 = 0;
            MobileOperator op0 = null;
            CharArrayBuffer cBuf = new CharArrayBuffer(6);

            while (computeStatisticsCursor.moveToNext()) {
                final long t = computeStatisticsCursor.getLong(0);
                computeStatisticsCursor.copyStringToBuffer(1, cBuf);
                final MobileOperator op = MobileOperator.fromString(cBuf);
                final NetworkClass nc = NetworkClass.getNetworkClass(computeStatisticsCursor.getInt(2));
                final boolean isFemtocell = computeStatisticsCursor.getInt(3) != 0;

                if (t0 != 0) {
                    if (op != null && op.equals(op0)) {
                        final long dt = t - t0;
                        if (MobileOperator.ORANGE.equals(op)) {
                            timeOnOrange += dt;
                        } else if (MobileOperator.FREE_MOBILE.equals(op)) {
                            timeOnFreeMobile += dt;
                            if (isFemtocell) {
                                timeOnFemtocell += dt;
                            }
                            else if (NetworkClass.NC_3G.equals(nc)) {
                                timeOnFreeMobile3G += dt;
                            }
                            else if (NetworkClass.NC_4G.equals(nc)) {
                                timeOnFreeMobile4G += dt;
                            }
                        }
                    }
                }

                t0 = t;
                op0 = op;
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            try {
                if (computeStatisticsCursor != null)
                    computeStatisticsCursor.close();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        final DailyStat s = new DailyStat();
        s.orange = timeOnOrange;
        s.freeMobile = timeOnFreeMobile;
        s.freeMobile3G = timeOnFreeMobile3G;
        s.freeMobile4G = timeOnFreeMobile4G;
        s.freeMobileFemtocell = timeOnFemtocell;
        return s;
    }

    private void registerDevice(String deviceId) throws IOException {
        final JSONObject json = new JSONObject();
        try {
            json.put("brand", Build.BRAND);
            json.put("model", Build.MODEL);
        } catch (JSONException e) {
            final IOException ioe = new IOException("Failed to prepare device registration request");
            ioe.initCause(e);
            throw ioe;
        }

        final byte[] rawJson = json.toString().getBytes("UTF-8");
        Log.i(TAG, "Registering device");

        final String url = createServerUrl("/device/" + deviceId);
        final HttpClient client = createHttpClient();
        try {
            client.put(url).expect(HttpURLConnection.HTTP_CREATED).content(rawJson, "application/json").execute();
        } catch (HttpClientException e) {
            final IOException ioe = new IOException("Failed to register device " + deviceId);
            ioe.initCause(e);
            throw ioe;
        }
    }

    private String createServerUrl(String path) {
        final String safePath;
        if (path == null) {
            return SERVER_API_URL + SERVER_API_VERSION;
        } else if (path.startsWith("/")) {
            safePath = path;
        } else {
            safePath = "/" + path;
        }
        return SERVER_API_URL + SERVER_API_VERSION + safePath;
    }

    private HttpClient createHttpClient() {
        if (httpUserAgent == null) {
            final PackageManager pm = getPackageManager();
            String applicationVersion = "0";
            try {
                final PackageInfo pkgInfo = pm.getPackageInfo(getPackageName(), 0);
                applicationVersion = pkgInfo.versionName;
            } catch (NameNotFoundException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            httpUserAgent = "FreeMobileNetstat/" + applicationVersion + " Android/" + Build.VERSION.SDK_INT;
        }

        final HttpClient client = new HttpClient(this);
        client.setConnectTimeout(10000);
        client.setReadTimeout(20000);
        client.setUserAgent(httpUserAgent);
        return client;
    }

    private String getDeviceId() {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        String deviceId = null;
        Cursor deviceCursor = null;
        try {
            deviceCursor = db.query("device", new String[]{"device_id"}, null, null, null, null, null);
            if (deviceCursor.moveToNext()) {
                deviceId = deviceCursor.getString(0);
            }
        }  catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            try {
                if (deviceCursor != null)
                    deviceCursor.close();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        if (deviceId == null) {
            // Generate a new device identifier.
            deviceId = UUID.randomUUID().toString();

            // Store this device identifier in the database.
            final ContentValues cv = new ContentValues(1);
            cv.put("device_id", deviceId);
            db.insertOrThrow("device", null, cv);
        }
        return deviceId;
    }

    private static long dateAtMidnight(long d) {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");
        final Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    private static class UploadDatabaseHelper extends SQLiteOpenHelper {
        private static final int UPLOAD_DATABASE_VERSION = 5;

        private final Context context;

        public UploadDatabaseHelper(final Context context) {
            super(context, "upload.db", null, UPLOAD_DATABASE_VERSION);
            this.context = context;
        }

        private String[] schema() {
            return new String[] {
                "CREATE TABLE daily_stat(stat_timestamp TIMESTAMP PRIMARY KEY, "
                    + "orange INTEGER NOT NULL, free_mobile INTEGER NOT NULL, "
                    + "free_mobile_3g INTEGER NOT NULL, free_mobile_4g INTEGER NOT NULL, "
                    + "free_mobile_femtocell INTEGER NOT NULL, sync INTEGER NOT NULL);",
                "CREATE TABLE device(device_id TEXT PRIMARY KEY);"
            };
        }

        private void applyStatements(SQLiteDatabase db, final String[] queries) {
            for (final String query : queries) {
                db.execSQL(query);
            }
        }

        private void initSchema(SQLiteDatabase db) {
            applyStatements(db, schema());
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (!db.isReadOnly()) {
                initSchema(db);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (!db.isReadOnly()) {
                switch (oldVersion) {
                    case 4:
                        this.upgradeV4ToV5(db);
                        break;
                    default:
                        db.execSQL("DROP TABLE IF EXISTS daily_stat");
                        db.execSQL("DROP TABLE IF EXISTS device");
                        onCreate(db);
                }
            }
        }

        private void upgradeV4ToV5(final SQLiteDatabase db) {
            String[] statements = new String[] {
                "DROP TABLE IF EXISTS daily_stat;",
                "DROP TABLE IF EXISTS device;"
            };
            applyStatements(db, statements);

            initSchema(db);

            final File db_testing = context.getDatabasePath("upload_testing.db");
            if (!db_testing.exists()) {
                return;
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.execSQL("ATTACH DATABASE ? AS testing;", new String[]{db_testing.getAbsolutePath()});
            db.beginTransaction();
            statements = new String[] {
                "INSERT INTO main.daily_stat(stat_timestamp, orange, free_mobile, free_mobile_3g, "
                    + "free_mobile_4g, free_mobile_femtocell, sync) "
                    + "SELECT * FROM testing.daily_stat_testing;",
                "INSERT INTO main.device(device_id) SELECT * FROM testing.device_testing;"
            };
            applyStatements(db, statements);
            db.setTransactionSuccessful();
            db.endTransaction();
            db.execSQL("DETACH DATABASE testing;");
            db.beginTransaction();

            context.deleteDatabase("upload_testing.db");
        }
    }

    private static class DailyStat {
        public long orange;
        public long freeMobile;
        public long freeMobile3G;
        public long freeMobile4G;
        public long freeMobileFemtocell;
    }
}
