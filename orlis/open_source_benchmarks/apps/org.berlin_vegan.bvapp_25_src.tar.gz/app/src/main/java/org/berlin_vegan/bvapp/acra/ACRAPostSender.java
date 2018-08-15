/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.acra;

import android.content.Context;
import android.util.Log;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.berlin_vegan.bvapp.BuildConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * ACRA Mailer. Easy to use mailer that sends you all crash reports from your Android apps.
 * <p/>
 * Source: https://github.com/d-a-n/acra-mailer
 */
public class ACRAPostSender implements ReportSender {
    private final static String TAG = "ACRAPostSender";
    private final static String BASE_URL = "http://www.berlin-vegan.de/cgi-bin/acra.php?email=bv-app@berlin-vegan.de";
    private final static String SHARED_SECRET = "aehiePh2Aew8atui";
    private Map<String, String> custom_data = null;

    public ACRAPostSender() {
    }

    ACRAPostSender(HashMap<String, String> custom_data) {
        this.custom_data = custom_data;
    }

    private static String md5(String s) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(s.getBytes(), 0, s.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {

        String url = getUrl();
        Log.d(TAG, url);

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> parameters = new ArrayList<>();

            if (custom_data != null) {
                for (Map.Entry<String, String> entry : custom_data.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            parameters.add(new BasicNameValuePair("DATE", new Date().toString()));
            parameters.add(new BasicNameValuePair("REPORT_ID", report.get(ReportField.REPORT_ID)));
            parameters.add(new BasicNameValuePair("APP_VERSION_CODE", report.get(ReportField.APP_VERSION_CODE)));
            parameters.add(new BasicNameValuePair("APP_VERSION_NAME", report.get(ReportField.APP_VERSION_NAME)));
            parameters.add(new BasicNameValuePair("PACKAGE_NAME", report.get(ReportField.PACKAGE_NAME)));
            parameters.add(new BasicNameValuePair("FILE_PATH", report.get(ReportField.FILE_PATH)));
            parameters.add(new BasicNameValuePair("PHONE_MODEL", report.get(ReportField.PHONE_MODEL)));
            parameters.add(new BasicNameValuePair("ANDROID_VERSION", report.get(ReportField.ANDROID_VERSION)));
            parameters.add(new BasicNameValuePair("BUILD", report.get(ReportField.BUILD)));
            parameters.add(new BasicNameValuePair("BRAND", report.get(ReportField.BRAND)));
            parameters.add(new BasicNameValuePair("PRODUCT", report.get(ReportField.PRODUCT)));
            parameters.add(new BasicNameValuePair("TOTAL_MEM_SIZE", report.get(ReportField.TOTAL_MEM_SIZE)));
            parameters.add(new BasicNameValuePair("AVAILABLE_MEM_SIZE", report.get(ReportField.AVAILABLE_MEM_SIZE)));
            parameters.add(new BasicNameValuePair("CUSTOM_DATA", report.get(ReportField.CUSTOM_DATA)));
            parameters.add(new BasicNameValuePair("STACK_TRACE", report.get(ReportField.STACK_TRACE)));
            parameters.add(new BasicNameValuePair("INITIAL_CONFIGURATION", report.get(ReportField.INITIAL_CONFIGURATION)));
            parameters.add(new BasicNameValuePair("CRASH_CONFIGURATION", report.get(ReportField.CRASH_CONFIGURATION)));
            parameters.add(new BasicNameValuePair("DISPLAY", report.get(ReportField.DISPLAY)));
            parameters.add(new BasicNameValuePair("USER_COMMENT", report.get(ReportField.USER_COMMENT)));
            parameters.add(new BasicNameValuePair("USER_APP_START_DATE", report.get(ReportField.USER_APP_START_DATE)));
            parameters.add(new BasicNameValuePair("USER_CRASH_DATE", report.get(ReportField.USER_CRASH_DATE)));
            parameters.add(new BasicNameValuePair("DUMPSYS_MEMINFO", report.get(ReportField.DUMPSYS_MEMINFO)));
            parameters.add(new BasicNameValuePair("DROPBOX", report.get(ReportField.DROPBOX)));
            parameters.add(new BasicNameValuePair("LOGCAT", report.get(ReportField.LOGCAT)));
            parameters.add(new BasicNameValuePair("EVENTSLOG", report.get(ReportField.EVENTSLOG)));
            parameters.add(new BasicNameValuePair("RADIOLOG", report.get(ReportField.RADIOLOG)));
            parameters.add(new BasicNameValuePair("IS_SILENT", report.get(ReportField.IS_SILENT)));
            parameters.add(new BasicNameValuePair("DEVICE_ID", report.get(ReportField.DEVICE_ID)));
            parameters.add(new BasicNameValuePair("INSTALLATION_ID", report.get(ReportField.INSTALLATION_ID)));
            parameters.add(new BasicNameValuePair("USER_EMAIL", report.get(ReportField.USER_EMAIL)));
            parameters.add(new BasicNameValuePair("DEVICE_FEATURES", report.get(ReportField.DEVICE_FEATURES)));
            parameters.add(new BasicNameValuePair("ENVIRONMENT", report.get(ReportField.ENVIRONMENT)));
            parameters.add(new BasicNameValuePair("SETTINGS_SYSTEM", report.get(ReportField.SETTINGS_SYSTEM)));
            parameters.add(new BasicNameValuePair("SETTINGS_SECURE", report.get(ReportField.SETTINGS_SECURE)));
            parameters.add(new BasicNameValuePair("SHARED_PREFERENCES", report.get(ReportField.SHARED_PREFERENCES)));
            parameters.add(new BasicNameValuePair("APPLICATION_LOG", report.get(ReportField.APPLICATION_LOG)));
            parameters.add(new BasicNameValuePair("MEDIA_CODEC_LIST", report.get(ReportField.MEDIA_CODEC_LIST)));
            parameters.add(new BasicNameValuePair("THREAD_DETAILS", report.get(ReportField.THREAD_DETAILS)));
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            Log.d(TAG, "HTTP response code: " + httpResponse.getStatusLine().getStatusCode());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding", e);
        } catch (IOException e) {
            Log.e(TAG, "IO exception", e);
        }
    }

    private String getUrl() {
        String token = getToken();
        String key = getKey(token);
        return String.format("%s&token=%s&key=%s&", BASE_URL, token, key);
    }

    private String getKey(String token) {
        return md5(String.format("%s+%s", SHARED_SECRET, token));
    }

    private String getToken() {
        return md5(UUID.randomUUID().toString());
    }
}

