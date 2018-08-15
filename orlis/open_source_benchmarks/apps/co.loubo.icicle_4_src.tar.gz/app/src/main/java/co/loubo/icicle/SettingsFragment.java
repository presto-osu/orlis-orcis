package co.loubo.icicle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;



public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener  {
	GlobalState gs;
	private ListPreference mListPreference;
	private CheckBoxPreference mWifiOnlyPref;
	private String PREF_KEY_REFRESH_RATE = "pref_key_refresh_rate";
	private String PREF_KEY_WIFI_ONLY = "pref_key_wifi_only";
    private String PREF_KEY_NODES = "pref_key_nodes";
    private String PREF_KEY_FRIEND_NODES = "pref_key_friend_nodes";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        mListPreference = (ListPreference)getPreferenceScreen().findPreference(PREF_KEY_REFRESH_RATE);
        mWifiOnlyPref = (CheckBoxPreference) getPreferenceScreen().findPreference(PREF_KEY_WIFI_ONLY);
        this.gs = (GlobalState) getActivity().getApplication();
        Preference button = findPreference(PREF_KEY_NODES);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) { 
                        	Intent intent = new Intent(getActivity(), NodeManagerActivity.class);
                        	startActivity(intent);
                            return true;
                        }
                    });
        Preference friendButton = findPreference(PREF_KEY_FRIEND_NODES);
        friendButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(getActivity(), FriendNodeManagerActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
    
    @Override
	public void onResume() {
        super.onResume();
        
        mListPreference.setValue(String.valueOf(this.gs.getRefresh_rate()));
        mWifiOnlyPref.setChecked(this.gs.isWifiOnly());
        // Setup the initial values
        mListPreference.setSummary("Current value is " + mListPreference.getEntry());

        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
	public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
    
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set new summary, when a preference value changes
        if (key.equals(PREF_KEY_REFRESH_RATE)) {
            mListPreference.setSummary("Current value is " + mListPreference.getEntry().toString());
            this.gs.setRefresh_rate(Integer.parseInt(mListPreference.getValue()));
        }
        if (key.equals(PREF_KEY_WIFI_ONLY)) {
            this.gs.setWifiOnly(mWifiOnlyPref.isChecked());
        }
    }
}