/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.functional;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.ui.ListViewItem;
import com.gabm.fancyplaces.ui.ListViewItemHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gabm on 15/05/15.
 */
public class FancyPlacesArrayAdapter extends ArrayAdapter<FancyPlace> implements IOnListModeChangeListener {

    private HashMap<FancyPlace, ListViewItem> listViewItems = new HashMap<>();

    public FancyPlacesArrayAdapter(Context context, int resourceId, List<FancyPlace> items) {
        super(context, resourceId, items);
    }

    @Override
    public void remove(FancyPlace object) {
        listViewItems.remove(object);
        super.remove(object);
    }

    public List<FancyPlace> getSelectedFancyPlaces() {
        ArrayList<FancyPlace> resultList = new ArrayList<>();

        for (Map.Entry<FancyPlace, ListViewItem> entry : listViewItems.entrySet()) {
            if (entry.getValue().isSelected())
                resultList.add(entry.getKey());
        }

        return resultList;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        FancyPlace fancyPlace = getItem(position);

        LayoutInflater mInflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ListViewItemHolder holder = null;
        ListViewItem item = null;

        // if holder exists: re-use!
        if (convertView != null) {
            holder = (ListViewItemHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.list_item_fancy_place, null);
            holder = new ListViewItemHolder(convertView);
            convertView.setTag(holder);
        }

        // if listviewitem exists, re-use!
        if (listViewItems.containsKey(fancyPlace)) {
            item = listViewItems.get(fancyPlace);
            item.setHolder(holder);
        } else {
            item = new ListViewItem(getContext(), holder);
            listViewItems.put(fancyPlace, item);
        }

        item.setFancyPlace(fancyPlace);

        return convertView;
    }

    public void toggleSelected(int i)
    {
        listViewItems.get(getItem(i)).toggleAndAnimateSelected();
    }

    @Override
    public void onListModeChange(int newMode) {
        if (newMode == MODE_NORMAL)
        {
            for (Map.Entry<FancyPlace, ListViewItem> entry : listViewItems.entrySet()) {
                entry.getValue().setSelectable(false);
            }

        } else if (newMode == MODE_MULTI_SELECT)
        {
            for (Map.Entry<FancyPlace, ListViewItem> entry : listViewItems.entrySet()) {
                entry.getValue().setSelectable(true);
            }
        }
    }
}