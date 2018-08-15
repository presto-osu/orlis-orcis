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

import android.widget.ArrayAdapter;

import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.ui.OsmMarker;

/**
 * Created by gabm on 24/05/15.
 */
public interface IMapHandler {
    void setCamera(double lat, double lng, float zoom);

    void animateCamera(double lat, double lng, int duration);

    void animateCamera(double lat, double lng, float zoom, int duration);

    void clearMarkers();

    void removeMarker(OsmMarker markerToRemove);

    void addMarker(double lat, double lng, String text, boolean showInfoWindow);

    void setCurrentLocationMarker(double lat, double lng, String title);

    void setAdapter(ArrayAdapter<FancyPlace> in_adapter);

}
