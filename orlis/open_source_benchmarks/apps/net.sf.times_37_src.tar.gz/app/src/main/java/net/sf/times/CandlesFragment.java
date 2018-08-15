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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import java.util.Calendar;
import java.util.Random;

/**
 * Shows candle images.
 *
 * @author Moshe Waisberg
 */
public class CandlesFragment extends ZmanimFragment {

    private static final int[] SHABBAT_CANDLES = {R.id.candle_1, R.id.candle_2};
    private static final int[] YOM_KIPPURIM_CANDLES = {R.id.candle_1};
    private static final int[] CHANNUKA_CANDLES = {R.id.candle_1, R.id.candle_2, R.id.candle_3, R.id.candle_4, R.id.candle_5, R.id.candle_6, R.id.candle_7, R.id.candle_8};

    /** The candles view for Shabbat. */
    private ViewGroup candlesShabbat;
    /** The candles view for Channuka. */
    private ViewGroup candlesChannuka;
    /** The candles view for Yom Kippurim. */
    private ViewGroup candlesKippurim;
    /** The animation scheduler. */
    private final Handler handler = new Handler();
    /** The flaming candle animations. */
    private CandleAnimation[] animations;
    /** Randomizer. */
    private final Random random = new Random();

    /**
     * Constructs a new candles view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the XMl attributes.
     * @param defStyle
     *         the default style.
     */
    public CandlesFragment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Constructs a new candles view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the XML attributes.
     */
    public CandlesFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructs a new candles view.
     *
     * @param context
     *         the context.
     */
    public CandlesFragment(Context context) {
        super(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected ZmanimAdapter createAdapter() {
        return new CandlesAdapter(context, settings);
    }

    @SuppressLint("InflateParams")
    @Override
    public ZmanimAdapter populateTimes(Calendar date) {
        CandlesAdapter adapter = (CandlesAdapter) super.populateTimes(date);

        stopAnimation();

        if (adapter == null)
            return null;

        int holiday = adapter.getCandlesHoliday();
        int candlesCount = adapter.getCandlesCount();
        boolean animate = settings.isCandlesAnimated();
        ImageView view;

        switch (holiday) {
            case JewishCalendar.YOM_KIPPUR:
                if (candlesKippurim == null) {
                    ViewGroup group = (ViewGroup) inflater.inflate(R.layout.candles_kippurim, null);
                    addView(group);
                    candlesKippurim = group;

                    // assert candlesCount == YOM_KIPPURIM_CANDLES.length;
                    animations = new CandleAnimation[candlesCount];
                    for (int i = 0; i < candlesCount; i++) {
                        view = (ImageView) group.findViewById(YOM_KIPPURIM_CANDLES[i]);
                        animations[i] = new CandleAnimation(handler, view, random);
                    }
                }
                if (candlesShabbat != null)
                    candlesShabbat.setVisibility(View.GONE);
                if (candlesChannuka != null)
                    candlesChannuka.setVisibility(View.GONE);
                candlesKippurim.setVisibility(View.VISIBLE);
                break;
            case JewishCalendar.CHANUKAH:
                if (candlesChannuka == null) {
                    ViewGroup group = (ViewGroup) inflater.inflate(R.layout.candles_channuka, null);
                    addView(group);
                    candlesChannuka = group;

                    // create all candles in case user navigates to future day.
                    final int allCandlesCount = CHANNUKA_CANDLES.length;
                    // assert candlesCount <= allCandlesCount;
                    animations = new CandleAnimation[allCandlesCount + 1];
                    for (int i = 0; i < allCandlesCount; i++) {
                        view = (ImageView) group.findViewById(CHANNUKA_CANDLES[i]);
                        animations[i] = new CandleAnimation(handler, view, random);
                    }
                    view = (ImageView) group.findViewById(R.id.candle_shamash);
                    animations[allCandlesCount] = new CandleAnimation(handler, view, random);
                }
                // Only show relevant candles.
                for (int i = 0; i < candlesCount; i++) {
                    candlesChannuka.findViewById(CHANNUKA_CANDLES[i]).setVisibility(View.VISIBLE);
                }
                for (int i = candlesCount; i < CHANNUKA_CANDLES.length; i++) {
                    candlesChannuka.findViewById(CHANNUKA_CANDLES[i]).setVisibility(View.INVISIBLE);
                }
                if (candlesShabbat != null)
                    candlesShabbat.setVisibility(View.GONE);
                if (candlesKippurim != null)
                    candlesKippurim.setVisibility(View.GONE);
                candlesChannuka.setVisibility(View.VISIBLE);
                break;
            default:
                if (candlesCount == 0) {
                    // Should never happen!
                    if (candlesShabbat != null)
                        candlesShabbat.setVisibility(View.GONE);
                    if (candlesKippurim != null)
                        candlesKippurim.setVisibility(View.GONE);
                    if (candlesChannuka != null)
                        candlesChannuka.setVisibility(View.GONE);
                } else {
                    if (candlesShabbat == null) {
                        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.candles_shabbat, null);
                        addView(group);
                        candlesShabbat = group;

                        // assert candlesCount == SHABBAT_CANDLES.length;
                        animations = new CandleAnimation[candlesCount];
                        for (int i = 0; i < candlesCount; i++) {
                            view = (ImageView) group.findViewById(SHABBAT_CANDLES[i]);
                            animations[i] = new CandleAnimation(handler, view, random);
                        }
                    }
                    if (candlesKippurim != null)
                        candlesKippurim.setVisibility(View.GONE);
                    if (candlesChannuka != null)
                        candlesChannuka.setVisibility(View.GONE);
                    candlesShabbat.setVisibility(View.VISIBLE);
                }
                break;
        }

        if (animate)
            startAnimation();

        return adapter;
    }

    @Override
    protected void setOnClickListener(View view, ZmanimItem item) {
        // No clicking allowed.
    }

    private void stopAnimation() {
        final CandleAnimation[] anims = animations;
        if (anims == null)
            return;
        for (Runnable anim : anims) {
            if (anim == null)
                continue;
            handler.removeCallbacks(anim);
        }
    }

    private void startAnimation() {
        final CandleAnimation[] anims = animations;
        if (anims == null)
            return;
        for (Runnable anim : anims) {
            if (anim == null)
                continue;
            handler.post(anim);
        }
    }
}