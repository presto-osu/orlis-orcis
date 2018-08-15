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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;

import me.grantland.widget.AutofitHelper;


/**
 * Created by Graham on 21/12/2014.
 */
public class CalendarAdapter extends BaseAdapter {

    // references to our items
    public String[] days;
    float[] powerDays;
    float[] floatDays;
    float[] highPowerDays;
    float[] highTempDays;
    float[] highPVVoltDays;
    float[] hiBatVoltDays;
    private Context context;
    DateTime today;
    private DateTime month;
    private ArrayList<String> items;
    int lastDayOfMonth;
    int firstDayOfFirstWeek;

    public CalendarAdapter(Context c, DateTime monthCalendar) {
        month = monthCalendar;
        today = DateTime.now().withTimeAtStartOfDay();
        context = c;
        this.items = new ArrayList<String>();
        try {
            refreshDays(month);
        }
        catch (Exception ex) {
            Log.w(getClass().getName(), String.format("CalendarAdapter refreshDays failed ex: %s", ex));
        }
    }

    public void setPowerSeries(float[] data) {
        this.powerDays = data;
    }
    public void setFloatSeries(float[] data) {
        this.floatDays = data;
    }
    public void setHighPowerSeries(float[] floatArray) {
        this.highPowerDays = floatArray;
    }
    public void setHighTempSeries(float[] floatArray) {
        this.highTempDays = floatArray;
    }
    public void setHighPVVoltSeries(float[] floatArray) {
        this.highPVVoltDays = floatArray;
    }
    public void setHighBatVoltSeries(float[] floatArray) {
        this.hiBatVoltDays = floatArray;
    }

    public int getCount() {
        return days.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);

        }
        v.setEnabled(false);
        v.setClickable(false);
        v.setFocusable(false);
        TextView dayView = (TextView) v.findViewById(R.id.date);
        dayView.setEnabled(false);
        dayView.setClickable(false);
        dayView.setFocusable(false);
        TextView stateView = (TextView) v.findViewById(R.id.state);
        TextView floatView = (TextView) v.findViewById(R.id.isfloat);
        TextView hiPower = (TextView) v.findViewById(R.id.hiPower);
        TextView hiTemp = (TextView) v.findViewById(R.id.hiTemp);
        TextView hiPVVolt = (TextView) v.findViewById(R.id.hiPVVolt);
        TextView hiBatVolt = (TextView) v.findViewById(R.id.hiBatVolt);

        if (position >= firstDayOfFirstWeek && position <= lastDayOfMonth + firstDayOfFirstWeek) {
            try {

                DateTime cellDate = month.withDayOfMonth(position - firstDayOfFirstWeek + 1);
                if (cellDate.compareTo(today) < 0) {
                    int dif = Days.daysBetween(cellDate, today).getDays() - 1;
                    if (powerDays != null) {
                        if (dif >= 0 && dif < powerDays.length) {
                            String t = String.valueOf(powerDays[dif] / 10.0f) + " kWh";
                            if (floatDays[dif] > 0) {
                                floatView.setText(context.getString(R.string.CalendarFloat));
                            }
                            stateView.setText(t);
                        }
                    }
                    if (highPowerDays != null) {
                        if (dif >= 0 && dif < highPowerDays.length) {
                            int pd = (int)highPowerDays[dif];
                            String t = String.valueOf(pd) + " w";
                            hiPower.setText(t);
                        }
                    }
                    if (highTempDays != null) {
                        if (dif >= 0 && dif < highTempDays.length) {
                            float temp = highTempDays[dif] / 10.0f;
                            if (MonitorApplication.chargeControllers().useFahrenheit()) {
                                temp = temp * 1.8f + 32f;
                            }
                            String t = String.format("%.1f", temp) + "\u00b0";
                            hiTemp.setText(t);
                        }
                    }
                    if (highPVVoltDays != null) {
                        if (dif >= 0 && dif < highPVVoltDays.length) {
                            String t = String.valueOf(highPVVoltDays[dif] / 10.0f) + " v";
                            hiPVVolt.setText(t);
                        }
                    }
                    if (hiBatVoltDays != null) {
                        if (dif >= 0 && dif < hiBatVoltDays.length) {
                            String t = String.valueOf(hiBatVoltDays[dif] / 10.0f) + " v";
                            hiBatVolt.setText(t);
                        }
                    }

                    AutofitHelper.create(stateView);
                    AutofitHelper.create(floatView);
                    AutofitHelper.create(hiPower);
                    AutofitHelper.create(hiTemp);
                    AutofitHelper.create(hiPVVolt);
                    AutofitHelper.create(hiBatVolt);
                }
                else {
                    stateView.setText("");
                    floatView.setText("");
                    hiPower.setText("");
                    hiTemp.setText("");
                    hiPVVolt.setText("");
                    hiBatVolt.setText("");
                }

                dayView.setText(days[position]);
                AutofitHelper.create(dayView);
                // mark current day as focused
                if (cellDate.compareTo(today) == 0) {
                    v.setBackgroundResource(R.drawable.item_background_focused);
                } else {
                    v.setBackgroundResource(R.drawable.list_item_background);
                }
            } catch (Exception ex) {
                stateView.setText("");
                floatView.setText("");
                hiPower.setText("");
                hiTemp.setText("");
                hiPVVolt.setText("");
                hiBatVolt.setText("");
                Log.w(getClass().getName(), "getView exception: " + ex);
            }
        }
        else {
            dayView.setText("");
            stateView.setText("");
            floatView.setText("");
            hiPower.setText("");
            hiTemp.setText("");
            hiPVVolt.setText("");
            hiBatVolt.setText("");
        }
        return v;
    }

    public void refreshDays(DateTime month) {
        this.month = month;
        // clear items
        items.clear();

        lastDayOfMonth = month.dayOfMonth().withMaximumValue().getDayOfMonth();
        firstDayOfFirstWeek = month.getDayOfWeek() % 7;

        // figure size of the array
        if (firstDayOfFirstWeek == 0) {
            days = new String[lastDayOfMonth];
        } else {
            days = new String[lastDayOfMonth + firstDayOfFirstWeek];
        }
        // populate empty days before first real day
        if (firstDayOfFirstWeek > 0) {
            for (int j = 0; j < firstDayOfFirstWeek; j++) {
                days[j] = "";
            }
        }
        // populate days
        int dayNumber = 1;
        for (int i = firstDayOfFirstWeek; i < days.length; i++) {
            days[i] = "" + dayNumber;
            dayNumber++;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        // Return true for clickable, false for not
        return false;
    }
}