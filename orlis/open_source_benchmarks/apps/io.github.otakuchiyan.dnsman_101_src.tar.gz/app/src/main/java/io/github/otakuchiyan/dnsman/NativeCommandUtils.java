/*
 - Author: otakuchiyan
 - License: GNU GPLv3
 - Description: The impletion and interface class
 */

package io.github.otakuchiyan.dnsman;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.chainfire.libsuperuser.Shell;

public final class NativeCommandUtils implements ValueConstants{
    private NativeCommandUtils(){
    }

    private static List<String> runWithLog(String command){
        Log.d("NativeCommand", "-> " + command);
        return Shell.SU.run(command);
    }

    private static List<String> runWithLog(List<String> command){
        for(String s: command) {
            Log.d("NativeCommand", "-> " + s);
        }
        return Shell.SU.run(command);
    }

    private static List<String> runWithLog(String[] command){
        for(String s: command) {
            Log.d("NativeCommand", "-> " + s);
        }
        return Shell.SU.run(command);
    }


    public static int setDnsViaSetprop(String dns1, String dns2) {
		String[] setCommands = {
            SETPROP_COMMAND_PREFIX + "1 \"" + dns1 + '\"',
            SETPROP_COMMAND_PREFIX + "2 \"" + dns2 + '\"'
        };

        runWithLog(setCommands);
        return 0;
    }

	public static int setDnsViaIptables(String dns){
        List<String> cmds = new ArrayList<>();

        String cmd1 = String.format(SETRULE_COMMAND, "-A", "udp", dns);
        String cmd2 = String.format(SETRULE_COMMAND, "-A", "tcp", dns);

        cmds.add(cmd1);
        cmds.add(cmd2);

        Log.d("DNSManager.rules", cmd1);
        Log.d("DNSManager.rules", cmd2);

        return runWithLog(cmds).isEmpty() ? 0 : ERROR_UNKNOWN;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getNetId(Context c){
        String netId = "";
        ConnectivityManager manager =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        try{
            Network[] networks = manager.getAllNetworks();
            for(Network i: networks){
                netId = i.getClass().getDeclaredField("netId").get(i).toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return netId;
    }

    public static int setDnsViaNdc(Context c, String dns1, String dns2){
        String cmd;
        ConnectivityManager manager =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //>=5.0
            String netId = getNetId(c);
            if(netId.equals("")){
                return ERROR_GET_NETID_FAILED;
            }
            cmd = String.format(SETNETDNS_COMMAND, netId, dns1, dns2);
        } else {
            String interfaceName;
            NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();
            if(currentNetworkInfo == null){
                return ERROR_GET_CURRENT_NETWORK_FAILED;
            }
            DnsmanCore.refreshInfo2InterfaceMap(c);
            interfaceName = DnsmanCore.info2interfaceMap.get(currentNetworkInfo.getTypeName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { //>=4.3
                cmd = String.format(SETIFDNS_COMMAND, interfaceName, dns1, dns2);
            } else { //<=4.2
                cmd = String.format(SETIFDNS_COMMAND_BELOW_42, interfaceName, dns1, dns2);
            }
        }

        List<String> result = runWithLog(cmd);

        String resultString = result.get(0);
        try{
            if(!resultString.substring(0, 3).equals("200")){
                return ERROR_UNKNOWN;
            }
        }catch (Exception e){
            Toast.makeText(c, "Error occured.\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    public static int flushDnsViaNdc(Context c){
        String flushdns_cmd;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String netId = getNetId(c);
            if(netId.equals("")){
                return ERROR_GET_NETID_FAILED;
            }
            flushdns_cmd = String.format(ValueConstants.FLUSHNET_COMMAND, netId);
        } else { //<=4.4, netId will be ignored
            flushdns_cmd = ValueConstants.FLUSHDEFAULTIF_COMMAND;
        }

        runWithLog(flushdns_cmd);
        return 0;
    }

    public static int flushDnsViaSettings(){
        List<String> cmds = new ArrayList<>();
        cmds.add("settings put global airplane_mode_on 1");
        cmds.add("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
        cmds.add("settings put global airplane_mode_on 0");
        cmds.add("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
        runWithLog(cmds);
        return 0;
    }

    //Below Android 4.2
    public static int flushDnsViaApi(Context context){

        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 1);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        context.sendBroadcast(intent);
        try {
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }
        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", false);
        context.sendBroadcast(intent);
        return 0;
    }

	public static boolean isRulesAlivable(String dns){
		String cmd = CHECKRULE_COMMAND_PREFIX + dns;

        Log.d("DNSManager[CMD]", cmd);
		return !runWithLog(cmd).isEmpty();
	}

    public static List<String> deleteRules(String dns){
        List<String> cmds = new ArrayList<>();
        String cmd1 = String.format(SETRULE_COMMAND, "-D", "udp", dns);
        String cmd2 = String.format(SETRULE_COMMAND, "-D", "tcp", dns);

        cmds.add(cmd1);
        cmds.add(cmd2);

        Log.d("DNSManager.deleteRules", cmd1);
        Log.d("DNSManager.deleteRules", cmd2);
        return runWithLog(cmds);
    }
	
	public static List<String> getCurrentPropDNS(){
		return Shell.SH.run(CHECKPROP_COMMANDS);
	}

}
