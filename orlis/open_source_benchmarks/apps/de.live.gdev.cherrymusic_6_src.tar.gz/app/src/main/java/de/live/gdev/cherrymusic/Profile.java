package de.live.gdev.cherrymusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Profile {
    /*
     *   Statics
     */
    public static class PREFERENCES {
        public static final String SELPROFILE = "selProfile";
        public static final String AUTOLOGIN = "autologin";
        public static final String SSLACCEPT = "sslAccept";
        public static final String FILENAME = "filename";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String PATH = "path";
    }

    public static Profile getDefaultProfile(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        int nr = pref.getInt(PREFERENCES.SELPROFILE, 0);
        return new Profile(context, nr);
    }

    public static void setDefaultProfile(Context context, Profile profile) {
        setDefaultProfile(context, profile.profileId);
    }

    public static void setDefaultProfile(Context context, int profileId) {
        context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit()
                .putInt(PREFERENCES.SELPROFILE, profileId)
                .commit();
    }


    /*
     *  Profile
     */
    private SharedPreferences preference;
    private Context context;

    private String path;
    private String filename;
    private String username;
    private String password;
    private boolean autoLogin;
    private boolean acceptAllSsl;
    private int profileId;

    public Profile(Context context, int profileId) {
        this.context = context;
        this.profileId = profileId;
        preference = context.getSharedPreferences("Profile" + profileId, Context.MODE_PRIVATE);
        reloadSettings();
    }

    public void saveSettings() {
        preference.edit()
                .putString(PREFERENCES.PATH, getPath())
                .putString(PREFERENCES.FILENAME, getFilename())
                .putString(PREFERENCES.USERNAME, getUsername())
                .putString(PREFERENCES.PASSWORD, getPassword())
                .putBoolean(PREFERENCES.AUTOLOGIN, isAutoLogin())
                .putBoolean(PREFERENCES.SSLACCEPT, isAcceptAllSsl())
                .apply();
    }

    public void reloadSettings() {
        filename = preference.getString(PREFERENCES.FILENAME, context.getString(R.string.webapp_default_filename));
        path = preference.getString(PREFERENCES.PATH, "");
        username = preference.getString(PREFERENCES.USERNAME, "");
        password = preference.getString(PREFERENCES.PASSWORD, "");
        autoLogin = preference.getBoolean(PREFERENCES.AUTOLOGIN, false);
        acceptAllSsl = preference.getBoolean(PREFERENCES.SSLACCEPT, false);
    }


    public boolean areSettingsValid() {
        return !TextUtils.isEmpty(getFilename())
                && !TextUtils.isEmpty(getPath())
                && !TextUtils.isEmpty(getUsername())
                && !TextUtils.isEmpty(getPassword());
    }

    public String getPath() {
        if (!path.endsWith("/") && !path.isEmpty())
            return path += "/";
        return path;
    }

    public String getFullPath() {
        return getPath() + getFilename();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public boolean isAcceptAllSsl() {
        return acceptAllSsl;
    }

    public void setAcceptAllSsl(boolean acceptAllSsl) {
        this.acceptAllSsl = acceptAllSsl;
    }

    public int getId() {
        return profileId;
    }
}
