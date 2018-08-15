/*

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package org.osmocom.tacdatabaseclient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 225;
    private static final int MY_PERMISSIONS_INTERNET = 223;

    private static final String TAG = "osmoTAC";
    private String imei, model, manufacturer, tac;
    private long app_status;

    // UI elements
    private FloatingActionButton button_send;
    private Button button_allow;
    private Toolbar toolbar;
    private TextView text_send_data;
    private TextView text_send_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // needed for getDeviceInfo()
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // UI elements
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView text_footer = (TextView) findViewById(R.id.url_text);
        text_send_data = (TextView) findViewById(R.id.send_data);
        text_send_status = (TextView) findViewById(R.id.send_status);
        button_allow = (Button) findViewById(R.id.button_allow);
        button_send = (FloatingActionButton) findViewById(R.id.fab);
        button_send.setEnabled(false);

        if (getStatus(this) > 0) {
            TextView header = (TextView) findViewById(R.id.blabla);
            header.setText(R.string.already_done);
            button_allow.setText(getText(R.string.uninstall));

            button_allow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uninstallApp();
                }
            });
        } else {
            button_allow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPermissions();
                }
            });
        }

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInformation();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getDeviceInformation();

                } else {
                    text_send_data.setText("permissions denied!");
                }
                return;
            }
        }
    }

    protected void requestPermissions() {
        Log.d(TAG, "requesting permissions");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            getDeviceInformation();
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_INTERNET);
        }
    }

    protected void getDeviceInformation() {
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = mngr.getDeviceId();

        if (imei == null) {
            text_send_data.setText(R.string.tac_error);
            return;
        }

        model = Build.MODEL;
        manufacturer = Build.MANUFACTURER;
        tac = imei.substring(0, 8);

        button_send.setEnabled(true);
        button_allow.setEnabled(false);

        String inf = String.format("This is the data that we send:\n\n\t\tTAC: %s\n\t\tModel: %s" +
                "\n\t\tManufacturer: %s", tac, model, manufacturer);
        text_send_data.setText(inf);

        Log.d(TAG, Build.HARDWARE);
        Log.d(TAG, Build.MODEL + "" + Build.MANUFACTURER);
        Log.d(TAG, tac);
    }

    protected void sendInformation() {
        URL url;
        HttpURLConnection urlConnection;
        String reqURL = getString(R.string.submit_resource);
        int statusCode;

        text_send_status.setText(R.string.send_status);

        try {
            reqURL += "?tac=" + URLEncoder.encode(tac, "utf-8");
            reqURL += "&model=" + URLEncoder.encode(model, "utf-8");
            reqURL += "&manufacturer=" + URLEncoder.encode(manufacturer, "utf-8");

            url = new URL(reqURL);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.disconnect();
            statusCode = urlConnection.getResponseCode();
            text_send_status.append(getString(R.string.send_done));

            Log.d(TAG, "status: " + statusCode);
            if (urlConnection != null) {
                if (statusCode == 404) {
                    setStatus(this);
                    button_send.setEnabled(false);
                    showToast(getString(R.string.thanks_msg));
                    Log.d(TAG, "app_status:" + String.valueOf(app_status));
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "unsupported encoding");
            showToast(getString(R.string.error_string));
            return;
        } catch (MalformedURLException me) {
            Log.d(TAG, "malformed URL!?");
            showToast(getString(R.string.error_string));
            return;
        } catch (IOException e) {
            text_send_status.setText(getString(R.string.send_error));
            showToast(getString(R.string.error_string));
        }

    }

    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private void setStatus(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.app_status), 1);
        editor.commit();
    }

    private long getStatus(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(getString(R.string.app_status), 0);
    }

    private void uninstallApp() {
        Uri packageUri = Uri.parse(getString(R.string.package_uri));
        Intent uninstallIntent =
                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        startActivity(uninstallIntent);
    }

}
