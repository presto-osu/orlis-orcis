package com.thefonz.ed_tool.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by thefonz on 26/03/15.
 */
public class Utils extends Application {

    protected final static String TAG = "ED-Tool";

    // Toast calls
    public static void showToast_Short(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
    public static void showToast_Long(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
    /**
     * This method checks if the device has an active internet
     * connection or not.
     *
     * @param context
     *          - context of the class from where it is called
     * @return
     *      Returns true if there is internet connectivity
     */
    public static Boolean checkInternet(Context context){
        final String LOGMETHOD = " checkInternet ";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            String LOGBODY = " Network Connection Found ! ";
            LogSuccess(context.getApplicationContext(), TAG, LOGMETHOD, LOGBODY);
            return true;
        }
        else if (netInfo != null && (netInfo.getState() == NetworkInfo.State.DISCONNECTED || netInfo.getState() == NetworkInfo.State.DISCONNECTING || netInfo.getState() == NetworkInfo.State.SUSPENDED || netInfo.getState() == NetworkInfo.State.UNKNOWN)){
            String msg = " No Internet Connection Found ! ";
            Utils.showToast_Long(context.getApplicationContext(), msg);
            String LOGBODY = "" + msg;
            LogError(context.getApplicationContext(), TAG, LOGMETHOD, LOGBODY);
            return false;
        }
        else{
            String msg = " No Internet Connection Found ! ";
            Utils.showToast_Long(context.getApplicationContext(), msg);
            String LOGBODY = "" + msg;
            LogError(context.getApplicationContext(), TAG, LOGMETHOD, LOGBODY);
            return false;
        }
    }
    // Error Logging
    public static void LogSuccess(Context context, String TAG, String LOGMETHOD, String LOGBODY) {
        Log.i(TAG, LOGMETHOD + " Success ! " + LOGBODY);
    }
    public static void LogError(Context context, String TAG, String LOGMETHOD, String LOGBODY) {
        Log.e(TAG, LOGMETHOD + " Error ! " + LOGBODY);
    }
    public static void LogWarning(Context context, String TAG, String LOGMETHOD, String LOGBODY) {
        Log.e(TAG, LOGMETHOD + " Warning ! " + LOGBODY);
    }
    public static void m(String message)
    {
        Log.d("theFONZ", message);
    }
}