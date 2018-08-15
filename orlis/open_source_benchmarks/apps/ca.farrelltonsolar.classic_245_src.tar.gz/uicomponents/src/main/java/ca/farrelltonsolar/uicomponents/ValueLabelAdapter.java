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

package ca.farrelltonsolar.uicomponents;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ValueLabelAdapter extends LabelAdapter {


	private Context mContext;
	private LabelOrientation mOrientation;
    private String format;

	public ValueLabelAdapter(Context context, LabelOrientation orientation, String format) {
		mContext = context;
		mOrientation = orientation;
        if (format == null || format.isEmpty()) {
            format = "%.1f";
        }
        this.format = format;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView labelTextView;
		if (convertView == null) {
			convertView = new TextView(mContext);
		}

		labelTextView = (TextView) convertView;

        if (position == 0) {
            labelTextView.setText("");
            return convertView;
        }
        labelTextView.setGravity(Gravity.CENTER);
		labelTextView.setPadding(2, 0, 2, 0);
		labelTextView.setText(String.format(format, getItem(position)));
        labelTextView.setTextColor(labelColor());
		return convertView;
	}
}
