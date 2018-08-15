package com.easwareapps.transparentwidget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * ॐ
 * लोकाः समस्ताः सुखिनो भवन्तु॥
 * <p/>
 * EmptyWidget
 * Copyright (C) 2016  vishnu
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class AppListAdapter extends BaseAdapter {

    List appsList;
    Context context;
    PackageManager packageManager;
    public AppListAdapter(Context context, List pkgAppsList) {
        this.context = context;
        appsList = pkgAppsList;
        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return appsList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View tv =convertView;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            tv = inflater.inflate(R.layout.app, parent, false);
        }
        ResolveInfo packageInfo = (ResolveInfo) appsList.get(position);
        String appName = packageInfo.loadLabel(packageManager).toString();
        TextView app = (TextView) tv.findViewById(R.id.name);
        ImageView icon = (ImageView) tv.findViewById(R.id.icon);
        app.setText(appName);
        Drawable d = packageInfo.loadIcon(packageManager);
        icon.setImageDrawable(d);
        return tv;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
}
