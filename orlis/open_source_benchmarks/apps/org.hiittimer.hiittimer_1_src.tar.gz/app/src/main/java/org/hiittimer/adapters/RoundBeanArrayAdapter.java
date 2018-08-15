/*
 * 
 * HIIT Timer - A simple timer for high intensity trainings
 Copyright (C) 2015 Lorenzo Chiovini

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.hiittimer.adapters;

import java.util.List;

import org.hiittimer.beans.RoundBean;
import org.hiittimer.hiittimer.R;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public final class RoundBeanArrayAdapter extends ArrayAdapter<RoundBean> {

	public RoundBeanArrayAdapter(Context context, List<RoundBean> rounds) {
		super(context, R.layout.round, rounds);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final RoundBean round = getItem(position);
		ViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			convertView = inflater.inflate(R.layout.round, parent, false);

			viewHolder.roundNumber = (TextView) convertView.findViewById(R.id.textViewRoundNumber);
			viewHolder.roundWorkInSeconds = (TextView) convertView.findViewById(R.id.textViewRoundWorkInSeconds);
			viewHolder.roundRestInSeconds = (TextView) convertView.findViewById(R.id.textViewRoundRestInSeconds);

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Resources resources = convertView.getResources();

		viewHolder.roundNumber.setText(new StringBuilder().append(resources.getString(R.string.round_number))
				.append(":").append(round.getNumber()).toString());
		viewHolder.roundWorkInSeconds.setText(new StringBuilder()
				.append(resources.getString(R.string.round_work_in_seconds)).append(":")
				.append(round.getWorkInSeconds()));
		viewHolder.roundRestInSeconds.setText(new StringBuilder()
				.append(resources.getString(R.string.round_rest_in_seconds)).append(":")
				.append(round.getRestInSeconds()));

		return convertView;

	}

	private static class ViewHolder {
		TextView roundNumber, roundWorkInSeconds, roundRestInSeconds;

	}
}
