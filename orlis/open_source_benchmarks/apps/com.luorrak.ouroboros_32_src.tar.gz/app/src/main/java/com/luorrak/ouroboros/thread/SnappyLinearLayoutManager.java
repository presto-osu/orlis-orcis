package com.luorrak.ouroboros.thread;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SnappyLinearLayoutManager extends LinearLayoutManager implements SnappyRecyclerView.ISnappyLayoutManager {
    // These variables are from android.widget.Scroller, which is used, via ScrollerCompat, by
    // Recycler View. The scrolling distance calculation logic originates from the same place. Want
    // to use their variables so as to approximate the look of normal Android scrolling.
    // Find the Scroller fling implementation in android.widget.Scroller.fling().
    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private static double FRICTION = 0.84;

    private double deceleration;

    public SnappyLinearLayoutManager(Context context) {
        super(context);
        calculateDeceleration(context);
    }

    public SnappyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        calculateDeceleration(context);
        setAutoMeasureEnabled(false);
    }

    private void calculateDeceleration(Context context) {
        deceleration = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.3700787 // inches per meter
                // pixels per inch. 160 is the "default" dpi, i.e. one dip is one pixel on a 160 dpi
                // screen
                * context.getResources().getDisplayMetrics().density * 160.0f * FRICTION;
    }

    @Override
    public int getPositionForVelocity(int velocityX, int velocityY) {
        if (getChildCount() == 0) {
            return 0;
        }
        if (getOrientation() == HORIZONTAL) {
            return calcPosForVelocity(velocityX, getPosition(getChildAt(0)));
        } else {
            return calcPosForVelocity(velocityY, getPosition(getChildAt(0)));
        }
    }

    private int calcPosForVelocity(int velocity, int currPos) {
        if (velocity < 0) {
            return Math.max(currPos, 0);
        } else {
            return currPos + 1;
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {

                    // I want a behavior where the scrolling always snaps to the beginning of
                    // the list. Snapping to end is also trivial given the default implementation.
                    // If you need a different behavior, you may need to override more
                    // of the LinearSmoothScrolling methods.
                    protected int getHorizontalSnapPreference() {
                        return SNAP_TO_START;
                    }

                    protected int getVerticalSnapPreference() {
                        return SNAP_TO_START;
                    }

                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return SnappyLinearLayoutManager.this
                                .computeScrollVectorForPosition(targetPosition);
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    /**
     * This implementation obviously doesn't take into account the direction of the
     * that preceded it, but there is no easy way to get that information without more
     * hacking than I was willing to put into it.
     */
    @Override
    public int getFixScrollPos() {
        if (this.getChildCount() == 0) {
            return 0;
        }

        final View child = getChildAt(0);
        final int childPos = getPosition(child);

        if (getOrientation() == HORIZONTAL
                && Math.abs(child.getLeft()) > child.getMeasuredWidth() / 2) {
            // Scrolled first view more than halfway offscreen
            return childPos + 1;
        } else if (getOrientation() == VERTICAL
                && Math.abs(child.getTop()) > child.getMeasuredWidth() / 2) {
            // Scrolled first view more than halfway offscreen
            return childPos + 1;
        }
        return childPos;
    }

    //http://stackoverflow.com/questions/26649406/nested-recycler-view-height-doesnt-wrap-its-content
    private int[] mMeasuredDimension = new int[2];

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);
        int width = 0;
        int height = 0;
        for (int i = 0; i < getItemCount(); i++) {
            measureScrapChild(recycler, i,
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension);

            if (getOrientation() == HORIZONTAL) {
                width = width + mMeasuredDimension[0];
                if (i == 0) {
                    height = mMeasuredDimension[1];
                }
            } else {
                height = height + mMeasuredDimension[1];
                if (i == 0) {
                    width = mMeasuredDimension[0];
                }
            }
        }
        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
                width = widthSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
                height = heightSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
        View view = recycler.getViewForPosition(position);
        if (view != null) {
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    getPaddingLeft() + getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    getPaddingTop() + getPaddingBottom(), p.height);
            view.measure(childWidthSpec, childHeightSpec);
            measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
            measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
            recycler.recycleView(view);
        }
    }
}
