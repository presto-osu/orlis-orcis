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
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.farrelltonsolar.uicomponents.LabelAdapter;
import ca.farrelltonsolar.uicomponents.ValueLabelAdapter;


/**
 * Created by Graham on 19/12/2014.
 */
public class HourLabelAdapter extends LabelAdapter {
    private Context mContext;
    int hourNow;

    public HourLabelAdapter(Context context, ValueLabelAdapter.LabelOrientation orientation) {
        mContext = context;
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();
        hourNow = now.hour;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView labelTextView;
        if (convertView == null) {
            convertView = new TextView(mContext);
        }

        labelTextView = (TextView) convertView;
        labelTextView.setPadding(2, 0, 2, 0);
        int hour = ((hourNow + position) % 24) +1;
        String suffix = "am";
        if (hour >= 12 && hour < 24) {
            suffix = "pm";
        }
        if (hour >= 13) {
            hour -= 12;
        }
        if (position == 0 || position == getCount() - 1) {
            labelTextView.setText("");
            return convertView;
        }
        labelTextView.setGravity(Gravity.CENTER);
        labelTextView.setText(String.format("%d%s", hour, suffix));
        labelTextView.setTextColor(labelColor());
        return convertView;
    }
}
