package im.r_c.android.clearweather.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import im.r_c.android.clearweather.model.Consts;
import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.model.WeatherInfo;

/**
 * ClearWeather
 * Created by richard on 16/5/4.
 */
public class WeatherInfoFetcher {
    private static final String TAG = "WeatherInfoFetcher";

    public static final String KEY_HEWEATHER = "HeWeather data service 3.0";
    public static final String KEY_NOW = "now";
    public static final String KEY_CONDITION = "cond";
    public static final String KEY_TXT = "txt";
    public static final String KEY_TXT_DAY = "txt_d";
    public static final String KEY_TXT_NIGHT = "txt_n";
    public static final String KEY_TEMPERATURE = "tmp";
    public static final String KEY_MAX = "max";
    public static final String KEY_MIN = "min";
    public static final String KEY_DAILY_FORECAST = "daily_forecast";
    public static final String KEY_HUMIDITY = "hum";
    public static final String KEY_RAIN_PROBABILITY = "pop";
    public static final String KEY_VISIBILITY = "vis";


    public static WeatherInfo fetch(County county) {
        // Will block the thread
        String jsonString = HttpUtils.getSync(String.format(Consts.API_WEATHER_INFO, county.getCode()));
        if (jsonString == null) {
            return null;
        }

        WeatherInfo info = new WeatherInfo();
        info.setCounty(county);
        info.setUpdateTimestamp(System.currentTimeMillis());
        info.setRawJSONString(jsonString);
        return parse(info);
    }

    public static WeatherInfo parse(WeatherInfo info) {
        if (info == null || info.getCounty() == null || info.getRawJSONString() == null) {
            return null;
        }

        try {
            JSONObject object = JSON.parseObject(info.getRawJSONString());
            JSONObject infoObj = object.getJSONArray(KEY_HEWEATHER).getJSONObject(0);

            JSONObject now = infoObj.getJSONObject(KEY_NOW);
            info.setCountyWeather(now.getJSONObject(KEY_CONDITION).getString(KEY_TXT));
            info.setNowTemperature(now.getIntValue(KEY_TEMPERATURE));

            JSONArray dailyForecast = infoObj.getJSONArray(KEY_DAILY_FORECAST);

            // These codes are too silly, I know that :(
            JSONObject today = dailyForecast.getJSONObject(0);
            info.setTodayDayWeather(today.getJSONObject(KEY_CONDITION).getString(KEY_TXT_DAY));
            info.setTodayNightWeather(today.getJSONObject(KEY_CONDITION).getString(KEY_TXT_NIGHT));
            info.setTodayMaxTemperature(today.getJSONObject(KEY_TEMPERATURE).getIntValue(KEY_MAX));
            info.setTodayMinTemperature(today.getJSONObject(KEY_TEMPERATURE).getIntValue(KEY_MIN));
            info.setTodayHumidity(today.getIntValue(KEY_HUMIDITY));
            info.setTodayRainProbability(today.getIntValue(KEY_RAIN_PROBABILITY));
            info.setTodayVisibility(today.getIntValue(KEY_VISIBILITY));

            JSONObject tomorrow = dailyForecast.getJSONObject(1);
            info.setTomorrowDayWeather(tomorrow.getJSONObject(KEY_CONDITION).getString(KEY_TXT_DAY));
            info.setTomorrowMaxTemperature(tomorrow.getJSONObject(KEY_TEMPERATURE).getIntValue(KEY_MAX));
            info.setTomorrowMinTemperature(tomorrow.getJSONObject(KEY_TEMPERATURE).getIntValue(KEY_MIN));

            JSONObject dayAfterTomorrow = dailyForecast.getJSONObject(2);
            info.setDayAfterTomorrowDayWeather(dayAfterTomorrow.getJSONObject(KEY_CONDITION).getString(KEY_TXT_DAY));
            info.setDayAfterTomorrowMaxTemperature(dayAfterTomorrow.getJSONObject(KEY_TEMPERATURE).getIntValue(KEY_MAX));
            info.setDayAfterTomorrowMinTemperature(dayAfterTomorrow.getJSONObject(KEY_TEMPERATURE).getIntValue(KEY_MIN));

            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
