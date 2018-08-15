package im.r_c.android.clearweather.model;

/**
 * ClearWeather
 * Created by richard on 16/5/3.
 */
public class WeatherInfo {
    public static final String KEY_COUNTY_CODE = "county_code";
    public static final String KEY_UPDATED = "updated";
    public static final String KEY_JSON = "json";

    private County county;
    private String countyWeather;
    private int nowTemperature;
    private String todayDayWeather;
    private String todayNightWeather;
    private int todayMinTemperature;
    private int todayMaxTemperature;
    private int todayHumidity;
    private int todayRainProbability;
    private int todayVisibility;
    private String tomorrowDayWeather;
    private int tomorrowMaxTemperature;
    private int tomorrowMinTemperature;
    private String dayAfterTomorrowDayWeather;
    private int dayAfterTomorrowMaxTemperature;
    private int dayAfterTomorrowMinTemperature;
    private long updateTimestamp;
    private String rawJSONString;

    public WeatherInfo() {
    }

    public County getCounty() {
        return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }

    public String getCountyWeather() {
        return countyWeather;
    }

    public void setCountyWeather(String countyWeather) {
        this.countyWeather = countyWeather;
    }

    public int getNowTemperature() {
        return nowTemperature;
    }

    public void setNowTemperature(int nowTemperature) {
        this.nowTemperature = nowTemperature;
    }

    public String getTodayDayWeather() {
        return todayDayWeather;
    }

    public void setTodayDayWeather(String todayDayWeather) {
        this.todayDayWeather = todayDayWeather;
    }

    public String getTodayNightWeather() {
        return todayNightWeather;
    }

    public void setTodayNightWeather(String todayNightWeather) {
        this.todayNightWeather = todayNightWeather;
    }

    public int getTodayMinTemperature() {
        return todayMinTemperature;
    }

    public void setTodayMinTemperature(int todayMinTemperature) {
        this.todayMinTemperature = todayMinTemperature;
    }

    public int getTodayMaxTemperature() {
        return todayMaxTemperature;
    }

    public void setTodayMaxTemperature(int todayMaxTemperature) {
        this.todayMaxTemperature = todayMaxTemperature;
    }

    public int getTodayHumidity() {
        return todayHumidity;
    }

    public void setTodayHumidity(int todayHumidity) {
        this.todayHumidity = todayHumidity;
    }

    public int getTodayRainProbability() {
        return todayRainProbability;
    }

    public void setTodayRainProbability(int todayRainProbability) {
        this.todayRainProbability = todayRainProbability;
    }

    public int getTodayVisibility() {
        return todayVisibility;
    }

    public void setTodayVisibility(int todayVisibility) {
        this.todayVisibility = todayVisibility;
    }

    public String getTomorrowDayWeather() {
        return tomorrowDayWeather;
    }

    public void setTomorrowDayWeather(String tomorrowDayWeather) {
        this.tomorrowDayWeather = tomorrowDayWeather;
    }

    public int getTomorrowMaxTemperature() {
        return tomorrowMaxTemperature;
    }

    public void setTomorrowMaxTemperature(int tomorrowMaxTemperature) {
        this.tomorrowMaxTemperature = tomorrowMaxTemperature;
    }

    public int getTomorrowMinTemperature() {
        return tomorrowMinTemperature;
    }

    public void setTomorrowMinTemperature(int tomorrowMinTemperature) {
        this.tomorrowMinTemperature = tomorrowMinTemperature;
    }

    public String getDayAfterTomorrowDayWeather() {
        return dayAfterTomorrowDayWeather;
    }

    public void setDayAfterTomorrowDayWeather(String dayAfterTomorrowDayWeather) {
        this.dayAfterTomorrowDayWeather = dayAfterTomorrowDayWeather;
    }

    public int getDayAfterTomorrowMaxTemperature() {
        return dayAfterTomorrowMaxTemperature;
    }

    public void setDayAfterTomorrowMaxTemperature(int dayAfterTomorrowMaxTemperature) {
        this.dayAfterTomorrowMaxTemperature = dayAfterTomorrowMaxTemperature;
    }

    public int getDayAfterTomorrowMinTemperature() {
        return dayAfterTomorrowMinTemperature;
    }

    public void setDayAfterTomorrowMinTemperature(int dayAfterTomorrowMinTemperature) {
        this.dayAfterTomorrowMinTemperature = dayAfterTomorrowMinTemperature;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getRawJSONString() {
        return rawJSONString;
    }

    public void setRawJSONString(String rawJSONString) {
        this.rawJSONString = rawJSONString;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "county=" + county +
                ", countyWeather='" + countyWeather + '\'' +
                ", nowTemperature=" + nowTemperature +
                ", todayDayWeather='" + todayDayWeather + '\'' +
                ", todayNightWeather='" + todayNightWeather + '\'' +
                ", todayMinTemperature=" + todayMinTemperature +
                ", todayMaxTemperature=" + todayMaxTemperature +
                ", todayHumidity=" + todayHumidity +
                ", todayRainProbability=" + todayRainProbability +
                ", todayVisibility=" + todayVisibility +
                ", tomorrowDayWeather='" + tomorrowDayWeather + '\'' +
                ", tomorrowMaxTemperature=" + tomorrowMaxTemperature +
                ", tomorrowMinTemperature=" + tomorrowMinTemperature +
                ", dayAfterTomorrowDayWeather='" + dayAfterTomorrowDayWeather + '\'' +
                ", dayAfterTomorrowMaxTemperature=" + dayAfterTomorrowMaxTemperature +
                ", dayAfterTomorrowMinTemperature=" + dayAfterTomorrowMinTemperature +
                ", updateTimestamp=" + updateTimestamp +
                '}';
    }
}
