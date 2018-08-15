package fr.tvbarthel.apps.simpleweatherforcast.utils;

import android.content.Context;

import fr.tvbarthel.apps.simpleweatherforcast.R;

/**
 * A simple utility class to convert and get a human readable temperature.
 */
public final class TemperatureUtils {


    /**
     * Convert a temperature in Celsius to the requested unit.
     *
     * @param context              a {@link android.content.Context}
     * @param temperatureInCelsius the given temperature in Celsius
     * @param temperatureUnit      the requested unit
     * @return the temperature converted
     */
    public static long convertTemperature(Context context, double temperatureInCelsius, String temperatureUnit) {
        double temperatureConverted = temperatureInCelsius;
        if (temperatureUnit.equals(context.getString(R.string.temperature_unit_fahrenheit_symbol))) {
            temperatureConverted = temperatureInCelsius * 1.8f + 32f;
        } else if (temperatureUnit.equals(context.getString(R.string.temperature_unit_kelvin_symbol))) {
            temperatureConverted = temperatureInCelsius + 273.15f;
        }
        return Math.round(temperatureConverted);
    }

    // Non-instantiable class.
    private TemperatureUtils() {
    }
}
