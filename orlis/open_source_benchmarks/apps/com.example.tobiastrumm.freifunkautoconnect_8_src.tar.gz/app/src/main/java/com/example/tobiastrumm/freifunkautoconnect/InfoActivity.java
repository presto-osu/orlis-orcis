package com.example.tobiastrumm.freifunkautoconnect;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.TextView;


public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set version name.
        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "Error";
        }
        tv_version.setText(versionName);

        // Activate urls
        TextView tv_freifunk_logo = (TextView) findViewById(R.id.tv_freifunk_logo);
        tv_freifunk_logo.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tv_sourcecode = (TextView) findViewById(R.id.tv_sourcecode);
        tv_sourcecode.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }
}
