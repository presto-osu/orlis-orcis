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
package org.pixmob.freemobile.netstat.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;

import org.pixmob.freemobile.netstat.Constants;
import org.pixmob.freemobile.netstat.MobileOperator;
import org.pixmob.freemobile.netstat.MonitorService;
import org.pixmob.freemobile.netstat.PermissionsManager;
import org.pixmob.freemobile.netstat.R;
import org.pixmob.freemobile.netstat.SyncService;
import org.pixmob.freemobile.netstat.feature.Features;
import org.pixmob.freemobile.netstat.feature.SharedPreferencesSaverFeature;
import org.pixmob.freemobile.netstat.util.IntentFactory;

/**
 * Main application activity.
 * @author Pixmob
 */
@SuppressLint("CommitPrefEdits")
public class Netstat extends FragmentActivity {

    public static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
    };
    public static final String EXPORT_TASK_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static final int REQUIRED_PERMISSIONS_CODE = 0;
    public static final int EXPORT_TASK_PERMISSION_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new StatisticsFragment()).commitAllowingStateLoss();
        }

        if (MobileOperator.FREE_MOBILE.isCurrentSimOwner(this) != 1) {
            new UnsupportedSimDialogFragment().show(getSupportFragmentManager(), "error");
            return;
        }

        final int applicationVersion;
        try {
            applicationVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // Unlikely to happen.
            throw new RuntimeException("Failed to get application version", e);
        }

        final String versionKey = "version";
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        final int lastKnownVersion = prefs.getInt(versionKey, 0);
        if (lastKnownVersion != applicationVersion) {
            // Store the current application version.
            final SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putInt(versionKey, applicationVersion);
            Features.getFeature(SharedPreferencesSaverFeature.class).save(prefsEditor);

            // The application was updated: let's show changelog.
            startActivity(new Intent(this, DocumentBrowser.class).putExtra(DocumentBrowser.INTENT_EXTRA_URL,
                    "CHANGELOG.html"));
        }

        requestRequiredPermissions();

        // FIXME Remove this (+ SP key + messages strings + workaround in MonitorService) once OxygenOS has fixed the bug.
        showOnePlusTwoErrorMessage();
    }

    private void checkManualMode() {
        final SharedPreferences prefs = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
        final boolean manualModeDetectionEnabled = prefs.getBoolean(Constants.SP_KEY_MANUAL_MODE_DETECTION_ENABLED, true);

        if (manualModeDetectionEnabled) {
            PhoneStateListener mPhoneListener = new PhoneStateListener() {
                @Override
                public void onServiceStateChanged(ServiceState serviceState) {
                    if (serviceState.getIsManualSelection()) {
                        showManualModeDialog();
                    }
                    super.onServiceStateChanged(serviceState);
                }
            };

            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void showManualModeDialog() {
        final Resources res = getResources();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.manual_mode_dialog_title))
                .setMessage(res.getString(R.string.manual_mode_dialog_message))
                .setNegativeButton(
                        res.getString(R.string.manual_mode_dialog_action_dismiss),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final SharedPreferences prefs = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
                                final SharedPreferences.Editor prefsEditor = prefs.edit();
                                prefsEditor.putBoolean(Constants.SP_KEY_MANUAL_MODE_DETECTION_ENABLED, false);
                                Features.getFeature(SharedPreferencesSaverFeature.class).save(prefsEditor);
                            }
                        }
                )
                .setNeutralButton(
                        res.getString(R.string.manual_mode_dialog_action_close),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );

        final Intent networkOperatorSettingsIntent = IntentFactory.networkOperatorSettings(this);
        if (networkOperatorSettingsIntent != null) {
            alertDialogBuilder = alertDialogBuilder.setPositiveButton(
                    res.getString(R.string.manual_mode_dialog_action_open_network_settings),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(networkOperatorSettingsIntent);
                        }
                    }
            );
        }

        alertDialogBuilder = alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        alertDialogBuilder.show();
    }

    private void requestRequiredPermissions() {
        if (PermissionsManager.checkSelfPermissions(this, REQUIRED_PERMISSIONS) != PackageManager.PERMISSION_GRANTED) {
            if (PermissionsManager.shouldShowRequestPermissionsRationale(this, REQUIRED_PERMISSIONS)) {
                showPermissionExplanationDialog();
            }
            else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS_CODE);
            }
        }
        else {
            checkManualMode();
            startService();
        }

    }

    private void showPermissionExplanationDialog() {
        final Activity _this = this;
        Resources res = getResources();

        showInformationDialog(
                res.getString(R.string.required_permission_explanation_title),
                res.getString(R.string.required_permission_explanation_message),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(_this, REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS_CODE);
                    }
                }
        );
    }

    private void showInformationDialog(String title, String message, DialogInterface.OnClickListener callback) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                    android.R.string.yes,
                    callback
            )
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void startService() {
        final Context c = getApplicationContext();
        final Intent i = new Intent(c, MonitorService.class);
        c.startService(i);

        SyncService.schedule(this, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUIRED_PERMISSIONS_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkManualMode();
                    startService();
                } else {
                    showPermissionExplanationDialog();
                }

                break;
            case EXPORT_TASK_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((StatisticsFragment)getSupportFragmentManager().findFragmentById(android.R.id.content)).launchExportTask();
                }

                break;
        }
    }

    private void showOnePlusTwoErrorMessage() {
        if (MonitorService.ONE_PLUS_TWO_MANUFACTURER.equals(Build.MANUFACTURER) && MonitorService.ONE_PLUS_TWO_MODEL.equals(Build.MODEL)) {
            final SharedPreferences prefs = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
            if (!prefs.getBoolean(Constants.SP_KEY_ONE_PLUS_TWO_MESSAGE_SEEN, false)) {
                Resources res = getResources();
                new AlertDialog.Builder(this)
                        .setTitle(res.getString(R.string.one_plus_two_error_message_title))
                        .setMessage(res.getString(R.string.one_plus_two_error_message_message))
                        .setPositiveButton(
                                res.getString(R.string.one_plus_two_error_message_dismiss),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        final SharedPreferences.Editor prefsEditor = prefs.edit();
                                        prefsEditor.putBoolean(Constants.SP_KEY_ONE_PLUS_TWO_MESSAGE_SEEN, true);
                                        Features.getFeature(SharedPreferencesSaverFeature.class).save(prefsEditor);
                                    }
                                }
                        )
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

    }
    
    public void enlargeChart(View view) {
    	Intent intent = new Intent(this, MobileNetworkChartActivity.class);
    	startActivity(intent);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Enable "better" gradients:
        // http://stackoverflow.com/a/2932030/422906
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.getDecorView().getBackground().setDither(true);
    }
}
