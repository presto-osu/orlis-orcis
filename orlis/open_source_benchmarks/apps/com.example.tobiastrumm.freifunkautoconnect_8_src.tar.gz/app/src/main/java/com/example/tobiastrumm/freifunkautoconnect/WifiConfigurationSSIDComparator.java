package com.example.tobiastrumm.freifunkautoconnect;

import android.net.wifi.WifiConfiguration;

import java.util.Comparator;

/**
 * Custom Comparator that lets you compare the SSID of a WifiConfiguration Object with a String (another SSID)
 */
public class WifiConfigurationSSIDComparator implements Comparator<Object> {
    @Override
    public int compare(Object wifiConfiguration, Object ssid) {
        if(!(wifiConfiguration instanceof WifiConfiguration) || !(ssid instanceof String)){
            throw new ClassCastException();
        }
        return ((WifiConfiguration) wifiConfiguration).SSID.compareTo(((String) ssid));
    }
}