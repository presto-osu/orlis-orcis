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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import ca.farrelltonsolar.uicomponents.LabelAdapter;


/**
 * Created by Graham on 19/12/2014.
 */
public class DayLabelAdapter extends LabelAdapter {
    private Context mContext;
    DateTime today;

    public DayLabelAdapter(Context context, LabelOrientation orientation) {
        mContext = context;
        today = DateTime.now().withTimeAtStartOfDay();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.day_label_item, parent, false);
        convertView.setPadding(2, 0, 2, 0);
        TextView date = (TextView) convertView.findViewById(R.id.dayLabel_date);
        TextView day = (TextView) convertView.findViewById(R.id.dayLabel_day);
        if (position == 0) {
            date.setText("");
            day.setText("");
            return convertView;
        }
        DateTime labelDate;
        try {
            labelDate = today.minusDays(getCount() - position);
            date.setGravity(Gravity.CENTER);
            String dateStamp = DateTimeFormat.forPattern("dd").print(labelDate);
            date.setText(dateStamp);
            date.setTextColor(labelColor());
            day.setGravity(Gravity.CENTER);
            dateStamp = DateTimeFormat.forPattern("E").print(labelDate);
            day.setText(dateStamp);
            day.setTextColor(labelColor());
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("today.minusDays failed ex: %s", ex));
        }
        return convertView;
    }
}
