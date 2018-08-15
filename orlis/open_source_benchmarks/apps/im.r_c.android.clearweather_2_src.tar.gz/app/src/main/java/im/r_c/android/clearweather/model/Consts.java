package im.r_c.android.clearweather.model;

/**
 * ClearWeather
 * Created by richard on 16/5/2.
 */
public class Consts {
    public static final String DATABASE_FILE_NAME = "ClearWeather.db";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_TABLE_COUNTY = "County";
    public static final String DATABASE_TABLE_WEATHER_INFO = "WeatherInfo";

    public static final String API_COUNTY_LIST = "http://7xo46j.com1.z0.glb.clouddn.com/heweather-cn-city-list.json";
    public static final String API_WEATHER_INFO = "http://richardchien-clear-weather-api.daoapp.io/%s";

    public static final String EXTRA_COUNTY = "county";
    public static final String EXTRA_AUTO_UPDATE = "auto update";

    public static final String FORMAT_TEMPERATURE = "%s˚~%s˚";
    public static final String FORMAT_HUMIDITY = "%s%%";
    public static final String FORMAT_RAIN_PROBABILITY = "%s%%";
    public static final String FORMAT_VISIBILITY = "%skm";

    public static final String FORMAT_DATE_TIME = "yyyy.MM.dd HH:mm";
    public static final String CITY_LIST_FILENAME = "heweather-cn-city-list.json";
}
