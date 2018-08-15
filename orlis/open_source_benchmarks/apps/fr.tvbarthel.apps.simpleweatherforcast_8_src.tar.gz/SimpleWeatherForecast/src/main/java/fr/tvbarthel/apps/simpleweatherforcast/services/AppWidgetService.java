package fr.tvbarthel.apps.simpleweatherforcast.services;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

import fr.tvbarthel.apps.simpleweatherforcast.ui.WeatherRemoteViewsFactory;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final Context context = getApplicationContext();
        return new WeatherRemoteViewsFactory(getApplicationContext(), intent);
    }
}
