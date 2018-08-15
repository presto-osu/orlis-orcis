package gq.nulldev.animeopenings.app;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Project: AnimeOpenings
 * Created: 03/10/15
 * Author: nulldev
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Put the screen into landscape to force ads on smaller devices
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        addPreferencesFromResource(R.xml.pref_main);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            //We are returning to main activity, finish this one and start main activity.
            Intent goMain = new Intent(this, );
            goMain.putExtra("showAd", false);
            startActivity(goMain);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
