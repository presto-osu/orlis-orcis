package io.github.otakuchiyan.dnsman;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class ExecuteIntentService extends IntentService implements ValueConstants{
    public ExecuteIntentService() {
        super("ExecuteIntentService");
    }

    public static boolean startActionByInfo(Context context, NetworkInfo info) {
        DnsmanCore dnsmanCore = new DnsmanCore(context);

        String extra_dns[] = dnsmanCore.getDnsByKeyPrefix(info.getTypeName());

        //Fallback to global
        if(extra_dns[0].equals("") && extra_dns[1].equals("")) {
            extra_dns = dnsmanCore.getGlobalDns();
        }

        if(extra_dns[0].equals("") && extra_dns[1].equals("")){
            return false;
        }

        startActionByString(context, extra_dns);
        return true;
    }

    //Array will be transform to two variable at here
    public static void startActionByString(Context c, String[] dnsEntry){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = preferences.edit();
        String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);

        editor.putString(KEY_LAST_DNS1, dnsEntry[0]);
        editor.putString(KEY_LAST_DNS2, dnsEntry[1]);
        editor.apply();

        Intent intent = new Intent(c, ExecuteIntentService.class);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DNS1, dnsEntry[0]);
        intent.putExtra(EXTRA_DNS2, dnsEntry[1]);
        c.startService(intent);
    }

    public static Intent setWithLastDnsIntent(Context c){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);

        String lastDns1 = preferences.getString(KEY_LAST_DNS1, "");
        String lastDns2 = preferences.getString(KEY_LAST_DNS2, "");

        if(lastDns1.equals("") && lastDns2.equals("")){
            return null;
        }

        Intent intent = new Intent(c, ExecuteIntentService.class);

        Log.d("LASTDNS", lastDns1 + "  " + lastDns2);

        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DNS1, lastDns1);
        intent.putExtra(EXTRA_DNS2, lastDns2);
        return intent;
    }

    public static void setWithLastDns(Context c){
        Intent i = setWithLastDnsIntent(c);
        if(i == null){
            Toast.makeText(c, R.string.toast_no_last_dns, Toast.LENGTH_SHORT).show();
            return;
        }
        c.startService(i);
    }


//Need completing
    //Always can be used, because delete rules and disconnect vpn needn't default dns
    public static Intent restoreIntent(Context c){
        boolean isNeedDns = true;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);

        switch (method) {
            case METHOD_IPTABLES:
                method = METHOD_DELETE_RULES;
                isNeedDns = false;
                break;
            case METHOD_VPN:
                int code = new DnsVpnService().disconnect();
                sendResult(c, code);
                return null;
        }


        String dns1 = "";
        String dns2 = "";

        if(isNeedDns) {
            dns1 = preferences.getString(KEY_NETWORK_DNS1, "");
            dns2 = preferences.getString(KEY_NETWORK_DNS2, "");

            if(dns1.equals("") && dns2.equals("")) {
                sendResult(c, ValueConstants.ERROR_NO_DNS);
                return null;
            }
        }

        Intent intent = new Intent(c, ExecuteIntentService.class);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DNS1, dns1);
        intent.putExtra(EXTRA_DNS2, dns2);
        return intent;
    }

    public static void restore(Context c){
        Intent intent = restoreIntent(c);
        if(intent != null) {
            c.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Context context = getApplicationContext();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            int resultCode = 0;

            final String method = intent.getStringExtra(EXTRA_METHOD);
            final String dns1 = intent.getStringExtra(EXTRA_DNS1);
            final String dns2 = intent.getStringExtra(EXTRA_DNS2);

            //For firewall rules mode
            final String lastHijackedDns = preferences.getString(KEY_HIJACKED_LAST_DNS, "");
            final boolean isAutoFlush = preferences.getBoolean(KEY_PREF_AUTO_FLUSH, false);
            final boolean isRoot = preferences.getBoolean(KEY_IS_ROOT, false);

            //Below 4.2
            if(!isRoot && isAutoFlush && Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1){
                NativeCommandUtils.flushDnsViaApi(context);
            }


            switch(method){
                case METHOD_VPN:
                    VpnWrapperActivity.perform(context, dns1, dns2);
                    break;
                case METHOD_ACCESSIBILITY:
                    break;
                case METHOD_NDC:
                    resultCode = NativeCommandUtils.setDnsViaNdc(context, dns1, dns2);
                    break;
                case METHOD_IPTABLES:
                    if(NativeCommandUtils.isRulesAlivable(dns1)) {
                        return;
                    }else if (!dns1.equals(lastHijackedDns) && //IP was changed
                        NativeCommandUtils.isRulesAlivable(lastHijackedDns)){
                        NativeCommandUtils.deleteRules(lastHijackedDns);
                    }
                    editor.putString(KEY_HIJACKED_LAST_DNS, dns1);
                    resultCode = NativeCommandUtils.setDnsViaIptables(dns1);
                    break;
                case METHOD_DELETE_RULES:
                    NativeCommandUtils.deleteRules(lastHijackedDns);
                    editor.putString(KEY_HIJACKED_LAST_DNS, "");
                    break;
                case METHOD_MODULE:
                    break;
                case METHOD_SETPROP:
                    resultCode = NativeCommandUtils.setDnsViaSetprop(dns1, dns2);
                    break;
            }

            editor.apply();

            if(isRoot && isAutoFlush){
                NativeCommandUtils.flushDnsViaNdc(context);
            }

            if(resultCode <= 1000 && intent.getBooleanExtra(KEY_IS_RESTORE, false)) {
                resultCode = ValueConstants.RESTORE_SUCCEED;
            }

            if(!method.equals(METHOD_VPN)) {
                sendResultWithDns(context, resultCode, dns1, dns2);
            }
        }
    }

    private static void sendResult(Context c, int result_code){
        sendResultWithDns(c, result_code, "", "");
    }

    private static void sendResultWithDns(Context c, int result_code, String dns1, String dns2){
        Intent result_intent = new Intent(ACTION_SET_DNS);
        result_intent.putExtra(EXTRA_RESULT_CODE, result_code);
        result_intent.putExtra(EXTRA_DNS1, dns1);
        result_intent.putExtra(EXTRA_DNS2, dns2);
        LocalBroadcastManager.getInstance(c).sendBroadcast(result_intent);
    }
}
