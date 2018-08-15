package com.github.dfa.diaspora_android.ui;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.github.dfa.diaspora_android.R;

/**
 * Created by vanitas on 21.06.16.
 */
public class BottomBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {
    private static final FastOutSlowInInterpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private boolean mIsAnimatingOut = false;

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final LinearLayout child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final LinearLayout child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {

        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed < 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
            // User scrolled down and the FAB is currently visible -> hide the FAB
            animateOut(child);
        } else if (dyConsumed > 0 && child.getVisibility() != View.VISIBLE) {
            // User scrolled up and the FAB is currently not visible -> show the FAB
            animateIn(child);
        }
    }

    private void animateOut(final LinearLayout linearLayout) {
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(linearLayout).translationY(168F).alpha(0.0F).setInterpolator(INTERPOLATOR).withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {
                        public void onAnimationStart(View view) {
                            BottomBarBehavior.this.mIsAnimatingOut = true;
                        }

                        public void onAnimationCancel(View view) {
                            BottomBarBehavior.this.mIsAnimatingOut = false;
                        }

                        public void onAnimationEnd(View view) {
                            BottomBarBehavior.this.mIsAnimatingOut = false;
                            view.setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(linearLayout.getContext(), R.anim.bottom_bar_up);
            anim.setInterpolator(INTERPOLATOR);
            anim.setDuration(200L);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    BottomBarBehavior.this.mIsAnimatingOut = true;
                }

                public void onAnimationEnd(Animation animation) {
                    BottomBarBehavior.this.mIsAnimatingOut = false;
                    linearLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                }
            });
            linearLayout.startAnimation(anim);
        }
    }

    private void animateIn(LinearLayout linearLayout) {
        linearLayout.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(linearLayout).translationY(0).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                    .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                    .start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(linearLayout.getContext(), R.anim.bottom_bar_down);
            anim.setDuration(200L);
            anim.setInterpolator(INTERPOLATOR);
            linearLayout.startAnimation(anim);
        }
    }
}