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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RadioGroup;

import java.util.HashMap;
import java.util.Map;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.Odometer;

/**
 * Created by Graham on 14/12/2014.
 */
public class EnergyFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.EnergyTabTitle;
    private boolean isSlaveReceiverRegistered = false;
    Map<String, Float> slaveControllerEnergy = new HashMap<String, Float>();
    Map<String, Float> slaveControllerTotalEnergy = new HashMap<String, Float>();

    public EnergyFragment() {
        super(R.layout.fragment_energy);
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        View v = this.getView().findViewById(R.id.EnergyToday);
        if (v != null) {
            BaseGauge energyTodayGauge = (BaseGauge) v;
            energyTodayGauge.setTargetValue(0.0f);
            energyTodayGauge.setGreenRange(10, 100);
        }
        slaveControllerEnergy.clear();
        slaveControllerTotalEnergy.clear();
        RadioGroup radioGroup = (RadioGroup) this.getView().findViewById(R.id.radio_unit_system);
        if (MonitorApplication.chargeControllers().showSystemView() == false) {
            radioGroup.setVisibility(View.INVISIBLE);
        }
        else {
            radioGroup.setVisibility(View.VISIBLE);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.radio_system:
                            registerReceiver();
                            break;
                        case R.id.radio_unit:
                            unRegisterReceiver();
                            break;
                    }
                    slaveControllerEnergy.clear();
                    slaveControllerTotalEnergy.clear();
                }
            });
            radioGroup.check(R.id.radio_unit);
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

    public void setReadings(Readings readings) {
        try {
            View v = this.getView().findViewById(R.id.EnergyToday);
            if (v != null) {
                BaseGauge energyTodayGauge = (BaseGauge) v;
                float slaveSum = 0.0f;
                for (float f : slaveControllerEnergy.values()) {
                    slaveSum += f;
                }
                float energySum = readings.getFloat(RegisterName.EnergyToday) + slaveSum;
                energyTodayGauge.setTargetValue(energySum);

            }
            v = this.getView().findViewById(R.id.EnergyTotalValue);
            if (v != null) {
                Odometer odometer = (Odometer) v;

                float slaveSum = 0.0f;
                for (float f : slaveControllerTotalEnergy.values()) {
                    slaveSum += f;
                }
                float totalEnergySum = readings.getFloat(RegisterName.TotalEnergy) + slaveSum;

                float val = totalEnergySum * 10;
                int decval = (int) val;
                odometer.setValue(decval);
            }
        } catch (Exception ignore) {

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterReceiver();
    }

    private void unRegisterReceiver() {
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
    }

    private void registerReceiver() {
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
            slaveControllerEnergy.put(uniqueId, bundle.getFloat(RegisterName.EnergyToday.name(), 0));
            slaveControllerTotalEnergy.put(uniqueId, bundle.getFloat(RegisterName.TotalEnergy.name(), 0));
        }
    };
}
