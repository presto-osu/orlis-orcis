/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package activities;

import org.domogik.domodroid13.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

public class Sliding_Drawer extends LinearLayout {

    @SuppressWarnings("unused")
    private static final String TAG = "Panel";

    public interface OnPanelListener {
        void onPanelClosed(Sliding_Drawer panel);

        void onPanelOpened(Sliding_Drawer panel);
    }

    private boolean mIsShrinking;
    private final int mPosition;
    private final int mDuration;
    private final boolean mLinearFlying;
    private View mHandle;
    private View mContent;
    private Drawable mOpenedHandle;
    private Drawable mClosedHandle;
    private float mTrackX;
    private float mTrackY;
    private float mVelocity;

    private OnPanelListener panelListener;

    private static final int TOP = 0;
    private static final int BOTTOM = 1;
    private static final int LEFT = 2;
    public static final int RIGHT = 3;

    private enum State {
        ABOUT_TO_ANIMATE,
        ANIMATING,
        READY,
        TRACKING,
        FLYING,
    }

    private State mState;
    private Interpolator mInterpolator;
    private final GestureDetector mGestureDetector;
    private int mContentHeight;
    private int mContentWidth;
    private final int mOrientation;
    private final PanelOnGestureListener mGestureListener;

    public Sliding_Drawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Panel);
        mDuration = a.getInteger(R.styleable.Panel_animationDuration, 750);
        mPosition = a.getInteger(R.styleable.Panel_position, BOTTOM);
        mLinearFlying = a.getBoolean(R.styleable.Panel_linearFlying, false);
        mOpenedHandle = a.getDrawable(R.styleable.Panel_openedHandle);
        mClosedHandle = a.getDrawable(R.styleable.Panel_closedHandle);
        a.recycle();
        mOrientation = (mPosition == TOP || mPosition == BOTTOM) ? VERTICAL : HORIZONTAL;
        setOrientation(mOrientation);
        mState = State.READY;
        mGestureListener = new PanelOnGestureListener();
        mGestureDetector = new GestureDetector(mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
    }


    public void setOnPanelListener(OnPanelListener onPanelListener) {
        panelListener = onPanelListener;
    }


    public View getHandle() {
        return mHandle;
    }

    public View getContent() {
        return mContent;
    }


    public void setInterpolator(Interpolator i) {
        mInterpolator = i;
    }

    public void setOpen(boolean open, boolean animate) {
        if (isOpen() ^ open) {
            mIsShrinking = !open;
            if (animate) {
                mState = State.ABOUT_TO_ANIMATE;
                if (!mIsShrinking) {

                    mContent.setVisibility(VISIBLE);
                }
                post(startAnimation);
            } else {
                mContent.setVisibility(open ? VISIBLE : GONE);
                postProcess();
            }
        }
    }

    public boolean isOpen() {
        return mContent.getVisibility() == VISIBLE;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandle = findViewById(R.id.panelHandle);
        if (mHandle == null) {
            throw new RuntimeException("Your Panel must have a View whose id attribute is 'R.id.panelHandle'");
        }
        mHandle.setOnTouchListener(touchListener);

        mContent = findViewById(R.id.panelContent);
        if (mContent == null) {
            throw new RuntimeException("Your Panel must have a View whose id attribute is 'R.id.panelContent'");
        }

        removeView(mHandle);
        removeView(mContent);
        if (mPosition == TOP || mPosition == LEFT) {
            addView(mContent);
            addView(mHandle);
        } else {
            addView(mHandle);
            addView(mContent);
        }

        if (mClosedHandle != null) {
            mHandle.setBackgroundDrawable(mClosedHandle);
        }
        mContent.setVisibility(GONE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mContentWidth = mContent.getWidth();
        mContentHeight = mContent.getHeight();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (mState == State.ABOUT_TO_ANIMATE && !mIsShrinking) {
            int delta = mOrientation == VERTICAL ? mContentHeight : mContentWidth;
            if (mPosition == LEFT || mPosition == TOP) {
                delta = -delta;
            }
            if (mOrientation == VERTICAL) {
                canvas.translate(0, delta);
            } else {
                canvas.translate(delta, 0);
            }
        }
        if (mState == State.TRACKING || mState == State.FLYING) {
            canvas.translate(mTrackX, mTrackY);
        }
        super.dispatchDraw(canvas);
    }

    private float ensureRange(float v, int min, int max) {
        v = Math.max(v, min);
        v = Math.min(v, max);
        return v;
    }

    private final OnTouchListener touchListener = new OnTouchListener() {
        int initX;
        int initY;
        boolean setInitialPosition;

        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                initX = 0;
                initY = 0;
                if (mContent.getVisibility() == GONE) {
                    if (mOrientation == VERTICAL) {
                        initY = mPosition == TOP ? -1 : 1;
                    } else {
                        initX = mPosition == LEFT ? -1 : 1;
                    }
                }
                setInitialPosition = true;
            } else {
                if (setInitialPosition) {
                    initX *= mContentWidth;
                    initY *= mContentHeight;
                    mGestureListener.setScroll(initX, initY);
                    setInitialPosition = false;
                    initX = -initX;
                    initY = -initY;
                }
                event.offsetLocation(initX, initY);
            }
            if (!mGestureDetector.onTouchEvent(event)) {
                if (action == MotionEvent.ACTION_UP) {
                    post(startAnimation);
                }
            }
            return false;
        }
    };

    private final Runnable startAnimation = new Runnable() {
        public void run() {

            TranslateAnimation animation;
            int fromXDelta = 0, toXDelta = 0, fromYDelta = 0, toYDelta = 0;
            if (mState == State.FLYING) {
                mIsShrinking = (mPosition == TOP || mPosition == LEFT) ^ (mVelocity > 0);
            }
            int calculatedDuration;
            if (mOrientation == VERTICAL) {
                int height = mContentHeight;
                if (!mIsShrinking) {
                    fromYDelta = mPosition == TOP ? -height : height;
                } else {
                    toYDelta = mPosition == TOP ? -height : height;
                }
                if (mState == State.TRACKING) {
                    if (Math.abs(mTrackY - fromYDelta) < Math.abs(mTrackY - toYDelta)) {
                        mIsShrinking = !mIsShrinking;
                        toYDelta = fromYDelta;
                    }
                    fromYDelta = (int) mTrackY;
                } else if (mState == State.FLYING) {
                    fromYDelta = (int) mTrackY;
                }

                if (mState == State.FLYING && mLinearFlying) {
                    calculatedDuration = (int) (1000 * Math.abs((toYDelta - fromYDelta) / mVelocity));
                    calculatedDuration = Math.max(calculatedDuration, 20);
                } else {
                    calculatedDuration = mDuration * Math.abs(toYDelta - fromYDelta) / mContentHeight;
                }
            } else {
                int width = mContentWidth;
                if (!mIsShrinking) {
                    fromXDelta = mPosition == LEFT ? -width : width;
                } else {
                    toXDelta = mPosition == LEFT ? -width : width;
                }
                if (mState == State.TRACKING) {
                    if (Math.abs(mTrackX - fromXDelta) < Math.abs(mTrackX - toXDelta)) {
                        mIsShrinking = !mIsShrinking;
                        toXDelta = fromXDelta;
                    }
                    fromXDelta = (int) mTrackX;
                } else if (mState == State.FLYING) {
                    fromXDelta = (int) mTrackX;
                }

                if (mState == State.FLYING && mLinearFlying) {
                    calculatedDuration = (int) (1000 * Math.abs((toXDelta - fromXDelta) / mVelocity));
                    calculatedDuration = Math.max(calculatedDuration, 20);
                } else {
                    calculatedDuration = mDuration * Math.abs(toXDelta - fromXDelta) / mContentWidth;
                }
            }

            mTrackX = mTrackY = 0;
            if (calculatedDuration == 0) {
                mState = State.READY;
                if (mIsShrinking) {
                    mContent.setVisibility(GONE);
                }
                postProcess();
                return;
            }

            animation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
            animation.setDuration(calculatedDuration);
            animation.setAnimationListener(animationListener);
            if (mState == State.FLYING && mLinearFlying) {
                animation.setInterpolator(new LinearInterpolator());
            } else if (mInterpolator != null) {
                animation.setInterpolator(mInterpolator);
            }
            startAnimation(animation);
        }
    };

    private final AnimationListener animationListener = new AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            mState = State.READY;
            if (mIsShrinking) {
                mContent.setVisibility(GONE);
            }
            postProcess();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
            mState = State.ANIMATING;
        }
    };

    private void postProcess() {
        if (mIsShrinking && mClosedHandle != null) {
            mHandle.setBackgroundDrawable(mClosedHandle);
        } else if (!mIsShrinking && mOpenedHandle != null) {
            mHandle.setBackgroundDrawable(mOpenedHandle);
        }
        if (panelListener != null) {
            if (mIsShrinking) {
                panelListener.onPanelClosed(Sliding_Drawer.this);
            } else {
                panelListener.onPanelOpened(Sliding_Drawer.this);
            }
        }
    }

    class PanelOnGestureListener implements OnGestureListener {
        float scrollY;
        float scrollX;

        public void setScroll(int initScrollX, int initScrollY) {
            scrollX = initScrollX;
            scrollY = initScrollY;
        }

        public boolean onDown(MotionEvent e) {
            scrollX = scrollY = 0;
            if (mState != State.READY) {
                return false;
            }
            mState = State.ABOUT_TO_ANIMATE;
            mIsShrinking = mContent.getVisibility() == VISIBLE;
            if (!mIsShrinking) {

                mContent.setVisibility(VISIBLE);
            }
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mState = State.FLYING;
            mVelocity = mOrientation == VERTICAL ? velocityY : velocityX;
            post(startAnimation);
            return true;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mState = State.TRACKING;
            float tmpY = 0, tmpX = 0;
            if (mOrientation == VERTICAL) {
                scrollY -= distanceY;
                if (mPosition == TOP) {
                    tmpY = ensureRange(scrollY, -mContentHeight, 0);
                } else {
                    tmpY = ensureRange(scrollY, 0, mContentHeight);
                }
            } else {
                scrollX -= distanceX;
                if (mPosition == LEFT) {
                    tmpX = ensureRange(scrollX, -mContentWidth, 0);
                } else {
                    tmpX = ensureRange(scrollX, 0, mContentWidth);
                }
            }
            if (tmpX != mTrackX || tmpY != mTrackY) {
                mTrackX = tmpX;
                mTrackY = tmpY;
                invalidate();
            }
            return true;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            post(startAnimation);
            return true;
        }
    }

    public Drawable getmOpenedHandle() {
        return mOpenedHandle;
    }


    public void setmOpenedHandle(Drawable mOpenedHandle) {
        this.mOpenedHandle = mOpenedHandle;
    }


    public Drawable getmClosedHandle() {
        return mClosedHandle;
    }


    public void setmClosedHandle(Drawable mClosedHandle) {
        this.mClosedHandle = mClosedHandle;
    }
}
