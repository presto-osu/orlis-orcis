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

package ca.farrelltonsolar.uicomponents;

import android.graphics.Color;
import android.widget.BaseAdapter;

public abstract class LabelAdapter extends BaseAdapter {

    public int labelColor() {
        return labelColor;
    }

    public void setlabelColor(int mLabelColor) {
        this.labelColor = mLabelColor;
    }

    private int labelColor = Color.BLACK;


    public enum LabelOrientation {
        HORIZONTAL, VERTICAL
    }
	private double[] mValues;

	void setValues(double[] points) {
		mValues = points;
	}

	@Override
	public int getCount() {
		return mValues.length;
	}

	public Double getItem(int position) {
		return mValues[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}