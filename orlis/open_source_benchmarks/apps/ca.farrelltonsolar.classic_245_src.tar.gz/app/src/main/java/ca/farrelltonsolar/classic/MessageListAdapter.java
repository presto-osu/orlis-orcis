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
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Graham on 26/12/2014.
 */
public class MessageListAdapter extends ArrayAdapter<Pair<Severity, String>> {

    public MessageListAdapter(Context context) {
        super(context, R.layout.message_item);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            view = inflater.inflate(R.layout.message_item, parent, false);
        } else {
            view = convertView;
        }
        TextView value = (TextView) view.findViewById(R.id.message_text);
        ImageView icon = (ImageView) view.findViewById(R.id.severity_icon);
        Pair<Severity, String> item = getItem(position);
        if (item != null) {

            switch (item.first) {
                case info:
                    icon.setImageResource(R.drawable.information);
                    break;
                case warning:
                    icon.setImageResource(R.drawable.warning);
                    break;
                case alert:
                    icon.setImageResource(R.drawable.alert);
                    break;
            }

            value.setText(item.second);
        }
        return view;
    }
}
