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
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * A drawable that changes it's Paint color depending on the Drawable State
 * <p>
 * Subclasses should implement {@link #doDraw(android.graphics.Canvas, android.graphics.Paint)}
 * </p>
 *
 * @hide
 */
public abstract class StateDrawable extends Drawable {
    private ColorStateList mTintStateList;
    private final Paint mPaint;
    private int mCurrentColor;
    private int mAlpha = 255;

    public StateDrawable(@NonNull ColorStateList tintStateList) {
        super();
        mTintStateList = tintStateList;
        mCurrentColor = tintStateList.getDefaultColor();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public boolean isStateful() {
        return (mTintStateList.isStateful()) || super.isStateful();
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean handled = super.setState(stateSet);
        handled = updateTint(stateSet) || handled;
        return handled;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private boolean updateTint(int[] state) {
        final int color = mTintStateList.getColorForState(state, mCurrentColor);
        if (color != mCurrentColor) {
            mCurrentColor = color;
            //We've changed states
            invalidateSelf();
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setColor(mCurrentColor);
        int alpha = modulateAlpha(Color.alpha(mCurrentColor));
        mPaint.setAlpha(alpha);
        doDraw(canvas, mPaint);
    }

    public void setColorStateList(@NonNull ColorStateList tintStateList) {
        mTintStateList = tintStateList;
    }

    /**
     * Subclasses should implement this method to do the actual drawing
     *
     * @param canvas The current {@link android.graphics.Canvas} to draw into
     * @param paint  The {@link android.graphics.Paint} preconfigurred with the current
     *               {@link android.content.res.ColorStateList} color
     */
    abstract void doDraw(Canvas canvas, Paint paint);

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        invalidateSelf();
    }

    int modulateAlpha(int alpha) {
        int scale = mAlpha + (mAlpha >> 7);
        return alpha * scale >> 8;
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

}
