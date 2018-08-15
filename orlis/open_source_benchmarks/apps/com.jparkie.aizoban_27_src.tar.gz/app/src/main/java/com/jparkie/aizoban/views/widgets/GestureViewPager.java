package com.jparkie.aizoban.views.widgets;

import android.content.Context;
import android.graphics.Matrix;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.jparkie.aizoban.views.fragments.PageFragment;

public class GestureViewPager extends ViewPager {
    public static final String TAG = GestureViewPager.class.getSimpleName();

    private static final float LEFT_REGION = 0.33f;
    private static final float RIGHT_REGION = 0.66f;
    private static final float SWIPE_TOLERANCE = 0.25f;

    private GestureImageView mGestureImageView;
    private GestureDetector mGestureDetector;

    private float mStartDragX;

    private boolean mIsLockZoom;
    private Matrix mZoomMatrix;

    private OnChapterBoundariesOutListener mOnChapterBoundariesOutListener;
    private OnChapterSingleTapListener mOnChapterSingleTapListener;

    public GestureViewPager(Context context) {
        super(context);

        initialize();
    }

    public GestureViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        initialize();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            fetchGestureImageView();

            mGestureDetector.onTouchEvent(ev);

            if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                if (this.getCurrentItem() == 0 || this.getCurrentItem() == this.getAdapter().getCount() - 1) {
                    mStartDragX = ev.getX();
                }
            }

            if (mGestureImageView != null) {
                if (!mGestureImageView.canScrollParent()) {
                    return false;
                } else {
                    if (mIsLockZoom) {
                        mZoomMatrix = mGestureImageView.getZoomMatrix();
                    }
                }
            }

            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // Do Nothing.
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (mOnChapterBoundariesOutListener != null) {
                if (this.getCurrentItem() == 0) {
                    if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        float displacement = ev.getX() - mStartDragX;

                        if (ev.getX() > mStartDragX && displacement > getWidth() * SWIPE_TOLERANCE) {
                            mOnChapterBoundariesOutListener.onFirstPageOut();
                            return true;
                        }

                        mStartDragX = 0;
                    }
                } else if (this.getCurrentItem() == this.getAdapter().getCount() - 1) {
                    if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        float displacement = mStartDragX - ev.getX();

                        if (ev.getX() < mStartDragX && displacement > getWidth() * SWIPE_TOLERANCE) {
                            mOnChapterBoundariesOutListener.onLastPageOut();
                            return true;
                        }

                        mStartDragX = 0;
                    }
                }
            }

            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // Do Nothing.
        }

        return false;
    }

    private void initialize() {
        mGestureDetector = new GestureDetector(getContext(), new ImageViewGestureListener());

        mZoomMatrix = new Matrix();
    }

    private void fetchGestureImageView() {
        mGestureImageView = (GestureImageView) findViewWithTag(PageFragment.TAG + ":" + getCurrentItem());
    }

    public boolean getIsLockZoom() {
        return mIsLockZoom;
    }

    public void setIsLockZoom(boolean isLockZoom) {
        mIsLockZoom = isLockZoom;
    }

    public void applyViewSettings() {
        if (mIsLockZoom) {
            fetchGestureImageView();

            if (mGestureImageView != null) {
                mGestureImageView.setZoomMatrix(mZoomMatrix);
            }
        }
    }

    public void setOnChapterBoundariesOutListener(OnChapterBoundariesOutListener onChapterBoundariesOutListener) {
        mOnChapterBoundariesOutListener = onChapterBoundariesOutListener;
    }

    public void setOnChapterSingleTapListener(OnChapterSingleTapListener onChapterSingleTapListener) {
        mOnChapterSingleTapListener = onChapterSingleTapListener;
    }

    public interface OnChapterBoundariesOutListener {
        public void onFirstPageOut();

        public void onLastPageOut();
    }

    public interface OnChapterSingleTapListener {
        public void onSingleTap();
    }

    private class ImageViewGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            if (mGestureImageView != null) {
                if (mGestureImageView.isInitialized()) {
                    if (mGestureImageView.getScale() > mGestureImageView.MIN_SCALE) {
                        mGestureImageView.zoomToPoint(mGestureImageView.MIN_SCALE, getWidth() / 2, getHeight() / 2);
                    } else if (mGestureImageView.getScale() < mGestureImageView.MAX_SCALE) {
                        mGestureImageView.zoomToPoint(mGestureImageView.MAX_SCALE, motionEvent.getX(), motionEvent.getY());
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            if (mGestureImageView != null) {
                if (mGestureImageView.isInitialized()) {
                    mGestureImageView.cancelFling();
                }
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            if (mGestureImageView != null) {
                if (mGestureImageView.isInitialized()) {
                    mGestureImageView.postTranslate(-v, -v2);
                }
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            if (mGestureImageView != null) {
                if (mGestureImageView.isInitialized()) {
                    mGestureImageView.startFling(v, v2);
                }
            }

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            final int position = getCurrentItem();
            final float positionX = motionEvent.getX();

            if (positionX < getWidth() * LEFT_REGION) {
                if (position != 0) {
                    setCurrentItem(position - 1, true);
                } else {
                    if (mOnChapterBoundariesOutListener != null) {
                        mOnChapterBoundariesOutListener.onFirstPageOut();
                    }
                }
            } else if (positionX > getWidth() * RIGHT_REGION) {
                if (position != getAdapter().getCount() - 1) {
                    setCurrentItem(position + 1, true);
                } else {
                    if (mOnChapterBoundariesOutListener != null) {
                        mOnChapterBoundariesOutListener.onLastPageOut();
                    }
                }
            } else {
                if (mOnChapterSingleTapListener != null) {
                    mOnChapterSingleTapListener.onSingleTap();
                }
            }

            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            // Do Nothing.
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {
            // Do Nothing.
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }
    }
}