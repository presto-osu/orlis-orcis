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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.TemperatureGauge;

/**
 * Created by Graham on 14/12/2014.
 */
public class TemperatureFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.TemperatureTabTitle;
    private boolean useFahrenheit = false;
    private boolean showShuntTemperature = false;
    private boolean isTriStar = false;
    
    public TemperatureFragment() {
        super(R.layout.fragment_temperature);
        ChargeController controller = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (controller != null) {
            showShuntTemperature = controller != null && controller.hasWhizbang();
            isTriStar = controller.deviceType() ==  DeviceType.TriStar;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (showShuntTemperature) {
            layoutId = R.layout.fragment_temperature_shunt;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void initializeReadings(View view, Bundle savedInstanceState) {
        useFahrenheit = MonitorApplication.chargeControllers().useFahrenheit();
        SetScale();
    }

    private void SetScale() {
        TemperatureGauge gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.BatTemperature);
        gaugeView.setFahrenheit(useFahrenheit);
        gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.FETTemperature);
        gaugeView.setFahrenheit(useFahrenheit);
        gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.PCBTemperature);
        if (isTriStar) {
            gaugeView.setVisibility(gaugeView.INVISIBLE);
        }
        gaugeView.setFahrenheit(useFahrenheit);
        if (showShuntTemperature) {
            gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.ShuntTemp);
            gaugeView.setFahrenheit(useFahrenheit);
        }
    }

    @Override
    public void setReadings(Readings reading) {
        try {
            BaseGauge gaugeView = (BaseGauge) this.getView().findViewById(R.id.BatTemperature);
            float batteryTemp = reading.getFloat(RegisterName.BatTemperature);
            gaugeView.setTargetValue(toSelectedScale(batteryTemp));

            gaugeView = (BaseGauge) this.getView().findViewById(R.id.FETTemperature);
            float fetTemp = reading.getFloat(RegisterName.FETTemperature);
            gaugeView.setTargetValue(toSelectedScale(fetTemp));

            gaugeView = (BaseGauge) this.getView().findViewById(R.id.PCBTemperature);
            float pcbTemp = reading.getFloat(RegisterName.PCBTemperature);
            gaugeView.setTargetValue(toSelectedScale(pcbTemp));

            if (showShuntTemperature) {
                gaugeView = (BaseGauge) this.getView().findViewById(R.id.ShuntTemp);
                float shuntTemp = reading.getFloat(RegisterName.ShuntTemperature);
                gaugeView.setTargetValue(toSelectedScale(shuntTemp));
            }

        } catch (Exception ignore) {

        }
    }
    
    private float toSelectedScale (float celcius) {
        return useFahrenheit ? celcius * 1.8f + 32 : celcius;
        
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        if (MonitorApplication.chargeControllers().useFahrenheit() != useFahrenheit) { // changed?
            useFahrenheit = MonitorApplication.chargeControllers().useFahrenheit();
            SetScale();
        }
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
