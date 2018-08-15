package de.pinyto.exalteddicer;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class SetPreferenceActivity extends ActionBarActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.action_settings);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

    }

}
