package io.github.otakuchiyan.dnsman;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static android.os.Build.VERSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends ListActivity implements ValueConstants {
    private DnsmanCore dnsmanCore;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private LinearLayout currentDnsLayout;
    private TextView currentMethod;
    private TextView currentDnsText1, currentDnsText2;
    private TextView networkDnsText1, networkDnsText2;
    private TextView alertText;
    private String showDns1 = "", showDns2 = "";


    private SimpleAdapter adapter;
    private List<Map<String, String>> mDnsEntryList;

    //Used to update by another thread
    private BroadcastReceiver resultCodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            int result_code = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
            String dns1 = intent.getStringExtra(EXTRA_DNS1);
            String dns2 = intent.getStringExtra(EXTRA_DNS2);

            String dnsToast = mPreferences.getString(KEY_PREF_TOAST, TOAST_SHOW);
            boolean isShowNotify = mPreferences.getBoolean(KEY_PREF_NOTIFICATION, true);

            //Succeed
            if(result_code <= 1000){
                //Toast
                if (dnsToast.equals(TOAST_SHOW)) {

                    showToastByCodeWithDns(context, result_code, dns1, dns2);
                }
                if(isShowNotify) {
                    ControlNotification.notify(context, dns1, dns2);
                }

                String currentMethod = mPreferences.getString(KEY_PREF_METHOD, METHOD_VPN);


                switch (currentMethod){
                    case METHOD_VPN:
                    case METHOD_NDC: //Prevent net.dnsX was not changed
                        setCurrentDns(dns1, dns2);
                        break;
                    default:
                        setCurrentDns();
                }
                refreshCurrentDns();

            } else {
                //Not never show
                if (!dnsToast.equals(TOAST_NEVER)) {
                    showToastByCode(context, result_code);
                }

            }

        }

        private void showToastByCode(Context c, int code){
            showToastByCodeWithDns(c, code, "", "");
        }

        private void showToastByCodeWithDns(Context context, int code, String dns1, String dns2){
            String toastString;

            switch (code) {
                case 0:
                    toastString = context.getText(R.string.toast_set_succeed).toString();
                    toastString += !dns1.equals("") ? "\nDNS: " + dns1 : "";
                    toastString += !dns2.equals("") ? "\nDNS: " + dns2 : "";
                    break;
                case ValueConstants.RESTORE_SUCCEED:
                    toastString = context.getText(R.string.toast_restored).toString();
                    break;
                case ValueConstants.ERROR_NO_DNS:
                    toastString = context.getText(R.string.toast_no_dns_to_restore).toString();
                    break;
                case ERROR_GET_NETID_FAILED:
                    toastString = getString(R.string.toast_get_netid_failed);
                    break;
                case ERROR_GET_CURRENT_NETWORK_FAILED:
                    toastString = getString(R.string.toast_get_current_network_failed);
                    break;
                case ERROR_BAD_ADDRESS:
                    toastString = context.getString(R.string.toast_bad_address);
                    break;
                default:
                    toastString = context.getText(R.string.toast_set_failed).toString();
                    toastString += "\n" + context.getText(R.string.toast_unknown_error).toString();
            }

            Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show();
        }
    };

    BroadcastReceiver updateNetworkDnsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshNetworkDns();
        }
    };



    private void initVariable() {
        dnsmanCore = new DnsmanCore(this);
        DnsmanCore.initDnsMap(this);
        DnsmanCore.initResourcesMap();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        BackupNetworkDnsTask.startAction(this);
        mEditor.putBoolean(KEY_IS_ROOT, Shell.SU.available());
        mEditor.putBoolean(KEY_PREF_NOTIFICATION, false);
    }

    private void firstBoot(){
        showNoticeDialog();
        choiceMethod();


        Set<String> toSavedDNS = new HashSet<>(Arrays.asList(DEFAULT_DNS_LIST));
        mEditor.putStringSet(KEY_DNS_LIST, toSavedDNS);
        mEditor.apply();
    }

    private void showNoticeDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.notice_dialog_title))
                .setMessage(getText(R.string.notice_dialog_message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void choiceMethod(){
        boolean isRoot = Shell.SU.available();

        if(isRoot){
            switch (VERSION.SDK_INT){
                default:
                    mEditor.putString(KEY_PREF_METHOD, METHOD_NDC);
                    break;
            }
        }/*else{
            switch (VERSION.SDK_INT){
                case VERSION_CODES.KITKAT: //Escape 4.4 kitkat bug
                    mEditor.putString(KEY_PREF_METHOD, METHOD_ACCESSIBILITY);
                    break;
            }
        }*/

        mEditor.apply();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        initVariable();


        setTitle();
        if (mPreferences.getBoolean(KEY_FIRST_BOOT, true)) {
            firstBoot();
            mEditor.putBoolean(KEY_FIRST_BOOT, false);
            mEditor.apply();
        }


        setListView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DNS_CHANGE || resultCode == RESULT_OK){
            refreshList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentMode();
        updateListWhenIndividualChanged();
        refreshCurrentDns();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(updateNetworkDnsReceiver,
                new IntentFilter(ACTION_NETWORK_CONNECTED));
        broadcastManager.registerReceiver(resultCodeReceiver,
                new IntentFilter(ACTION_SET_DNS));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setTitle(){
        final PackageManager pm = getPackageManager();
        try{
            final PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            String label = "DNS man " + info.versionName;

            ActionBar actionBar = getActionBar();
            if(actionBar != null){
                actionBar.setTitle(label);
            }
        }catch (PackageManager.NameNotFoundException e){
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_set_with_last_dns:
                ExecuteIntentService.setWithLastDns(this);
                break;
            case R.id.action_restore:
                ExecuteIntentService.restore(this);
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    //List part START

    private boolean lastStatus = false;
    private void updateListWhenIndividualChanged(){
        boolean isEnabled = mPreferences.getBoolean(KEY_PREF_INDIVIDUAL_MODE, false);
        if(isEnabled != lastStatus){
            refreshList();
            lastStatus = isEnabled;
        }
    }

    private void refreshList(){
        mDnsEntryList.clear();
        mDnsEntryList.addAll(buildList());
        adapter.notifyDataSetChanged();
    }

    private List<Map<String, String>> buildList(){

        List<Map<String, String>> dnsEntryList = new ArrayList<>();

        dnsEntryList.add(getGlobalDnsEntry());

        if(mPreferences.getBoolean(KEY_PREF_INDIVIDUAL_MODE, false)) {
            for (NetworkInfo info : DnsmanCore.supportedNetInfoList) {
                dnsEntryList.add(getNetworkDnsEntry(info));
            }
        }

        return dnsEntryList;
    }

    private Map<String, String> getNetworkDnsEntry(NetworkInfo info){
        return getDnsEntry(info.getTypeName(), DnsmanCore.info2resMap.get(info.getTypeName()));
    }

    private Map<String, String> getGlobalDnsEntry(){
        return getDnsEntry("g", R.string.category_global);
    }

    private Map<String, String> getDnsEntry(String prefix, int resource){
        Map<String, String> dnsEntry = new HashMap<>();
        dnsEntry.put("prefix", prefix);
        dnsEntry.put("label", getText(resource).toString());

        String[] dnsData = dnsmanCore.getDnsByKeyPrefix(prefix);
        String dnsEntryString = "";
        boolean isNoDns = false;

        if(dnsData[0].isEmpty() && dnsData[1].isEmpty()){
            isNoDns = true;
        }
        if (!isNoDns) {
            if (!dnsData[0].isEmpty()) {
                dnsEntryString += dnsData[0] + ' ';
            }
            dnsEntryString += dnsData[1];
        }

        dnsEntry.put("dnsText", dnsEntryString);
        return dnsEntry;
    }

    private void setListView(){
        mDnsEntryList = buildList();
        adapter = new SimpleAdapter(this, mDnsEntryList,
                android.R.layout.simple_list_item_2,
                new String[] {"label", "dnsText"},
                new int[]{android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);

        initCurrentStatusView(this);
        refreshCurrentMode();
        refreshNetworkDns();

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> dnsEntry = (Map<String, String>) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), DnsEditActivity.class);
                i.putExtra("label", dnsEntry.get("label"));
                i.putExtra("prefix", dnsEntry.get("prefix"));
                startActivityForResult(i, ValueConstants.REQUEST_DNS_CHANGE);
            }
        });
        listView.addHeaderView(currentDnsLayout);
    }

    //List part END


    public void initCurrentStatusView(Context context) {
        currentDnsLayout = new LinearLayout(context);
        LinearLayout.inflate(context, R.layout.current_status_view, currentDnsLayout);
        currentDnsLayout.setOnClickListener(null);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        currentMethod = (TextView) currentDnsLayout.findViewById(R.id.current_mode_text);
        currentDnsText1 = (TextView) currentDnsLayout.findViewById(R.id.currentDnsText1);
        currentDnsText2 = (TextView) currentDnsLayout.findViewById(R.id.currentDnsText2);
        networkDnsText1 = (TextView) currentDnsLayout.findViewById(R.id.networkDnsText1);
        networkDnsText2 = (TextView) currentDnsLayout.findViewById(R.id.networkDnsText2);
        alertText = (TextView) currentDnsLayout.findViewById(R.id.alertText);
    }

    private void setFirewallRulesAlert(boolean isEnable){
        if(isEnable) {
            alertText.setText(R.string.text_firewall_override);
        }else{
            alertText.setText("");
        }
    }

    public void refreshCurrentMode(){
        String method = mPreferences.getString(KEY_PREF_METHOD, METHOD_VPN);
        currentMethod.setText(DnsmanCore.method2resMap.get(method));
    }

    public void refreshNetworkDns() {
        String dns1 = mPreferences.getString(KEY_NETWORK_DNS1, "");
        String dns2 = mPreferences.getString(KEY_NETWORK_DNS2, "");
        networkDnsText1.setText(dns1);
        networkDnsText2.setText(dns2);
    }

    private void refreshCurrentDns(){
        if (!showDns1.equals("") || !showDns2.equals("")) {
            currentDnsText1.setText(showDns1);
            currentDnsText2.setText(showDns2);
        } else {
            new getDNSTask().execute(this);
        }
    }

    public void setCurrentDns(){
        setCurrentDns("", "");
    }

    public void setCurrentDns(String dns1, String dns2){
        showDns1 = dns1;
        showDns2 = dns2;
    }

    private class getDNSTask extends AsyncTask<Context, Void, List<String>> {
        boolean haveRules = false;
        protected List<String> doInBackground(Context... contexts) {
            List<String> currentDNSData = new ArrayList<>();


            mPreferences = PreferenceManager.getDefaultSharedPreferences(contexts[0]);

            //Check firewall rules
            String currentMethod = mPreferences.getString(KEY_PREF_METHOD, METHOD_VPN);
            if(mPreferences.getBoolean(KEY_IS_ROOT, false)) {
                String dns = mPreferences.getString(KEY_HIJACKED_LAST_DNS, "");
                if (!dns.equals("") && NativeCommandUtils.isRulesAlivable(dns)) {
                    haveRules = true;
                    currentDNSData.add(dns);
                    currentDNSData.add("");//Fill
                    return currentDNSData;
                }
            }

            //Check system properties
            List<String> prop_dns = NativeCommandUtils.getCurrentPropDNS();
            if (!prop_dns.isEmpty()) {
                currentDNSData.addAll(prop_dns);
            }
            for (int i = 0; i != currentDNSData.size(); i++) {
                Log.d("MainActivity", "data = " + currentDNSData.get(i));
            }
            return currentDNSData;
        }

        protected void onPostExecute(List<String> data) {
            currentDnsText1.setText(data.get(0));
            currentDnsText2.setText(data.get(1));
            setFirewallRulesAlert(haveRules);
        }
    }

}



