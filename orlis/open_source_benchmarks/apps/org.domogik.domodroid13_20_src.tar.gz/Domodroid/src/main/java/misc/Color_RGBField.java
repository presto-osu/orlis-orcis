package misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

@SuppressWarnings("ALL")
public class Color_RGBField extends View {
    private final Paint mPaint;
    public float mCurrentHue = 0;
    public int mCurrentX = 0;
    public int mCurrentY = 255;
    public final int[] mHueBarColors = new int[258];
    private final int[] mMainColors = new int[65536];
    public int widgetSize;
    private int index;
    public float[] hsv4 = {0, 0, 1};
    public float[] hsv5 = {0, 1, 1};


    public Color_RGBField(Context context, int color, int defaultColor) {
        super(context);
        int mDefaultColor = defaultColor;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setStrokeWidth(16);

        float[] hsv4 = {0, 0, 0};

        for (int x = 0; x < 32; x++) {
            float[] hsv5 = {mCurrentHue, (float) (x + 1) / 32f, 1};
            int[] colors = new int[2];
            colors[0] = Color.HSVToColor(hsv5);
            colors[1] = Color.HSVToColor(hsv4);
            Shader shader = new LinearGradient(0, 0, 0, 128, colors, null, Shader.TileMode.REPEAT);
            mPaint.setShader(shader);
            canvas.drawLine((x * 8) + 12, 0, (x * 8) + 12, 128, mPaint);
        }
        mPaint.setShader(null);
        mPaint.setStrokeWidth(2);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        canvas.drawCircle(mCurrentX + 8, mCurrentY / 2, 6, mPaint);

        int mCurrentColor = mMainColors[index];

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(268, 128);
    }

    public void refresh() {
        invalidate();
    }


}