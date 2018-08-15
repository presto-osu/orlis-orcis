package com.alexcruz.papuhwalls;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class Palette implements Transformation {

    private static final Palette INSTANCE = new Palette();
    private static final Map<Bitmap, android.support.v7.graphics.Palette> CACHE = new HashMap<Bitmap, android.support.v7.graphics.Palette>();

    public static abstract class PaletteTarget implements Target {
        protected abstract void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from, android.support.v7.graphics.Palette palette);

        @Override
        public final void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            final android.support.v7.graphics.Palette palette = getPalette(bitmap);
            onBitmapLoaded(bitmap, from, palette);
        }

    }

    public static android.support.v7.graphics.Palette getPalette(Bitmap bitmap) {
        return CACHE.get(bitmap);
    }

    public static abstract class PaletteCallback implements Callback {
        private WeakReference<ImageView> mImageView;

        public PaletteCallback(@NonNull ImageView imageView) {
            mImageView = new WeakReference<ImageView>(imageView);
        }

        protected abstract void onSuccess(android.support.v7.graphics.Palette palette);

        @Override
        public final void onSuccess() {
            if (getImageView() == null) {
                return;
            }

            final Bitmap bitmap = ((BitmapDrawable) getImageView().getDrawable()).getBitmap();
            final android.support.v7.graphics.Palette palette = getPalette(bitmap);

            onSuccess(palette);

        }

        private ImageView getImageView() {
            return mImageView.get();
        }

    }

    public static Palette instance() {
        return INSTANCE;
    }

    @Override
    public final Bitmap transform(Bitmap source) {
        final android.support.v7.graphics.Palette palette = android.support.v7.graphics.Palette.generate(source);

        CACHE.put(source, palette);

        return source;

    }

    @Override
    public String key() {
        return "";
    }

    private Palette() {
    }


}
