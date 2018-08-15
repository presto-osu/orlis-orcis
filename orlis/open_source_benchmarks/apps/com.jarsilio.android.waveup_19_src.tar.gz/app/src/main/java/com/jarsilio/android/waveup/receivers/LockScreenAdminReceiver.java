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

package com.jarsilio.android.waveup.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jarsilio.android.waveup.R;
import com.jarsilio.android.waveup.Settings;

public class LockScreenAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "LockScreenAdminReceiver";

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);

        Log.d(TAG, "Disabled lock device admin");

        Toast.makeText(context, R.string.removed_device_admin_rights, Toast.LENGTH_SHORT).show();
        Settings.getInstance(context).setLockScreen(false);
    }
}
