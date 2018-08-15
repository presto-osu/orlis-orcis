package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Created by 西行寺幽玄 on 3/12/2016.
 */
public class BackupNetworkDnsTask extends AsyncTask<Context, Void, Void> implements ValueConstants{
    @Override
    protected Void doInBackground(Context... params) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(params[0]);
        SharedPreferences.Editor editor = preferences.edit();
        List<String> networkDnsList = NativeCommandUtils.getCurrentPropDNS();
        editor.putString(KEY_NETWORK_DNS1, networkDnsList.get(0));
        editor.putString(KEY_NETWORK_DNS2, networkDnsList.get(1));
        editor.apply();
        return null;
    }

    public static void startAction(Context c){
        new BackupNetworkDnsTask().execute(c);
    }
}
