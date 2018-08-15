/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import org.pixmob.freemobile.netstat.Event;
import org.pixmob.freemobile.netstat.MobileOperator;
import org.pixmob.freemobile.netstat.R;

/**
 * Custom component showing battery levels with a chart.
 * @author Pixmob
 */
public class BatteryChart extends View {
    private Bitmap cache;
    private int freeMobileColor;
    private int orangeColor;
    private Paint mobileOperatorPaint;
    private int bgColor1;
    private int bgColor2;
    private Paint bgPaint;
    private Paint yBarPaint;
    private Paint yTextPaint;
    private Paint batteryLevelBorderPaint;
    private Paint batteryLevelPaint;
    private Paint cursorPaint;
    private Paint textCursorPaint;
    private Event[] events;
    private float touchX = -1;
    private float graphLeft;
    private int graphRight;
    private int graphBottom;
    private float graphTop;
    private float textCursorLeft;
    private float textCursorBottom;
    private String textCursor;
    private int textCursorFormat = DateUtils.FORMAT_SHOW_TIME;

    public BatteryChart(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        invalidateCache();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        if (MotionEvent.ACTION_DOWN == action || MotionEvent.ACTION_MOVE == action) {
            final float x = event.getX();
            if (x >= graphLeft && x <= graphRight) {
                if (events != null && events.length != 0) {
                    final long t0 = events[0].timestamp;
                    final long t = Math.round((x - graphLeft) / getXFactor()) + t0;
                    textCursor = DateUtils.formatDateTime(getContext(), t, textCursorFormat);

                    touchX = x;
                    invalidate();
                    return true;
                }
            }
        }
        if (MotionEvent.ACTION_UP == action) {
            touchX = -1;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private float getXFactor() {
        if (events == null || events.length == 0) {
            return 0;
        }

        return (graphRight - graphLeft) / (events[events.length - 1].timestamp - events[0].timestamp);
    }

    private void doDraw(Canvas canvas) {
        final float y0Mob = 0;

        // Draw every lines with a single call to Canvas#drawLines, for better
        // performance.
        final float[] lines = new float[12 * 4];
        int lineIdx = 0;
        lines[lineIdx++] = graphLeft;
        lines[lineIdx++] = graphTop;
        lines[lineIdx++] = graphLeft;
        lines[lineIdx++] = graphBottom;
        lines[lineIdx++] = graphLeft;
        lines[lineIdx++] = graphBottom;
        lines[lineIdx++] = graphRight;
        lines[lineIdx++] = graphBottom;

        // Draw Y units.
        final float x0Text = graphLeft - toDip(5);
        final float yFactor = (graphBottom - graphTop) / 100f;
        final int bandSize = 10;
        final float yBand = yFactor * bandSize;
        final float yAscent2 = yTextPaint.ascent() / 2;
        for (int i = bandSize; i <= 100; i += bandSize) {
            if (bgPaint.getColor() == bgColor2) {
                bgPaint.setColor(bgColor1);
            } else {
                bgPaint.setColor(bgColor2);
            }
            final float y = graphBottom - yFactor * i;
            canvas.drawRect(graphLeft, y, graphRight, y + yBand, bgPaint);
            if (i != 100) {
                canvas.drawText(String.valueOf(i), x0Text, y - yAscent2, yTextPaint);
            }
            lines[lineIdx++] = graphLeft;
            lines[lineIdx++] = y;
            lines[lineIdx++] = graphRight;
            lines[lineIdx++] = y;
        }

        final boolean drawChart = events != null && events.length > 1;
        Path batteryLevelBorderPath = null;

        if (drawChart) {
            final long t0 = events[0].timestamp;
            final float xFactor = getXFactor();

            final int eventCount = events.length;

            final Path batteryPath = new Path();
            batteryPath.moveTo(graphLeft, graphBottom);
            batteryPath.incReserve(eventCount + 2);
            float lastY = 0;

            for (int i = 0; i < eventCount; ++i) {
                final Event e = events[i];
                float x = (e.timestamp - t0) * xFactor + graphLeft;
                if (x < graphLeft) {
                    continue;
                }
                final float y = graphBottom - e.batteryLevel * yFactor;

                if (i != 0) {
                    batteryPath.lineTo(x, y);
                } else {
                    batteryPath.moveTo(x, y);
                }
                lastY = y;

                final MobileOperator mobOp = MobileOperator.fromString(e.mobileOperator);
                if (mobOp != null) {
                    float x2;
                    if (i != 0) {
                        final Event e0 = events[i - 1];
                        if (e.powerOn && !e0.powerOn) {
                            continue;
                        }

                        final MobileOperator mobOp0 = MobileOperator.fromString(e0.mobileOperator);
                        if (mobOp0 != null) {
                            if (MobileOperator.FREE_MOBILE.equals(mobOp0)) {
                                mobileOperatorPaint.setColor(freeMobileColor);
                            } else if (MobileOperator.ORANGE.equals(mobOp0)) {
                                mobileOperatorPaint.setColor(orangeColor);
                            }

                            x2 = (e0.timestamp - t0) * xFactor + graphLeft;
                            if (x2 < graphLeft) {
                                x2 = graphLeft;
                            }
                            if (x2 > graphRight) {
                                x2 = graphRight;
                            }
                            canvas.drawLine(x2, y0Mob, x, y0Mob, mobileOperatorPaint);
                        }
                    }
                }
            }

            batteryLevelBorderPath = new Path(batteryPath);

            batteryPath.lineTo(graphRight, lastY);
            batteryPath.lineTo(graphRight, graphBottom);
            batteryPath.lineTo(graphLeft, graphBottom);
            canvas.drawPath(batteryPath, batteryLevelPaint);
        }

        // Draw axes.
        canvas.drawLines(lines, yBarPaint);

        if (drawChart) {
            canvas.clipRect(graphLeft, 0, graphRight, graphBottom);
            canvas.drawPath(batteryLevelBorderPath, batteryLevelBorderPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Lazy initialize paint properties, once.
        if (batteryLevelPaint == null) {
            batteryLevelPaint = new Paint();
            batteryLevelPaint.setStyle(Paint.Style.FILL);

            final int c1 = getResources().getColor(R.color.battery_level_color1);
            final int c2 = getResources().getColor(R.color.battery_level_color2);
            batteryLevelPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), c1, c2,
                    Shader.TileMode.CLAMP));
        }
        if (batteryLevelBorderPaint == null) {
            batteryLevelBorderPaint = new Paint();
            batteryLevelBorderPaint.setAntiAlias(true);
            batteryLevelBorderPaint.setStyle(Paint.Style.STROKE);
            batteryLevelBorderPaint.setStrokeWidth(4);
            batteryLevelBorderPaint.setStrokeCap(Paint.Cap.ROUND);
            batteryLevelBorderPaint.setStrokeJoin(Paint.Join.ROUND);
            batteryLevelBorderPaint.setColor(getResources().getColor(R.color.battery_level_border_color));
        }
        if (bgPaint == null) {
            bgColor1 = getResources().getColor(R.color.battery_bg_color1);
            bgColor2 = getResources().getColor(R.color.battery_bg_color2);
            bgPaint = new Paint();
            bgPaint.setStyle(Paint.Style.FILL);
        }
        if (yTextPaint == null) {
            yTextPaint = new Paint();
            yTextPaint.setAntiAlias(true);
            yTextPaint.setSubpixelText(true);
            yTextPaint.setColor(getResources().getColor(R.color.battery_y_text_color));
            yTextPaint.setTextSize(getResources().getDimension(R.dimen.battery_y_text_size));
            yTextPaint.setTextAlign(Paint.Align.RIGHT);
            yTextPaint.setStrokeWidth(2);

            graphLeft = yTextPaint.measureText("100") + 2;
        }
        if (yBarPaint == null) {
            yBarPaint = new Paint();
            yBarPaint.setColor(getResources().getColor(R.color.battery_y_bar_color));
            yBarPaint.setStrokeWidth(2);
        }
        if (mobileOperatorPaint == null) {
            mobileOperatorPaint = new Paint();
            mobileOperatorPaint.setStrokeWidth(16);
            mobileOperatorPaint.setStyle(Paint.Style.STROKE);

            orangeColor = getResources().getColor(R.color.orange_network_color1);
            freeMobileColor = getResources().getColor(R.color.free_mobile_network_color1);
        }
        if (cursorPaint == null) {
            cursorPaint = new Paint();
            cursorPaint.setAntiAlias(true);
            cursorPaint.setColor(getResources().getColor(R.color.battery_cursor_color));
            cursorPaint.setStrokeWidth(2);
            cursorPaint.setStyle(Paint.Style.FILL);
        }
        if (textCursorPaint == null) {
            textCursorPaint = new Paint();
            textCursorPaint.setAntiAlias(true);
            textCursorPaint.setSubpixelText(true);
            textCursorPaint.setColor(getResources().getColor(R.color.battery_text_cursor_color));
            textCursorPaint.setTextSize(getResources().getDimension(R.dimen.battery_y_text_size));
            textCursorPaint.setTextAlign(Paint.Align.RIGHT);

            textCursorBottom = -textCursorPaint.ascent() + toDip(5);
            graphTop = textCursorBottom + toDip(4);
        }

        // No image is available in the cache: render a new one.
        if (cache == null) {
            cache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas c = new Canvas(cache);
            doDraw(c);
        }

        canvas.drawBitmap(cache, 0, 0, null);

        if (touchX != -1) {
            canvas.drawLine(touchX, graphTop, touchX, graphBottom, cursorPaint);
            canvas.drawText(textCursor, textCursorLeft, textCursorBottom, textCursorPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidateCache();

        graphRight = getWidth();
        graphBottom = getHeight();
        textCursorLeft = graphRight - toDip(2);
    }

    private float toDip(float pixels) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, getResources()
                .getDisplayMetrics());
    }

    public void setData(Event[] events) {
        this.events = events;
        invalidateCache();

        if (events != null && events.length != 0) {
            final long duration = events[events.length - 1].timestamp - events[0].timestamp;
            if (duration >= 86400 * 1000) {
                // Over a day: show date+time.
                textCursorFormat = DateUtils.FORMAT_SHOW_DATE;
            } else {
                // One day: show time only.
                textCursorFormat = DateUtils.FORMAT_SHOW_TIME;
            }
        }
    }

    private void invalidateCache() {
        if (cache != null) {
            cache.recycle();
            cache = null;
        }
    }
}
