package com.evenement.eveControl;

import android.util.Log;

import javax.net.ssl.HostnameVerifier ;
import javax.net.ssl.SSLSession;

public class HostVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i("RestUtilImpl", "Approving certificate for " + hostname);
        return true;
    }

}