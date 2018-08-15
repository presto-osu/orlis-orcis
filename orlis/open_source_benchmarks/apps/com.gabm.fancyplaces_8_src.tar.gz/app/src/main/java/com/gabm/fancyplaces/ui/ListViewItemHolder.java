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

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gabm.fancyplaces.R;

/**
 * Created by gabm on 20/06/15.
 */
public class ListViewItemHolder {
    public ImageView thumbnailView;
    public TextView titleTextView;
    public LinearLayout backgroundLayoutView;
    public ListViewItemHolder(View v) {
        thumbnailView = (ImageView) v.findViewById(R.id.li_fp_thumbnail);
        titleTextView = (TextView) v.findViewById(R.id.li_fp_title);
        backgroundLayoutView = (LinearLayout) v.findViewById(R.id.li_background);
    }
}