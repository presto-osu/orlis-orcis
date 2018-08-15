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

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This fragment displays the power gauges
 */
public class RealTimeChartFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.RealTimeChartTabTitle;
    private CustomLineChart mChart;
    private boolean showWhizbangCurrent;

    public RealTimeChartFragment() {
        super(R.layout.real_time_chart);
        ChargeController controller = MonitorApplication.chargeControllers().getCurrentChargeController();
        showWhizbangCurrent = controller != null && controller.hasWhizbang();
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        mChart = (CustomLineChart) view.findViewById(R.id.chart1);
        mChart.setDescription("");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.TRANSPARENT);
        CustomMarkerView mv = new CustomMarkerView(getActivity().getBaseContext(), R.layout.custom_marker_view_layout);
        mChart.setMarkerView(mv);
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        // add empty data
        mChart.setData(data);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextSize(14f);
        leftAxis.setLabelCount(10, false);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            setupDataSet(data);
        }
        // load any recorded data while off this tab
        MonitorActivity ma = (MonitorActivity)getActivity();
        for (RecordEntry r : ma.record) {
            DateTime dt = new DateTime(r.time);
            addEntry(r.volt, r.supplyCurrent, r.batteryCurrent, r.state, dt);
        }
    }

    public void setReadings(Readings readings) {
        addEntry(readings.getFloat(RegisterName.BatVoltage), readings.getFloat(RegisterName.BatCurrent), readings.getFloat(RegisterName.WhizbangBatCurrent), readings.getInt(RegisterName.ChargeState), DateTime.now());
    }

    private boolean validState(int state) {
        boolean rVal = false;
        switch (state) {
            case 0:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 10:
            case 18:
                rVal = true;
                break;
        }
        return rVal;
    }

    private void addEntry(float volt, float supplyCurrent, float batteryCurrent, int chargeState, DateTime date) {
        try {
            if (volt != 0 && validState(chargeState)) { // don't chart disconnects
                LineData data = mChart.getData();
                if (data != null) {
                    ILineDataSet set = data.getDataSetByIndex(0);
                    // set.addEntry(...); // can be called as well
                    if (set == null) {
                        set = setupDataSet(data);
                    }
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
                    float codedState = chargeState;
                    // add a new x-value first
                    data.addXValue(formatter.print(date));
                    data.addEntry(new Entry(volt, set.getEntryCount(), RegisterName.BatVoltage), 0);
                    data.addEntry(new Entry(supplyCurrent, set.getEntryCount(), RegisterName.BatCurrent), 1);
                    data.addEntry(new Entry(codedState / 10, set.getEntryCount(), RegisterName.ChargeState), 2);
                    if (showWhizbangCurrent) {
                        data.addEntry(new Entry(batteryCurrent, set.getEntryCount(), RegisterName.WhizbangBatCurrent), 3);
                    }
                    // let the chart know it's data has changed
                    mChart.notifyDataSetChanged();
                    // move to the latest entry
                    mChart.moveViewToX(data.getXValCount());
                }
            }
        } catch (Exception all) {
            Log.w(getClass().getName(), String.format("setReadings Exception ex: %s", all));
        }
    }

    private LineDataSet createBatVoltsSet() {
        LineDataSet set = new LineDataSet(null, getString(R.string.BatVoltsTitle));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(238,0,35));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(12f);
        set.setDrawValues(false);
        set.setHighlightEnabled(true);
        return set;
    }

    private LineDataSet createSupplyCurrentSet() {
        LineDataSet set = new LineDataSet(null, getString(R.string.SupplyCurrentTitle));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.YELLOW);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setHighlightEnabled(true);
        return set;
    }

    private LineDataSet createBatteryCurrentSet() {
        LineDataSet set = new LineDataSet(null, getString(R.string.BatCurrentTitle));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.CYAN);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setHighlightEnabled(true);
        return set;
    }

    private LineDataSet createStateSet() {
        LineDataSet set = new LineDataSet(null, getString(R.string.StateOfChargeTabTitle));
        set.setColor(Color.BLUE);
        set.setFillAlpha(65);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setLineWidth(10f);
        set.setDrawStepped(true);
        set.setHighlightEnabled(true);
        set.setColors(CustomLineChartRenderer.STATE_COLORS);
        return set;
    }

    private ILineDataSet setupDataSet(LineData data) {
        ILineDataSet set;
        set = createBatVoltsSet();
        data.addDataSet(set);
        set = createSupplyCurrentSet();
        data.addDataSet(set);
        set = createStateSet();
        data.addDataSet(set);
        if (showWhizbangCurrent) {
            set = createBatteryCurrentSet();
            data.addDataSet(set);
        }
        return set;
    }
}
