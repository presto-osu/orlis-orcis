/**
 * Copyright (C) 2013 Damien Chazoule
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

package com.doomy.library.Internal.Compat;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.doomy.library.Internal.Drawable.AlmostRippleDrawable;
import com.doomy.library.Internal.Drawable.MarkerDrawable;

/**
 * Wrapper compatibility class to call some API-Specific methods
 * And offer alternate procedures when possible
 *
 * @hide
 */
public class SeekBarCompat {

    /**
     * Sets the custom Outline provider on API>=21.
     * Does nothing on API<21
     *
     * @param view
     * @param markerDrawable
     */
    public static void setOutlineProvider(View view, final MarkerDrawable markerDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SeekBarCompatDontCrash.setOutlineProvider(view, markerDrawable);
        }
    }

    /**
     * Our DiscreteSeekBar implementation uses a circular drawable on API < 21
     * because we don't set it as Background, but draw it ourselves
     *
     * @param colorStateList
     * @return
     */
    public static Drawable getRipple(ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return SeekBarCompatDontCrash.getRipple(colorStateList);
        } else {
            return new AlmostRippleDrawable(colorStateList);
        }
    }

    /**
     * As our DiscreteSeekBar implementation uses a circular drawable on API < 21
     * we want to use the same method to set its bounds as the Ripple's hotspot bounds.
     *
     * @param drawable
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public static void setHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //We don't want the full size rect, Lollipop ripple would be too big
            int size = (right - left) / 8;
            DrawableCompat.setHotspotBounds(drawable, left + size, top + size, right - size, bottom - size);
        } else {
            drawable.setBounds(left, top, right, bottom);
        }
    }

    /**
     * android.support.v4.view.ViewCompat SHOULD include this once and for all!!
     * But it doesn't...
     *
     * @param view
     * @param background
     */
    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            SeekBarCompatDontCrash.setBackground(view, background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    /**
     * Sets the TextView text direction attribute when possible
     *
     * @param textView
     * @param textDirection
     * @see android.widget.TextView#setTextDirection(int)
     */
    public static void setTextDirection(TextView textView, int textDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            SeekBarCompatDontCrash.setTextDirection(textView, textDirection);
        }
    }

    public static boolean isInScrollingContainer(ViewParent p) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return SeekBarCompatDontCrash.isInScrollingContainer(p);
        }
        return false;
    }

    public static boolean isHardwareAccelerated(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return SeekBarCompatDontCrash.isHardwareAccelerated(view);
        }
        return false;
    }
}
