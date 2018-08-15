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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ca.farrelltonsolar.uicomponents.AbstractSeries;
import ca.farrelltonsolar.uicomponents.ChartView;
import ca.farrelltonsolar.uicomponents.LabelAdapter;
import ca.farrelltonsolar.uicomponents.LinearSeries;
import ca.farrelltonsolar.uicomponents.ValueLabelAdapter;

/**
 * Created by Graham on 19/12/2014.
 */
public class HourLogChart extends Fragment {

    private boolean isReceiverRegistered;
    ChartView chartView;
    private List<AbstractSeries> mSeries = new ArrayList<AbstractSeries>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.hour_logs_chart, container, false);
        // Find the chart view
        chartView = (ChartView) theView.findViewById(R.id.chart_view);
        LabelAdapter left = new ValueLabelAdapter(this.getActivity(), ValueLabelAdapter.LabelOrientation.VERTICAL, "%.1f");
        LabelAdapter bottom = new HourLabelAdapter(this.getActivity(), ValueLabelAdapter.LabelOrientation.HORIZONTAL);
        chartView.setLeftLabelAdapter(left);
        chartView.setBottomLabelAdapter(bottom);
        setHasOptionsMenu(true);
        return theView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(HourLogChart.this.getActivity()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterReceiver();
        Log.d(getClass().getName(), "onStop");
    }

    private void setupSpinner(MenuItem item) {
        try {
            item.setVisible(true);
            item.setActionView(R.layout.action_chart_select);
            View view = MenuItemCompat.getActionView(item);
            if (view instanceof Spinner) {
                Spinner spinner = (Spinner) view;
                String[] itemArray = getResources().getStringArray(R.array.minute_log_chart_selection);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, itemArray); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController(); // cc got removed?
                if (cc != null) {
                    spinner.setSelection(cc.getHourLogMenuSelection(), false);
                    spinner.setOnItemSelectedListener(new OnItemSelectedListenerWrapper(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            chartView.clearSeries();
                            if (position < mSeries.size()) {
                                ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController(); // cc got removed?
                                if (cc != null) {
                                    cc.setHourLogMenuSelection(position);
                                    chartView.addSeries(mSeries.get(position));
                                }
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }));
                }
            }
        }
        catch (Exception ex) {
            Log.w(getClass().getName(), String.format("Hour Log Chart failed to load setupSpinner %s ex: %s", Thread.currentThread().getName(), ex));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.hour_log_chart_menu, menu); // inflate the menu
        MenuItem shareItem = menu.findItem(R.id.hour_log_chart_preference);
        setupSpinner(shareItem);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(HourLogChart.this.getActivity()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LogEntry logs = (LogEntry) intent.getSerializableExtra("logs");
                if (logs != null) {
                    unRegisterReceiver();
                    new ChartLoader(logs).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    Log.d(getClass().getName(), String.format("Hour Log Chart received logs from classic %s", Thread.currentThread().getName()));
                }
            } catch (Exception e) {
                Log.w(getClass().getName(), String.format("Hour Log Chart failed to load logs %s ex: %s", Thread.currentThread().getName(), e));
            }

        }
    };

    private class ChartLoader extends AsyncTask<String, Void, Boolean> {
        private ChartLoader(LogEntry logs) {
            this.logs = logs;
        }

        LogEntry logs;
        LinearSeries seriesPower;
        LinearSeries seriesInputVoltage;
        LinearSeries seriesBatteryVoltage;
        LinearSeries seriesOutputCurrent;
        LinearSeries seriesChargeState;
        LinearSeries seriesEnergy;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                short[] timeStamps = logs.getShortArray(Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY);
                seriesPower = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_POWER_HOURLY_CATEGORY));
                seriesInputVoltage = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY));
                seriesBatteryVoltage = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY));
                seriesOutputCurrent = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY));
                seriesChargeState = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_CHARGE_STATE_HOURLY_CATEGORY));
                seriesEnergy = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_ENERGY_HOURLY_CATEGORY));
                Log.d(getClass().getName(), String.format("Chart doInBackground completed %s", Thread.currentThread().getName()));
                return true;
            } catch (Exception ex) {
                Log.w(getClass().getName(), String.format("Hour Log Chart failed to load logs in doInBackground %s ex: %s", Thread.currentThread().getName(), ex));
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean resultOk) {
            try {
            if (resultOk) {
            mSeries.add(seriesPower);
            mSeries.add(seriesInputVoltage);
            mSeries.add(seriesBatteryVoltage);
            mSeries.add(seriesOutputCurrent);
//            mSeries.add(seriesChargeState);
            mSeries.add(seriesEnergy);
            ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController(); // cc got removed?
            if (cc != null) {
                int currentSelection = cc.getHourLogMenuSelection();
                if (currentSelection >= mSeries.size()) {
                    currentSelection = 0;
                    cc.setHourLogMenuSelection(0);
                }
                chartView.addSeries(mSeries.get(currentSelection));
            }
            Log.d(getClass().getName(), String.format("Chart onPostExecute completed %s", Thread.currentThread().getName()));
            }
            else {
                mSeries.add(getLinearSeries(null, null)); // just load test patterns if no logs are available or read failed
            }
            } catch (Exception ex) {
                Log.w(getClass().getName(), String.format("Hour Log Chart failed to load logs in onPostExecute %s ex: %s", Thread.currentThread().getName(), ex));
            }
        }
    }

    private LinearSeries getLinearSeries(short[] timeStamps, float[] yAxis) {
        // Create the data points
        boolean pointsAdded = false;
        LinearSeries series = new LinearSeries();
        series.setLineColor(Color.YELLOW);
        series.setLineWidth(4);
        if (timeStamps != null && yAxis != null && yAxis.length >= timeStamps.length) {
            short offset = 1440; // 24 hrs ago
            for (int i = 0; i < timeStamps.length; i++) {
                short t = timeStamps[i];
                t = (short) (offset - t);
                series.addPoint(new LinearSeries.LinearPoint(t, yAxis[i]));
                pointsAdded = true;
            }
        }
        if (pointsAdded == false) { // default to test pattern
            for (double i = 0d; i <= (2d * Math.PI); i += 0.1d) {
                series.addPoint(new LinearSeries.LinearPoint(i, Math.sin(i))); // test pattern
            }
        }
        return series;
    }

}
