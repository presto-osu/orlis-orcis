package org.itishka.pointim.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import org.itishka.pointim.R;
import org.itishka.pointim.fragments.SettingsFragment;


public class SettingsActivity extends ThemedActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
