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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import ca.farrelltonsolar.uicomponents.BaseGauge;

public class SystemFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.SystemTabTitle;
    private boolean isSlaveReceiverRegistered = false;
    Map<String, Float> slaveControllerPower = new HashMap<String, Float>();
    Map<String, Float> slaveControllerWhizbangJr = new HashMap<String, Float>();

    public SystemFragment() {
        super(R.layout.fragment_system);
    }

    public void setReadings(Readings readings) {
        try {
            float slavePowerSum = 0.0f;
            for (float f : slaveControllerPower.values()) {
                slavePowerSum += f;
            }
            float totalPowerIntake = readings.getFloat(RegisterName.Power) + slavePowerSum;
            View v = this.getView().findViewById(R.id.Load);
            if (v != null) {
                BaseGauge gaugeView = (BaseGauge) v;
                float whizbangPower = 0.0f;
                float batteryVolts = readings.getFloat(RegisterName.BatVoltage);
                if (readings.getReadings().containsKey(RegisterName.WhizbangBatCurrent.name())) {
                    whizbangPower = readings.getFloat(RegisterName.WhizbangBatCurrent) * batteryVolts;
                }
                for (float f : slaveControllerWhizbangJr.values()) {
                    whizbangPower += f;
                }
                float consumption = totalPowerIntake - whizbangPower;
                gaugeView.setTargetValue(consumption);
            }
            v = this.getView().findViewById(R.id.Power);
            if (v != null) {
                BaseGauge gaugeView = (BaseGauge) v;
                gaugeView.setTargetValue(totalPowerIntake);
            }
        } catch (Exception ignore) {

        }
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        View v = this.getView().findViewById(R.id.Load);
        if (v != null) {

            BaseGauge gaugeView = (BaseGauge) v;
            if (gaugeView != null) {
                setupGauge(gaugeView);
            }
        }
        v = this.getView().findViewById(R.id.Power);
        if (v != null) {
            BaseGauge gaugeView = (BaseGauge) v;
            gaugeView.setGreenRange(10, 100);
            gaugeView.setTargetValue(0.0f);
        }
        slaveControllerPower.clear();
        slaveControllerWhizbangJr.clear();
    }

    private void setupGauge(BaseGauge gaugeView) {
        gaugeView.setUnit("W");
        gaugeView.setGreenRange(10, 100);
        gaugeView.setTargetValue(0.0f);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isSlaveReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mSlaveReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isSlaveReceiverRegistered = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isSlaveReceiverRegistered) {
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mSlaveReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS_SLAVE));
            isSlaveReceiverRegistered = true;
        }
    }

    // Our handler for received Intents.
    protected BroadcastReceiver mSlaveReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle bundle = intent.getBundleExtra("readings");
            String uniqueId = intent.getStringExtra("uniqueId");
            if (bundle.containsKey(RegisterName.WhizbangBatCurrent.name())) {
                float wbCurrent = bundle.getFloat(RegisterName.WhizbangBatCurrent.name(), 0);
                float slaveVoltage = bundle.getFloat(RegisterName.BatVoltage.name(), 0);
                slaveControllerWhizbangJr.put(uniqueId, wbCurrent * slaveVoltage);
            }
            slaveControllerPower.put(uniqueId, bundle.getFloat(RegisterName.Power.name(), 0));
        }
    };
}
