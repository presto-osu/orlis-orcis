package org.amoradi.syncopoli;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public final static String KEY_SERVER_ADDRESS = "pref_key_server_address";
    public final static String KEY_PROTOCOL = "pref_key_protocol";
    public final static String KEY_RSYNC_USERNAME = "pref_key_username";
    public final static String KEY_RSYNC_OPTIONS = "pref_key_options";
    public final static String KEY_PRIVATE_KEY = "pref_key_private_key";
    public final static String KEY_PORT = "pref_key_port";
    public final static String KEY_FREQUENCY = "pref_key_frequency";
    public final static String KEY_RSYNC_PASSWORD = "pref_key_rsync_password";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        String summary = sharedPreferences.getString(key, "Not set");
        pref.setSummary(summary);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        initializeSummaries();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.menu_settings).setVisible(false);
        menu.findItem(R.id.action_done).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_run).setVisible(false);
    }

    private void initializeSummaries() {
        String[] keys = {KEY_SERVER_ADDRESS, KEY_PROTOCOL, KEY_RSYNC_USERNAME, KEY_RSYNC_OPTIONS,
                         KEY_PRIVATE_KEY, KEY_PORT, KEY_FREQUENCY, KEY_RSYNC_PASSWORD};
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();

        for (String key : keys) {
            onSharedPreferenceChanged(sp, key);
        }
    }
}
