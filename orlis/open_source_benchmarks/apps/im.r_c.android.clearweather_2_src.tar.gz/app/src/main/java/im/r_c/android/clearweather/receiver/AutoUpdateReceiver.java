package im.r_c.android.clearweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import im.r_c.android.clearweather.service.FetchWeatherInfoService;

/**
 * ClearWeather
 * Created by richard on 16/5/4.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FetchWeatherInfoService.startAutoUpdate(context);
    }
}
