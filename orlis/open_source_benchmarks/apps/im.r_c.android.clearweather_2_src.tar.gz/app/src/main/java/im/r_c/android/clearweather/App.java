package im.r_c.android.clearweather;

import android.app.Application;
import android.os.Handler;

/**
 * ClearWeather
 * Created by richard on 16/4/29.
 */
public class App extends Application {
    private static Handler mHandler = new Handler();
    public static Handler getMainHandler() {
        return mHandler;
    }
}
