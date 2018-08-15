/*
    The MIT License (MIT)
    
    Copyright (c) 2014 Nir Hartmann
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
package com.samsung.srpol.parallax;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

public abstract class ParallaxedView {
    static public boolean isAPI11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    protected WeakReference<View> view;
    protected int lastOffset;
    protected List<Animation> animations;

    abstract protected void translatePreICS(View view, float offset);

    public ParallaxedView(View view) {
        this.lastOffset = 0;
        this.animations = new ArrayList<Animation>();
        this.view = new WeakReference<View>(view);
    }

    public boolean is(View v) {
        return (v != null && view != null && view.get() != null && view.get()
                .equals(v));
    }

    @SuppressLint("NewApi")
    public void setOffset(float offset) {
        View view = this.view.get();
        if (view != null)
            if (isAPI11) {
                view.setTranslationY(offset);
            } else {
                translatePreICS(view, offset);
            }
    }

    public void setAlpha(float alpha) {
        View view = this.view.get();
        if (view != null)
            if (isAPI11) {
                view.setAlpha(alpha);
            } else {
                alphaPreICS(view, alpha);
            }
    }

    protected synchronized void addAnimation(Animation animation) {
        animations.add(animation);
    }

    protected void alphaPreICS(View view, float alpha) {
        addAnimation(new AlphaAnimation(alpha, alpha));
    }

    protected synchronized void animateNow() {
        View view = this.view.get();
        if (view != null) {
            AnimationSet set = new AnimationSet(true);
            for (Animation animation : animations)
                if (animation != null)
                    set.addAnimation(animation);
            set.setDuration(0);
            set.setFillAfter(true);
            view.setAnimation(set);
            set.start();
            animations.clear();
        }
    }

    public void setView(View view) {
        this.view = new WeakReference<View>(view);
    }

    public View getView() {
        return this.view.get();
    }
}
