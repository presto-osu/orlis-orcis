package gq.nulldev.animeopenings.app.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Project: AndroidUtils
 * Created: 21/10/15
 * Author: nulldev
 */
public class ConcurrencyUtils {

    static Handler handler;

    public static void runOnUiThread(Runnable r) {
        if(handler == null)
            handler = new Handler(Looper.getMainLooper());
        handler.post(r);
    }
}
