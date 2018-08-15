package fr.tvbarthel.apps.simpleweatherforcast.ui;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.tvbarthel.apps.simpleweatherforcast.MainActivity;
import fr.tvbarthel.apps.simpleweatherforcast.R;
import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastJsonParser;
import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastModel;
import fr.tvbarthel.apps.simpleweatherforcast.utils.SharedPreferenceUtils;
import fr.tvbarthel.apps.simpleweatherforcast.utils.TemperatureUtils;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WeatherRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mAppWidgetId;
    private List<DailyForecastModel> mDailyForecasts;
    private int[] mColors;
    private SimpleDateFormat mSimpleDateFormat;
    private String mTemperatureUnit;

    public WeatherRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mSimpleDateFormat = new SimpleDateFormat("EEEE dd MMMM", Locale.getDefault());
    }

    @Override
    public void onCreate() {
        mColors = new int[]{R.color.holo_blue,
                R.color.holo_purple,
                R.color.holo_yellow,
                R.color.holo_red,
                R.color.holo_green};
    }

    @Override
    public void onDataSetChanged() {
        final String lastKnownWeather = SharedPreferenceUtils.getLastKnownWeather(mContext);
        mDailyForecasts = DailyForecastJsonParser.parse(lastKnownWeather);
        mTemperatureUnit = SharedPreferenceUtils.getTemperatureUnitSymbol(mContext);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mDailyForecasts.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final DailyForecastModel dailyForecast = mDailyForecasts.get(position);
        final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.row_app_widget);
        final long temperature = TemperatureUtils.convertTemperature(mContext, dailyForecast.getTemperature(), mTemperatureUnit);
        final int backgroundColor = mColors[position % mColors.length];
        final String date = mSimpleDateFormat.format(dailyForecast.getDateTime() * 1000);

        remoteViews.setTextViewText(R.id.row_app_widget_date, date);
        remoteViews.setTextViewText(R.id.row_app_widget_temperature, temperature + mTemperatureUnit);
        remoteViews.setTextViewText(R.id.row_app_widget_weather, dailyForecast.getDescription());
        remoteViews.setInt(R.id.row_app_widget_background, "setBackgroundResource", backgroundColor);
        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(MainActivity.EXTRA_PAGE_POSITION, position);
        remoteViews.setOnClickFillInIntent(R.id.row_app_widget_root, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
