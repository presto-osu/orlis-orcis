package org.itishka.pointim.widgets;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by Tishka17 on 17.03.2016.
 */
public class HideAnimationHelper {
    private View mView;
    private boolean mIsHidingProgress = false;
    private ObjectAnimator mAnimator = null;

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animator) {
            mIsHidingProgress = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mView.setVisibility(View.INVISIBLE);
            mIsHidingProgress = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mIsHidingProgress = false;
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    };

    public HideAnimationHelper() {
    }

    public HideAnimationHelper(View view) {
        setView(view);
    }

    public boolean isHidingProgress() {
        return mIsHidingProgress;
    }

    public void showView() {
        if (mView == null) return;
        if (isViewHiddenOrHiding()) {
            mView.setVisibility(View.VISIBLE);
            if (mAnimator != null) mAnimator.cancel();
            mAnimator = ObjectAnimator.ofFloat(mView, "alpha", 1f);
            mAnimator.setDuration(250);
            mAnimator.start();
        }
    }

    public void hideView() {
        if (mView == null) return;
        if (!isViewHiddenOrHiding()) {
            if (mAnimator != null) mAnimator.cancel();
            mAnimator = ObjectAnimator.ofFloat(mView, "alpha", 0f);
            mAnimator.setDuration(250);
            mAnimator.addListener(mAnimatorListener);
            mAnimator.start();
        }
    }

    public boolean isViewHiddenOrHiding() {
        if (mView == null) return false;
        return isHidingProgress() || mView.getVisibility() == View.INVISIBLE || mView.getVisibility() == View.GONE;
    }

    public void toggleView() {
        if (isViewHiddenOrHiding())
            showView();
        else
            hideView();
    }

    public void setView(View view) {
        if (mView != null) {
            if (mAnimator != null) {
                mAnimator.cancel();
                mAnimator.removeListener(mAnimatorListener);
            }
            if (isViewHiddenOrHiding()) {
                view.setVisibility(View.GONE);
            }
            mIsHidingProgress = false;
        }
        this.mView = view;
    }
}
