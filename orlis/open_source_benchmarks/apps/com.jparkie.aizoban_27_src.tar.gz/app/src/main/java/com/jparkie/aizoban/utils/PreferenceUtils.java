package com.jparkie.aizoban.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;

public class PreferenceUtils {
    private PreferenceUtils() {
        throw new AssertionError();
    }

    public static void initializePreferences() {
        Context context = AizobanApplication.getInstance();

        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getString(context.getString(R.string.preference_download_storage_key), null) == null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(context.getString(R.string.preference_download_storage_key), context.getFilesDir().getAbsolutePath());
            editor.commit();
        }
    }

    public static int getStartupScreen() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Hack Fix: http://stackoverflow.com/questions/5227478/getting-integer-or-index-values-from-a-list-preference
        return Integer.valueOf(sharedPreferences.getString(context.getString(R.string.preference_startup_key), context.getString(R.string.preference_startup_default_value)));
    }

    public static String getSource() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.preference_source_key), context.getString(R.string.preference_source_default_value));
    }

    public static String getViewType() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.preference_view_type_key), context.getString(R.string.preference_view_type_default_value));
    }


    public static boolean isLazyLoading() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_lazy_loading_key), true);
    }

    public static boolean isRightToLeftDirection() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_direction_key), false);
    }

    public static void setDirection(boolean isRightToLeftDirection) {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_direction_key), isRightToLeftDirection);
        editor.commit();
    }

    public static boolean isLockOrientation() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_orientation_key), false);
    }

    public static void setOrientation(boolean isLockOrientation) {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_orientation_key), isLockOrientation);
        editor.commit();
    }

    public static boolean isLockZoom() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_zoom_key), false);
    }

    public static void setZoom(boolean isLockZoom) {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_zoom_key), isLockZoom);
        editor.commit();
    }

    public static boolean isWiFiOnly() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_download_wifi_key), true);
    }

    public static boolean isExternalStorage() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String preferenceDirectory = sharedPreferences.getString(context.getString(R.string.preference_download_storage_key), null);
        String internalDirectory = context.getFilesDir().getAbsolutePath();

        return !preferenceDirectory.equals(internalDirectory);
    }

    public static String getDownloadDirectory() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.preference_download_storage_key), context.getFilesDir().getAbsolutePath());
    }
}
