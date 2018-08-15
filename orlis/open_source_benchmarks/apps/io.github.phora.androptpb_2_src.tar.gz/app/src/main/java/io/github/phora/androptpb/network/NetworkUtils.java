package io.github.phora.androptpb.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by phora on 8/24/15.
 */
public class NetworkUtils {

    private static final String LOG_TAG = "NetworkUtils";

    private static NetworkUtils nm;

    private Context c;

    public final static String METHOD_POST = "POST";
    public final static String METHOD_PUT = "PUT";
    public final static String METHOD_GET = "GET";
    public final static String METHOD_DELETE = "DELETE";



    public enum DeleteResult {
        SUCCESS, NO_CONN, ALREADY_GONE
    }
    public static NetworkUtils getInstance(Context ctxt) {
        if (nm == null) {
            nm = new NetworkUtils(ctxt);
        }
        return nm;
    }

    private NetworkUtils(Context context) {
        c = context;
    }

    public HttpURLConnection openConnection(String serverPath, String method) {
        URL url = null;

        try {
            url = new URL(serverPath);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        HttpURLConnection conn;

        try {
            if (isConnectedToInternet(c)) {

                if (url != null) {
                    conn = (HttpsURLConnection) url.openConnection();
                } else {
                    //listen
                    return null;
                }

                String loc = conn.getHeaderField("Location");
                if (loc != null) {
                    serverPath = loc; //needed?
                    url = new URL(loc);
                }
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod(method);
                conn.setUseCaches(false);
                if (!method.equals(METHOD_DELETE)) {
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                }
                if (method.equals(METHOD_GET)) {
                    conn.setDoOutput(false);
                }

                conn.setRequestProperty("User-Agent", "AndroPTPB");
                conn.setRequestProperty("Expect", "100-continue");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + RequestData.boundary);
                //conn.setRequestProperty("connection", "Keep-Alive");

                return conn;
            }
        }
        catch (IOException e) {
            //some error handling
            Log.d(LOG_TAG, "Failed uploads: " + e.getMessage());
            return null;
        }

        return null;
    }

    public List<String[]> getFormatters(HttpURLConnection conn) throws IOException {
        List<String[]> output = new LinkedList<>();
        conn.connect();
        InputStream stream = conn.getInputStream();

        if (stream != null) {
            Log.d(LOG_TAG, "Got response for formatters, reading now");
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = br.readLine()) != null) {
                sb.append(String.format("%s\n", line));
            }

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sbRes = sb.toString();

            try {
                JSONArray jArr = new JSONArray(sbRes);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONArray jSubArr = jArr.getJSONArray(i);
                    String[] subOutput = new String[jSubArr.length()];
                    for (int j = 0; j < jSubArr.length(); j++) {
                        subOutput[j] = jSubArr.getString(j);
                    }
                    output.add(subOutput);
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Unable to retrieve formatters:");
                Log.d(LOG_TAG, sbRes);
            }
        }
        Log.d(LOG_TAG, "Finished retrieving formatters");
        return output;
    }

    public List<String> getStyles(HttpURLConnection conn) throws IOException {
        List<String> output = new LinkedList<>();
        conn.connect();
        InputStream stream = conn.getInputStream();

        if (stream != null) {
            Log.d(LOG_TAG, "Got response for styles, reading now");
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = br.readLine()) != null) {
                sb.append(String.format("%s\n", line));
            }

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sbRes = sb.toString();

            try {
                JSONArray jArr = new JSONArray(sbRes);
                for (int i = 0; i < jArr.length(); i++) {
                    String s = jArr.getString(i);
                    output.add(s);
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Unable to retrieve styles:");
                Log.d(LOG_TAG, sbRes);
            }
        }
        Log.d(LOG_TAG, "Finished retrieving styles");
        return output;
    }

    public List<String[]> getHintGroups(HttpURLConnection conn) throws IOException {
        List<String[]> output = new LinkedList<>();
        conn.connect();
        InputStream stream = conn.getInputStream();

        if (stream != null) {
            Log.d(LOG_TAG, "Got response for uploads, reading now");
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = br.readLine()) != null) {
                sb.append(String.format("%s\n", line));
            }

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sbRes = sb.toString();

            try {
                JSONArray jArr = new JSONArray(sbRes);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONArray jSubArr = jArr.getJSONArray(i);
                    String[] subOutput = new String[jSubArr.length()];
                    for (int j = 0; j < jSubArr.length(); j++) {
                        subOutput[j] = jSubArr.getString(j);
                    }
                    output.add(subOutput);
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Unable to retrieve hints:");
                Log.d(LOG_TAG, sbRes);
            }
        }
        Log.d(LOG_TAG, "Finished retrieving hints");
        return output;
    }

    public UploadData getReplaceResult(HttpURLConnection conn, String serverPath, boolean isPrivate) throws IOException {
        UploadData output = null;

        //should we flush request manually before going in here?
        conn.getOutputStream().flush();
        InputStream stream = conn.getInputStream();

        if (stream != null) {
            Log.d(LOG_TAG, "Got response for uploads, reading now");
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line = null;

            String token = null;
            String sha1 = null;
            String detectedHint = null;

            while((line = br.readLine()) != null) {
                sb.append(String.format("%s\n", line));
            }

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sbRes = sb.toString();
            try {
                JSONObject jObj = new JSONObject(sbRes);

                if (isPrivate) {
                    token = jObj.getString("long");
                }
                else {
                    token = jObj.getString("short");
                }

                sha1 = jObj.getString("digest");

                String trimmedData = jObj.getString("url");
                String fullUrl = "%1$s/%2$s";
                   String removeForHint = String.format(fullUrl, serverPath, token);
                detectedHint = trimmedData.replaceFirst(Pattern.quote(removeForHint), "");

                if (TextUtils.isEmpty(detectedHint)) {
                    detectedHint = null;
                }

                if (token != null && sha1 != null) {
                    output = new UploadData(serverPath, token, null, null, sha1, isPrivate, null);
                    output.setPreferredHint(detectedHint);
                }
            }
            catch (JSONException e)
            {
                Log.d(LOG_TAG, "Couldn't parse result from replace");
                Log.d(LOG_TAG, sbRes);
            }
        }
        Log.d(LOG_TAG, "Finished uploads");
        return output;
    }

    public DeleteResult getDeleteResult(HttpURLConnection conn) {
        try {
            //conn.connect();
            int respCode = conn.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                return DeleteResult.SUCCESS;
            }
            else if (respCode == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return DeleteResult.ALREADY_GONE;
            }
            else {
                return DeleteResult.NO_CONN;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return DeleteResult.NO_CONN;
        }
    }

    public UploadData getUploadResult(String serverPath, boolean isPrivate, HttpURLConnection conn) throws IOException {
        UploadData output = null;

        //should we flush request manually before going in here?
        conn.getOutputStream().flush();
        InputStream stream = conn.getInputStream();

        if (stream != null) {
            Log.d(LOG_TAG, "Got response for uploads, reading now");
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line = null;

            String token = null;
            String sha1 = null;
            String uuid = null;
            String vanity = null;
            String detectedHint = null;
            Long sunset = null;

            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSZZZZZ", Locale.ENGLISH);

            while((line = br.readLine()) != null) {
                sb.append(String.format("%s\n", line));
            }

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sbRes = sb.toString();
            try {
                JSONObject jObj = new JSONObject(sbRes);

                if (isPrivate) {
                    token = jObj.getString("long");
                }
                else {
                    token = jObj.getString("short");
                }

                sha1 = jObj.getString("digest");
                uuid = jObj.getString("uuid");

                if (jObj.has("label")) {
                    vanity = jObj.getString("label");
                }

                if (jObj.has("sunset")) {
                    Date date = null;
                    try {
                        date = fmt.parse(jObj.getString("sunset"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "Can't parse date: " + e.getMessage());
                    }
                    sunset = date.getTime() / 1000;
                }

                String trimmedData = jObj.getString("url");
                String fullUrl = "%1$s/%2$s";
                if (vanity != null) {
                    String removeForHint = String.format(fullUrl, serverPath, vanity);
                    detectedHint = trimmedData.replaceFirst(Pattern.quote(removeForHint), "");
                }
                else {
                    String removeForHint = String.format(fullUrl, serverPath, token);
                    detectedHint = trimmedData.replaceFirst(Pattern.quote(removeForHint), "");
                }
                if (TextUtils.isEmpty(detectedHint)) {
                    detectedHint = null;
                }

                if (token != null && sha1 != null && uuid != null) {
                    output = new UploadData(serverPath, token, vanity, uuid, sha1, isPrivate, sunset);
                    output.setPreferredHint(detectedHint);
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Couldn't parse result from upload");
                Log.d(LOG_TAG, sbRes);
            }
        }
        Log.d(LOG_TAG, "Finished uploads");
        return output;
    }

    //https://github.com/Schoumi/Goblim/blob/master/app/src/main/java/fr/mobdev/goblim/NetworkManager.java
    private boolean isConnectedToInternet(Context context)
    {
        //verify the connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
        {
            NetworkInfo.State networkState = networkInfo.getState();
            if (networkState.equals(NetworkInfo.State.CONNECTED))
            {
                return true;
            }
        }
        return false;
    }

    public UploadData getRedirectResult(String serverPath, HttpURLConnection conn) throws IOException {
        UploadData output = null;

        //should we flush request manually before going in here?
        conn.getOutputStream().flush();
        InputStream stream = conn.getInputStream();

        if (stream != null) {
            Log.d(LOG_TAG, "Got response for redirect, reading now");
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            String token;

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = br.readLine()) != null) {
                sb.append(String.format("%s\n", line));
            }

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sbRes = sb.toString();
            try {
                JSONObject jObj = new JSONObject(sbRes);
                token = jObj.getString("short");
                output = new UploadData(serverPath, token, null, null, null, false, null);
            }
            catch (JSONException e)
            {
                Log.d(LOG_TAG, "Couldn't parse result from url shorten");
                Log.d(LOG_TAG, sbRes);
            }
        }
        Log.d(LOG_TAG, "Finished submitting redirect");
        return output;
    }
}
