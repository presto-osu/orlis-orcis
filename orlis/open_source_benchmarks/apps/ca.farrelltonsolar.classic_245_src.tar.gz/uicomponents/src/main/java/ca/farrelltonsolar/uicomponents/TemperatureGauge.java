/*
 * Copyright (c) 2014. FarrelltonSolar
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import android.util.AttributeSet;

/**
 * Created by Graham on 11/01/2015.
 */
public class TemperatureGauge extends BaseGauge {

    private boolean fahrenheit;
    
    public TemperatureGauge(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public TemperatureGauge(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TemperatureGauge(final Context context) {
        this(context, null, 0);

    }

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(boolean fahrenheit) {
        this.fahrenheit = fahrenheit;
        this.setScaleEnd(fahrenheit ? 212 : 100);
        setUnit(fahrenheit ? "\u2109" : "\u2103");
    }

    protected void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        super.readAttrs(context, attrs, defStyle);
        this.mReadingPrecision = 1;
        this.setMajorTickPercentOfRange(7.1428);
        this.setScaleStart(-40);
        this.setGreenRange(28.6, 71.4);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gauge, defStyle, 0);
        setFahrenheit(a.getBoolean(R.styleable.Gauge_fahrenheit, false));
        a.recycle();

    }
}
