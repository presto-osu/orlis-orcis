/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.helpers;

import com.afollestad.materialdialogs.MaterialDialog;

import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;
import org.berlin_vegan.bvapp.data.GastroLocationFilter;
import org.berlin_vegan.bvapp.data.Locations;
import org.berlin_vegan.bvapp.data.Preferences;
import org.berlin_vegan.bvapp.views.GastroFilterView;

/**
 * Processes the selection from {@code GastroFilterView}
 */
public class GastroLocationFilterCallback extends MaterialDialog.ButtonCallback {
    private final LocationsOverviewActivity mLocationListActivity;

    public GastroLocationFilterCallback(LocationsOverviewActivity locationListActivity) {
        mLocationListActivity = locationListActivity;
    }

    @Override
    public void onPositive(MaterialDialog dialog) {
        final GastroFilterView filterView = (GastroFilterView) dialog.getCustomView();
        final Locations locations = mLocationListActivity.getLocations();
        if (filterView != null) {
            final GastroLocationFilter filter = filterView.getCurrentFilter();
            locations.showFiltersResult(filter);
            Preferences.saveGastroFilter(mLocationListActivity, filter);
        }
    }
}
