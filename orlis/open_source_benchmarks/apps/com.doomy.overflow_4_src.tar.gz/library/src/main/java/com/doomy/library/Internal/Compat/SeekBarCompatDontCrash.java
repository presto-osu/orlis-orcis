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

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.widget.TextView;

import com.doomy.library.Internal.Drawable.MarkerDrawable;

/**
 * Wrapper compatibility class to call some API-Specific methods
 * And offer alternate procedures when possible
 *
 * @hide
 */
@TargetApi(21)
class SeekBarCompatDontCrash {
    public static void setOutlineProvider(View marker, final MarkerDrawable markerDrawable) {
        marker.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setConvexPath(markerDrawable.getPath());
            }
        });
    }

    public static Drawable getRipple(ColorStateList colorStateList) {
        return new RippleDrawable(colorStateList, null, null);
    }

    public static void setBackground(View view, Drawable background) {
        view.setBackground(background);
    }

    public static void setTextDirection(TextView number, int textDirection) {
        number.setTextDirection(textDirection);
    }

    public static boolean isInScrollingContainer(ViewParent p) {
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    public static boolean isHardwareAccelerated(View view) {
        return view.isHardwareAccelerated();
    }
}
