/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.view.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class LayoutWeightAnimation extends Animation {

    private View view;
    private float fromWeight;
    private float toWeight;
    private boolean increase;

    public LayoutWeightAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutWeightAnimation(View view, float fromWeight, float toWeight) {
        this.view = view;
        this.fromWeight = fromWeight;
        this.toWeight = toWeight;
        increase = toWeight >= fromWeight;
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    public boolean willChangeTransformationMatrix() {
        return false;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (increase)
            lp.weight = (toWeight - fromWeight) * interpolatedTime;
        else
            lp.weight = (fromWeight - toWeight) * (1f - interpolatedTime);
        view.setLayoutParams(lp);
    }
}
