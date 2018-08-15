package io.github.otakuchiyan.dnsman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.VpnService;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class VpnWrapperActivity extends Activity implements ValueConstants{
    private String dns1, dns2;

    public static void perform(Context c, String dns1, String dns2){
        Intent i = new Intent(c, VpnWrapperActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(EXTRA_DNS1, dns1);
        i.putExtra(EXTRA_DNS2, dns2);
        c.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent dnsData = getIntent();
        dns1 = dnsData.getStringExtra(ValueConstants.EXTRA_DNS1);
        dns2 = dnsData.getStringExtra(ValueConstants.EXTRA_DNS2);

        Intent i = VpnService.prepare(this);
        if (i != null) {
            startActivityForResult(i, ValueConstants.REQUEST_VPN);
        } else {
            launchServiceWithTimeDelay(this, dns1, dns2);
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        if(reqCode == ValueConstants.REQUEST_VPN || resCode == RESULT_OK){
            launchServiceWithTimeDelay(this, dns1, dns2);
        }
    }

    private void launchServiceWithTimeDelay(final Context context, final String dns1, final String dns2){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final long delay = Long.valueOf(preferences.getString(KEY_VPN_DELAY, "0"));

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay * 1000); //ms to s
                    DnsVpnService.perform(context, dns1, dns2);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        finish();
    }
}
