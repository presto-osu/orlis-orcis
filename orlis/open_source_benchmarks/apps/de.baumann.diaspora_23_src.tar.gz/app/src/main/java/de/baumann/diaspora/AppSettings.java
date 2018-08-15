package de.baumann.diaspora;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by de-live-gdev on 20.03.16.
 */
public class AppSettings {
    private Context context;
    private SharedPreferences pref;

    public AppSettings(Context context){
        this.context = context.getApplicationContext();
        pref = this.context.getSharedPreferences("app", Context.MODE_PRIVATE);
    }

    private void setString(String key, String value){
        pref.edit().putString(key,value).apply();
    }

    /*
    //     Setters & Getters
    */
    private static final String PREF_PROFILE_ID = "profileID";
    public String getProfileId(){
        return pref.getString(PREF_PROFILE_ID, "");
    }
    public void setProfileId(String profileId){
        setString(PREF_PROFILE_ID, profileId);
    }
}
