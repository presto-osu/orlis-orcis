package info.staticfree.SuperGenPass;

/*
 Visual Hash (SHA-1)
 Copyright (C) 2009-2015  Steve Pomeroy <steve@staticfree.info>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p> This displays a visual representation of the output of a hash function. Bits in the hash
 * output are mapped directly to colored shapes and their positions. </p> <p/> <p> This is intended
 * to allow rapid visual identification of a known input based by using spatial memory. For example,
 * by hashing a password and displaying the output to the user as they type it, they can learn to
 * identify when they have typed the password successfully by recognizing their password's distinct
 * shapes, colors and arrangements. </p>
 *
 * @author Steve Pomeroy
 */
@SuppressWarnings("MagicNumber")
public class VisualHash extends Drawable {
    private static final String TAG = VisualHash.class.getSimpleName();
    @NonNull
    private final MessageDigest mMessageDigest;
    @Nullable
    private byte[] mHash;

    @NonNull
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float mScaleX, mScaleY;

    /**
     * <p> Make sure that any hash function you specify has an even number of bytes in the output,
     * as each shape requires 2 bytes of data. </p> <p/> <p> This will draw n/2 shapes, where n is
     * the number of bytes of output from the hash function. These shapes will possibly overlap and
     * possibly be the same color as the background, so one shouldn't rely on a certain number of
     * shapes to be visible at any given time. </p>
     *
     * @param hashFunction the name of the hash function, eg. "SHA-1"
     * @throws NoSuchAlgorithmException if the specified hash algorithm can't be found.
     */
    public VisualHash(@NonNull final String hashFunction) throws NoSuchAlgorithmException {
        mMessageDigest = MessageDigest.getInstance(hashFunction);

        init();
    }

    /**
     * Creates a new {@link VisualHash} using SHA-1. This will draw 10 shapes, though some may be
     * obscured by others.
     * @throws NoSuchAlgorithmException if SHA-1 can't be found.
     */
    public VisualHash() throws NoSuchAlgorithmException {
        this("SHA-1");
    }

    private void init() {
        p.setStyle(Style.FILL);
        p.setStrokeWidth(0);
        setData("".getBytes());
    }

    /**
     * Sets the data and digests it into the visual hash.
     *
     * @param input the data to be hashed. This value is discarded immediately after computing the
     * hash.
     */
    public void setData(@NonNull final byte[] input) {
        if (input.length == 0) {
            mHash = null;
        } else {
            mMessageDigest.update(input);
            mHash = mMessageDigest.digest();
        }

        invalidateSelf();
    }

    private static final int TYPE_CIRCLE = 0x00, TYPE_SQUARE = 0x01, TYPE_STAR = 0x02,
            TYPE_TRIANGLE = 0x03, TYPE_PLUS = 0x04, TYPE_X = 0x05, TYPE_DIAMOND = 0x06,
            TYPE_SMALL_CIRCLE = 0x07;

    // the below defines the offsets to pull the bits of the hash out into
    // visual characteristics.

    // how many bytes per shape. The mapping below would need to be adjusted if this is
    // changed.
    private static final int BYTES_PER_SHAPE = 2;

    // the shape type
    private static final int TYPE_OFFSET = 0, TYPE_MAX = 0x7, TYPE_MASK = TYPE_MAX << TYPE_OFFSET;

    // this creates a grid of 4x4 potential shapes.
    private static final int X_OFFSET = 3, X_MAX = 0x7, X_MASK = X_MAX << X_OFFSET;

    private static final int Y_OFFSET = 6, Y_MAX = 0x7, Y_MASK = Y_MAX << Y_OFFSET;

    // There are 64 possible colors
    private static final int R_OFFSET = 9, R_MAX = 0x3, R_MASK = R_MAX << R_OFFSET;

    private static final int G_OFFSET = 11, G_MAX = 0x3, G_MASK = G_MAX << G_OFFSET;

    private static final int B_OFFSET = 13, B_MAX = 0x3, B_MASK = B_MAX << B_OFFSET;

    // one extra bit remain
    // ,A_OFFSET = 15, A_MAX = 0x1, A_MASK = A_MAX << A_OFFSET

    /**
     * The spacing between the shapes. Units are pre-scale pixels.
     */
    private static final int SPACING = 2;

    private static final int SHAPE_ALPHA = 255;

    /**
     * This constant is based on the static shape definitions.
     */
    private static final int RADIUS = 8;

    /**
     * The amount to move 0,0 so that drawing a circle with the given radius will be visible.
     */
    private static final int ORIGIN_OFFSET = RADIUS + SPACING / 2;

    /**
     * pixel width; centers are 3 shapes wide
     */
    private static final int PRESCALE_CENTER_WIDTH = (RADIUS * 2 + SPACING) * 3;

    /**
     * The size of the rendered area before it's been scaled to fit the drawable's bounds. Width is
     * 4 shapes wide.
     */
    private static final int PRESCALE_WIDTH = (RADIUS * 2 + SPACING) * 4;
    private static final int PRESCALE_HEIGHT = (RADIUS * 2 + SPACING) * 4;

    private static final Path TRIANGLE = new Path();
    private static final Path STAR = new Path();
    private static final Path PLUS = new Path();
    private static final Path X = new Path();
    private static final Path DIAMOND = new Path();

    static {
        TRIANGLE.moveTo(-RADIUS, RADIUS);
        TRIANGLE.lineTo(RADIUS, RADIUS); // _
        TRIANGLE.lineTo(0, -RADIUS);
        TRIANGLE.lineTo(-RADIUS, RADIUS);

        // star is based on radius size 8
        //
        // this was drawn in Inkscape and converted to this using
        // the python script svg2java.py included in /extras
        STAR.moveTo(0.000000f, -8.475681f);
        STAR.lineTo(1.893601f, -2.597389f);
        STAR.lineTo(8.069343f, -2.612960f);
        STAR.lineTo(3.063910f, 1.004453f);
        STAR.lineTo(4.987128f, 6.873122f);
        STAR.lineTo(0.000000f, 3.230514f);
        STAR.lineTo(-4.987129f, 6.873122f);
        STAR.rLineTo(1.923218f, -5.868669f);
        STAR.rLineTo(-5.005433f, -3.617414f);
        STAR.rLineTo(6.175743f, 0.015571f);
        STAR.lineTo(0.000000f, -8.475681f);

        PLUS.rMoveTo(2.084458f, -2.117061f);
        PLUS.rLineTo(5.865234f, 0.000000f);
        PLUS.rLineTo(0.000000f, 4.296875f);
        PLUS.rLineTo(-5.865234f, 0.000000f);
        PLUS.rLineTo(0.000000f, 5.865234f);
        PLUS.rLineTo(-4.296875f, 0.000000f);
        PLUS.rLineTo(0.000000f, -5.865234f);
        PLUS.rLineTo(-5.865234f, 0.000000f);
        PLUS.rLineTo(0.000000f, -4.296875f);
        PLUS.rLineTo(5.865234f, 0.000000f);
        PLUS.rLineTo(0.000000f, -5.875977f);
        PLUS.rLineTo(4.296875f, 0.000000f);
        PLUS.rLineTo(0.000000f, 5.875977f);

        X.moveTo(3.723963f, 0.060475f);
        X.lineTo(8.083338f, 4.419850f);
        X.lineTo(4.438807f, 8.064382f);
        X.lineTo(0.079432f, 3.705007f);
        X.lineTo(-4.279943f, 8.064382f);
        X.lineTo(-7.924475f, 4.419850f);
        X.rLineTo(4.359375f, -4.359375f);
        X.rLineTo(-4.359375f, -4.359375f);
        X.rLineTo(3.644531f, -3.644531f);
        X.rLineTo(4.359375f, 4.359375f);
        X.rLineTo(4.359375f, -4.371094f);
        X.rLineTo(3.644531f, 3.644531f);
        X.rLineTo(-4.359375f, 4.371094f);

        DIAMOND.moveTo(0, -RADIUS);
        DIAMOND.lineTo(RADIUS, 0);
        DIAMOND.lineTo(0, RADIUS);
        DIAMOND.lineTo(-RADIUS, 0);
        DIAMOND.lineTo(0, -RADIUS);
    }

    @Override
    protected void onBoundsChange(@NonNull final Rect bounds) {
        super.onBoundsChange(bounds);

        final int width = bounds.width();
        final int height = bounds.height();
        mScaleX = width / (float) PRESCALE_WIDTH;
        mScaleY = height / (float) PRESCALE_HEIGHT;
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {

        canvas.scale(mScaleX, mScaleY);

        if (mHash == null) {
            // this state means there's no useful data.
            return;
        }

        // go through all the bytes in the hash and draw them as shapes.
        for (int offset = 0; offset < mHash.length; offset += BYTES_PER_SHAPE) {
            final int dat = (0xff & mHash[offset]) | (0xff00 & (mHash[offset + 1] << 8));

            final int type = (dat & TYPE_MASK) >> TYPE_OFFSET;
            final int x = (dat & X_MASK) >> X_OFFSET;
            final int y = (dat & Y_MASK) >> Y_OFFSET;

            // TODO use this bit for something
            // final int a = (dat & A_MASK) >> A_OFFSET;

            p.setARGB(SHAPE_ALPHA, scaleInt(R_MAX, (dat & R_MASK) >> R_OFFSET, 255),
                    scaleInt(G_MAX, (dat & G_MASK) >> G_OFFSET, 255),
                    scaleInt(B_MAX, (dat & B_MASK) >> B_OFFSET, 255));

            final float xCenterScaled = ORIGIN_OFFSET + scale(X_MAX, x, PRESCALE_CENTER_WIDTH);
            final float yCenterScaled = ORIGIN_OFFSET + scale(Y_MAX, y, PRESCALE_CENTER_WIDTH);

            canvas.save();
            canvas.translate(xCenterScaled, yCenterScaled);

            switch (type) {
                case TYPE_STAR:
                    canvas.drawPath(STAR, p);
                    break;

                case TYPE_CIRCLE:
                    canvas.drawCircle(0, 0, RADIUS, p);
                    break;

                case TYPE_TRIANGLE:
                    canvas.drawPath(TRIANGLE, p);
                    break;

                case TYPE_SQUARE:
                    canvas.drawRect(-RADIUS, -RADIUS, RADIUS, RADIUS, p);
                    break;

                case TYPE_PLUS:
                    canvas.drawPath(PLUS, p);
                    break;

                case TYPE_X:
                    canvas.drawPath(X, p);
                    break;

                case TYPE_DIAMOND:
                    canvas.drawPath(DIAMOND, p);
                    break;

                case TYPE_SMALL_CIRCLE:
                    canvas.drawCircle(0, 0, RADIUS / 2, p);
                    break;
            }
            canvas.restore();
        }
    }

    /**
     * Scale an int linearly, starting at zero.
     *
     * @param valueMax the maximum input value
     * @param value the value to scale
     * @param max the maximum output value
     * @return the scaled value as an int
     */
    private static int scaleInt(final int valueMax, final int value, final int max) {
        return (int) ((value / (float) valueMax) * max);
    }

    /**
     * Scale an int linearly, starting at zero.
     *
     * @param valueMax the maximum input value
     * @param value the value to scale
     * @param max the maximum output value
     * @return the scaled value as a float
     */
    private static float scale(final int valueMax, final int value, final int max) {
        return (value / (float) valueMax) * max;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(final int alpha) {
        // do nothing
    }

    @Override
    public void setColorFilter(final ColorFilter arg0) {
        // do nothing
    }

    @Override
    public int getIntrinsicWidth() {
        return PRESCALE_WIDTH;
    }

    @Override
    public int getIntrinsicHeight() {
        return PRESCALE_HEIGHT;
    }
}
