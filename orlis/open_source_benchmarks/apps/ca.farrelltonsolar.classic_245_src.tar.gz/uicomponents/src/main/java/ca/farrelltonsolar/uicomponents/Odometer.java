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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TableLayout;

/**
 * Created by Graham on 17/12/2014.
 */
public class Odometer extends TableLayout {
    private static final int NUM_DIGITS = 6;

    private int mCurrentValue;

    private OdometerSpinner[] mDigitSpinners;

    public Odometer(Context context) {
        super(context);
        initialize();
    }

    public Odometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        mDigitSpinners = new OdometerSpinner[NUM_DIGITS];

        // Inflate the view from the layout resource.
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService(infService);
        li.inflate(R.layout.widget_odometer, this, true);

        mDigitSpinners[0] = (OdometerSpinner) findViewById(R.id.widget_odometer_spinner_1);
        mDigitSpinners[1] = (OdometerSpinner) findViewById(R.id.widget_odometer_spinner_10);
        mDigitSpinners[2] = (OdometerSpinner) findViewById(R.id.widget_odometer_spinner_100);
        mDigitSpinners[3] = (OdometerSpinner) findViewById(R.id.widget_odometer_spinner_1k);
        mDigitSpinners[4] = (OdometerSpinner) findViewById(R.id.widget_odometer_spinner_10k);
        mDigitSpinners[5] = (OdometerSpinner) findViewById(R.id.widget_odometer_spinner_100k);

    }

    public void setValue(int value) {
        int remainder = value % 10;
        mCurrentValue = value / 10;
        int tempValue = mCurrentValue;

        for (int i = 5; i > 0; --i) {

            int factor = (int) Math.pow(10, i);
//            int nextFactor = (int)Math.pow(10, i-1);

            int digitVal = (int) Math.floor(tempValue / factor);
            tempValue -= (digitVal * factor);
            if (factor - tempValue == 1) {
                mDigitSpinners[i].setCurrentDigit(digitVal, remainder);
            } else {
                mDigitSpinners[i].setCurrentDigit(digitVal, 0);
            }
        }
        mDigitSpinners[0].setCurrentDigit(tempValue, remainder);
    }

    public float getValue() {
        return mCurrentValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get width and height size and mode
        int wSpec = MeasureSpec.getSize(widthMeasureSpec);

        int hSpec = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        // calculate max height from width
        float contentHeight = ((float) wSpec / NUM_DIGITS)
                * OdometerSpinner.IDEAL_ASPECT_RATIO;

        int maxHeight = (int) Math.ceil(contentHeight);

        int width = wSpec;
        int height = hSpec;

        if (maxHeight < hSpec) {
            height = maxHeight;
        }

        setMeasuredDimension(width, height);
    }
}
