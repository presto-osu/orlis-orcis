/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preferences {
    public final static String API_KEY = "api_key";
    public final static String PORT_NUMBER = "port";
    public final static String USE_HTTPS = "use_https";
    public final static String TRUST_ALL_CERTIFICATES = "trust_all_certificates";
    public final static String EXTENSION_PATH = "extension_path";
    public final static String ADDRESS = "address";
    public final static String V1_SICKBEARD_URL = "sickbeard_url";
    public final static String ACKNOWLEDGED_SHOW_ADDING_HELP = "acknowledged_show_adding_help";
    public final static String HTTP_USERNAME = "http_username";
    public final static String HTTP_PASSWORD = "http_password";
    public final static String GROUP_INACTIVE_SHOWS = "group_inactive_shows";
    public final static String SHOW_BANNERS = "show_banners_in_show_list";
    public final static String PROFILE_NAME = "profile_name";
    public final static String DEFAULT_PROFILE_NAME = "Default";
    public final static String EMPHASIZE_SHOW_NAME = "emphasize_show_name";

    private Context mContext;

    public Preferences(Context context) {
        mContext = context;
        update(); // Migrate old user's preferences
    }

    public SharedPreferences getSharedPreferences() {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String profileName = defaultPreferences.getString(Preferences.PROFILE_NAME, Preferences.DEFAULT_PROFILE_NAME);
        if (profileName.equalsIgnoreCase(Preferences.DEFAULT_PROFILE_NAME)) {
            return defaultPreferences;
        }
        else {
            return mContext.getSharedPreferences(profileName, Context.MODE_PRIVATE);
        }
    }

    private void update() {
        // Ensure any users from version 1.0 have their preferences updated.
        String url = getV1Url();
        if (url != null && url.trim().length() > 0) {
            Pattern pattern = Pattern.compile("(http(s?)://)?([^:/]+)(:[0-9]+)?(/.*)?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                String prefix = matcher.group(1);
                String address = matcher.group(3);
                String port = matcher.group(4);
                String path = matcher.group(5);
                boolean useHTTPS = prefix != null && prefix.toLowerCase().contains("https");

                if (port != null) {
                    port = port.replace(":", "");
                }

                putBoolean(Preferences.USE_HTTPS, useHTTPS);
                putString(Preferences.ADDRESS, address);
                putString(Preferences.PORT_NUMBER, port);
                putString(Preferences.EXTENSION_PATH, path);
                putBoolean(Preferences.TRUST_ALL_CERTIFICATES, true);
            }

            putString(Preferences.V1_SICKBEARD_URL, null);
        }

        // Set trust all certificates to true if migrating to v1.2
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!preferences.contains(Preferences.TRUST_ALL_CERTIFICATES)) {
            putBoolean(Preferences.TRUST_ALL_CERTIFICATES, true);
        }

        // Set status grouping to true if migrating to v1.3
        if (!preferences.contains(Preferences.GROUP_INACTIVE_SHOWS)) {
            putBoolean(Preferences.GROUP_INACTIVE_SHOWS, true);
        }

        // Set show name emphasis to be true by default
        if (!preferences.contains(Preferences.EMPHASIZE_SHOW_NAME)) {
            putBoolean(Preferences.EMPHASIZE_SHOW_NAME, true);
        }
    }

    public String getAddress() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getString(ADDRESS, "");
    }

    public String getApiKey() {
        SharedPreferences preferences = getSharedPreferences();
        String key = preferences.getString(API_KEY, null);
        return key != null ? key.trim() : null;
    }

    public boolean getGroupInactiveShows() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(Preferences.GROUP_INACTIVE_SHOWS, true);
    }

    public String getHttpUsername() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getString(HTTP_USERNAME, "");
    }

    public String getHttpPassword() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getString(HTTP_PASSWORD, "");
    }

    public int getPort() {
        SharedPreferences preferences = getSharedPreferences();
        String port = preferences.getString(PORT_NUMBER, "");
        return Integer.valueOf(port);
    }

    public boolean getHttpsEnabled() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(Preferences.USE_HTTPS, false);
    }

    public boolean getShowBannersInShowList() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(Preferences.SHOW_BANNERS, false);
    }

    public String getSickbeardUrl() {
        SharedPreferences preferences = getSharedPreferences();
        String address = preferences.getString(Preferences.ADDRESS, "");
        String port = preferences.getString(Preferences.PORT_NUMBER, null);
        String path = preferences.getString(Preferences.EXTENSION_PATH, "/");
        boolean useHTTPS = preferences.getBoolean(Preferences.USE_HTTPS, false);

        if (address == null || address.trim().equals("")) {
            return null;
        }
        else {
            String url = !address.toLowerCase().startsWith("https://") && !address.toLowerCase().startsWith("http://") ?  (useHTTPS ? "https://" : "http://") : "";
            url += address.trim();

            // Remove trailing slashes to avoid URLs such as 127.0.0.1/:8081/api/ once fully built
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            if (port != null) {
                url += ":" + port.trim();
            }

            // Ensure the path starts with a forward slash if the user entered a folder name on its own.
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            url += path.trim().equals("") ? "/" : path;
            url = url.trim();

            if (!url.endsWith("/")) {
                url += "/";
            }

            return url;
        }
    }

    public boolean getTrustAllCertificatesFlag() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(Preferences.TRUST_ALL_CERTIFICATES, true);
    }

    public boolean getEmphasizeShowNameFlag() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(Preferences.EMPHASIZE_SHOW_NAME, true);
    }

    public String getV1Url() {
        SharedPreferences preferences = getSharedPreferences();
        String url = preferences.getString(V1_SICKBEARD_URL, null);

        if (url != null && !url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            url = "http://" + url;
        }

        if (url != null && !url.endsWith("/")) {
            url += "/";
        }

        return url != null ? url.trim() : null;
    }

    public boolean hasAcknowledgedShowAddingHelp() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(ACKNOWLEDGED_SHOW_ADDING_HELP, false);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void putInt(String key, int value) {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void putString(String key, String value) {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getSelectedProfileName() {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return defaultPreferences.getString(Preferences.PROFILE_NAME, Preferences.DEFAULT_PROFILE_NAME);
    }

    public Set<String> getProfileSet() {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return defaultPreferences.getStringSet("profiles", new HashSet<String>());
    }

    public void selectProfile(String name) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putString(Preferences.PROFILE_NAME, name);
        editor.commit();
    }

    public void deleteProfile(String name) {
        Set<String> profiles = getProfileSet();
        profiles.remove(name);
        updateProfileSet(profiles);
        if (getSelectedProfileName().equals(name)) {
            selectProfile(Preferences.DEFAULT_PROFILE_NAME);
        }
    }

    public void updateProfileSet(Set<String> names) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putStringSet("profiles", names);
        editor.commit();
    }
}