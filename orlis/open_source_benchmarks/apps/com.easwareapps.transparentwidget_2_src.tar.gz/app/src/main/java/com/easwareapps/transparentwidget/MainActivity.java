package com.easwareapps.transparentwidget;

import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    AppWidgetManager appWidgetManager;

    ComponentName mAdminName;
    TextView disableAdmin;

    final static int DISABLE_ADMIN = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        disableAdmin = (TextView) findViewById(R.id.disable_admin);
        mAdminName = new ComponentName(this, AdminManageReceiver.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null)
            toolbar.setLogo(R.mipmap.ic_launcher);


        Context context = getApplicationContext();
        appWidgetManager = AppWidgetManager.getInstance(context);

        SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        Switch toggle = (Switch) findViewById(R.id.toggle_switch);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleWidget(!isChecked);
            }
        });

        toggle.setChecked(!pref.getBoolean("show_all", false));







    }



    private void toggleWidget(boolean state){

        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        SharedPreferences pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        pref.edit().putBoolean("show_all", state).apply();



        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, EmptyWidgetAppProvider.class));
        if (appWidgetIds.length > 0) {
            new EmptyWidgetAppProvider().onUpdate(context, appWidgetManager, appWidgetIds);
        }
        //initButton();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void redirectToMarket(View v){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.
                parse("https://play.google.com/store/apps/details?" +
                        "id=com.easwareapps.transparentwidget"));
        startActivity(intent);
    }

    public void shareApp(View v){
        String msg = getResources().getString(R.string.msg);
        msg += "\n\nhttps://play.google.com/store/apps/details?" +
                "id=com.easwareapps.transparentwidget";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.share_via)));
    }

    public void showOtherApps(View v){



    }

    public void uninstallApp(View v){
        ComponentName devAdminReceiver = new ComponentName(this, AdminManageReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.removeActiveAdmin(devAdminReceiver);

        Uri packageUri = Uri.parse("package:com.easwareapps.transparentwidget");
        Intent uninstallIntent =
                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        startActivityForResult(uninstallIntent, 999);
        //disableAdmin.setEnabled(false);

    }


}

