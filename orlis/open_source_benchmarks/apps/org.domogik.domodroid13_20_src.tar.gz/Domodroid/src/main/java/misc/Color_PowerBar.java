package misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
import android.view.View;

public class Color_PowerBar extends View {
    private final Paint mPaint;
    private float mCurrentHue = 0;
    private int mCurrentColor;
    private final int[] mHueBarColors = new int[258];
    private final int[] mMainColors = new int[65536];
    public int widgetSize;

    public Color_PowerBar(Context context, int color, int defaultColor) {
        super(context);

        // Get the current hue from the current color and update the main color field
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        mCurrentHue = hsv[0];
        updateMainColors();

        mCurrentColor = color;

        // Initialize the colors of the hue slider bar
        int index = 0;
        for (float i = 0; i < 256; i++) // Red (#f00) to pink (#f0f)
        {
            mHueBarColors[index] = Color.rgb((int) i, (int) i, (int) i);
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
        canvas.drawText("Luminosity", 12, 15, mPaint);

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_MOVE) return true;
        float x = event.getX();
        float y = event.getY();


        // If the touch event is located in the hue bar
        if (x > 0 && x < 256 && y > 0 && y < 40) {
            // Update the main field colors
            mCurrentHue = (255 - x) * 360 / 255;
            updateMainColors();

            // Update the current selected color
            int mCurrentY = 0;
            int transY = mCurrentY - 60;
            int index = 256 * (transY - 1) + 0;
            if (index > 0 && index < mMainColors.length)
                mCurrentColor = mMainColors[256 * (transY - 1) + 0];

            // Force the redraw of the dialog
            invalidate();
        }
        return true;
    }
}