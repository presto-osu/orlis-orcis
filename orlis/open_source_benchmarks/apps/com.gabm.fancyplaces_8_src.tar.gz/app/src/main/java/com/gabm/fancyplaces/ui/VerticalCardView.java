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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gabm.fancyplaces.R;

/**
 * Created by gabm on 23.12.14.
 */
public class VerticalCardView extends LinearLayout {

    private String title;

    public VerticalCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.com_gabm_fancyplaces_VerticalCardView,
                0, 0);


        try {
            title = a.getString(R.styleable.com_gabm_fancyplaces_VerticalCardView_card_title);
        } finally {
            a.recycle();
        }

        // own settings
        this.setOrientation(VERTICAL);
        this.setPadding(30, 30, 30, 30);

        // text view as title
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 20, 0, 10);
        this.addView(textView);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
        invalidate();
        requestLayout();
    }
}
