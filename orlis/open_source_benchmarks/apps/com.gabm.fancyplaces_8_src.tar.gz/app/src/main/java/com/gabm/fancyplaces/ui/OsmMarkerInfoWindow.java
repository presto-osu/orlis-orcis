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

package com.gabm.fancyplaces.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gabm.fancyplaces.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by gabm on 25/05/15.
 */
public class OsmMarkerInfoWindow {

    protected View mView;
    protected boolean mIsVisible;
    protected MapView mMapView;



    private TextView titleView = null;

    public OsmMarkerInfoWindow(MapView mapView, OsmMarker parentMarker) {
        this.mMapView = mapView;
        this.mIsVisible = false;

        ViewGroup parent = (ViewGroup) mapView.getParent();
        LayoutInflater inflater = LayoutInflater.from(mapView.getContext());

        this.mView = inflater.inflate(R.layout.marker_fancy_place, parent, false);
        this.mView.setTag(this);


        titleView = (TextView) mView.findViewById(R.id.info_window_title);
        titleView.setOnClickListener(parentMarker);

    }


    public void setTitle(String title) {
        titleView.setText(title);
    }


    public void open(Object object, GeoPoint position, int offsetX, int offsetY) {
        this.close();
        MapView.LayoutParams lp = new MapView.LayoutParams(-2, -2, position, 8, offsetX, offsetY);
        this.mMapView.addView(this.mView, lp);
        this.mIsVisible = true;
    }

    public void close() {
        if(this.mIsVisible) {
            this.mIsVisible = false;
            ((ViewGroup)this.mView.getParent()).removeView(this.mView);
        }

    }

    public boolean isOpen() {
        return this.mIsVisible;
    }

    public static void closeAllInfoWindowsOn(MapView mapView) {
        ArrayList opened = getOpenedInfoWindowsOn(mapView);
        Iterator var3 = opened.iterator();

        while(var3.hasNext()) {
            OsmMarkerInfoWindow infoWindow = (OsmMarkerInfoWindow)var3.next();
            infoWindow.close();
        }

    }

    public static ArrayList<OsmMarkerInfoWindow> getOpenedInfoWindowsOn(MapView mapView) {
        int count = mapView.getChildCount();
        ArrayList<OsmMarkerInfoWindow> opened = new ArrayList<>(count);

        for(int i = 0; i < count; ++i) {
            View child = mapView.getChildAt(i);
            Object tag = child.getTag();
            if(tag != null && tag instanceof OsmMarkerInfoWindow) {
                OsmMarkerInfoWindow infoWindow = (OsmMarkerInfoWindow)tag;
                opened.add(infoWindow);
            }
        }

        return opened;
    }


}
