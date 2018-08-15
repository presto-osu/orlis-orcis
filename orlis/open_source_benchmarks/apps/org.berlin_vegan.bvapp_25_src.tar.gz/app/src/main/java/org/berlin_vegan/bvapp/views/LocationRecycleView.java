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


package org.berlin_vegan.bvapp.views;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import org.berlin_vegan.bvapp.data.Locations;

public class LocationRecycleView extends RecyclerView {


    private View mEmptySearch;
    private View mEmptyFavorite;

    private Locations mLocations;
    final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            checkIfEmpty();
        }
    };


    public LocationRecycleView(Context context) {
        super(context);
    }


    public LocationRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLocations(Locations locations) {
        mLocations = locations;
    }

    void checkIfEmpty() {
        final boolean empty = getAdapter().getItemCount() == 0;
        setVisibility(GONE);
        if (mEmptySearch != null) {
            mEmptySearch.setVisibility(GONE);
        }
        if (mEmptyFavorite != null) {
            mEmptyFavorite.setVisibility(GONE);
        }

        if (empty) {
            if (mLocations.getDataType() == Locations.DATA_TYPE.FAVORITE) {
                if (mEmptyFavorite != null) {
                    mEmptyFavorite.setVisibility(VISIBLE);
                }
            } else if (mLocations.getSearchState()) {
                if (mEmptySearch != null) {
                    mEmptySearch.setVisibility(VISIBLE);
                }
            }
        } else {
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    public void setEmptyViews(View emptyFavorite, View emptySearch) {
        this.mEmptyFavorite = emptyFavorite;
        this.mEmptySearch = emptySearch;
    }


}
