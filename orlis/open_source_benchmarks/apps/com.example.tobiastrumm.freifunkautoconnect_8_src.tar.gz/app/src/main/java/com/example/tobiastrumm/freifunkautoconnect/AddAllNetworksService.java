package com.example.tobiastrumm.freifunkautoconnect;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

public class AddAllNetworksService extends IntentService {

    public static final String INPUT_NETWORKS = "input_networks";
    public static final String STATUS_TYPE = "status_type";
    public static final String STATUS_TYPE_PROGRESS = "type_progress";
    public static final String STATUS_TYPE_FINISHED = "type_finished";
    public static final String STATUS_PROGRESS = "status_progress";
    public static final String BROADCAST_ACTION = "com.example.tobiastrumm.freifunkautoconnect.addallnetworkservice.BROADCAST";
    ArrayList<Network> networks;

    public AddAllNetworksService(){
        super("AddAllNetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        networks = intent.getParcelableArrayListExtra(INPUT_NETWORKS);

        // Add all networks to network configuration
        int i = 0;
        WifiManager wmAsync = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        for(Network n: networks){
            if(!n.active){
                n.active = true;
                // Create WifiConfiguration and add it to the known networks.
                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = n.ssid;
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                int networkId = wmAsync.addNetwork(wc);
                wmAsync.enableNetwork(networkId, false);
            }
            i++;
            responseProgress(i);
        }
        // Save configuration
        wmAsync.saveConfiguration();

        responseFinished();
    }

    private void responseProgress(int i){
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(STATUS_TYPE, STATUS_TYPE_PROGRESS);
        localIntent.putExtra(STATUS_PROGRESS, i);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void responseFinished(){
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(STATUS_TYPE, STATUS_TYPE_FINISHED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
