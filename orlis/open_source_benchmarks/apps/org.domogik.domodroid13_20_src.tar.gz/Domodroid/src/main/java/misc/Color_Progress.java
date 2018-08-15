package misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.DisplayMetrics;

import misc.tracerengine;

import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.SeekBar;

public class Color_Progress extends SeekBar {

    private final Paint mPaint;
    private float mCurrentHue = 0;
    private final int[] mHueBarColors = new int[258];
    public int widgetSize;
    private final int mode;

    public float[] hsv0 = {0, 0, 0};
    public float[] hsv1 = {0, 0, 0};

    public float[] hsv2 = {0, 0, 0};
    public float[] hsv3 = {0, 0, 1};

    private final float[] hsv5 = {0, 0, 0};
    private final float[] hsv6 = {0, 0, 1};

    private final int dpiClassification;


    public Color_Progress(tracerengine Trac, Context context, int mode, int color) {
        super(context);
        this.mode = mode;

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        dpiClassification = dm.densityDpi;
        if (dpiClassification == 240) {
            setLayoutParams(new LayoutParams(278, 60, Gravity.CENTER_HORIZONTAL));
        } else if (dpiClassification == 160) {
            setLayoutParams(new LayoutParams(272, 40, Gravity.CENTER_HORIZONTAL));
        } else {
            setLayoutParams(new LayoutParams(272, 40, Gravity.CENTER_HORIZONTAL));
        }


        float[] hsv = new float[3];
        Color.colorToHSV(0, hsv);
        mCurrentHue = hsv[0];

        if (mode == 0) {
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
        }
        if (mode == 2) {
            int index = 0;
            for (int i = 0; i < 256; i++) {
                mHueBarColors[index] = Color.rgb(i, i, i);
                index++;
            }
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(12);
        //todo redraw the cursor to good size
/*
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.buttonseekbar);
        Bitmap thumb=Bitmap.createBitmap(50,50, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(thumb);
        canvas.drawBitmap(bitmap, new Rect(20, 20, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(20, 20, thumb.getWidth(), thumb.getHeight()), null);
        Drawable drawable = new BitmapDrawable(getResources(),thumb);
        setThumb(drawable);
*/
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
        if (mode == 0) {
            for (int x = 0; x < 256; x++) {
                if (translatedHue != x) {
                    mPaint.setColor(mHueBarColors[x]);
                    mPaint.setStrokeWidth(1);
                }
                if (dpiClassification == 240) {
                    canvas.drawLine(x + 12, 10, x + 12, 36, mPaint);
                } else if (dpiClassification == 160) {
                    canvas.drawLine(x + 6, 6, x + 6, 24, mPaint);
                } else {
                    canvas.drawLine(x + 12, 10, x + 12, 36, mPaint);
                }

            }
        } else if (mode == 1) {
            int[] colors = new int[2];
            colors[0] = Color.HSVToColor(hsv0);
            colors[1] = Color.HSVToColor(hsv1);
            //Log.e("metri", ""+dpiClassification);
            if (dpiClassification == 240) {
                Shader shader = new LinearGradient(12, 0, 268, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(12, 10, 268, 36, mPaint);
            } else if (dpiClassification == 160) {
                Shader shader = new LinearGradient(6, 0, 262, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(6, 6, 262, 24, mPaint);
            } else {
                Shader shader = new LinearGradient(6, 0, 262, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(6, 6, 262, 24, mPaint);
            }
        } else if (mode == 2) {
            int[] colors = new int[2];
            colors[0] = Color.HSVToColor(hsv2);
            colors[1] = Color.HSVToColor(hsv3);
            if (dpiClassification == 240) {
                Shader shader = new LinearGradient(12, 0, 268, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(12, 10, 268, 36, mPaint);
            } else if (dpiClassification == 160) {
                Shader shader = new LinearGradient(6, 0, 262, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(6, 6, 262, 24, mPaint);
            } else {
                Shader shader = new LinearGradient(6, 0, 262, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(6, 6, 262, 24, mPaint);
            }
        } else if (mode == 3) {
            int[] colors = new int[2];
            colors[0] = Color.HSVToColor(hsv5);
            colors[1] = Color.HSVToColor(hsv6);
            if (dpiClassification == 240) {
                Shader shader = new LinearGradient(12, 0, 268, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(12, 10, 268, 36, mPaint);
            } else if (dpiClassification == 160) {
                Shader shader = new LinearGradient(6, 0, 262, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(6, 6, 262, 24, mPaint);
            } else {
                Shader shader = new LinearGradient(6, 0, 262, 0, colors, null, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                canvas.drawRect(6, 6, 262, 24, mPaint);
            }
        }
        super.onDraw(canvas);

    }
}

