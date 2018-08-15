package im.r_c.android.clearweather.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import im.r_c.android.clearweather.R;
import im.r_c.android.clearweather.db.WeatherInfoDAO;
import im.r_c.android.clearweather.model.Consts;
import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.model.WeatherInfo;
import im.r_c.android.clearweather.service.FetchWeatherInfoService;
import im.r_c.android.clearweather.util.L;

/**
 * ClearWeather
 * Created by richard on 16/4/29.
 */
public class CardFragment extends Fragment {
    private static final String TAG = "CardFragment";

    public static CardFragment newInstance(County county) {
        Bundle args = new Bundle();
        args.putSerializable(Consts.EXTRA_COUNTY, county);
        CardFragment fragment = new CardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.tv_county_name)
    TextView mTvCountyName;

    @Bind(R.id.tv_county_weather)
    TextView mTvCountyWeather;

    @Bind(R.id.tv_now_temperature)
    TextView mTvNowTemperature;

    @Bind(R.id.tv_now_temperature_unit)
    TextView mTvNowTemperatureUnit;

    @Bind(R.id.tv_today_day_weather)
    TextView mTvTodayDayWeather;

    @Bind(R.id.tv_today_night_weather)
    TextView mTvTodayNightWeather;

    @Bind(R.id.tv_today_temperature)
    TextView mTvTodayTemperature;

    @Bind(R.id.tv_today_humidity)
    TextView mTvTodayHumidity;

    @Bind(R.id.tv_today_rain_probability)
    TextView mTvTodayRainProbability;

    @Bind(R.id.tv_today_visibility)
    TextView mTvTodayVisibility;

    @Bind(R.id.tv_tomorrow_day_weather)
    TextView mTvTomorrowDayWeather;

    @Bind(R.id.tv_tomorrow_temperature)
    TextView mTvTomorrowTemperature;

    @Bind(R.id.tv_day_after_tomorrow_day_weather)
    TextView mTvDayAfterTomorrowDayWeather;

    @Bind(R.id.tv_day_after_tomorrow_temperature)
    TextView mTvDayAfterTomorrowTemperature;

    @Bind(R.id.tv_update_time)
    TextView mTvUpdateTime;

    private County mCounty;
    private Timer mTimer;
    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        ButterKnife.bind(this, view);

        mCounty = (County) getArguments().getSerializable(Consts.EXTRA_COUNTY);
        assert mCounty != null;
        mTvCountyName.setText(mCounty.getName());
        FetchWeatherInfoService.start(getContext(), mCounty);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // start a timer for checking weather info from db
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                WeatherInfoDAO dao = new WeatherInfoDAO(CardFragment.this.getContext());
                final WeatherInfo info = dao.query(mCounty);
                dao.close();
                if (CardFragment.this.isVisible()) {
                    if (info != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                L.v(TAG, "Showing weather info: " + info.toString());
                                L.v(TAG, "Of county: " + mCounty.toString());
                                showWeatherInfo(info);
                            }
                        });
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                L.v(TAG, "Showing empty weather info");
                                L.v(TAG, "Of county: " + mCounty.toString());
                                showEmptyInfo(mCounty);
                            }
                        });
                    }
                }
            }
        }, 0, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();

        // cancel the timer
        mTimer.cancel();
        mTimer.purge();
    }

    private void showWeatherInfo(WeatherInfo info) {
        mTvCountyName.setText(info.getCounty().getName());
        mTvCountyWeather.setText(info.getCountyWeather());
        mTvNowTemperature.setText(String.valueOf(info.getNowTemperature()));
        mTvNowTemperatureUnit.setVisibility(View.VISIBLE);

        mTvTodayDayWeather.setText(info.getTodayDayWeather());
        mTvTodayNightWeather.setText(info.getTodayNightWeather());
        mTvTodayTemperature.setText(String.format(Consts.FORMAT_TEMPERATURE, info.getTodayMinTemperature(), info.getTodayMaxTemperature()));
        mTvTodayHumidity.setText(String.format(Consts.FORMAT_HUMIDITY, info.getTodayHumidity()));
        mTvTodayRainProbability.setText(String.format(Consts.FORMAT_RAIN_PROBABILITY, info.getTodayRainProbability()));
        mTvTodayVisibility.setText(String.format(Consts.FORMAT_VISIBILITY, info.getTodayVisibility()));

        mTvTomorrowDayWeather.setText(info.getTodayDayWeather());
        mTvTomorrowTemperature.setText(String.format(Consts.FORMAT_TEMPERATURE, info.getTomorrowMinTemperature(), info.getTomorrowMaxTemperature()));

        mTvDayAfterTomorrowDayWeather.setText(info.getDayAfterTomorrowDayWeather());
        mTvDayAfterTomorrowTemperature.setText(String.format(Consts.FORMAT_TEMPERATURE, info.getDayAfterTomorrowMinTemperature(), info.getDayAfterTomorrowMaxTemperature()));

        mTvUpdateTime.setText(String.format(getString(R.string.format_update_time), DateFormat.format(Consts.FORMAT_DATE_TIME, info.getUpdateTimestamp())));
    }

    private void showEmptyInfo(County county) {
        mTvCountyName.setText(mCounty.getName());
        mTvCountyWeather.setText("");
        mTvNowTemperature.setText(getString(R.string.empty_now_temperature));
        mTvNowTemperatureUnit.setVisibility(View.GONE);

        mTvTodayDayWeather.setText("");
        mTvTodayNightWeather.setText("");
        mTvTodayTemperature.setText("");
        mTvTodayHumidity.setText("");
        mTvTodayRainProbability.setText("");
        mTvTodayVisibility.setText("");

        mTvTomorrowDayWeather.setText("");
        mTvTomorrowTemperature.setText("");

        mTvDayAfterTomorrowDayWeather.setText("");
        mTvDayAfterTomorrowTemperature.setText("");

        mTvUpdateTime.setText("");
    }
}
