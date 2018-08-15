package com.thefonz.ed_tool.preferences;

/**
 * Created by thefonz on 04/04/15.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

import com.thefonz.ed_tool.R;

public class AppPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean immersiveMode = SP.getBoolean("immersiveMode",false);
        String selectTheme = SP.getString("selectTheme", "1");

        assert selectTheme != null;
        if (selectTheme.equalsIgnoreCase("1")) {
            setTheme(R.style.AppThemeDark);
        }
        else
        {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        final View decorView = getWindow().getDecorView();

        if (immersiveMode) {
            // Set immersive mode
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            // Register UI change listener to re-set immersive mode if refocused
            decorView.setOnSystemUiVisibilityChangeListener
                    (new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            // Note that system bars will only be "visible" if none of the
                            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                // The system bars are visible. Make any desired changes
                                decorView.setSystemUiVisibility(
                                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                                // adjustments to your UI, such as showing the action bar or
                                // other navigational controls.
                            } else {
                                // The system bars are NOT visible. Make any desired changes
                                // adjustments to your UI, such as hiding the action bar or
                                // other navigational controls.
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Leave blank if you do not want anything to happen
    }

}
