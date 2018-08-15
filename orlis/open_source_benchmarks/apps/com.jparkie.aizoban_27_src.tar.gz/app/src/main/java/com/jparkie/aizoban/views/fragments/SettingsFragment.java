package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.SettingsPresenter;
import com.jparkie.aizoban.presenters.SettingsPresenterImpl;
import com.jparkie.aizoban.presenters.mapper.SettingsMapper;
import com.jparkie.aizoban.views.SettingsView;

public class SettingsFragment extends PreferenceFragment implements SettingsView, SettingsMapper, Preference.OnPreferenceClickListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    private SettingsPresenter mSettingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettingsPresenter = new SettingsPresenterImpl(this, this);

        addPreferencesFromResource(R.xml.preferences);

        findPreference(getString(R.string.preference_view_google_play_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_view_disclaimer_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_latest_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_favourite_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_recent_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_image_cache_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_view_open_source_licenses_key)).setOnPreferenceClickListener(this);

        mSettingsPresenter.initializeDownloadDirectory();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSettingsPresenter.initializeViews();
    }

    // SettingsView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_settings);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void toastClearedLatest() {
        Toast.makeText(getActivity(), R.string.toast_cleared_latest, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastClearedFavourite() {
        Toast.makeText(getActivity(), R.string.toast_cleared_favourite, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastClearedRecent() {
        Toast.makeText(getActivity(), R.string.toast_cleared_recent, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastClearedImageCache() {
        Toast.makeText(getActivity(), R.string.toast_cleared_image_cache, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastExternalStorageError() {
        Toast.makeText(getActivity(), R.string.toast_external_storage_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // SettingsMapper:

    @Override
    public ListPreference getDownloadStoragePreference() {
        return (ListPreference)findPreference(getString(R.string.preference_download_storage_key));
    }

    // Preference.OnPreferenceClickListener:

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return mSettingsPresenter.onPreferenceClick(preference);
    }
}