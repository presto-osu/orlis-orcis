package fr.tvbarthel.apps.simpleweatherforcast.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import fr.tvbarthel.apps.simpleweatherforcast.R;

public class SharedPreferenceUtils {

    public static final long REFRESH_TIME_AUTO = 1000 * 60 * 60 * 2; // 2 hours in millis.
    public static final long REFRESH_TIME_MANUAL = 1000 * 60 * 10; // 10 minutes.

    public static String KEY_LAST_UPDATE = "SharedPreferenceUtils.Key.LastUpdate";
    public static String KEY_LAST_KNOWN_JSON_WEATHER = "SharedPreferenceUtils.Key.LastKnownJsonWeather";
    public static String KEY_TEMPERATURE_UNIT_SYMBOL = "SharedPreferenceUtils.Key.TemperatureUnitSymbol";

    private static SharedPreferences getDefaultSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getLastKnownWeather(final Context context) {
        return getDefaultSharedPreferences(context).getString(KEY_LAST_KNOWN_JSON_WEATHER, null);
    }

    public static long getLastUpdate(final Context context) {
        return getDefaultSharedPreferences(context).getLong(KEY_LAST_UPDATE, 0);
    }

    public static void storeWeather(final Context context, final String jsonWeather) {
        final Editor editor = getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_LAST_KNOWN_JSON_WEATHER, jsonWeather);
        editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
        editor.apply();
    }

    public static void storeTemperatureUnitSymbol(final Context context, final String unitSymbol) {
        final Editor editor = getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_TEMPERATURE_UNIT_SYMBOL, unitSymbol);
        editor.apply();
    }

    public static String getTemperatureUnitSymbol(final Context context) {
        return getDefaultSharedPreferences(context).getString(KEY_TEMPERATURE_UNIT_SYMBOL,
                context.getString(R.string.temperature_unit_celsius_symbol));
    }

    public static void registerOnSharedPreferenceChangeListener(final Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean isWeatherOutdated(Context context, boolean isManualRefresh) {
        final long refreshTimeInMillis = isManualRefresh ? REFRESH_TIME_MANUAL : REFRESH_TIME_AUTO;
        final long lastUpdate = SharedPreferenceUtils.getLastUpdate(context);
        return System.currentTimeMillis() - lastUpdate > refreshTimeInMillis;
    }

}
