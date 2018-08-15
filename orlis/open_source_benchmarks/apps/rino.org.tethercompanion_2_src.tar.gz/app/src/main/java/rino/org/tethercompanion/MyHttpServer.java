package rino.org.tethercompanion;

/*
 * This is the source code of Tether companion for Android.
 * It is licensed under GNU GPL v. 3 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Rinat Kurmaev, 2015-2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import fi.iki.elonen.NanoHTTPD;



public class MyHttpServer extends NanoHTTPD {
    private Context context;
    int batteryLevel;
    private Connectivity connManger;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };

    MyHttpServer(Context context)
    {
        super(8000);
        this.context=context;
    }


    @Override
    public Response serve(IHTTPSession session) {

        context.getApplicationContext().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        connManger = new Connectivity();

        Map<String, String> parms = session.getParms();

        //Log.d("params", String.valueOf(parms));
        String answer = "<!DOCTYPE html>\n<html><body>\n<meta charset=\"UTF-8\">";
        //css
        //there is still no CSS
        //end of css
        answer += "<H1>Welcome to Tether companion</H1>\n";
        answer +="<p>" + context.getResources().getString(R.string.battery_level) + " :" + batteryLevel + "% </p>\n";
        answer += "<p>" +  context.getResources().getString(R.string.network_type) +" :" +String.valueOf(connManger.SubType(context)) + "</p>\n";
        answer += "<p>IP: " + getWifiApIpAddress() +"</p>\n";
        if (connManger.isConnected(context))
        {
            answer +="<p> " +context.getResources().getString(R.string.network_connected) + "</p>\n";
        }
        else
        {
            answer +="<p> " +context.getResources().getString(R.string.network_disconnected) + "</p>\n";

        }
        if (connManger.isRestricted())
        {
            answer +="<p> " +context.getResources().getString(R.string.network_restricted) + "</p>\n";
        }
        else
        {
            answer +="<p> " +context.getResources().getString(R.string.network_nonrestricted) + "</p>\n";
        }
        return newFixedLengthResponse( answer + "</body></html>\n" );
    }



    public String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ex", ex.toString());
        }
        return null;
    }

}