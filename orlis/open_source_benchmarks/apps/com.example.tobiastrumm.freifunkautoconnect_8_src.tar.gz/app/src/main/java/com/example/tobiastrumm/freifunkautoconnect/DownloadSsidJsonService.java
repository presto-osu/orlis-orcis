package com.example.tobiastrumm.freifunkautoconnect;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadSsidJsonService extends IntentService {

    private final static String TAG = DownloadSsidJsonService.class.getSimpleName();
    private final static String SSID_URL = "https://raw.githubusercontent.com/WIStudent/freifunk-ssids/freifunk_auto_connect_production/ssids.json";
    public static final String BROADCAST_ACTION = "com.example.tobiastrumm.freifunkautoconnect.downloadssidjsonservice.BROADCAST";
    public static final String STATUS_TYPE = "status_type";
    public static final String STATUS_TYPE_REPLACED = "type_replaced";
    public static final String STATUS_TYPE_NO_NEW_FILE = "type_no_new_file";


    public DownloadSsidJsonService(){
        super("DownloadSsidJsonService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        StringBuilder builder = new StringBuilder();
        JSONObject downloaded_ssids;
        JSONObject existing_ssids;
        try {
            // Download json File.
            Log.d(TAG, "Start downloading ssid file");
            URL url = new URL(SSID_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if(statusCode == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                downloaded_ssids = new JSONObject(builder.toString());
            } else{
                Log.w(TAG, url.toExternalForm() + " : Failed to download file. Response Code " + statusCode);
                return;
            }
            urlConnection.disconnect();
            Log.d(TAG, "ssids.json was downloaded");

            // Read ssids.json from internal storage.
            String jsonString = "";
            InputStreamReader is = new InputStreamReader(openFileInput("ssids.json"));
            BufferedReader reader = new BufferedReader(is);
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString += line;
            }
            reader.close();
            existing_ssids = new JSONObject(jsonString);
             // Compare version of downloaded and existing ssid files.
            int version_existing_ssids = existing_ssids.getInt("version");
            int version_downloaded_ssids = downloaded_ssids.getInt("version");

            // If version of downloaded json file is bigger, replace existing json file.
            Log.d(TAG, "Version of existing ssids.json: " + version_existing_ssids +  " Version of downloaded ssids.json: " + version_downloaded_ssids);
            if(version_downloaded_ssids > version_existing_ssids){
                FileOutputStream outputStream = openFileOutput("ssids.json", Context.MODE_PRIVATE);
                outputStream.write(builder.toString().getBytes());
                outputStream.close();
                responseFileReplaced();
            }
            else{
                responseNoNewFile();
            }

            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(getString(R.string.preference_timestamp_last_ssid_download), System.currentTimeMillis() / 1000L);
            editor.apply();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void responseFileReplaced(){
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(STATUS_TYPE, STATUS_TYPE_REPLACED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void responseNoNewFile(){
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(STATUS_TYPE, STATUS_TYPE_NO_NEW_FILE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
