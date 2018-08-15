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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import org.pixmob.freemobile.netstat.ui.Netstat;

import static org.pixmob.freemobile.netstat.Constants.SP_KEY_ENABLE_AT_BOOT;
import static org.pixmob.freemobile.netstat.Constants.SP_NAME;
import static org.pixmob.freemobile.netstat.Constants.TAG;

/**
 * This broadcast receiver will start the {@link MonitorService} when the phone
 * has completed its boot sequence.
 * @author Pixmob
 */
public class MonitorServiceStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            final SharedPreferences p = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            final boolean enabled = p.getBoolean(SP_KEY_ENABLE_AT_BOOT, false);
            if (!enabled) {
                Log.i(TAG, "Monitor service is not started at boot");
            } else {
                Log.i(TAG, "Starting monitor service");
                
                final Context applicationContext = context.getApplicationContext();
                if (PermissionsManager.checkRequiredPermissions(applicationContext) == PackageManager.PERMISSION_GRANTED) {
                    applicationContext.startService(new Intent(applicationContext, MonitorService.class));
                }
                else {
                    Intent mainActivityIntent = new Intent(applicationContext, Netstat.class);
                    mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    applicationContext.startActivity(mainActivityIntent);
                }
            }
        }
    }
}
