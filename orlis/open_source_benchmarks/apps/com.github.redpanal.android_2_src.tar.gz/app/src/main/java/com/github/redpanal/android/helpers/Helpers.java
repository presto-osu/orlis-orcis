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

package com.github.redpanal.android.helpers;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Helpers {

    public static boolean isOnline(Context context){
        ConnectivityManager cnm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cnm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static boolean isUsingMobile(Context context) {
        ConnectivityManager cnm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cnm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ni != null && ni.isConnectedOrConnecting();
    }

    // Show a warning when is connected using mobile Internet
    public static void warningMobile(Context ctx) {
        if (Helpers.isUsingMobile(ctx)) {
            Toast.makeText(
                    ctx,
                    "Warning: Connected via mobile",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
