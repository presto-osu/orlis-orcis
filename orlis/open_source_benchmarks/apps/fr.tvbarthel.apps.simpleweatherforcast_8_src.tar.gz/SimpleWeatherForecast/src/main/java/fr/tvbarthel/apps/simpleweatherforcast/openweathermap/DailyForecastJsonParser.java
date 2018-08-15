package fr.tvbarthel.apps.simpleweatherforcast.openweathermap;


import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DailyForecastJsonParser extends AsyncTask<String, Void, ArrayList<DailyForecastModel>> {

    private static final String TAG_WEATHER_LIST = "list";
    private static final String TAG_HUMIDITY = "humidity";
    private static final String TAG_WEATHER = "weather";
    private static final String TAG_WEATHER_DESCRIPTION = "description";
    private static final String TAG_TEMPERATURE = "temp";
    private static final String TAG_TEMPERATURE_DAY = "day";
    private static final String TAG_TEMPERATURE_MIN = "min";
    private static final String TAG_TEMPERATURE_MAX = "max";
    private static final String TAG_DATE_TIME = "dt";

    @Override
    protected ArrayList<DailyForecastModel> doInBackground(String... params) {
        return parse(params[0]);
    }

    public static ArrayList<DailyForecastModel> parse(final String json) {
        final ArrayList<DailyForecastModel> result = new ArrayList<DailyForecastModel>();
        if (json != null) {
            try {
                JSONObject root = new JSONObject(json);
                JSONArray weatherList = root.getJSONArray(TAG_WEATHER_LIST);
                for (int i = 0; i < weatherList.length(); i++) {
                    result.add(parseDailyForecast(weatherList.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static DailyForecastModel parseDailyForecast(JSONObject jsonDailyForecast) {
        final DailyForecastModel model = new DailyForecastModel();
        model.setDateTime(parseDateTime(jsonDailyForecast));
        model.setHumidity(parseHumidity(jsonDailyForecast));
        model.setDescription(parseWeatherDescription(jsonDailyForecast));
        parseTemperature(model, jsonDailyForecast);
        return model;
    }

    private static void parseTemperature(DailyForecastModel model, JSONObject dailyForecast) {
        try {
            JSONObject jsonTemperature = dailyForecast.getJSONObject(TAG_TEMPERATURE);
            model.setTemperature(parseDayTemperature(jsonTemperature));
            model.setMinTemperature(parseMinTemperature(jsonTemperature));
            model.setMaxTemperature(parseMaxTemperature(jsonTemperature));
        } catch (JSONException exception) {
            exception.printStackTrace();
            model.setTemperature(0d);
            model.setMinTemperature(0d);
            model.setMaxTemperature(0d);
        }
    }

    private static Double parseDayTemperature(JSONObject temperature) {
        return parseDouble(temperature, TAG_TEMPERATURE_DAY, 0d);
    }

    private static Double parseMinTemperature(JSONObject temperature) {
        return parseDouble(temperature, TAG_TEMPERATURE_MIN, 0d);
    }

    private static Double parseMaxTemperature(JSONObject temperature) {
        return parseDouble(temperature, TAG_TEMPERATURE_MAX, 0d);
    }


    private static Double parseDouble(JSONObject object, String tag, Double defaultValue) {
        try {
            return object.getDouble(tag);
        } catch (JSONException exception) {
            exception.printStackTrace();
            return defaultValue;
        }
    }

    private static String parseWeatherDescription(JSONObject dailyForecast) {
        String weatherDescription = "";
        try {
            weatherDescription = dailyForecast.getJSONArray(TAG_WEATHER).getJSONObject(0).getString(TAG_WEATHER_DESCRIPTION);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return weatherDescription;
    }

    private static int parseHumidity(JSONObject dailyForecast) {
        int humidity = 0;
        try {
            humidity = dailyForecast.getInt(TAG_HUMIDITY);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return humidity;
    }

    private static long parseDateTime(JSONObject dailyForecast) {
        long dateTime = 0;
        try {
            dateTime = dailyForecast.getLong(TAG_DATE_TIME);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return dateTime;
    }

}
