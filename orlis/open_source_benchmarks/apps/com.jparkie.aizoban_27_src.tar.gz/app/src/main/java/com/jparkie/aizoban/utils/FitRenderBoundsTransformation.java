package com.jparkie.aizoban.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.jparkie.aizoban.views.widgets.GestureImageView;

public class FitRenderBoundsTransformation extends BitmapTransformation {
    private static final int MAX_BITMAP_DIMENSION = 2048;
    private static final int PAINT_FLAGS = Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG;

    private GestureImageView mImageView;
    private int mMaximumAcceleratedWidth;
    private int mMaximumAcceleratedHeight;

    public FitRenderBoundsTransformation(Context context, GestureImageView imageView) {
        super(context);

        mImageView = imageView;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return fitOpenGLRenderBounds(toTransform, pool, outWidth, outHeight);
    }

    @Override
    public String getId() {
        return "com.jparkie.aizoban.utils" + "." + FitRenderBoundsTransformation.class.getSimpleName();
    }

    private Bitmap fitOpenGLRenderBounds(Bitmap toFit, BitmapPool pool, int outWidth, int outHeight) {
        if (toFit.getWidth() == outWidth || toFit.getHeight() == outHeight) {
            return toFit;
        }

        mMaximumAcceleratedWidth = (mImageView.getMaximumAcceleratedWidth() > MAX_BITMAP_DIMENSION) ? mImageView.getMaximumAcceleratedWidth() : MAX_BITMAP_DIMENSION;
        mMaximumAcceleratedHeight = (mImageView.getMaximumAcceleratedHeight() > MAX_BITMAP_DIMENSION) ? mImageView.getMaximumAcceleratedHeight() : MAX_BITMAP_DIMENSION;
        mImageView = null;

        final int bitmapWidth = toFit.getWidth();
        final int bitmapHeight = toFit.getHeight();

        if (bitmapWidth > mMaximumAcceleratedWidth || bitmapHeight > mMaximumAcceleratedHeight) {
            final float minPercentage = Math.min(mMaximumAcceleratedWidth / (float) bitmapWidth, mMaximumAcceleratedHeight / (float) bitmapHeight);

            final int targetWidth = (int)(minPercentage * bitmapWidth);
            final int targetHeight = (int)(minPercentage * bitmapHeight);

            Bitmap.Config config = toFit.getConfig() != null ? toFit.getConfig() : Bitmap.Config.ARGB_8888;
            Bitmap toReuse = pool.get(targetWidth, targetHeight, config);
            if (toReuse == null) {
                toReuse = Bitmap.createBitmap(targetWidth, targetHeight, config);
            }
            Canvas canvas = new Canvas(toReuse);
            Matrix matrix = new Matrix();
            matrix.setScale(minPercentage, minPercentage);
            Paint paint = new Paint(PAINT_FLAGS);
            canvas.drawBitmap(toFit, matrix, paint);

            return toReuse;
        }

        return toFit;
    }
}
