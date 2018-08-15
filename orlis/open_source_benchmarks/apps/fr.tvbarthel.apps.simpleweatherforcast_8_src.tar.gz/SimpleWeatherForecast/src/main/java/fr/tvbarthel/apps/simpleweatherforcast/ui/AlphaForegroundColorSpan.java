package fr.tvbarthel.apps.simpleweatherforcast.ui;

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;


/**
 * A {@link ForegroundColorSpan} with alpha.
 * Thanks to Cyril Mottier and Flavien Laurent.
 * http://flavienlaurent.com/blog/2013/11/20/making-your-action-bar-not-boring
 */
public class AlphaForegroundColorSpan extends ForegroundColorSpan {

    private float mAlpha;

    public AlphaForegroundColorSpan(int color) {
        super(color);
    }

    public AlphaForegroundColorSpan(Parcel src) {
        super(src);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(getAlphaColor());
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getAlpha() {
        return mAlpha;
    }

    private int getAlphaColor() {
        int foregroundColor = getForegroundColor();
        return Color.argb((int) (mAlpha * 255), Color.red(foregroundColor), Color.green(foregroundColor),
                Color.blue(foregroundColor));
    }
}
