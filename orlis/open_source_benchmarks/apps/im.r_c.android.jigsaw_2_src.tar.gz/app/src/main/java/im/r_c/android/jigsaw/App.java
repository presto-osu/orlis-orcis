package im.r_c.android.jigsaw;

import android.app.Application;
import android.os.Handler;

/**
 * Jigsaw
 * Created by richard on 16/5/16.
 */
public class App extends Application {
    private static Handler sHandler = new Handler();
    public static Handler getMainHandler() {
        return sHandler;
    }
}
