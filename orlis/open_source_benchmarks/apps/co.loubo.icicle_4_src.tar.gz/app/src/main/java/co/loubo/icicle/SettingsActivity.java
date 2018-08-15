package co.loubo.icicle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;


public class SettingsActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setResult(Activity.RESULT_OK);
    }

    @Override
    protected void onStart() {
        ((GlobalState)getApplication()).registerActivity(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        ((GlobalState)getApplication()).unregisterActivity(this);
        super.onStop();
    }
}