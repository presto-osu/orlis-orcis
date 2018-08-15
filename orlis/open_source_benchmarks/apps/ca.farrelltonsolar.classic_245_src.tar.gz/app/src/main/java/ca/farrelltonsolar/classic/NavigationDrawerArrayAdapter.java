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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Graham on 24/12/2014.
 */
public class NavigationDrawerArrayAdapter extends ArrayAdapter<ChargeControllerInfo> {

    public NavigationDrawerArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public NavigationDrawerArrayAdapter(Context themedContext, int drawer_list_item_activated, int deviceName) {
        super(themedContext, drawer_list_item_activated, deviceName);
    }

    public NavigationDrawerArrayAdapter(Context context, int resource, ChargeControllerInfo[] objects) {
        super(context, resource, objects);
    }

    public NavigationDrawerArrayAdapter(Context context, int resource, int textViewResourceId, ChargeControllerInfo[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public NavigationDrawerArrayAdapter(Context context, int resource, List<ChargeControllerInfo> objects) {
        super(context, resource, objects);
    }

    public NavigationDrawerArrayAdapter(Context context, int resource, int textViewResourceId, List<ChargeControllerInfo> objects) {
        super(context, resource, textViewResourceId, objects);
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return getItem(position).isReachable();
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ImageView noConnection = (ImageView) view.findViewById(R.id.NoConnection);
        ChargeControllerInfo cc = getItem(position);
        noConnection.setVisibility(cc.isReachable() ? View.INVISIBLE : View.VISIBLE);
        return view;
    }

}
