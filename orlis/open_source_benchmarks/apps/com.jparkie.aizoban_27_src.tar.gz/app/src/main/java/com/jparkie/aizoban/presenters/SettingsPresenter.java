package com.jparkie.aizoban.presenters;

import android.preference.Preference;

public interface SettingsPresenter {
    public void initializeDownloadDirectory();

    public void initializeViews();

    public boolean onPreferenceClick(Preference preference);
}
