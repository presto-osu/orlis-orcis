/*
 * Copyright (c) 2016 Juan García Basilio
 *
 * This file is part of WaveUp.
 *
 * WaveUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WaveUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WaveUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jarsilio.android.waveup;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jarsilio.android.waveup.receivers.LockScreenAdminReceiver;

public class MainActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";

    private static final int ADD_DEVICE_ADMIN_REQUEST_CODE = 1;
    private static final int UNINSTALL_REQUEST_CODE = 200;
    private static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 300;
    private static final int UNINSTALL_CANCELED_MSG_SHOW_TIME = 5000;
    private static final int UNINSTALL_CANCELED_MSG_SHOW_INTERVAL = 1000;

    private static boolean removeAdminRights = false;

    private static final String[] READ_PHONE_STATE_PERMISSION = {"android.permission.READ_PHONE_STATE"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Starting WaveUp MainActivity (GUI)");
        showInitialDialog();
        getSettings().setPreferenceActivity(this);
        super.onCreate(savedInstanceState);
        createLayout();
        startService();
        registerPreferencesListener();
    }

    private void showInitialDialog() {
        if (!getSettings().isInitialDialogShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.alert_dialog_title);
            builder.setMessage(R.string.alert_dialog_message);
            builder.setPositiveButton(R.string.alert_dialog_ok_button, null);
            builder.show();

            getSettings().setInitialDialogShown(true);
        }
    }

    private Settings getSettings() {
        return Settings.getInstance(getApplicationContext());
    }

    private void createLayout() {
        addPreferencesFromResource(R.xml.settings);
        Button uninstallButton = new Button(getApplicationContext());
        uninstallButton.setText(R.string.uninstall_button);
        uninstallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                uninstallApp();
            }
        });

        ListView listView = getListView();
        listView.addFooterView(uninstallButton);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ProximitySensorManager.getInstance(getApplicationContext()).startOrStopListeningDependingOnConditions();
        switch (key) {
            case Settings.ENABLED:
                if (getSettings().isServiceEnabled()) {
                    requestReadPhoneStatePermission();
                }
                startService();
            case Settings.LOCK_SCREEN:
                if (getSettings().isLockScreen() && !getSettings().isLockScreenAdmin()) {
                    requestLockScreenAdminRights();
                }
                break;
            case Settings.LOCK_SCREEN_WITH_POWER_BUTTON:
                if (getSettings().isLockScreenWithPowerButton()) {
                    if (!Root.requestSuPermission()) {
                        getSettings().setLockScreenWithPowerButton(false);
                        Toast.makeText(this, R.string.root_access_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void startService() {
        if (getSettings().isServiceEnabled()) {
            Log.i(TAG, "Starting WaveUpService");
            startService(new Intent(this, WaveUpService.class));
            Toast.makeText(this, R.string.wave_up_service_started, Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Stopping WaveUpService");
            stopService(new Intent(this, WaveUpService.class));
            Toast.makeText(this, R.string.wave_up_service_stopped, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestReadPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, READ_PHONE_STATE_PERMISSION, READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestLockScreenAdminRights() {
        ComponentName lockScreenAdminComponentName = new ComponentName(getApplicationContext(), LockScreenAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, lockScreenAdminComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.lock_admin_rights_explanation);
        startActivityForResult(intent, ADD_DEVICE_ADMIN_REQUEST_CODE);
    }

    private void uninstallApp() {
        if (getSettings().isLockScreenAdmin()) {
            Log.i(TAG, "Removing lock screen admin rights");
            ComponentName devAdminReceiver = new ComponentName(getApplicationContext(), LockScreenAdminReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.removeActiveAdmin(devAdminReceiver);
            removeAdminRights = true;

            // If the user cancels the uninstall he/she will have to switch it back on (to request the admin rights again)
            getSettings().setLockScreen(false);
        }

        Log.i(TAG, "Uninstalling app");
        Uri packageURI = Uri.parse("package:" + "com.jarsilio.android.waveup");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivityForResult(uninstallIntent, UNINSTALL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ADD_DEVICE_ADMIN_REQUEST_CODE:
                if (!getSettings().isLockScreenAdmin()) {
                    // If the user does not activate lock admin switch off lock screen option
                    getSettings().setLockScreen(false);
                } else {
                    ProximitySensorManager.getInstance(getApplicationContext()).startOrStopListeningDependingOnConditions();
                }
                break;
            case UNINSTALL_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED && removeAdminRights) {
                    final Toast canceledMsg = Toast.makeText(this, R.string.removed_device_admin_rights, Toast.LENGTH_SHORT);
                    canceledMsg.show();
                    /* Show message UNINSTALL_CANCELED_MSG_SHOW_TIME second */
                    new CountDownTimer(UNINSTALL_CANCELED_MSG_SHOW_TIME, UNINSTALL_CANCELED_MSG_SHOW_INTERVAL) {
                        public void onTick(long millisUntilFinished) { canceledMsg.show(); }
                        public void onFinish() { canceledMsg.cancel(); }
                    }.start();
                    removeAdminRights = false;
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerPreferencesListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterPreferencesListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPreferencesListener();
    }

    private void registerPreferencesListener() {
        getSettings().getPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPreferencesListener() {
        getSettings().getPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
