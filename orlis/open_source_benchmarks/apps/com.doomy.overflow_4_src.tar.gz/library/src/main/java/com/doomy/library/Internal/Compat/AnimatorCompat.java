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

import android.os.Build;

/**
 * Currently, there's no {@link android.animation.ValueAnimator} compatibility version
 * and as we didn't want to throw in external dependencies, we made this small class.
 * <p/>
 * <p>
 * This will work like {@link android.support.v4.view.ViewPropertyAnimatorCompat}, that is,
 * not doing anything on API<11 and using the default {@link android.animation.ValueAnimator}
 * on API>=11
 * </p>
 * <p>
 * This class is used to provide animation to the {@link org.adw.library.widgets.discreteseekbar.DiscreteSeekBar}
 * when navigating with the Keypad
 * </p>
 *
 * @hide
 */
public abstract class AnimatorCompat {
    public interface AnimationFrameUpdateListener {
        public void onAnimationFrame(float currentValue);
    }

    AnimatorCompat() {

    }

    public abstract void cancel();

    public abstract boolean isRunning();

    public abstract void setDuration(int progressAnimationDuration);

    public abstract void start();

    public static final AnimatorCompat create(float start, float end, AnimationFrameUpdateListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new AnimatorCompatV11(start, end, listener);
        } else {
            return new AnimatorCompatBase(start, end, listener);
        }
    }

    private static class AnimatorCompatBase extends AnimatorCompat {

        private final AnimationFrameUpdateListener mListener;
        private final float mEndValue;

        public AnimatorCompatBase(float start, float end, AnimationFrameUpdateListener listener) {
            mListener = listener;
            mEndValue = end;
        }

        @Override
        public void cancel() {

        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void setDuration(int progressAnimationDuration) {

        }

        @Override
        public void start() {
            mListener.onAnimationFrame(mEndValue);
        }
    }
}
