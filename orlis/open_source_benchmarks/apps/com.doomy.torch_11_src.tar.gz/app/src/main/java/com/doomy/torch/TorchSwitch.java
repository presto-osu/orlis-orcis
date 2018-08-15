/**
 * Copyright (C) 2014 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.torch;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

public class TorchSwitch extends BroadcastReceiver {

    // Declaring your view and variables
	private static final String TAG = "TorchSwitch";
    public static final String TOGGLE_FLASHLIGHT = "com.doomy.torch.TOGGLE_FLASHLIGHT";
    public static final String TORCH_STATE_CHANGED = "com.doomy.torch.TORCH_STATE_CHANGED";
    private SharedPreferences mPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean mPrefDevice = mPreferences.getBoolean("mPrefDevice", false);

        if (!mPrefDevice) {
            TorchWidgetProvider mProvider = new TorchWidgetProvider();
            mProvider.disableWidget(context);
        }

        if (intent.getAction().equals(TOGGLE_FLASHLIGHT)) {
            boolean sos = intent.getBooleanExtra("sos", 
					mPreferences.getBoolean(SettingsActivity.KEY_SOS, false));

            Intent mIntent = new Intent(context, TorchService.class);
            if (this.isTorchServiceRunning(context)) {
                context.stopService(mIntent);
            } else {
                mIntent.putExtra("sos", sos);
                context.startService(mIntent);
            }
        }
    }

    private boolean isTorchServiceRunning(Context context) {

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> mList = mActivityManager.getRunningServices(100);

        if (!(mList.size() > 0)) {
            return false;
		}
        for (RunningServiceInfo mServiceInfo : mList) {
            ComponentName mServiceName = mServiceInfo.service;
            if (mServiceName.getClassName().endsWith(".TorchService")
                    || mServiceName.getClassName().endsWith(".RootTorchService"))
                return true;
        }
        return false;
    }
}
