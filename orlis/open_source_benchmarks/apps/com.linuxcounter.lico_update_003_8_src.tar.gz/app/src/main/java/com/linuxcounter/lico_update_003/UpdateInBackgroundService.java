package com.linuxcounter.lico_update_003;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class UpdateInBackgroundService extends IntentService {

    final static String TAG = "MyDebugOutput";

    public String sAppVersion = "0.0.8";
    // int sleepTime = 30; // Seconds
    int sleepTime = 28800; // Seconds
    static String senddata = null;
    public String aSendData[] = {};
    @SuppressLint({"NewApi", "SdCardPath"})
    protected Handler handler;
    protected Toast mToast;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_MACHINE = "com.linuxcounter.lico_update_003.action.UpdateMachine";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateMachine(Context context) {
        Intent intent = new Intent(context, UpdateInBackgroundService.class);
        intent.setAction(ACTION_UPDATE_MACHINE);
        context.startService(intent);
    }

    public UpdateInBackgroundService() {
        super("UpdateInBackgroundService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handleActionUpdateMachine(intent);
    }

    /**
     * Handle action UpdateMachine in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateMachine(Intent intent) {
        try {
            for(;;) {

                ////////////////////////////////////////////////////////////////////////////////




                String toks[] = null;
                String MemTotalt = null;
                int MemTotal = 0;
                String MemFreet = null;
                int MemFree = 0;
                String SwapTotalt = null;
                int SwapTotal = 0;
                String SwapFreet = null;
                int SwapFree = 0;
                String cpumodel = null;
                String scpunum = null;
                int cpunum = 0;

                System.getProperty("user.region");

                String androidversion;
                androidversion = System.getProperty("http.agent").replaceAll(".*Android *([0-9.]+).*", "$1");

                Log.d(TAG, "UpdateInBackgroundService: androidversion: " + androidversion);

                System.getProperty("os.version");

                String loadavg = "";
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/loadavg")), 1000);
                    String[] temp = reader.readLine().split(" ");
                    loadavg = temp[0] + " " + temp[1] + " " + temp[2];
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Log.d(TAG, "UpdateInBackgroundService: loadavg: " + loadavg);

                String cpuinfo = "";
                try {
                    cpuinfo = getStringFromFile("/proc/cpuinfo").replace("\r", "").replace("\n\n", "\n");
                    toks = cpuinfo.split("\n");
                    for (int a = 0; a < toks.length; a++) {
                        String k = (String) toks[a];
                        String[] toks2 = k.split(":");
                        if (toks2[0].trim().matches("^Processor.*") && toks2[1].trim().matches("^[a-zA-Z]+.*")) {
                            cpumodel = toks2[1].trim();
                        } else if (toks2[0].trim().matches("^processor.*") && toks2[1].trim().matches("^[0-9]+")) {
                            scpunum = toks2[1].trim();
                        }
                    }
                    try {
                        cpunum = Integer.parseInt(scpunum);
                        cpunum++;
                    } catch (NumberFormatException nfe) {
                        System.out.println("Could not parse " + nfe);
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                Log.d(TAG, "UpdateInBackgroundService: cpumodel: " + cpumodel);
                Log.d(TAG, "UpdateInBackgroundService: cpunum: " + cpunum);

                String flags = "";
                try {
                    String flagst = getStringFromFile("/proc/cpuinfo").replace("\r", "").replace("\n\n", "\n");
                    toks = flagst.split("\n");
                    for (int a = 0; a<toks.length; a++) {
                        String k = (String) toks[a];
                        String[] toks2 = k.split(":");
                        if (toks2[0].trim().matches("^Features.*") && toks2[1].trim().matches("^[a-zA-Z]+.*")) {
                            flags = toks2[1].trim();
                        }
                        if (toks2[0].trim().matches("^flags.*") && toks2[1].trim().matches("^[a-zA-Z]+.*")) {
                            flags = toks2[1].trim();
                        }
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                Log.d(TAG, "UpdateInBackgroundService: flags: " + flags);

                String meminfo = "";
                try {
                    meminfo = getStringFromFile("/proc/meminfo");
                    toks = meminfo.split("\n");
                    for (int a = 0; a < toks.length; a++) {
                        String k = (String) toks[a];
                        String[] toks2 = k.split(":");
                        if (toks2[0].trim().matches(".*MemTotal.*")) {
                            MemTotalt = toks2[1].replace(" kB", "").trim();
                            MemTotal = Integer.parseInt(MemTotalt.toString());
//				   MemTotal = (MemTotal * 1024);
                        } else if (toks2[0].trim().matches(".*MemFree.*")) {
                            MemFreet = toks2[1].replace(" kB", "").trim();
                            MemFree = Integer.parseInt(MemFreet.toString());
//				   MemFree = (MemFree * 1024);
                        } else if (toks2[0].trim().matches(".*SwapTotal.*")) {
                            SwapTotalt = toks2[1].replace(" kB", "").trim();
                            SwapTotal = Integer.parseInt(SwapTotalt.toString());
//				   SwapTotal = (SwapTotal * 1024);
                        } else if (toks2[0].trim().matches(".*SwapFree.*")) {
                            SwapFreet = toks2[1].replace(" kB", "").trim();
                            SwapFree = Integer.parseInt(SwapFreet.toString());
//				   SwapFree = (SwapFree * 1024);
                        }
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                Log.d(TAG, "UpdateInBackgroundService: MemTotal: " + MemTotal);
                Log.d(TAG, "UpdateInBackgroundService: MemFree: " + MemFree);
                Log.d(TAG, "UpdateInBackgroundService: SwapTotal: " + SwapTotal);
                Log.d(TAG, "UpdateInBackgroundService: SwapFree: " + SwapFree);

                long total = 0;
                long avail = 0;
                File dir = null;
                boolean extexists = false;

                dir = new File("/mnt/sdcard/external_sd/");
                if (dir.exists() && (dir.isDirectory() || dir.isFile())) {
                    total = TotalMemoryOfDir("/mnt/sdcard/external_sd/");
                    avail = FreeMemoryOfDir("/mnt/sdcard/external_sd/");
                    extexists = true;
                }
                dir = new File("/mnt/extSdCard/");
                if (dir.exists() && (dir.isDirectory() || dir.isFile())) {
                    total = TotalMemoryOfDir("/mnt/extSdCard/");
                    avail = FreeMemoryOfDir("/mnt/extSdCard/");
                    extexists = true;
                }
                String sdpath = Environment.getExternalStorageDirectory().getPath();
                dir = new File(sdpath);
                if (dir.exists() && (dir.isDirectory() || dir.isFile())) {
                    total = TotalMemoryOfDir(sdpath);
                    avail = FreeMemoryOfDir(sdpath);
                    extexists = true;
                }

                long disktotal = 0;
                if (extexists == true) {
                    disktotal = TotalMemory() + total;
                } else {
                    disktotal = TotalMemory();
                }
                Log.d(TAG, "UpdateInBackgroundService: disktotal: " + disktotal);

                long freedisk = 0;
                if (extexists == true) {
                    freedisk = FreeMemory() + avail;
                } else {
                    freedisk = FreeMemory();
                }
                Log.d(TAG, "UpdateInBackgroundService: freedisk: " + freedisk);

                String hostname = "localhost";
                String filename = ".linuxcounter";
                String filepath = Environment.getExternalStorageDirectory() + "/data/com.linuxcounter.lico_update_003";
                File readFile = new File(filepath, filename);
                String machine_id = "";
                String machine_updatekey = "";
                String load = "";
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(readFile)), 1000);
                    load = reader.readLine();
                    reader.close();
                    String[] toks1 = load.split(" ");
                    machine_id = (String) toks1[0];
                    machine_updatekey = (String) toks1[1];
                } catch (Exception e1) {
                    // Do nothing
                }

                String machine = System.getProperty("os.arch");
                String version = "";
                try {
                    version = Command("uname -r").trim();
                } catch (Exception e1) {
                    try {
                        version = getStringFromFile("/proc/sys/kernel/osrelease");
                    } catch (Exception e2) {
                        version = "unknown";
                    }
                }
                Log.d(TAG, "UpdateInBackgroundService: machine: " + machine);
                Log.d(TAG, "UpdateInBackgroundService: version: " + version);

                String uptime = Command("uptime").trim();
                String[] toks2 = uptime.split(", ");
                uptime = toks2[0].replace("up time: ", "").trim();
                Log.d(TAG, "UpdateInBackgroundService: uptime: " + uptime);

                String url = "http://api.linuxcounter.net/v1/machines/" + machine_id;

                boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
                String deviceclass = "Smartphone";
                if (tabletSize) {
                    deviceclass = "Tablet";
                }
                Log.d(TAG, "UpdateInBackgroundService: deviceclass: " + deviceclass);

                TelephonyManager tm = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                String countryCode = tm.getNetworkCountryIso();
                Log.d(TAG, "UpdateInBackgroundService: countryCode: " + countryCode);

                aSendData = new String[]{
                        "url#" + url,
                        "machine_id#" + machine_id,
                        "machine_updatekey#" + machine_updatekey,
                        "appversion#" + sAppVersion,
                        "hostname#" + hostname,
                        "cores#" + cpunum,
                        "flags#" + flags,
                        "diskspace#" + disktotal,
                        "diskspaceFree#" + freedisk,
                        "memory#" + MemTotal,
                        "memoryFree#" + MemFree,
                        "swap#" + SwapTotal,
                        "swapFree#" + SwapFree,
                        "distversion#" + androidversion,
                        "online#1",
                        "uptime#" + uptime,
                        "loadavg#" + loadavg,
                        "distribution#Android",
                        "kernel#" + version,
                        "cpu#" + cpumodel,
                        "country#" + countryCode.toUpperCase(),
                        "architecture#" + machine,
                        "class#" + deviceclass
                };

                postData(getApplicationContext(), aSendData);

                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                Context ctx = getApplicationContext();
                PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(this, MainActivity.class), 0);
                NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);

                b.setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker("The Linux Counter Project")
                        .setContentTitle("The Linux Counter Project")
                        .setContentText("Last update: " + currentDateTimeString)
                        .setContentIntent(contentIntent)
                        .setSound(null);

                NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, b.build());

                try {
                    Log.d(TAG, "UpdateInBackgroundService: Sleeping for " + sleepTime + " seconds...");
                    Thread.sleep((sleepTime * 1000));
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }

                ////////////////////////////////////////////////////////////////////////////////
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent msgIntent = new Intent(this, UpdateInBackgroundService.class);
        startService(msgIntent);
    }

    public String postData(Context context, final String postdata[]) {
        Log.d(TAG, "UpdateInBackgroundService: start postData()...");
        String responseBody = "";
        String[] firstseparated = postdata[0].split("#");
        String url = firstseparated[1];
        String[] secseparated = postdata[1].split("#");
        String machine_id = secseparated[1];
        String[] thirdseparated = postdata[2].split("#");
        final String machine_updatekey = thirdseparated[1];

        String data = null;
        final String contentType;
        contentType = "application/x-www-form-urlencoded";
        Log.d(TAG, "UpdateInBackgroundService: start Volley Send POST()...");

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "UpdateInBackgroundService: response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "UpdateInBackgroundService: error: " + error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                for (int i = 0; i < postdata.length; i++) {
                    String[] separated = postdata[i].split("#");
                    Log.d(TAG, "UpdateInBackgroundService: PATCH data:  " + separated[0] + "=" + separated[1]);
                    params.put(separated[0], separated[1]);
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                params.put("Content-Type", contentType);
                params.put("x-lico-machine-updatekey", machine_updatekey);
                return params;
            }
        };
        queue.add(sr);

        return responseBody;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        // Make sure you close all streams.
        fin.close();
        return ret;
    }

    public long TotalMemory() {
        StatFs statFs = null;
        statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long totalBlocks = (long) statFs.getBlockCount();
        long blockSize = (long) statFs.getBlockSize();
        long Total = ((((long) totalBlocks * (long) blockSize)) / 1024);
        return Total;
    }

    public long FreeMemory() {
        StatFs statFs = null;
        statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long availBlocks = (long) statFs.getAvailableBlocks();
        long blockSize = (long) statFs.getBlockSize();
        long free_memory = ((((long) availBlocks * (long) blockSize)) / 1024);
        return free_memory;
    }

    public long TotalMemoryOfDir(String dir) {
        StatFs statFs = null;
        statFs = new StatFs(dir);
        long totalBlocks = (long) statFs.getBlockCount();
        long blockSize = (long) statFs.getBlockSize();
        long Total = ((((long) totalBlocks * (long) blockSize)) / 1024);
        return Total;
    }

    public long FreeMemoryOfDir(String dir) {
        StatFs statFs = null;
        statFs = new StatFs(dir);
        long availBlocks = (long) statFs.getAvailableBlocks();
        long blockSize = (long) statFs.getBlockSize();
        long free_memory = ((((long) availBlocks * (long) blockSize)) / 1024);
        return free_memory;
    }

    public String Command(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isWiFiEnabled() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnected();
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnected();
        Log.d(TAG, "UpdateInBackgroundService: Connection 3G: " + is3g
                + " | Connection wifi: " + isWifi);
        if (!is3g && !isWifi) {
            Toast.makeText(getApplicationContext(),
                    "Please make sure, your network connection is ON ",
                    Toast.LENGTH_LONG).show();
        } else {
            return true;
        }
        return false;
    }
}
