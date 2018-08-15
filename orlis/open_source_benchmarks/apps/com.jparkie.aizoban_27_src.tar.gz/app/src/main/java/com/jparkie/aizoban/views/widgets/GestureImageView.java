package com.jparkie.aizoban.views.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.OverScroller;

import com.jparkie.aizoban.utils.PreferenceUtils;

public class GestureImageView extends ImageView {
    public static final String VIEW_TYPE_FIT_CENTRE = "Fit Centre";
    public static final String VIEW_TYPE_FIT_WIDTH = "Fit Width";
    public static final String VIEW_TYPE_FIT_HEIGHT = "Fit Height";

    public static final float MIN_SCALE = 1.00f;
    public static final float MAX_SCALE = 3.00f;

    private static final float ZOOM_DURATION = 200f;
    private static final long RUNNABLE_DELAY_MS = 1000 / 60;

    private String mViewType;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSupplementaryMatrix = new Matrix();
    private Matrix mDisplayMatrix = new Matrix();
    private float[] mMatrixValues = new float[9];

    private int mMaximumAcceleratedWidth;
    private int mMaximumAcceleratedHeight;
    private float mBitmapWidth;
    private float mBitmapHeight;

    private FlingRunnable mFlingRunnable;
    private ZoomRunnable mZoomRunnable;

    private ScaleGestureDetector mScaleGestureDetector;

    private boolean mInitialized;

    public GestureImageView(Context context) {
        super(context);

        initialize();
    }

    public GestureImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        initialize();
    }

    public GestureImageView(Context context, AttributeSet attributeSet, int definitionStyle) {
        super(context, attributeSet, definitionStyle);

        initialize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas.isHardwareAccelerated()) {
            mMaximumAcceleratedWidth = canvas.getMaximumBitmapWidth();
            mMaximumAcceleratedHeight = canvas.getMaximumBitmapHeight();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);

        if (bitmap != null) {
            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawable != null) {
            mBitmapWidth = drawable.getIntrinsicWidth();
            mBitmapHeight = drawable.getIntrinsicHeight();
        }
    }

    private void initialize() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ImageViewScaleGestureListener());
        mViewType = PreferenceUtils.getViewType();
    }

    public void initializeView() {
        if (!mInitialized) {
            setScaleType(ScaleType.MATRIX);

            initializeBaseMatrix();
            setImageMatrix(getImageViewMatrix());

            mInitialized = true;
        }
    }

    private void initializeBaseMatrix() {
        mBaseMatrix.reset();

        float widthScale = Math.min(getWidth() / mBitmapWidth, 2.00f);
        float heightScale = Math.min(getHeight() / mBitmapHeight, 2.00f);

        float actualScale = Math.min(widthScale, heightScale);
        if (mViewType.equals(VIEW_TYPE_FIT_CENTRE)) {
            actualScale = Math.min(widthScale, heightScale);
        } else if (mViewType.equals(VIEW_TYPE_FIT_WIDTH)) {
            actualScale = widthScale;
        } else if (mViewType.equals(VIEW_TYPE_FIT_HEIGHT)) {
            actualScale = heightScale;
        }

        mBaseMatrix.postScale(actualScale, actualScale);
        mBaseMatrix.postTranslate((getWidth() - mBitmapWidth * actualScale) / 2.00f, (getHeight() - mBitmapHeight * actualScale) / 2.00f);

        mInitialized = true;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    private float getTransX(Matrix matrix) {
        matrix.getValues(mMatrixValues);

        return mMatrixValues[Matrix.MTRANS_X];
    }

    private float getTransY(Matrix matrix) {
        matrix.getValues(mMatrixValues);

        return mMatrixValues[Matrix.MTRANS_Y];
    }

    private float getScaleX(Matrix matrix) {
        matrix.getValues(mMatrixValues);

        return mMatrixValues[Matrix.MSCALE_X];
    }

    private float getScaleY(Matrix matrix) {
        matrix.getValues(mMatrixValues);

        return mMatrixValues[Matrix.MSCALE_Y];
    }

    public float getScale() {
        return getScaleX(mSupplementaryMatrix);
    }

    private Matrix getImageViewMatrix() {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSupplementaryMatrix);

        return mDisplayMatrix;
    }

    public int getMaximumAcceleratedWidth() {
        return mMaximumAcceleratedWidth;
    }

    public int getMaximumAcceleratedHeight() {
        return mMaximumAcceleratedHeight;
    }

    private void center(boolean centerHorizontal, boolean centerVertical) {
        Matrix currentImageViewMatrix = getImageViewMatrix();

        RectF drawableRectangle = new RectF(0.00f, 0.00f, mBitmapWidth, mBitmapHeight);
        currentImageViewMatrix.mapRect(drawableRectangle);

        float height = drawableRectangle.height();
        float width = drawableRectangle.width();

        float deltaX = 0, deltaY = 0;

        if (centerHorizontal) {
            int viewWidth = getWidth();
            if (width < viewWidth) {
                deltaX = (viewWidth - width) / 2 - drawableRectangle.left;
            } else if (drawableRectangle.left > 0) {
                deltaX = -drawableRectangle.left;
            } else if (drawableRectangle.right < viewWidth) {
                deltaX = viewWidth - drawableRectangle.right;
            }
        }

        if (centerVertical) {
            int viewHeight = getHeight();
            if (height < viewHeight) {
                deltaY = (viewHeight - height) / 2 - drawableRectangle.top;
            } else if (drawableRectangle.top > 0) {
                deltaY = -drawableRectangle.top;
            } else if (drawableRectangle.bottom < viewHeight) {
                deltaY = getHeight() - drawableRectangle.bottom;
            }
        }

        mSupplementaryMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(getImageViewMatrix());
    }

    public void startFling(float velocityX, float velocityY) {
        if (mFlingRunnable != null) {
            mFlingRunnable.cancel();
        }

        mFlingRunnable = new FlingRunnable(velocityX, velocityY);
        postDelayed(mFlingRunnable, RUNNABLE_DELAY_MS);
    }

    public void cancelFling() {
        if (mFlingRunnable != null) {
            mFlingRunnable.cancel();
        }
    }

    private void panBy(float deltaX, float deltaY) {
        mSupplementaryMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(getImageViewMatrix());
    }

    public void postTranslate(float deltaX, float deltaY) {
        mSupplementaryMatrix.postTranslate(deltaX, deltaY);
        center(true, true);
    }

    public void zoomTo(float scale, float centerX, float centerY) {
        if (scale > MAX_SCALE) {
            scale = MAX_SCALE;
        }
        if (scale < MIN_SCALE) {
            scale = MIN_SCALE;
        }

        float oldScale = getScaleX(mSupplementaryMatrix);
        float deltaScale = scale / oldScale;

        mSupplementaryMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    private void zoomTo(float scale, float centerX, float centerY, float durationMs) {
        mZoomRunnable = new ZoomRunnable(scale, centerX, centerY, durationMs);
        postDelayed(mZoomRunnable, RUNNABLE_DELAY_MS);
    }

    public void zoomTo(float scale) {
        float centerX = getWidth() / 2.00f;
        float centerY = getHeight() / 2.00f;

        zoomTo(scale, centerX, centerY, ZOOM_DURATION);
    }

    public void zoomToPoint(float scale, float pointX, float pointY) {
        zoomTo(scale, pointX, pointY, ZOOM_DURATION);
    }

    public boolean canScrollParent() {
        if (mInitialized) {
            if (getTransX(mDisplayMatrix) == 0) {
                return true;
            } else if (mBitmapWidth * getScaleX(mDisplayMatrix) + getTransX(mDisplayMatrix) <= getWidth()) {
                return true;
            }

            return false;
        }

        return true;
    }

    public Matrix getZoomMatrix() {
        return mSupplementaryMatrix;
    }

    public void setZoomMatrix(Matrix oldSupplementaryMatrix) {
        float newScaleX = getScaleX(oldSupplementaryMatrix);
        float newScaleY = getScaleY(oldSupplementaryMatrix);

        mSupplementaryMatrix.setScale(newScaleX, newScaleY);
        center(true, true);
    }

    private class FlingRunnable implements Runnable {
        private OverScroller mOverScroller;

        private int mCurrentX;
        private int mCurrentY;

        public FlingRunnable(float inputVelocityX, float inputVelocityY) {
            mOverScroller = new OverScroller(getContext());

            int startX = (int) getTransX(mDisplayMatrix);
            int startY = (int) getTransY(mDisplayMatrix);
            int velocityX = (int) inputVelocityX;
            int velocityY = (int) inputVelocityY;
            int minX;
            int maxX;
            int minY;
            int maxY;

            int viewWidth = getMeasuredWidth();
            int viewHeight = getMeasuredHeight();

            float widthScale = Math.min(getWidth() / mBitmapWidth, 2.00f);
            float heightScale = Math.min(getHeight() / mBitmapHeight, 2.00f);
            float actualScale = Math.min(widthScale, heightScale);

            float redundantSpaceX = viewWidth - (actualScale * mBitmapWidth);
            float redundantSpaceY = viewHeight - (actualScale * mBitmapHeight);

            Rect drawableRectangle = new Rect(0, 0, (int) (mBitmapWidth * getScaleX(mDisplayMatrix)), (int) (mBitmapHeight * getScaleY(mDisplayMatrix)));
            int drawableWidth = drawableRectangle.width();
            int drawableHeight = drawableRectangle.height();

            if (drawableWidth > viewWidth) {
                minX = viewWidth - (int) redundantSpaceX - drawableWidth;
                maxX = 0;
            } else {
                minX = startX;
                maxX = startX;
            }

            if (drawableHeight > viewHeight) {
                minY = viewHeight - (int) redundantSpaceY - drawableHeight;
                maxY = 0;
            } else {
                minY = startY;
                maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

            mOverScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        }

        @Override
        public void run() {
            if (mOverScroller.isFinished()) {
                return;
            }

            if (mOverScroller.computeScrollOffset()) {
                int newX = mOverScroller.getCurrX();
                int newY = mOverScroller.getCurrY();

                int transX = newX - mCurrentX;
                int transY = newY - mCurrentY;

                mCurrentX = newX;
                mCurrentY = newY;

                postTranslate(transX, transY);
                postDelayed(this, RUNNABLE_DELAY_MS);
            }
        }

        public void cancel() {
            if (mOverScroller != null) {
                mOverScroller.forceFinished(true);
            }
        }
    }

    private class ZoomRunnable implements Runnable {
        private float mOldScale;

        private float mCenterX;
        private float mCenterY;

        private float mDurationMs;
        private float mIncrementPerMs;

        private long mStartTime;

        public ZoomRunnable(float scale, float centerX, float centerY, float durationMs) {
            mOldScale = getScaleX(mSupplementaryMatrix);

            mCenterX = centerX;
            mCenterY = centerY;

            mDurationMs = durationMs;
            mIncrementPerMs = (scale - mOldScale) / mDurationMs;

            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);
            float targetScale = mOldScale + (mIncrementPerMs * currentMs);
            zoomTo(targetScale, mCenterX, mCenterY);

            if (currentMs < mDurationMs) {
                postDelayed(this, RUNNABLE_DELAY_MS);
            } else {
                return;
            }
        }
    }

    private class ImageViewScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (mInitialized) {
                float scale = getScale() * scaleGestureDetector.getScaleFactor();
                zoomTo(scale, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
            }

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            // Do Nothing.
        }
    }
}