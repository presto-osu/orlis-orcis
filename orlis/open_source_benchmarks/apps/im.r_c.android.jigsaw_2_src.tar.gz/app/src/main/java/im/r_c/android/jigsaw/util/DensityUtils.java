package im.r_c.android.jigsaw.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * DensityUtils
 * Created by richard on 16/5/6.
 */
public class DensityUtils {
    public static int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2sp(Context context, float px) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float sp) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    public static float getDeviceDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }
}
