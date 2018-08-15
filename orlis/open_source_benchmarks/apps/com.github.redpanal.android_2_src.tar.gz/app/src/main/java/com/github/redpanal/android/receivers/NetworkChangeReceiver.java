/*
    This file is part of the Diaspora Native WebApp.

    Diaspora Native WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora Native WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.redpanal.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.redpanal.android.helpers.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public static final String CONNECTION_STATE_CHANGE = "CONNECTION_STATE_CHANGE";

    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);

        Intent broadcastIntent = new Intent(CONNECTION_STATE_CHANGE);
        broadcastIntent.putExtra("CONNECTION_STATE_CHANGE", status);
        context.sendBroadcast(broadcastIntent);

    }
}
