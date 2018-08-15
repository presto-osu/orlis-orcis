package org.pulpdust.lesserpad;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class LesserPadPrefs extends PreferenceActivity {
	int look_style;
	@Override
	public void onCreate(Bundle savedInstanceState){
        readPrefs();
       if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 13){
    	} else if (look_style > 0 || (Build.VERSION.SDK_INT <= 10 && Build.VERSION.SDK_INT >= 6)){ 
    		setTheme(R.style.AppTheme_Prefs_Dark);
    	} else {
    		setTheme(R.style.AppTheme_Prefs);
    	}
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
	
    public void readPrefs(){
    	SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(this);
    	look_style = Integer.parseInt(sprefs.getString("look_style", "0"));
    }


}
