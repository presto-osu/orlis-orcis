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

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
public class DayLogChart extends Fragment {
    private static final String ARG_MONTH = "month";
    private boolean isReceiverRegistered;
    ChartView chartView;
    private List<AbstractSeries> mSeries = new ArrayList<AbstractSeries>();

    public static DayLogChart newInstance(int month) {
        DayLogChart fragment = new DayLogChart();
        Bundle args = new Bundle();
        args.putInt(ARG_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.day_logs_chart, container, false);
        // Find the chart view
        chartView = (ChartView) theView.findViewById(R.id.chart_view);
        LabelAdapter left = new ValueLabelAdapter(this.getActivity(), ValueLabelAdapter.LabelOrientation.VERTICAL, "%.1f");
        LabelAdapter bottom = new DayLabelAdapter(this.getActivity(), ValueLabelAdapter.LabelOrientation.HORIZONTAL);
        chartView.setLeftLabelAdapter(left);
        chartView.setBottomLabelAdapter(bottom);
        setHasOptionsMenu(true);
        return theView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(DayLogChart.this.getActivity()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
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
                String[] itemArray = getResources().getStringArray(R.array.day_log_chart_selection);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, itemArray); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setSelection(MonitorApplication.chargeControllers().getCurrentChargeController().getDayLogMenuSelection(), false);
                spinner.setOnItemSelectedListener(new OnItemSelectedListenerWrapper(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        chartView.clearSeries();
                        if (position < mSeries.size()) {
                            MonitorApplication.chargeControllers().getCurrentChargeController().setDayLogMenuSelection(position);
                            chartView.addSeries(mSeries.get(position));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }));
            }
        }
        catch (Exception ex) {
            Log.w(getClass().getName(), String.format("Day Log Chart failed to load setupSpinner %s ex: %s", Thread.currentThread().getName(), ex));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.day_log_chart_menu, menu); // inflate the menu
        MenuItem shareItem = menu.findItem(R.id.day_log_chart_preference);
        setupSpinner(shareItem);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(DayLogChart.this.getActivity()).unregisterReceiver(mReadingsReceiver);
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
        LinearSeries seriesEnergy;
        LinearSeries seriesHighPower;
        LinearSeries seriesHighTemperature;
        LinearSeries seriesHighPvVolts;
        LinearSeries seriesHighBatteryVolts;
        LinearSeries seriesFloatTime;

        @Override
        protected Boolean doInBackground(String... params) {
            try {


                seriesEnergy = getLinearSeries(logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY), 10);
                seriesHighPower = getLinearSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY), 1);
                seriesHighTemperature = getLinearSeriesForTemperature(logs.getFloatArray(Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY));
                seriesHighPvVolts = getLinearSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY), 10);
                seriesHighBatteryVolts = getLinearSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY), 10);
                float[] secondsInFloat = logs.getFloatArray(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY);

                for (int i = 0; i < secondsInFloat.length; i++) {
                    secondsInFloat[i] = secondsInFloat[i] / 3600; // convert to hour
                }
                seriesFloatTime = getLinearSeries(logs.getFloatArray(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY), 1);
                Log.d(getClass().getName(), String.format("Chart doInBackground completed %s", Thread.currentThread().getName()));
                return true;
            }
            catch (Exception ex) {
                Log.w(getClass().getName(), String.format("Hour Log Chart failed to load logs in doInBackground %s ex: %s", Thread.currentThread().getName(), ex));
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean resultOk) {
            if (resultOk) {
                mSeries.add(seriesEnergy);
                mSeries.add(seriesHighPower);
                mSeries.add(seriesHighTemperature);
                mSeries.add(seriesHighPvVolts);
                mSeries.add(seriesHighBatteryVolts);
                mSeries.add(seriesFloatTime);
                ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController(); // cc got removed?
                if (cc != null) {
                    int currentSelection = cc.getDayLogMenuSelection();
                    if (currentSelection >= mSeries.size()) {
                        currentSelection = 0;
                        cc.setDayLogMenuSelection(0);
                    }
                    chartView.addSeries(mSeries.get(currentSelection));
                }
                Log.d(getClass().getName(), String.format("Chart onPostExecute completed %s", Thread.currentThread().getName()));
            }
            else {
                mSeries.add(getLinearSeries(null, 0)); // just load test patterns if no logs are available or read failed
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private LinearSeries getLinearSeries(float[] yAxis, float factor) {
        // Create the data points
        LinearSeries series = new LinearSeries();
        series.setLineColor(Color.YELLOW);
        series.setLineWidth(4);
        if (yAxis != null && yAxis.length >= 24) {
            for (int i = 0; i < 24; i++) {
                series.addPoint(new LinearSeries.LinearPoint(23 - i, yAxis[i] / factor));
            }
        }
        else { // default to test pattern
            for (double i = 0d; i <= (2d * Math.PI); i += 0.1d) {
                series.addPoint(new LinearSeries.LinearPoint(i, Math.sin(i))); // test pattern
            }
        }
        return series;
    }

    private LinearSeries getLinearSeriesForTemperature(float[] yAxis) {
        // Create the data points
        LinearSeries series = new LinearSeries();
        series.setLineColor(Color.YELLOW);
        series.setLineWidth(4);
        if (yAxis != null && yAxis.length >= 24) {
            if (MonitorApplication.chargeControllers().useFahrenheit()) {
                for (int i = 0; i < 24; i++) {
                    double v = (yAxis[i] / 10) * 1.8 + 32.0;
                    series.addPoint(new LinearSeries.LinearPoint(23 - i, v));
                }
            }
            else {
                for (int i = 0; i < 24; i++) {
                    series.addPoint(new LinearSeries.LinearPoint(23 - i, yAxis[i] / 10.0));
                }
            }
        }
        else { // default to test pattern
            for (double i = 0d; i <= (2d * Math.PI); i += 0.1d) {
                series.addPoint(new LinearSeries.LinearPoint(i, Math.sin(i))); // test pattern
            }
        }
        return series;
    }

}
