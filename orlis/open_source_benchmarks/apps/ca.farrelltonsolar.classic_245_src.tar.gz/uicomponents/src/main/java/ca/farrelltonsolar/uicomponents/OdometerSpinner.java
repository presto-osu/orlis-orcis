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

/**
 * Created by Graham on 17/12/2014.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * OdometerSpinner represents a single digit 'spinner' in an Odometer.
 * It displays digits 0-9 and wraps at 10 (...8-9-0-1...)
 */
public class OdometerSpinner extends View {
    public static final float IDEAL_ASPECT_RATIO = 1.66f;

    private float mWidth;
    private float mHeight;

    private GradientDrawable mBGGrad;

    private float mDigitX;

    private float mDigitY;

    private int mCurrentDigit;
    private int mCurrentRemainderDigit;
    private String mDigitString;
    private Paint mDigitPaint;

    private int mDigitAbove;

    private float mDigitAboveY;

    private String mDigitAboveString;

    /*
     * Simple constructor used when creating a view from code.
     */
    public OdometerSpinner(Context context) {
        super(context);

        initialize();
    }

    /*
     * This is called when a view is being constructed from an XML file,
     * supplying attributes that were specified in the XML file.
     */
    public OdometerSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    /*
     * Perform inflation from XML and apply a class-specific base style.
     * This constructor of View allows subclasses to use their own base
     * style when they are inflating.
     */
    public OdometerSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize();
    }

    /*
     * Initialize all of our class members and variables
     */
    private void initialize() {
        /*
		 *  Setup our background gradient to have a top-to-bottom orientation
		 *  and go from black to a medium gray to black again.
		 *  Colors here are of the form 0xAARRGGBB.
		 *  AA: alpha - 00 is transparent, FF is opaque
		 */
        mBGGrad = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF000000, 0xFFAAAAAA, 0xFF000000});

		/*
		 * The Paint used to draw the digit string. We set it to be
		 * anti-aliased, white and centered horizontally.
		 */
        mDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDigitPaint.setColor(Color.WHITE);
        mDigitPaint.setTextAlign(Paint.Align.CENTER);

        setCurrentDigit(0, 0);
    }

    public int getCurrentDigit() {
        return mCurrentDigit;
    }

    public void setCurrentDigit(int digit, int remainder) {
        int singleDigitValue = digit;

        if (singleDigitValue < 0)
            singleDigitValue = 0;
        if (singleDigitValue > 9)
            singleDigitValue = 9;

        mCurrentRemainderDigit = remainder;
        mCurrentDigit = singleDigitValue;

        mDigitAbove = mCurrentDigit + 1;

        if (mDigitAbove > 9)
            mDigitAbove = 0;

        mDigitString = String.valueOf(mCurrentDigit);
        mDigitAboveString = String.valueOf(mDigitAbove);

        setDigitYValues();
        invalidate();
    }

    private void setDigitYValues() {
        float postDelta = mHeight * mCurrentRemainderDigit / 10;
        mDigitY = findCenterY(mCurrentDigit);
        mDigitAboveY = findCenterY(mDigitAbove) - mHeight;
        mDigitY += postDelta;
        mDigitAboveY += postDelta;

    }

    private float findCenterY(int digit) {
        String text = String.valueOf(digit);
        Rect bounds = new Rect();
        mDigitPaint.getTextBounds(text, 0, text.length(), bounds);
        int textHeight = Math.abs(bounds.height());
        float result = mHeight - ((mHeight - textHeight) / 2);
        return result;
    }

    /*
     * This is where all of the drawing for the spinner is done.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // if our super has to do any drawing, do that first
        super.onDraw(canvas);

        // draw the background so it is below the digit
        mBGGrad.draw(canvas);

        // draw the digit text using our calculated position and Paint
        canvas.drawText(mDigitString, mDigitX, mDigitY, mDigitPaint);

        canvas.drawText(mDigitAboveString, mDigitX, mDigitAboveY, mDigitPaint);
    }

    /*
     * Measure the view and its content to determine the measured width and
     * the measured height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get width and height size and mode
        int wSpec = MeasureSpec.getSize(widthMeasureSpec);

        int hSpec = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = wSpec;
        int height = hSpec;

        // ideal height for the number display
        int idealHeight = (int) (wSpec * IDEAL_ASPECT_RATIO);

        if (idealHeight < hSpec) {
            height = idealHeight;
        }

        setMeasuredDimension(width, height);
    }

    /*
     * Called whenever the size of our View changes
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        // resize the background gradient
        mBGGrad.setBounds(0, 0, w, h);

        // set the text paint to draw appropriately-sized text
        mDigitPaint.setTextSize(h);

        mDigitX = mWidth / 2;

        setDigitYValues();
    }


}