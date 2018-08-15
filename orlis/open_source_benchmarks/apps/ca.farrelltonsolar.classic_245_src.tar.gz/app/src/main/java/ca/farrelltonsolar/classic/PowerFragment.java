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

package ca.farrelltonsolar.classic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.SolarGauge;


/**
 * This fragment displays the power gauges
 */
public class PowerFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.PowerTabTitle;

    public PowerFragment() {
        super(R.layout.fragment_power);
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        View v = view.findViewById(R.id.Power);
        if (v != null) {
            ((BaseGauge) v).setTargetValue(0.0f);
            ((BaseGauge) v).setGreenRange(10, 100);
        }
        v = view.findViewById(R.id.PVVoltage);
        if (v != null) {
            ((BaseGauge) v).setGreenRange(25.0, 75.0);
            ((BaseGauge) v).setTargetValue(0.0f);
        }
        v = view.findViewById(R.id.PVCurrent);
        if (v != null) {
            ((BaseGauge) v).setTargetValue(0.0f);
        }
        v = view.findViewById(R.id.BatVoltage);
        if (v != null) {
            ((BaseGauge) v).setGreenRange(55.0, 72.5);
            ((BaseGauge) v).setTargetValue(0.0f);
        }
        v = view.findViewById(R.id.BatCurrent);
        if (v != null) {
            ((BaseGauge) v).setTargetValue(0.0f);
        }

    }

    public void setReadings(Readings readings) {
        try {
            View v = this.getView().findViewById(R.id.Power);
            if (v != null) {
                ((SolarGauge) v).setTargetValue(readings.getFloat(RegisterName.Power));
                ((SolarGauge) v).setLeftLed(readings.getBoolean(RegisterName.Aux1));
                ((SolarGauge) v).setRightLed(readings.getBoolean(RegisterName.Aux2));
            }
            v = this.getView().findViewById(R.id.PVVoltage);
            if (v != null) {
                ((BaseGauge) v).setTargetValue(readings.getFloat(RegisterName.PVVoltage));
            }
            v = this.getView().findViewById(R.id.PVCurrent);
            if (v != null) {
                ((BaseGauge) v).setTargetValue(readings.getFloat(RegisterName.PVCurrent));
            }
            v = this.getView().findViewById(R.id.BatVoltage);
            if (v != null) {
                float bVolts = readings.getFloat(RegisterName.BatVoltage);
                if (bVolts > 125) { // 120 volt system!
                    ((BaseGauge) v).setScaleEnd(200);
                    ((BaseGauge) v).setTargetValue(bVolts);
                } else { // 12, 24, 48, 96
                    ((BaseGauge) v).setTargetValue(bVolts);
                }
            }
            v = this.getView().findViewById(R.id.BatCurrent);
            if (v != null) {
                float batAmps = readings.getFloat(RegisterName.BatCurrent);
                ((BaseGauge) v).setTargetValue(batAmps);
            }
        } catch (Exception ignore) {

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
