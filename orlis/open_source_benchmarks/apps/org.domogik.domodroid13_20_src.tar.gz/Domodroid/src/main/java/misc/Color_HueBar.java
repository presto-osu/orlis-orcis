package misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

public class Color_HueBar extends View {
    private final Paint mPaint;
    private float mCurrentHue = 0;
    private final int[] mHueBarColors = new int[258];
    private final int[] mMainColors = new int[65536];
    public int widgetSize;

    public Color_HueBar(Context context, int color, int defaultColor) {
        super(context);

        // Get the current hue from the current color and update the main color field
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        mCurrentHue = hsv[0];
        updateMainColors();


        // Initialize the colors of the hue slider bar
        int index = 0;
        for (float i = 0; i < 256; i += 256 / 42) // Red (#f00) to pink (#f0f)
        {
            mHueBarColors[index] = Color.rgb(255, 0, (int) i);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) // Pink (#f0f) to blue (#00f)
        {
            mHueBarColors[index] = Color.rgb(255 - (int) i, 0, 255);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) // Blue (#00f) to light blue (#0ff)
        {
            mHueBarColors[index] = Color.rgb(0, (int) i, 255);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) // Light blue (#0ff) to green (#0f0)
        {
            mHueBarColors[index] = Color.rgb(0, 255, 255 - (int) i);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) // Green (#0f0) to yellow (#ff0)
        {
            mHueBarColors[index] = Color.rgb((int) i, 255, 0);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) // Yellow (#ff0) to red (#f00)
        {
            mHueBarColors[index] = Color.rgb(255, 255 - (int) i, 0);
            index++;
        }

        // Initializes the Paint that will draw the View
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(12);
    }

    // Get the current selected color from the hue bar
    private int getCurrentMainColor() {
        int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
        int index = 0;
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(255, 0, (int) i);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(255 - (int) i, 0, 255);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(0, (int) i, 255);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(0, 255, 255 - (int) i);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb((int) i, 255, 0);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(255, 255 - (int) i, 0);
            index++;
        }
        return Color.RED;
    }

    // Update the main field colors depending on the current selected hue
    private void updateMainColors() {
        int mainColor = getCurrentMainColor();
        int index = 0;
        int[] topColors = new int[256];
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                if (y == 0) {
                    mMainColors[index] = Color.rgb(255 - (255 - Color.red(mainColor)) * x / 255, 255 - (255 - Color.green(mainColor)) * x / 255, 255 - (255 - Color.blue(mainColor)) * x / 255);
                    topColors[x] = mMainColors[index];
                } else
                    mMainColors[index] = Color.rgb((255 - y) * Color.red(topColors[x]) / 255, (255 - y) * Color.green(topColors[x]) / 255, (255 - y) * Color.blue(topColors[x]) / 255);
                index++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.LTGRAY);
        mPaint.setTextAlign(Align.LEFT);
        mPaint.setTextSize(15);
        mPaint.setStrokeWidth(0);
        canvas.drawText("Color Selector", 12, 15, mPaint);


        int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
        // Display all the colors of the hue bar with lines
        for (int x = 0; x < 256; x++) {
            // If this is not the current selected hue, display the actual color
            if (translatedHue != x) {
                mPaint.setColor(mHueBarColors[x]);
                mPaint.setStrokeWidth(1);
            }
            canvas.drawLine(x + 12, 20, x + 12, 60, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(268, 50);
    }
}