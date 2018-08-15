/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Compass view.
 *
 * @author Moshe Waisberg
 */
public class CompassView extends View {

    private float north;
    private float holiest;

    private Paint paintCircle;
    private Paint paintFrame;
    private Paint paintFrameOuter;
    private Paint paintFrameInner;
    private Paint paintNorth;
    private Paint paintEast;
    private Paint paintSouth;
    private Paint paintWest;
    private Paint paintNE;
    private Paint paintHoliest;
    private Paint paintFill;
    private Paint paintPivot;
    private Paint paintArrow;
    private final RectF rectFill = new RectF();
    private float widthHalf;
    private float heightHalf;
    private float radius;
    private float radiusPivot;
    private final Path pathArrowHoliest = new Path();
    private final Path pathArrowBig = new Path();
    private final Path pathArrowSmall = new Path();
    private final RectF rectFrameOuter = new RectF();
    private final RectF rectFrameInner = new RectF();

    private String labelNorth;
    private String labelEast;
    private String labelSouth;
    private String labelWest;

    /**
     * Constructs a new compass view.
     *
     * @param context
     *         the context.
     */
    public CompassView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructs a new compass view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the attributes.
     */
    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Constructs a new compass view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the attributes.
     * @param defStyle
     *         the default style.
     */
    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /** Initialise. */
    private void init(Context context) {
        Resources res = getResources();

        labelNorth = context.getString(R.string.north);
        labelEast = context.getString(R.string.east);
        labelSouth = context.getString(R.string.south);
        labelWest = context.getString(R.string.west);

        paintCircle = new Paint(Paint.DITHER_FLAG);
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(res.getColor(R.color.compass));

        paintFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFrame.setStyle(Paint.Style.STROKE);
        paintFrame.setStrokeWidth(res.getDimension(R.dimen.circle_thickness));
        paintFrame.setColor(res.getColor(R.color.compass_frame));

        paintFrameOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFrameOuter.setStyle(Paint.Style.STROKE);
        paintFrameOuter.setStrokeWidth(res.getDimension(R.dimen.circle_bevel_thickness));
        paintFrameOuter.setColor(res.getColor(R.color.compass_frame));

        paintFrameInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFrameInner.setStyle(Paint.Style.STROKE);
        paintFrameInner.setStrokeWidth(res.getDimension(R.dimen.circle_bevel_thickness));
        paintFrameInner.setColor(res.getColor(R.color.compass_frame));

        paintPivot = new Paint(Paint.DITHER_FLAG);
        paintPivot.setStyle(Paint.Style.FILL);
        paintPivot.setColor(res.getColor(R.color.compass_pivot));

        paintArrow = new Paint(Paint.DITHER_FLAG);
        paintArrow.setStyle(Paint.Style.FILL);

        paintNorth = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paintNorth.setStyle(Paint.Style.FILL_AND_STROKE);
        paintNorth.setStrokeWidth(res.getDimension(R.dimen.north_thickness));
        paintNorth.setTextSize(res.getDimension(R.dimen.label_size));
        paintNorth.setTextAlign(Align.CENTER);
        paintNorth.setColor(res.getColor(R.color.compass_north));

        paintSouth = new TextPaint(paintNorth);
        paintSouth.setStrokeWidth(res.getDimension(R.dimen.south_thickness));
        paintSouth.setColor(res.getColor(R.color.compass_south));

        paintEast = new TextPaint(paintSouth);
        paintEast.setStrokeWidth(res.getDimension(R.dimen.label_thickness));
        paintEast.setColor(res.getColor(R.color.compass_east));

        paintWest = new TextPaint(paintEast);
        paintEast.setColor(res.getColor(R.color.compass_west));

        paintNE = new Paint(paintEast);
        paintNE.setStrokeWidth(res.getDimension(R.dimen.label2_thickness));
        paintNE.setColor(res.getColor(R.color.compass_label2));

        paintHoliest = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHoliest.setStyle(Paint.Style.FILL_AND_STROKE);
        paintHoliest.setStrokeWidth(res.getDimension(R.dimen.holiest_thickness));
        paintHoliest.setColor(res.getColor(R.color.compass_holiest));

        paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFill.setColor(res.getColor(R.color.compass_arrow_bg));
        paintFill.setStyle(Paint.Style.STROKE);
        paintFill.setStrokeCap(Paint.Cap.BUTT);

        setAzimuth(0f);
        if (BuildConfig.DEBUG) {
            setAzimuth(1f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float w2 = widthHalf;
        final float h2 = heightHalf;
        final float r = radius;
        final float r7 = r * 0.7f;
        final float r9 = r * 0.9f;
        final float h2r7 = h2 - r7;
        final float h2r9 = h2 - r9;

        canvas.drawCircle(w2, h2, r, paintCircle);
        canvas.drawCircle(w2, h2, r, paintFrame);
        canvas.drawArc(rectFrameOuter, 0f, 360f, false, paintFrameOuter);
        canvas.drawArc(rectFrameInner, 0f, 360f, false, paintFrameInner);

        canvas.rotate(north, w2, h2);
        canvas.drawPath(pathArrowBig, paintNorth);
        canvas.drawText(labelNorth, w2, h2r7, paintNorth);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowBig, paintEast);
        canvas.drawText(labelEast, w2, h2r7, paintEast);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowBig, paintSouth);
        canvas.drawText(labelSouth, w2, h2r7, paintSouth);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowBig, paintWest);
        canvas.drawText(labelWest, w2, h2r7, paintWest);

        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        float sweepAngle = north + holiest;
        if (sweepAngle > 180f)
            sweepAngle -= 360f;
        else if (sweepAngle < -180f)
            sweepAngle += 360f;
        canvas.drawArc(rectFill, 315 - north, sweepAngle, false, paintFill);

        canvas.rotate(45 + holiest, w2, h2);
        canvas.drawLine(w2, h2, w2, h2r9, paintHoliest);
        canvas.drawPath(pathArrowHoliest, paintHoliest);

        canvas.drawCircle(w2, h2, radiusPivot, paintPivot);
    }

    /**
     * Set the azimuth to magnetic North pole.
     *
     * @param bearing
     *         the bearing in radians.
     */
    public void setAzimuth(float bearing) {
        north = (float) Math.toDegrees(-bearing);
        invalidate();
    }

    /**
     * Set the bearing to the Holy of Holies.
     *
     * @param bearing
     *         the bearing in degrees.
     */
    public void setHoliest(float bearing) {
        holiest = bearing;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Resources res = getResources();

        final int w2 = w / 2;
        final int h2 = h / 2;
        final float boundary = res.getDimension(R.dimen.padding) + res.getDimension(R.dimen.circle_thickness);
        final float r = Math.max(0, Math.min(w2, h2) - (boundary * 2f));
        final float r75 = r * 0.075f;
        final float r1 = r * 0.1f;
        final float r15 = r * 0.15f;
        final float r150 = r * 1.5f;
        final float r2 = r * 0.2f;
        final float r4 = r * 0.4f;
        final float r5 = r * 0.5f;
        final float r6 = r * 0.6f;
        final float r9 = r * 0.9f;
        final float r95 = r * 0.95f;
        final float h2r6 = h2 - r6;
        final float h2r9 = h2 - r9;
        final float h2r95 = h2 - r95;

        widthHalf = w2;
        heightHalf = h2;
        radius = r;
        radiusPivot = r15;

        if (r > 0) {
            RadialGradient gradientCircle = new RadialGradient(w2, h2, r * 3, res.getColor(R.color.compass), res.getColor(R.color.compass_gradient), Shader.TileMode.CLAMP);
            paintCircle.setShader(gradientCircle);

            RadialGradient gradientPivot = new RadialGradient(w2, h2, radiusPivot, res.getColor(R.color.compass_pivot), res.getColor(R.color.compass_pivot_dark), Shader.TileMode.CLAMP);
            paintPivot.setShader(gradientPivot);
        }

        LinearGradient gradientNorth = new LinearGradient(0, 0, 0, r150, res.getColor(R.color.compass_north_dark), res.getColor(R.color.compass_north), Shader.TileMode.CLAMP);
        paintNorth.setShader(gradientNorth);

        LinearGradient gradientEast = new LinearGradient(0, 0, 0, r150, res.getColor(R.color.compass_east_dark), res.getColor(R.color.compass_east), Shader.TileMode.CLAMP);
        paintEast.setShader(gradientEast);

        LinearGradient gradientSouth = new LinearGradient(0, 0, 0, r150, res.getColor(R.color.compass_south_dark), res.getColor(R.color.compass_south), Shader.TileMode.CLAMP);
        paintSouth.setShader(gradientSouth);

        LinearGradient gradientWest = new LinearGradient(0, 0, 0, r150, res.getColor(R.color.compass_west_dark), res.getColor(R.color.compass_west), Shader.TileMode.CLAMP);
        paintWest.setShader(gradientWest);

        final float sizeDirections = r2;
        paintNorth.setTextSize(sizeDirections);
        paintEast.setTextSize(sizeDirections);
        paintSouth.setTextSize(sizeDirections);
        paintWest.setTextSize(sizeDirections);

        float frameThickness = paintFrame.getStrokeWidth();
        float frameInnerThickness = paintFrameInner.getStrokeWidth();
        rectFill.left = w2 - r5;
        rectFill.top = h2 - r5;
        rectFill.right = w2 + r5;
        rectFill.bottom = h2 + r5;
        paintFill.setStrokeWidth(r - frameThickness - frameInnerThickness);

        float frameThicknessHalf = frameThickness / 2f;
        rectFrameOuter.left = w2 - r - frameThicknessHalf;
        rectFrameOuter.top = h2 - r - frameThicknessHalf;
        rectFrameOuter.right = w2 + r + frameThicknessHalf;
        rectFrameOuter.bottom = h2 + r + frameThicknessHalf;

        rectFrameInner.left = w2 - r + frameThicknessHalf;
        rectFrameInner.top = h2 - r + frameThicknessHalf;
        rectFrameInner.right = w2 + r - frameThicknessHalf;
        rectFrameInner.bottom = h2 + r - frameThicknessHalf;

        int colorFrame = res.getColor(R.color.compass_frame);
        int colorFrameHighlight = res.getColor(R.color.compass_frame_highlight);
        int colorFrameShadow = res.getColor(R.color.compass_frame_shadow);

        int[] colorsFrameOuter = {colorFrameShadow, colorFrameShadow, colorFrame, colorFrame, colorFrameHighlight, colorFrameHighlight, colorFrameHighlight, colorFrame, colorFrameShadow};
        float[] positionsFrameOuter = {0f, 0.125f, 0.250f, 0.375f, 0.500f, 0.625f, 0.750f, 0.875f, 1f};
        SweepGradient gradientFrameOuter = new SweepGradient(w2, h2, colorsFrameOuter, positionsFrameOuter);
        paintFrameOuter.setShader(gradientFrameOuter);

        int[] colorsFrameInner = {colorFrameHighlight, colorFrameHighlight, colorFrame, colorFrame, colorFrameShadow, colorFrameShadow, colorFrameShadow, colorFrame, colorFrameHighlight};
        float[] positionsFrameInner = positionsFrameOuter;
        SweepGradient gradientFrameInner = new SweepGradient(w2, h2, colorsFrameInner, positionsFrameInner);
        paintFrameInner.setShader(gradientFrameInner);

        pathArrowBig.reset();
        pathArrowBig.moveTo(w2 - r75, h2);
        pathArrowBig.lineTo(w2 + r75, h2);
        pathArrowBig.lineTo(w2, h2 - r6);
        pathArrowBig.close();

        pathArrowSmall.reset();
        pathArrowSmall.moveTo(w2 - r75, h2);
        pathArrowSmall.lineTo(w2 + r75, h2);
        pathArrowSmall.lineTo(w2, h2 - r4);
        pathArrowSmall.close();

        pathArrowHoliest.reset();
        pathArrowHoliest.moveTo(w2, h2r95);
        pathArrowHoliest.lineTo(w2 - r1, h2r6);
        pathArrowHoliest.quadTo(w2, h2r9, w2 + r1, h2r6);
        pathArrowHoliest.close();
    }
}
