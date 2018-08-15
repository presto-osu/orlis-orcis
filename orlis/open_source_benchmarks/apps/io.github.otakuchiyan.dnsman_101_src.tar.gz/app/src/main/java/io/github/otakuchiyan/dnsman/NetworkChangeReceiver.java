package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver implements ValueConstants{
    private boolean isFirstConnect = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm;
        NetworkInfo currentNet;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (isFirstConnect && sp.getBoolean(ValueConstants.KEY_PREF_AUTO_SETTING, true)) {
            //Not first boot
            if (!sp.getBoolean(ValueConstants.KEY_FIRST_BOOT, true)) {

                //Workaround to deal with multiple broadcast
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                currentNet = cm.getActiveNetworkInfo();
                if (currentNet != null) {
                    isFirstConnect = false;
                    String dnsToast = sp.getString(ValueConstants.KEY_PREF_TOAST, ValueConstants.TOAST_SHOW);
                    BackupNetworkDnsTask.startAction(context);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_NETWORK_CONNECTED));
                    if (!ExecuteIntentService.startActionByInfo(context, currentNet)) {
                        if (!dnsToast.equals(ValueConstants.TOAST_NEVER)) {
                            Toast.makeText(context, R.string.toast_no_dns, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }
}
