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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class BaseGauge extends BaseComponent {

    // widths are defined as the percent of gauge diameter
    public static float mOuterShadowWidthPercentOfDiameter = 3f;
    public static float mOuterRimWidthPercentOfDiameter = 5;
    public static float mInnerRimWidthPercentOfDiameter = 5;


    public static float mTitleHeightPercentOfRadius = 12; // percent of face radius
    public static float mReadingHeightPercentOfRadius = 18; // percent of face radius
    public static float mColoredRangePercentOfRadius = 1;

    //    public static final int[] OUTER_SHADOW_COLORS = {Color.argb(255, 255, 254, 187), Color.argb(200, 255, 247, 219), Color.argb(100, 255, 255, 255)};
    public static final int[] OUTER_SHADOW_COLORS = {Color.argb(255, 0, 0, 0), Color.argb(200, 255, 247, 219), Color.argb(100, 255, 255, 255)};

    protected PointF mTitlePosition;
    protected PointF mReadingPosition;

    protected float mScaleStartAngle;
    protected float mScaleEndAngle;
    protected float mAvailableAngle;
    protected Quadrant mFace = Quadrant.Full;

    protected float mScaleStartValue;
    protected float mScaleEndValue;

    protected float mFaceRadius;
    protected float mScaleRadius;
    protected float mInnerRimWidth;
    protected float mMajorTickStepValue;
    protected float mMajorTicksLength;
    protected float mMinorTicksLength;

    protected int mDefaultColor = Color.rgb(180, 180, 180);
    protected double mMajorTickPercentOfRange;
    protected int mMinorTicksPerDivision;

    protected String mGaugeTitle;
    protected String mReadingUnit;

    protected List<ColoredRange> mRanges = new ArrayList<ColoredRange>();

    protected int mTitleColor;
    protected int mReadingColor;
    protected int mTickLabelColor;

    protected Paint mNeedleScrewPaint;
    protected Paint mNeedleScrewBorderPaint;
    protected Paint mBackgroundPaint;
    protected Paint mBorderAccentPaint;
    protected Paint mFacePaint;
    protected Paint mFaceBorderPaint;
    protected Paint mFaceShadowPaint;
    protected Paint mNeedleLeftPaint;
    protected Paint mNeedleRightPaint;
    protected Paint mTicksPaint;
    protected Paint mTickLabelTextPaint;
    protected Paint mColoredRangePaint;
    protected Paint mTitleTextPaint;
    protected Paint mReadingTextPaint;

    protected boolean mBiDirectional;
    protected boolean mAutoScale;
    protected boolean mShowReading;
    protected boolean mShowScale;
    protected boolean mStaticBackgroundLoaded = false;
    protected boolean mShowOuterShadow;
    protected boolean mShowRim;
    protected boolean mShowInnerRim;
    protected int mReadingPrecision;

    protected float mTargetValue;
    protected float mTargetValueNoCaps;
    protected float mCurrentValue;

    protected RectF mGaugeRect;
    protected RectF mOuterRim;
    protected RectF mInnerRim;
    protected RectF mFaceRect;
    protected RectF mScaleArc;
    protected RectF mNeedleScrew;

    protected Path mNeedleRightPath;
    protected Path mNeedleLeftPath;
    protected float mNeedleWidth;

    protected float mNeedleVelocity;
    protected float mNeedleAcceleration;
    protected long mNeedleLastMoved = -1;

    protected Bitmap mBackground;

    private boolean mBSizeChangedComplete = false;

    public BaseGauge(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        readAttrs(context, attrs, defStyle);
        initDrawingTools();

        setLabelConverter(new LabelConverter() {
            @Override
            public String getLabelFor(float val, float min, float max) {
                return String.valueOf((int) Math.round(val));
            }
        });

    }

    public BaseGauge(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseGauge(final Context context) {
        this(context, null, 0);
    }

    public float getScaleStart() {
        return mScaleStartValue;
    }

    public void setScaleStart(float scaleStart) {
        if (mScaleStartValue != scaleStart) {
            this.mScaleStartValue = scaleStart;
            if (mScaleEndValue < mScaleStartValue)
                mScaleEndValue = mScaleStartValue + 1;
            invalidateAll();
        }
    }

    public float getScaleEnd() {
        return mScaleEndValue;
    }

    public void setScaleEnd(float scaleEnd) {
        if (scaleEnd < 0)
            scaleEnd = 0;
        if (mScaleEndValue != scaleEnd) {
            this.mScaleEndValue = scaleEnd;
            if (mScaleStartValue > mScaleStartValue)
                mScaleStartValue = mScaleEndValue - 1;
            invalidateAll();
        }
    }

    public boolean getBiDirectional() {
        return mBiDirectional;
    }

    public void setBiDirectional(boolean val) {
        if (mBiDirectional != val) {
            mBiDirectional = val;
            invalidateAll();
        }
    }

    public boolean getAutoScale() {
        return mAutoScale;
    }

    public void setAutoScale(boolean val) {
        if (mAutoScale != val) {
            mAutoScale = val;
            invalidateAll();
        }
    }

    public double getMajorTickPercentOfRange() {
        return mMajorTickPercentOfRange;
    }

    public void setMajorTickPercentOfRange(double val) {
        if (val <= 0 || val > 100)
            throw new IllegalArgumentException("Bad value specified as a major tick step percent.");
        if (mMajorTickPercentOfRange != val) {
            this.mMajorTickPercentOfRange = val;
            invalidateAll();
        }
    }

    public int getMinorTicks() {
        return mMinorTicksPerDivision;
    }

    public void setMinorTicksPerDivision(int minorTicks) {
        if (mMinorTicksPerDivision != minorTicks) {
            this.mMinorTicksPerDivision = minorTicks;
            invalidateAll();
        }
    }

    public void clearColoredRanges() {
        mRanges.clear();
        invalidateAll();
    }

    public void addColoredRange(double begin, double end, int color) {
        addRangeSub(begin, end, color);
        invalidateAll();
    }

    private void addRangeSub(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (begin < 0 || begin > 100)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (end < 0 || end > 100)
            throw new IllegalArgumentException("Incorrect number range specified!");
        mRanges.add(new ColoredRange(color, begin, end));
    }

    public void setGreenRange(double startGreen, double endGreen) {
        if (startGreen > 0) {
            addRangeSub(0, startGreen, Color.YELLOW);
        }
        addRangeSub(startGreen, endGreen, Color.GREEN);
        if (endGreen < 100) {
            addRangeSub(endGreen, 100, Color.RED);
        }
        invalidateAll();
    }

    protected void autoAdjustScale(float target) {
        setScaleEnd(getScale(getScaleEnd(), target));
        return;
    }

    protected float getScale(float currentScaleEnd, float target) {
        target = Math.abs(target);
        while (currentScaleEnd < target) {
            currentScaleEnd *= 2;
        }
        return currentScaleEnd;
    }

    public void setTargetValue(final float value) {
        if (mShowScale) {
            if (mAutoScale) {
                autoAdjustScale(value);
            }
            if (mBiDirectional) {
                if (value < -mScaleEndValue) {
                    mTargetValue = -mScaleEndValue;
                } else if (value > mScaleEndValue) {
                    mTargetValue = mScaleEndValue;
                } else {
                    mTargetValue = value;
                }
            } else {
                if (value < mScaleStartValue) {
                    mTargetValue = mScaleStartValue;
                } else if (value > mScaleEndValue) {
                    mTargetValue = mScaleEndValue;
                } else {
                    mTargetValue = value;
                }
            }
        } else {
            mTargetValue = value;
        }
        mTargetValueNoCaps = value;
        invalidate();
    }

    public void setTitle(String val) {
        mGaugeTitle = val;
    }

    public void setUnit(String val) {
        mReadingUnit = val;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w == 0 || h == 0)
            return;
        mBackground = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mBackground);
        mGaugeRect = getScaledRect(1);
        float scale = 1.0f;

        if (isInEditMode()) {
            Paint editPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            editPaint.setStyle(Paint.Style.FILL);
            editPaint.setColor(Color.RED);
            if (mFace == Quadrant.Full) {
                canvas.drawOval(mGaugeRect, editPaint);

            } else if (mFace == Quadrant.Half) {
                canvas.drawArc(mGaugeRect, 180, 180, true, editPaint);
            }
            scale -= mInnerRimWidthPercentOfDiameter / 100;
            mFaceRect = getScaledRect(scale);
            drawFace(canvas);
            return;
        }
        if (mShowOuterShadow) {
            scale = (100 - mOuterShadowWidthPercentOfDiameter) / 100;
        }
        if (mShowRim) {
            mOuterRim = getScaledRect(scale);
            scale -= mOuterRimWidthPercentOfDiameter / 100;
            if (mShowInnerRim) {
                mInnerRim = getScaledRect(scale);
                scale -= mInnerRimWidthPercentOfDiameter / 100;
                mFaceRect = getScaledRect(scale);
                mInnerRimWidth = (mInnerRim.width() - mFaceRect.width()) / 2;
                mFaceBorderPaint.setStrokeWidth((mInnerRim.width() - mFaceRect.width()) / 2);
            } else {
                mFaceRect = getScaledRect(scale);
                mFaceBorderPaint.setStrokeWidth((mOuterRim.width() - mFaceRect.width()) / 2);
            }
        } else {
            mFaceRect = getScaledRect(scale);
        }
        mScaleArc = getScaledRect(scale * 0.8f);
        mScaleRadius = mScaleArc.width() / 2;
        mNeedleScrew = getScaledRect(0.06f);
        mFaceRadius = mFaceRect.width() / 2;
        mFacePaint.setShader(new RadialGradient(mFaceRect.centerX(), mFaceRect.centerY(), mFaceRadius, new int[]{Color.rgb(50, 132, 206), Color.rgb(36, 89, 162), Color.rgb(27, 59, 131)}, new float[]{0.5f, 0.96f, 0.99f}, Shader.TileMode.MIRROR));
        mFaceShadowPaint.setShader(new RadialGradient(mFaceRect.centerX(), mFaceRect.centerY(), mFaceRadius, new int[]{Color.argb(60, 40, 96, 170),
                Color.argb(80, 15, 34, 98), Color.argb(120, 0, 0, 0), Color.argb(140, 0, 0, 0)},
                new float[]{0.60f, 0.85f, 0.96f, 0.99f}, Shader.TileMode.MIRROR));
        drawRim(canvas);
        drawFace(canvas);
        mBSizeChangedComplete = true;
        updateSizes();
        if (mShowScale) {
            drawScale(canvas);
        }
        makeNeedle();
    }

    private void makeNeedle() {
        mNeedleWidth = mNeedleScrew.width() / 4;
        float needleRadius = mScaleArc.width() / 2.1f;
        float counterBalanceSize = mNeedleScrew.width();
        float x = mGaugeRect.centerX();
        float y = mGaugeRect.centerY();
        mNeedleLeftPath = new Path();
        mNeedleLeftPath.moveTo(x, y);
        mNeedleLeftPath.moveTo(x, y + counterBalanceSize);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y + counterBalanceSize);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y - counterBalanceSize);
        mNeedleLeftPath.lineTo(x, y - needleRadius);
        mNeedleLeftPath.lineTo(x, y);
        mNeedleRightPath = new Path();
        mNeedleRightPath.moveTo(x, y);
        mNeedleRightPath.moveTo(x, y + counterBalanceSize);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y + counterBalanceSize);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y - counterBalanceSize);
        mNeedleRightPath.lineTo(x, y - needleRadius);
        mNeedleRightPath.lineTo(x, y);
    }

    public void invalidateAll() {
        if (mBSizeChangedComplete && !isInEditMode()) {
            if (mBackground != null) {
                mBackground.recycle();
            }
            mBackground = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(mBackground);
            drawRim(canvas);
            drawFace(canvas);
            updateSizes();
            if (mShowScale) {
                drawScale(canvas);
            }
        }
    }

    private void updateSizes() {
        if (!isInEditMode()) {
            mMajorTickStepValue = (float) ((mScaleEndValue - mScaleStartValue) * mMajorTickPercentOfRange / 100);
            if (mMajorTickStepValue < 1) {
                mMajorTickStepValue = 1;
            }
            mMajorTicksLength = mScaleRadius * 0.1f;
            mMinorTicksLength = mMajorTicksLength / 2.5f;
            float titleSize = mTitleHeightPercentOfRadius * mFaceRadius / 100;
            mTitleTextPaint.setTextSize(titleSize);
            mTitleTextPaint.setShadowLayer(2, 3, 0, Color.BLACK);
            float readingSize = mReadingHeightPercentOfRadius * mFaceRadius / 100;
            mReadingTextPaint.setTextSize(readingSize);
            mReadingTextPaint.setShadowLayer(2, 3, 0, Color.BLACK);
            mColoredRangePaint.setStrokeWidth(mColoredRangePercentOfRadius * mFaceRadius / 100);

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != mBackground) {
            canvas.drawBitmap(mBackground, 0, 0, mBackgroundPaint);
        }
        if (isInEditMode()) {
            return;
        }
        drawText(canvas);
        drawNeedle(canvas);
        new Thread(new Runnable() {
            @Override
            public void run() {
                computeCurrentValue();
            }
        }).start();
    }

    private void drawNeedle(Canvas canvas) {
        if (mCurrentValue > mScaleEndValue)
            mCurrentValue = mScaleEndValue;
        float minimumValue = mBiDirectional ? -mScaleEndValue : mScaleStartValue;
        if (mCurrentValue < minimumValue)
            mCurrentValue = minimumValue;
        float pos = mScaleStartValue < 0 ? mCurrentValue + Math.abs(mScaleStartValue) : mCurrentValue;
        float availableValues = mScaleEndValue - mScaleStartValue;
        float angle = mBiDirectional ? mScaleStartAngle + mAvailableAngle / 2 + pos * (mAvailableAngle / 2) / availableValues : mScaleStartAngle + (pos * mAvailableAngle / availableValues);
// this does not work on emulator when HW acc is enabled (libc crash)
//        setNeedleShadowPosition(angle);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(angle - 90, mGaugeRect.centerX(), mGaugeRect.centerY());
        canvas.drawPath(mNeedleRightPath, mNeedleRightPaint);
        canvas.drawPath(mNeedleLeftPath, mNeedleLeftPaint);

        canvas.restore();
        canvas.drawCircle(mNeedleScrew.centerX(), mNeedleScrew.centerY(), mNeedleScrew.width() / 2, getDefaultNeedleScrewPaint(mNeedleScrew));
        canvas.drawCircle(mNeedleScrew.centerX(), mNeedleScrew.centerY(), mNeedleScrew.width() / 2, getDefaultNeedleScrewBorderPaint());
    }

    private void drawScale(Canvas canvas) {
        if (mBiDirectional) {
            float top = mScaleStartAngle + mAvailableAngle / 2;
            drawScaleSegment(canvas, top, mScaleEndAngle, true);
            drawScaleSegment(canvas, mScaleStartAngle, top, false);
        } else {
            drawScaleSegment(canvas, mScaleStartAngle, mScaleEndAngle, true);
        }
    }

    private void drawScaleSegment(Canvas canvas, float startAngle, float endAngle, boolean clockwise) {
        float availableAngle = mBiDirectional ? mAvailableAngle / 2 : mAvailableAngle;
        float majorTickStepAngle = mMajorTickStepValue / (mScaleEndValue - mScaleStartValue) * availableAngle;
        float minorTickStepAngle = majorTickStepAngle / (1 + mMinorTicksPerDivision);
        double rads = majorTickStepAngle * Math.PI / 180;
        float maxLabelSize = (float) Math.sin(rads) * mScaleRadius;
        float maxTextSize = mScaleRadius * 0.15f;
        String textValue = mBiDirectional ? "-" + String.valueOf((int) mScaleEndValue) : String.valueOf((int) mScaleEndValue);

        final float testTextSize = 48f;
        // Get the bounds of the text, using our testTextSize.
        mTickLabelTextPaint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        mTickLabelTextPaint.getTextBounds(textValue, 0, textValue.length(), bounds);
        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * maxLabelSize / bounds.width();
        desiredTextSize = desiredTextSize > maxTextSize ? maxTextSize : desiredTextSize;
        // Set the paint for that size.
        mTickLabelTextPaint.setTextSize(desiredTextSize);

        float curProgress = mScaleStartValue;
        float currentAngle = clockwise ? startAngle : endAngle;
        while (currentAngle <= endAngle && currentAngle >= startAngle) {
            canvas.drawLine(
                    (float) (mGaugeRect.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (mScaleRadius - mMajorTicksLength / 2)),
                    (float) (mGaugeRect.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (mScaleRadius - mMajorTicksLength / 2)),
                    (float) (mGaugeRect.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (mScaleRadius + mMajorTicksLength / 2)),
                    (float) (mGaugeRect.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (mScaleRadius + mMajorTicksLength / 2)),
                    mTicksPaint
            );
            for (int i = 1; i <= mMinorTicksPerDivision; i++) {
                float angle = clockwise ? currentAngle + i * minorTickStepAngle : currentAngle - i * minorTickStepAngle;
                if (angle >= endAngle + minorTickStepAngle / 2 || angle <= startAngle - minorTickStepAngle / 2) {
                    break;
                }
                canvas.drawLine(
                        (float) (mGaugeRect.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * mScaleRadius),
                        (float) (mGaugeRect.centerY() - Math.sin(angle / 180 * Math.PI) * mScaleRadius),
                        (float) (mGaugeRect.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (mScaleRadius - mMinorTicksLength)),
                        (float) (mGaugeRect.centerY() - Math.sin(angle / 180 * Math.PI) * (mScaleRadius - mMinorTicksLength)),
                        mTicksPaint
                );
            }
            if (getLabelConverter() != null) {
                canvas.save();
                canvas.rotate(180 + currentAngle, mGaugeRect.centerX(), mGaugeRect.centerY());
                float txtX = mGaugeRect.centerX() + mScaleRadius + (float)(mMajorTicksLength * .8);
                float txtY = mGaugeRect.centerY();
                canvas.rotate(+90, txtX, txtY);
                float shadowAngle = currentAngle - 110;
                float dx = (float) Math.cos(shadowAngle / 180 * Math.PI) * 5f;
                float dy = -(float) Math.sin(shadowAngle / 180 * Math.PI) * 5f;
                mTickLabelTextPaint.setShadowLayer(2, dx, dy, Color.BLACK);
                canvas.drawText(getLabelConverter().getLabelFor(curProgress, mScaleStartValue, mScaleEndValue), txtX, txtY, mTickLabelTextPaint);
                canvas.restore();
            }
            if (clockwise) {
                currentAngle += majorTickStepAngle;
                curProgress += mMajorTickStepValue;
            } else {
                currentAngle -= majorTickStepAngle;
                curProgress -= mMajorTickStepValue;
            }
        }
        mColoredRangePaint.setColor(mDefaultColor);
        canvas.drawArc(mScaleArc, 180 + startAngle, availableAngle, false, mColoredRangePaint);
        if (!mBiDirectional || mRanges.size() != 2) { // hack for now
            for (ColoredRange range : mRanges) {
                mColoredRangePaint.setColor(range.getColor());
                double start = clockwise ? 180 + startAngle + (range.getBegin() * availableAngle / 100) : 180 + endAngle - (range.getEnd() * availableAngle / 100);
                double sweep = (range.getEnd() - range.getBegin()) * availableAngle / 100;
                canvas.drawArc(mScaleArc, (float) start, (float) sweep, false, mColoredRangePaint);
            }
        } else {
            ColoredRange range = mRanges.get(clockwise ? 1 : 0);
            mColoredRangePaint.setColor(range.getColor());
            double start = clockwise ? 180 + startAngle : 180 + endAngle - availableAngle;
            canvas.drawArc(mScaleArc, (float) start, (float) availableAngle, false, mColoredRangePaint);
        }
    }

    private void drawRim(final Canvas canvas) {
        if (mFace == Quadrant.Full) {
            if (mShowOuterShadow) {
                canvas.drawOval(mGaugeRect, getDefaultOuterShadowPaint(mGaugeRect));
            }
            if (mShowRim) {
                canvas.drawOval(mOuterRim, getRimPaint(mOuterRim));
                if (mShowInnerRim) {
                    canvas.drawOval(mInnerRim, mBorderAccentPaint);
                    // Draw the face border (inner rim)
                    canvas.drawCircle(mGaugeRect.centerX(), mGaugeRect.centerY(), mFaceRect.width() / 2 + mInnerRimWidth / 2, mFaceBorderPaint);
                }
            }
        } else if (mFace == Quadrant.Half) {
            if (mShowOuterShadow) {
                canvas.drawArc(mGaugeRect, 180, 180, true, getDefaultOuterShadowPaint(mGaugeRect));
            }
            if (mShowRim) {
                canvas.drawArc(mOuterRim, 180, 180, true, getRimPaint(mOuterRim));
                if (mShowInnerRim) {
                    canvas.drawArc(mInnerRim, 180, 180, true, mBorderAccentPaint);
                    // Draw the face border (inner rim)
                    //canvas.drawArc(mFaceRect, 180, 180, true, mFaceBorderPaint);
                }
            }
        }
        // Todo quarter gauge
    }

    private void drawFace(Canvas canvas) {
        if (mFace == Quadrant.Full) {
            // Draw the face gradient
            canvas.drawOval(mFaceRect, mFacePaint);
            // Draw the inner face shadow
            canvas.drawOval(mFaceRect, mFaceShadowPaint);
        } else if (mFace == Quadrant.Half) {
            canvas.drawArc(mFaceRect, 180, 180, true, mFacePaint);
            // Draw the inner face shadow
            canvas.drawArc(mFaceRect, 180, 180, true, mFaceShadowPaint);
        }
        // Todo quarter gauge
    }

    private void drawText(final Canvas canvas) {
        if (mGaugeTitle != null && mGaugeTitle.length() > 0) {
            float titleX = mFaceRect.centerX() + (mTitlePosition.x * mFaceRadius) / 100;
            float titleY = mFaceRect.centerY() + (mTitlePosition.y * mFaceRadius) / 100;
            canvas.drawText(mGaugeTitle, titleX, titleY, mTitleTextPaint);
        }
        if (mShowReading) {
            String textValue = valueFloatString(mTargetValueNoCaps);
            if (!TextUtils.isEmpty(mReadingUnit)) {
                textValue += " " + mReadingUnit;
            }
            float titleX = mFaceRect.centerX() + (mReadingPosition.x * mFaceRadius) / 100;
            float titleY = mFaceRect.centerY() + (mReadingPosition.y * mFaceRadius) / 100;
            canvas.drawText(textValue, titleX, titleY, mReadingTextPaint);
        }
    }

    private void setNeedleShadowPosition(final float angle) {
        float dx = (float) Math.cos(angle / 180 * Math.PI) * 5f;
        float dy = (float) Math.sin(angle / 180 * Math.PI) * 5f;
        if (dx < 0 || dy < 0) {
            // Move shadow from right to left
            mNeedleRightPaint.clearShadowLayer();
            mNeedleLeftPaint.setShadowLayer(5, dx, dy, Color.BLACK);
//            setLayerType(LAYER_TYPE_SOFTWARE, mNeedleLeftPaint);

        } else {
            // Move shadow from left to right
            mNeedleLeftPaint.clearShadowLayer();
            mNeedleRightPaint.setShadowLayer(5, dx, dy, Color.BLACK);
//            setLayerType(LAYER_TYPE_SOFTWARE, mNeedleRightPaint);
        }
    }

    private RectF getScaledRect(float factor) {
        if (mFace == Quadrant.Full) {
            final float scale = Math.min(mViewWidth, mViewHeight);
            RectF oval = new RectF(0, 0, scale * factor, scale * factor);
            oval.offset((mViewWidth - oval.width()) / 2 + getPaddingLeft(), (mViewHeight - oval.height()) / 2 + getPaddingTop());
            return oval;
        } else if (mFace == Quadrant.Half) {
            final float scale = Math.min(mViewWidth, mViewHeight * 2);
            RectF oval = new RectF(0, 0, scale * factor, scale * factor);
            oval.offset((mViewWidth - oval.width()) / 2 + getPaddingLeft(), (mViewHeight * 2 - oval.height()) / 2 + getPaddingTop());
            return oval;
        } else {
            // todo quarter gauge
            return null;
        }
    }

    private void computeCurrentValue() {
        if (!(Math.abs(mCurrentValue - mTargetValue) > 0.01f)) {
            return;
        }
        if (-1 != mNeedleLastMoved) {
            final float time = (System.currentTimeMillis() - mNeedleLastMoved) / 1000.0f;
            final float direction = Math.signum(mNeedleVelocity);
            if (Math.abs(mNeedleVelocity) < 90.0f) {
                mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            } else {
                mNeedleAcceleration = 0.0f;
            }
            mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            mCurrentValue += mNeedleVelocity * time;
            mNeedleVelocity += mNeedleAcceleration * time;
            if ((mTargetValue - mCurrentValue) * direction < 0.01f * direction) {
                mCurrentValue = mTargetValue;
                mNeedleVelocity = 0.0f;
                mNeedleAcceleration = 0.0f;
                mNeedleLastMoved = -1L;
            } else {
                mNeedleLastMoved = System.currentTimeMillis();
            }
            postInvalidate();
        } else {
            mNeedleLastMoved = System.currentTimeMillis();
            computeCurrentValue();
        }
    }

    private String valueFloatString(final double value) {
        String fmt = String.format("%%.%df", mReadingPrecision);
        return String.format(fmt, value);
    }

    @SuppressWarnings("NewApi")
    protected void initDrawingTools() {
        if (!isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null); // paint.setShadowLayer not supported in HW acc.
        }
        mAvailableAngle = mScaleEndAngle - mScaleStartAngle;
        if (mAvailableAngle > 180) {
            mFace = Quadrant.Full;
            mTitlePosition = new PointF(0, -30); // percent of face radius from center (- indicates location above center)
            mReadingPosition = new PointF(0, 40); // percent of face radius from center
        } else if (mAvailableAngle <= 90) {
            // todo quarter gauge
            mFace = Quadrant.Quarter;
        } else {

            mFace = Quadrant.Half;
            mTitlePosition = new PointF(0, -50); // percent of face radius from center (- indicates location above center)
            mReadingPosition = new PointF(0, -20); // percent of face radius from center
        }


        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.rgb(127, 127, 127));

        mFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mFaceBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFaceBorderPaint.setStyle(Paint.Style.STROKE);
        mFaceBorderPaint.setColor(Color.argb(100, 81, 84, 89));

        mFaceShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBorderAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderAccentPaint.setStyle(Paint.Style.STROKE);
        mBorderAccentPaint.setColor(Color.argb(100, 0, 0, 0));
        mBorderAccentPaint.setStrokeWidth(4);

        mTickLabelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTickLabelTextPaint.setColor(mTickLabelColor);
        mTickLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        mTickLabelTextPaint.setLinearText(true);

        mTitleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitleTextPaint.setColor(mTitleColor);
        mTitleTextPaint.setTextAlign(mFace == Quadrant.Quarter ? Paint.Align.LEFT : Paint.Align.CENTER);

        mReadingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReadingTextPaint.setColor(mReadingColor);
        mReadingTextPaint.setTextAlign(Paint.Align.CENTER);

        mTicksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTicksPaint.setStrokeWidth(3.0f);
        mTicksPaint.setStyle(Paint.Style.STROKE);
        mTicksPaint.setColor(mDefaultColor);

        mColoredRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColoredRangePaint.setStyle(Paint.Style.STROKE);
        mColoredRangePaint.setColor(mDefaultColor);

        mNeedleLeftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNeedleLeftPaint.setColor(Color.rgb(176, 10, 19));
        mNeedleRightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNeedleRightPaint.setColor(Color.rgb(252, 18, 30));
    }

    private Paint getDefaultNeedleScrewPaint(RectF smallOval) {
        if (mNeedleScrewPaint == null) {
            mNeedleScrewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNeedleScrewPaint.setShader(new RadialGradient(smallOval.centerX(), smallOval.centerY(), smallOval.width() / 2, new int[]{Color.rgb(171, 171, 171), Color.WHITE}, new float[]{0.05f, 0.9f}, Shader.TileMode.MIRROR));
        }
        return mNeedleScrewPaint;
    }

    private Paint getDefaultNeedleScrewBorderPaint() {
        if (mNeedleScrewBorderPaint == null) {
            mNeedleScrewBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mNeedleScrewBorderPaint.setStyle(Paint.Style.STROKE);
            mNeedleScrewBorderPaint.setColor(Color.argb(100, 81, 84, 89));
            mNeedleScrewBorderPaint.setStrokeWidth(1.0f);
        }
        return mNeedleScrewBorderPaint;
    }

    public Paint getDefaultOuterShadowPaint(RectF outer) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        float percent = mOuterShadowWidthPercentOfDiameter / 100;
        float[] shadowPositions = {1 - percent * .9f, 1 - percent * .2f, 1};
        paint.setShader(new RadialGradient(outer.centerX(), outer.centerY(), outer.width() / 2.0f, OUTER_SHADOW_COLORS, shadowPositions, Shader.TileMode.MIRROR));
        return paint;
    }

    private Paint getRimPaint(RectF innerRim) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(innerRim.left, innerRim.top, innerRim.left, innerRim.bottom, new int[]{
                Color.argb(255, 68, 73, 80), Color.argb(255, 91, 97, 105), Color.argb(255, 178, 180, 183), Color.argb(255, 188, 188, 190),
                Color.argb(255, 84, 90, 100), Color.argb(255, 137, 137, 137)}, new float[]{0, 0.1f, 0.2f, 0.4f, 0.8f, 1},
                Shader.TileMode.CLAMP));
        return paint;
    }

    protected void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gauge, defStyle, 0);
        mShowOuterShadow = a.getBoolean(R.styleable.Gauge_showOuterShadow, true);
        mShowRim = a.getBoolean(R.styleable.Gauge_showRim, true);
        mShowInnerRim = a.getBoolean(R.styleable.Gauge_showInnerRim, true);
        mBiDirectional = a.getBoolean(R.styleable.Gauge_biDirectional, false);
        mAutoScale = a.getBoolean(R.styleable.Gauge_autoScale, false);
        mShowScale = a.getBoolean(R.styleable.Gauge_showScale, true);
        setScaleStart(a.getInteger(R.styleable.Gauge_scaleStartValue, 0));
        setScaleEnd(a.getInteger(R.styleable.Gauge_scaleEndValue, 100));
        mScaleStartAngle = a.getInteger(R.styleable.Gauge_scaleStartAngle, -70);
        mScaleEndAngle = a.getInteger(R.styleable.Gauge_scaleEndAngle, 250);
        setMajorTickPercentOfRange(a.getFloat(R.styleable.Gauge_majorTickPercentOfRange, 10));
        setMinorTicksPerDivision(a.getInteger(R.styleable.Gauge_minorTicksPerDivision, 4));

        mShowReading = a.getBoolean(R.styleable.Gauge_showReading, false);
        mReadingUnit = a.getString(R.styleable.Gauge_readingUnit);
        mReadingPrecision = a.getInteger(R.styleable.Gauge_readingPrecision, 1);
        mGaugeTitle = a.getString(R.styleable.Gauge_gaugeTitle);

        mTitleColor = a.getColor(R.styleable.Gauge_titleColor, Color.WHITE);
        mReadingColor = a.getColor(R.styleable.Gauge_readingColor, Color.YELLOW);
        mTickLabelColor = a.getColor(R.styleable.Gauge_tickLabelColor, Color.WHITE);

        a.recycle();
    }

    public static class ColoredRange {

        private int color;
        private double begin;
        private double end;

        public ColoredRange(int color, double begin, double end) {
            this.color = color;
            this.begin = begin;
            this.end = end;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public double getBegin() {
            return begin;
        }

        public void setBegin(double begin) {
            this.begin = begin;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }
    }

    public enum Quadrant {
        Full,
        Half,
        Quarter;
    }

}
