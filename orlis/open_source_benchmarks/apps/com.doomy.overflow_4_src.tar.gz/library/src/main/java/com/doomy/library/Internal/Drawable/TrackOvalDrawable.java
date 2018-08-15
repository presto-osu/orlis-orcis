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

package com.doomy.library.Internal.Drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Simple {@link com.doomy.library.Internal.Drawable.StateDrawable} implementation
 * to draw circles/ovals
 *
 * @hide
 */
public class TrackOvalDrawable extends StateDrawable {
    private RectF mRectF = new RectF();

    public TrackOvalDrawable(@NonNull ColorStateList tintStateList) {
        super(tintStateList);
    }

    @Override
    void doDraw(Canvas canvas, Paint paint) {
        mRectF.set(getBounds());
        canvas.drawOval(mRectF, paint);
    }

}
