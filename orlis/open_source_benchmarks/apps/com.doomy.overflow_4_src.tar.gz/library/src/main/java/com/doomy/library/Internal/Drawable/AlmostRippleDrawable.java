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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class AlmostRippleDrawable extends StateDrawable implements Animatable {
    private static final long FRAME_DURATION = 1000 / 60;
    private static final int ANIMATION_DURATION = 250;

    private static final float INACTIVE_SCALE = 0f;
    private static final float ACTIVE_SCALE = 1f;
    private float mCurrentScale = INACTIVE_SCALE;
    private Interpolator mInterpolator;
    private long mStartTime;
    private boolean mReverse = false;
    private boolean mRunning = false;
    private int mDuration = ANIMATION_DURATION;
    private float mAnimationInitialValue;
    //We don't use colors just with our drawable state because of animations
    private int mPressedColor;
    private int mFocusedColor;
    private int mDisabledColor;
    private int mRippleColor;
    private int mRippleBgColor;

    public AlmostRippleDrawable(@NonNull ColorStateList tintStateList) {
        super(tintStateList);
        mInterpolator = new AccelerateDecelerateInterpolator();
        mFocusedColor = tintStateList.getColorForState(new int[]{android.R.attr.state_focused}, 0xFFFF0000);
        mPressedColor = tintStateList.getColorForState(new int[]{android.R.attr.state_pressed}, 0xFFFF0000);
        mDisabledColor = tintStateList.getColorForState(new int[]{-android.R.attr.state_enabled}, 0xFFFF0000);
    }

    @Override
    public void doDraw(Canvas canvas, Paint paint) {
        Rect bounds = getBounds();
        int size = Math.min(bounds.width(), bounds.height());
        float scale = mCurrentScale;
        int rippleColor = mRippleColor;
        int bgColor = mRippleBgColor;
        float radius = (size / 2);
        float radiusAnimated = radius * scale;
        if (scale > INACTIVE_SCALE) {
            if (bgColor != 0) {
                paint.setColor(bgColor);
                paint.setAlpha(decreasedAlpha(Color.alpha(bgColor)));
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
            }
            if (rippleColor != 0) {
                paint.setColor(rippleColor);
                paint.setAlpha(modulateAlpha(Color.alpha(rippleColor)));
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), radiusAnimated, paint);
            }
        }
    }

    private int decreasedAlpha(int alpha) {
        int scale = 100 + (100 >> 7);
        return alpha * scale >> 8;
    }

    @Override
    public boolean setState(int[] stateSet) {
        int[] oldState = getState();
        boolean oldPressed = false;
        for (int i : oldState) {
            if (i == android.R.attr.state_pressed) {
                oldPressed = true;
            }
        }
        super.setState(stateSet);
        boolean focused = false;
        boolean pressed = false;
        boolean disabled = true;
        for (int i : stateSet) {
            if (i == android.R.attr.state_focused) {
                focused = true;
            } else if (i == android.R.attr.state_pressed) {
                pressed = true;
            } else if (i == android.R.attr.state_enabled) {
                disabled = false;
            }
        }

        if (disabled) {
            unscheduleSelf(mUpdater);
            mRippleColor = mDisabledColor;
            mRippleBgColor = 0;
            mCurrentScale = ACTIVE_SCALE / 2;
            invalidateSelf();
        } else {
            if (pressed) {
                animateToPressed();
                mRippleColor = mRippleBgColor = mPressedColor;
            } else if (oldPressed) {
                mRippleColor = mRippleBgColor = mPressedColor;
                animateToNormal();
            } else if (focused) {
                mRippleColor = mFocusedColor;
                mRippleBgColor = 0;
                mCurrentScale = ACTIVE_SCALE;
                invalidateSelf();
            } else {
                mRippleColor = 0;
                mRippleBgColor = 0;
                mCurrentScale = INACTIVE_SCALE;
                invalidateSelf();
            }
        }
        return true;
    }

    public void animateToPressed() {
        unscheduleSelf(mUpdater);
        if (mCurrentScale < ACTIVE_SCALE) {
            mReverse = false;
            mRunning = true;
            mAnimationInitialValue = mCurrentScale;
            float durationFactor = 1f - ((mAnimationInitialValue - INACTIVE_SCALE) / (ACTIVE_SCALE - INACTIVE_SCALE));
            mDuration = (int) (ANIMATION_DURATION * durationFactor);
            mStartTime = SystemClock.uptimeMillis();
            scheduleSelf(mUpdater, mStartTime + FRAME_DURATION);
        }
    }

    public void animateToNormal() {
        unscheduleSelf(mUpdater);
        if (mCurrentScale > INACTIVE_SCALE) {
            mReverse = true;
            mRunning = true;
            mAnimationInitialValue = mCurrentScale;
            float durationFactor = 1f - ((mAnimationInitialValue - ACTIVE_SCALE) / (INACTIVE_SCALE - ACTIVE_SCALE));
            mDuration = (int) (ANIMATION_DURATION * durationFactor);
            mStartTime = SystemClock.uptimeMillis();
            scheduleSelf(mUpdater, mStartTime + FRAME_DURATION);
        }
    }

    private void updateAnimation(float factor) {
        float initial = mAnimationInitialValue;
        float destination = mReverse ? INACTIVE_SCALE : ACTIVE_SCALE;
        mCurrentScale = initial + (destination - initial) * factor;
        invalidateSelf();
    }

    private final Runnable mUpdater = new Runnable() {

        @Override
        public void run() {

            long currentTime = SystemClock.uptimeMillis();
            long diff = currentTime - mStartTime;
            if (diff < mDuration) {
                float interpolation = mInterpolator.getInterpolation((float) diff / (float) mDuration);
                scheduleSelf(mUpdater, currentTime + FRAME_DURATION);
                updateAnimation(interpolation);
            } else {
                unscheduleSelf(mUpdater);
                mRunning = false;
                updateAnimation(1f);
            }
        }
    };

    @Override
    public void start() {
        //No-Op. We control our own animation
    }

    @Override
    public void stop() {
        //No-Op. We control our own animation
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }
}
