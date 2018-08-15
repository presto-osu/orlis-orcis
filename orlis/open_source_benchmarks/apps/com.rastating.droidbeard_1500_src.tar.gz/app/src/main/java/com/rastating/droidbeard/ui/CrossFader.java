/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.ui;

import android.view.View;

public class CrossFader {
    private View mView1, mView2;
    private int mDuration;

    /***
     * Instantiate a new CrossFader object.
     * @param view1 the view to fade out
     * @param view2 the view to fade in
     * @param fadeDuration the duration in milliseconds for each fade to last
     */
    public CrossFader(View view1, View view2, int fadeDuration) {
        mView1 = view1;
        mView2 = view2;
        mDuration = fadeDuration;
    }

    /***
     * Start the cross-fade animation.
     */
    public void start() {
        mView1.setAlpha(0.0f);
        mView1.setVisibility(View.GONE);
        mView2.setAlpha(1.0f);
        mView2.setVisibility(View.VISIBLE);

        /*mView2.setAlpha(0f);
        mView2.setVisibility(View.VISIBLE);
        mView1.animate()
            .alpha(0f)
            .setDuration(mDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mView1.setVisibility(View.GONE);
                    mView2.animate()
                        .alpha(1f)
                        .setDuration(mDuration)
                        .setListener(null);
                }
            });*/
    }
}