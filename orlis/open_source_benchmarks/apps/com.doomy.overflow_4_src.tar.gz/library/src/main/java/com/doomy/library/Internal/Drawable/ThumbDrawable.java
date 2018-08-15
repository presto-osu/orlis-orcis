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
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.os.SystemClock;
import android.support.annotation.NonNull;

/**
 * <h1>HACK</h1>
 * <p>
 * Special {@link com.doomy.library.Internal.Drawable.StateDrawable} implementation
 * to draw the Thumb circle.
 * </p>
 * <p>
 * It's special because it will stop drawing once the state is pressed/focused BUT only after a small delay.
 * </p>
 * <p>
 * This special delay is meant to help avoiding frame glitches while the {@link com.doomy.library.Internal.Marker} is added to the Window
 * </p>
 *
 * @hide
 */
public class ThumbDrawable extends StateDrawable implements Animatable {
    //The current size for this drawable. Must be converted to real DPs
    public static final int DEFAULT_SIZE_DP = 12;
    private final int mSize;
    private boolean mOpen;
    private boolean mRunning;

    public ThumbDrawable(@NonNull ColorStateList tintStateList, int size) {
        super(tintStateList);
        mSize = size;
    }

    @Override
    public int getIntrinsicWidth() {
        return mSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return mSize;
    }

    @Override
    public void doDraw(Canvas canvas, Paint paint) {
        if (!mOpen) {
            Rect bounds = getBounds();
            float radius = (mSize / 2);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
        }
    }

    public void animateToPressed() {
        scheduleSelf(opener, SystemClock.uptimeMillis() + 100);
        mRunning = true;
    }

    public void animateToNormal() {
        mOpen = false;
        mRunning = false;
        unscheduleSelf(opener);
        invalidateSelf();
    }

    private Runnable opener = new Runnable() {
        @Override
        public void run() {
            mOpen = true;
            invalidateSelf();
            mRunning = false;
        }
    };

    @Override
    public void start() {
        //NOOP
    }

    @Override
    public void stop() {
        animateToNormal();
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }
}
