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

package ca.farrelltonsolar.classic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.Odometer;

/**
 * Created by Graham on 14/12/2014.
 */
public class CapacityFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.CapacityTabTitle;

    public CapacityFragment() {
        super(R.layout.fragment_capacity);
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setReadings(Readings readings) {
        try {
            BaseGauge gaugeView = (BaseGauge) this.getView().findViewById(R.id.AHNetValue);
            int val = readings.getInt(RegisterName.NetAmpHours);
            gaugeView.setTargetValue(val);

            View v = this.getView().findViewById(R.id.RemainingAHValue);
            if (v != null) {
                Odometer odometer = (Odometer) v;
                odometer.setValue(readings.getInt(RegisterName.RemainingAmpHours)* 10);
            }
            v = this.getView().findViewById(R.id.AHMinusValue);
            if (v != null) {
                Odometer odometer = (Odometer) v;
                odometer.setValue(readings.getInt(RegisterName.NegativeAmpHours)* 10);
            }
            v = this.getView().findViewById(R.id.AHPlusValue);
            if (v != null) {
                Odometer odometer = (Odometer) v;
                odometer.setValue(readings.getInt(RegisterName.PositiveAmpHours)* 10);
            }
            v = this.getView().findViewById(R.id.AHTotalValue);
            if (v != null) {
                Odometer odometer = (Odometer) v;
                odometer.setValue(readings.getInt(RegisterName.TotalAmpHours)* 10);
            }

        } catch (Exception ignore) {

        }
    }
}
