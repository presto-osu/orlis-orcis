/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.uicomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * Created by Graham on 05/12/2014.
 */
public class SOCGauge extends BaseComponent {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int DEFAULT_ORIENTATION = HORIZONTAL;

    public static final float DEFAULT_MAX = 100;
    public static final float DEFAULT_MIN = 0;

    public static final ValueToColorConverter DEFAULT_CONVERTER = new ValueToColorConverter() {
        @Override
        public int getColorOf(SOCGauge view, float value) {
            float percentage = view.getPercentage();
            if (percentage < 10.0f) {
                return Color.RED;
            }
            if (percentage < 20.0f) {
                return Color.rgb(255, 127, 0);
            }
            if (percentage < 40.0f) {
                return Color.rgb(255, 191, 0);
            }
            if (percentage < 50.0f) {
                return Color.rgb(255, 216, 0);
            }
            if (percentage < 70.0f) {
                return Color.rgb(255, 247, 0);
            }
            return Color.rgb(102, 255, 0);
        }
    };

    private float maxValue = DEFAULT_MAX;
    private float currentValue = 0;
    private float minValue = DEFAULT_MIN;
    private int orientation = DEFAULT_ORIENTATION;
    private ValueToColorConverter valueToColorConverter;

    protected RectF gaugeRect;
    protected RectF batteryRect;
    protected PointF textPosition;

    private int readingColor = 0xFFFFFFFF;

    private Paint contentPaint;
    private Paint bitmapPaint;
    private Paint ovalPaint;
    private Paint textPaint;
    private Bitmap batteryBitmap;

    public SOCGauge(Context context, AttributeSet attrs, final int defStyle) {
        super(context, attrs);
        readAttrs(context, attrs, defStyle);
        init();
        setLabelConverter(new LabelConverter() {
            @Override
            public String getLabelFor(float val, float min, float max) {
                if (max > min)
                    val = val * 100 / (max - min);
                return String.valueOf((int) Math.round(val)) + '%';
            }
        });
    }

    public SOCGauge(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SOCGauge(final Context context) {
        this(context, null, 0);
    }

    public float getMax() {
        return maxValue;
    }

    public void setMax(float max) {
        if (max < minValue) {
            throw new IllegalArgumentException("Illegal value: max < min");
        }
        this.maxValue = max;
        invalidate();
    }

    public float getMin() {
        return minValue;
    }

    public void setMin(float min) {
        if (maxValue < min) {
            throw new IllegalArgumentException("Illegal value: min > max");
        }
        this.minValue = min;
        invalidate();
    }

    public float getValue() {
        return currentValue;
    }

    public void setValue(float value) {
        if (value <= getMin())
            value = getMin();
        if (value >= getMax())
            value = getMax();
        this.currentValue = value;
        invalidate();
    }

    public ValueToColorConverter getValueToColorConverter() {
        return valueToColorConverter;
    }

    public void setValueToColorConverter(ValueToColorConverter valueToColorConverter) {
        this.valueToColorConverter = valueToColorConverter;
        invalidate();
    }

    private int getColorForValue(float value) {
        return valueToColorConverter == null ? DEFAULT_CONVERTER.getColorOf(this, value) : valueToColorConverter.getColorOf(this, value);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == VERTICAL || orientation == HORIZONTAL)
            this.orientation = orientation;
        else throw new IllegalArgumentException("Invalid orientation: " + orientation);

        if (getOrientation() == HORIZONTAL)
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_horisontal);
        else
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_vertical);
    }

    public float getPercentage() {
        return 100 * getValue() / (getMax() - getMin());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        gaugeRect = getRect(getWidth(), getHeight());
        batteryRect = getBatteryContentRect(gaugeRect);

        final float testTextSize = 96f;
        // Get the bounds of the text, using our testTextSize.
        textPaint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        textPaint.getTextBounds("100%", 0, 4, bounds);
        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * Math.min(batteryRect.width(), batteryRect.height()) / bounds.width();
        desiredTextSize = desiredTextSize > testTextSize ? testTextSize : desiredTextSize;
        // Set the paint for that size.
        textPaint.setTextSize(desiredTextSize);
        textPaint.setShadowLayer(3, 5, 5, Color.BLACK);
        textPaint.getTextBounds("100%", 0, 4, bounds);
        textPosition = new PointF(batteryRect.centerX(), batteryRect.centerY() + bounds.height() / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        contentPaint.setColor(Color.TRANSPARENT);
//        canvas.drawPaint(contentPaint);

        if (getWidth() == 0 || getHeight() == 0)
            return;

        RectF rect = getRect(getWidth(), getHeight());
        float alpha = rect.width() / getOriginalWidth();
        contentPaint.setColor(getColorForValue(getValue()));
        if (!isInEditMode())
            contentPaint.setMaskFilter(new BlurMaskFilter(3 * alpha, BlurMaskFilter.Blur.NORMAL));

        RectF contentRect = new RectF(batteryRect);
        float percentage = getPercentage();
        if (getOrientation() == HORIZONTAL) {
            contentRect.right = contentRect.left + contentRect.width() * percentage / 100;
        } else {
            contentRect.top = contentRect.top + contentRect.height() * (100 - percentage) / 100;
        }
        canvas.drawRect(contentRect, contentPaint);

        if (percentage > 0.5f && percentage < 99.0f) {
            ovalPaint.setColor(getColorForValue(getValue()));
            if (getOrientation() == HORIZONTAL) {
                canvas.drawOval(new RectF(contentRect.right - alpha * 20, contentRect.top, contentRect.right + alpha * 20, contentRect.bottom), ovalPaint);
                ovalPaint.setColor(Color.argb(6, 0, 0, 0));
                canvas.drawOval(new RectF(contentRect.right - alpha * 20, contentRect.top, contentRect.right + alpha * 20, contentRect.bottom), ovalPaint);
            } else {
                canvas.drawOval(new RectF(contentRect.left, contentRect.top - alpha * 25, contentRect.right, contentRect.top + alpha * 25), ovalPaint);
                ovalPaint.setColor(Color.argb(6, 0, 0, 0));
                canvas.drawOval(new RectF(contentRect.left, contentRect.top - alpha * 25, contentRect.right, contentRect.top + alpha * 25), ovalPaint);
            }

            contentPaint.setColor(getColorForValue(getValue()));
            if (getOrientation() == HORIZONTAL) {
                canvas.drawOval(new RectF(contentRect.right - alpha * 12, contentRect.top + alpha * 20, contentRect.right + alpha * 12, contentRect.bottom - alpha * 20), contentPaint);
                contentPaint.setColor(Color.argb(80, 255, 255, 255));
                if (!isInEditMode())
                    contentPaint.setMaskFilter(new BlurMaskFilter(6 * alpha, BlurMaskFilter.Blur.NORMAL));
                canvas.drawOval(new RectF(contentRect.right - alpha * 12, contentRect.top + alpha * 20, contentRect.right + alpha * 12, contentRect.bottom - alpha * 20), contentPaint);
            } else {
                canvas.drawOval(new RectF(contentRect.left + alpha * 20, contentRect.top - alpha * 12, contentRect.right - alpha * 20, contentRect.top + alpha * 12), contentPaint);
                contentPaint.setColor(Color.argb(80, 255, 255, 255));
                if (!isInEditMode())
                    contentPaint.setMaskFilter(new BlurMaskFilter(6 * alpha, BlurMaskFilter.Blur.NORMAL));
                canvas.drawOval(new RectF(contentRect.left + alpha * 20, contentRect.top - alpha * 12, contentRect.right - alpha * 20, contentRect.top + alpha * 12), contentPaint);
            }
        }

        Bitmap bmp = Bitmap.createScaledBitmap(batteryBitmap, (int) rect.width(), (int) rect.height(), true);
        canvas.drawBitmap(bmp, rect.left, rect.top, bitmapPaint);

        // debug, draw green outer box to show padding

//        Paint testPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        testPaint.setStyle(Paint.Style.STROKE);
//        testPaint.setStrokeWidth(3);
//        testPaint.setColor(Color.GREEN);
////        canvas.drawRect(0, 0, mViewWidth, mViewHeight, testPaint);
//        canvas.drawRect(batteryRect, testPaint);
//        canvas.drawRect(0, 0,  getWidth() + getPaddingLeft() + getPaddingRight(), getHeight() + getPaddingTop() + getPaddingBottom(), testPaint);

        // end debug

        canvas.drawText(getLabelConverter().getLabelFor(percentage, minValue, maxValue), textPosition.x, textPosition.y, textPaint);
    }

    private void init() {
        contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setStyle(Paint.Style.FILL);

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setStyle(Paint.Style.FILL);

        ovalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ovalPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(readingColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (getOrientation() == HORIZONTAL)
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_horisontal);
        else if (getOrientation() == VERTICAL)
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_vertical);
        else throw new IllegalStateException("Invalid orientation value: " + getOrientation());
    }

    private float getOriginalWidth() {
        if (!isInEditMode()) {
            return batteryBitmap.getWidth();
        } else {
            return (getOrientation() == HORIZONTAL) ? 467 : 216;
        }
    }

    private float getOriginalHeight() {
        if (!isInEditMode()) {
            return batteryBitmap.getHeight();
        } else {
            return (getOrientation() == HORIZONTAL) ? 216 : 467;
        }
    }

    private RectF getRect(float w, float h) {
        float availableWidth = w - getPaddingLeft() - getPaddingRight();
        float availableHeight = h - getPaddingTop() - getPaddingBottom();
        if (availableWidth / availableHeight >= getOriginalWidth() / getOriginalHeight()) {
            // we have abundant space horizontally
            float alpha = availableHeight / getOriginalHeight();
            float x = getPaddingLeft() + (availableWidth - alpha * getOriginalWidth()) / 2.0f;
            float y = getPaddingTop();
            return new RectF(x, y, x + alpha * getOriginalWidth(), h - getPaddingBottom());
        } else {
            // too much space vertically
            float alpha = availableWidth / getOriginalWidth();
            float x = getPaddingLeft();
            float y = getPaddingTop() + (availableHeight - alpha * getOriginalHeight()) / 2.0f;
            return new RectF(x, y, w - getPaddingRight(), y + alpha * getOriginalHeight());
        }
    }

    private RectF getBatteryContentRect(RectF rect) {
        if (getOrientation() == HORIZONTAL) {
            float alpha = rect.width() / getOriginalWidth();
            return new RectF(
                    rect.left + 20 * alpha,
                    rect.top + 8 * alpha,
                    rect.right - 40 * alpha,
                    rect.bottom - 8 * alpha
            );
        } else if (getOrientation() == VERTICAL) {
            float alpha = rect.height() / getOriginalHeight();
            return new RectF(
                    rect.left + 8 * alpha,
                    rect.top + 40 * alpha,
                    rect.right - 8 * alpha,
                    rect.bottom - 20 * alpha
            );
        } else throw new IllegalStateException("Invalid orientation value: " + getOrientation());
    }

    public static interface ValueToColorConverter {

        int getColorOf(SOCGauge view, float value);

    }

    protected void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gauge, defStyle, 0);
        int orientation = a.getInteger(R.styleable.Gauge_orientation, DEFAULT_ORIENTATION);
        if (orientation == VERTICAL || orientation == HORIZONTAL)
            this.orientation = orientation;
        setMin(a.getFloat(R.styleable.Gauge_scaleStartValue, DEFAULT_MIN));
        setMax(a.getFloat(R.styleable.Gauge_scaleEndValue, DEFAULT_MAX));
        readingColor = a.getColor(R.styleable.Gauge_readingColor, Color.YELLOW);
        a.recycle();
    }
}
