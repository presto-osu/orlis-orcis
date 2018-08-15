package com.saladdressing.veterondo.utils;

import com.saladdressing.veterondo.R;

public class Constants {

    public static final String OWM_API_KEY = "0d931dce668ff170f3e22d3ff29ff35c";
    public static final String INTRO_PLAYED = "introPlayed";
    public static final String IS_WINDY = "isWindy";
    public static final String IS_RAINY = "isRainy";
    public static final String DOT_CHOSEN_INTRO = "dotChosenIntro";
    public static final String FROM_INTRO = "fromIntro";

    public static final String TEMP_UNIT = "tempUnit";


    public static final String ICON_TO_SHOW = "iconToShow";
    public static final String DESC_TO_SHOW = "descToShow";

    public static final int RAIN_ICON = R.drawable.rain_clim;
    public static final int CLOUD_ICON = R.drawable.cloud;
    public static final int CLOUD_SUN_ICON = R.drawable.cloud_sun;
    public static final int FOG_ICON = R.drawable.fog_clim;
    public static final int DRIZZLE_ICON = R.drawable.cloud_drizzle;
    public static final int CLOUD_LIGHTNING_ICON = R.drawable.cloud_lightning;
    public static final int CLOUD_MOON_ICON = R.drawable.cloud_moon;
    public static final int CLOUD_SNOW_ICON = R.drawable.cloud_snow;
    public static final int CLOUD_SNOW_ALT_ICON = R.drawable.cloud_snow_alt;
    public static final int MOON_ICON = R.drawable.moon_clim;
    public static final int SUN_ICON = R.drawable.sun_clim;
    public static final int TORNADO_ICON = R.drawable.tornado;
    public static final int WIND_ICON = R.drawable.wind_clim;
    public static final int COLD_ICON = R.drawable.thermometer_zero;
    public static final int HOT_ICON = R.drawable.thermometer_hundred;




    public static boolean isNight(long sunrise, long sunset) {


        long epochTime = System.currentTimeMillis() / 1000;

        if (epochTime > sunrise && epochTime < sunset) {
            return false;
        } else {
            return true;
        }


    }


    public static double kelvinToCelsius(double kelvinTemp){
        return kelvinTemp - 273.15;
    }


    public static double kelvinToFarhenheit(double kelvinTemp) {
        return kelvinTemp * 9/5 - 459.67;

    }


}
