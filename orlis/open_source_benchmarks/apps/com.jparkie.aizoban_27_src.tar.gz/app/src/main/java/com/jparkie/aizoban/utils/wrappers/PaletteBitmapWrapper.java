package com.jparkie.aizoban.utils.wrappers;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

public class PaletteBitmapWrapper {
    private final Palette mPalette;
    private final Bitmap mBitmap;

    public PaletteBitmapWrapper(Palette palette, Bitmap bitmap) {
        mPalette = palette;
        mBitmap = bitmap;
    }

    public Palette getPalette() {
        return mPalette;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
