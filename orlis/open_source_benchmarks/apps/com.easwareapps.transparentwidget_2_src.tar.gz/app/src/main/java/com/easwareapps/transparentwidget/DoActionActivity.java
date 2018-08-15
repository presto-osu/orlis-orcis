package com.easwareapps.transparentwidget;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


/**
 * ॐ
 * लोकाः समस्ताः सुखिनो भवन्तु॥
 * <p/>
 * EmptyWidget
 * Copyright (C) 2016  vishnu
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class DoActionActivity extends AppCompatActivity{

    ComponentName mAdminName;
    final static int ENABLE_ADMIN = 100;

    final static int DO_NOTHING = 0;
    final static int LOCK_SCREEN = 1;
    final static int OPEN_APP = 2;
    final static int TOGGLE_FLASH = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int action = getIntent().getIntExtra("action", -1);

        Log.d("ACTION", action + "");
        try {
            doActions(action);
        }catch (Exception e){
            e.printStackTrace();
        }



    }

    private void doActions(int action){
        switch (action){
            case DO_NOTHING:
                break;
            case  LOCK_SCREEN:
                checkPermissionAndLock();
                break;
            case OPEN_APP:
                //openApp();
                break;
            case TOGGLE_FLASH:
                if(checkCameraPermissions()) {
                    toggleFlash();
                }
                break;
            default:
                break;
        }
    }

    private void checkPermissionAndLock(){
        mAdminName = new ComponentName(this, AdminManageReceiver.class);
        DevicePolicyManager mDPM  = (DevicePolicyManager)getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(!mDPM.isAdminActive(mAdminName)) {
            showAdminManagement();
        }else{
            lockNow();
            finish();
        }
    }

    private void showAdminManagement() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                R.string.desc_enable_admin);
        startActivityForResult(intent, ENABLE_ADMIN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_ADMIN) {
            lockNow();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void lockNow(){
        DevicePolicyManager mDPM  = (DevicePolicyManager)getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(!mDPM.isAdminActive(mAdminName)){
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.desc_cant_lock),
                    Toast.LENGTH_LONG).show();
        }else{
            mDPM.lockNow();
            finish();
        }
    }

    private boolean checkCameraPermissions(){
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }
        int hasStoragePermission = checkSelfPermission(Manifest.permission.CAMERA);
        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    TOGGLE_FLASH);
            return false;

        }
        return true;
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case TOGGLE_FLASH:
                int hasStoragePermission = checkSelfPermission(Manifest.permission.CAMERA);
                if (hasStoragePermission == PackageManager.PERMISSION_GRANTED) {
                    toggleFlash();
                    finish();
                }else{
                    Toast.makeText(this, "Can't Turn on Flash Without Camera Permission",
                            Toast.LENGTH_LONG).show();
                }

        }
    }

    private void toggleFlash(){
        Intent i = new Intent(this, DoActionService.class);
        i.setAction(TOGGLE_FLASH + "");
        startService(i);
    }

}
