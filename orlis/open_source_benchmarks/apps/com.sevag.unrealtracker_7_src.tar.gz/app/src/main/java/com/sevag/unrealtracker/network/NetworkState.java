package com.sevag.unrealtracker.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by sevag on 3/28/15.
 */
public class NetworkState {
    public static boolean isConnected(Activity callingActivity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) callingActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
