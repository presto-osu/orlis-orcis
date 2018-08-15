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
package org.pixmob.freemobile.netstat.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import org.pixmob.freemobile.netstat.ui.Netstat;

import static org.pixmob.freemobile.netstat.Constants.NOTIF_ACTION_NETWORK_OPERATOR_SETTINGS;
import static org.pixmob.freemobile.netstat.Constants.NOTIF_ACTION_STATISTICS;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_NOTIF_ACTION;
import static org.pixmob.freemobile.netstat.Constants.SP_NAME;

/**
 * Application intents.
 * @author Pixmob
 */
public final class IntentFactory {
    private IntentFactory() {
    }

    /**
     * Open network operator settings activity.
     */
    public static Intent networkOperatorSettings(Context context) {
        // Check if the network operator settings intent is available.
        Intent networkOperatorSettingsIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);

        ComponentName cName;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            cName = new ComponentName("com.android.phone", "com.android.phone.Settings");
        }
        else {
            cName = new ComponentName("com.android.phone","com.android.phone.MobileNetworkSettings");
        }

        networkOperatorSettingsIntent.setComponent(cName);
        return isIntentAvailable(context, networkOperatorSettingsIntent) ? networkOperatorSettingsIntent : null;
    }

    /**
     * Open wireless settings
     */
     public static Intent wirelessSettings(Context context) {
         final Intent wirelessSettingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
         return isIntentAvailable(context, wirelessSettingsIntent) ? wirelessSettingsIntent : null;
     }

    /**
     * Open statistics.
     */
    private static Intent statistics(Context context) {
        return new Intent(context, Netstat.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    /**
     * Get the intent to handle notification action.
     */
    public static Intent notificationAction(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return statistics(context);
        }

        final SharedPreferences p = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        final String notifAction = p.getString(SP_KEY_NOTIF_ACTION, NOTIF_ACTION_STATISTICS);
        if (NOTIF_ACTION_STATISTICS.equals(notifAction)) {
            return statistics(context);
        }
        if (NOTIF_ACTION_NETWORK_OPERATOR_SETTINGS.equals(notifAction)) {
            return networkOperatorSettings(context);
        }
        return null;
    }

    private static boolean isIntentAvailable(Context context, Intent i) {
        return !context.getPackageManager().queryIntentActivities(i, 0).isEmpty();
    }
}
