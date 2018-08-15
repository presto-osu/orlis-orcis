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
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 *
 * @author Moshe Waisberg
 */
public class ZmanimFragment<A extends ZmanimAdapter> extends FrameLayout {

    protected final Context context;
    protected LayoutInflater inflater;
    private OnClickListener onClickListener;
    /** The main list view. */
    protected ViewGroup view;
    /** The list. */
    protected ViewGroup list;
    /** Provider for locations. */
    protected ZmanimLocations locations;
    /** The settings and preferences. */
    protected ZmanimSettings settings;
    /** The gradient background. */
    private Drawable background;
    /** The master item selected row. */
    private View highlightRow;
    /** The master item background that is selected. */
    private Drawable highlightBackground;
    /** The master item background that is not selected. */
    private Drawable unhighlightBackground;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    /** The adapter. */
    private A adapter;
    /** The gesture detector. */
    private GestureDetector gestureDetector;

    /**
     * Constructs a new list.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the XMl attributes.
     * @param defStyle
     *         the default style.
     */
    public ZmanimFragment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(context);
    }

    /**
     * Constructs a new list.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the XML attributes.
     */
    public ZmanimFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    /**
     * Constructs a new list.
     *
     * @param context
     *         the context.
     */
    public ZmanimFragment(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    /** Initialise. */
    @SuppressLint("InflateParams")
    private void init(Context context) {
        if (!isInEditMode()) {
            settings = new ZmanimSettings(context);
            ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
            locations = app.getLocations();
        }

        inflater = LayoutInflater.from(context);
        view = (ViewGroup) inflater.inflate(R.layout.times_list, null);
        addView(view);
        list = (ViewGroup) view.findViewById(android.R.id.list);
    }

    /**
     * Create a new times adapter.
     *
     * @return the adapter.
     */
    @SuppressWarnings("unchecked")
    protected A createAdapter() {
        return (A) new ZmanimAdapter(context, settings);
    }

    /**
     * Get the times adapter.
     *
     * @return the adapter.
     */
    protected A getAdapter() {
        A adapter = this.adapter;
        if (adapter == null) {
            adapter = createAdapter();
            this.adapter = adapter;
        }
        return adapter;
    }

    /**
     * Populate the list with times.
     *
     * @param date
     *         the date.
     */
    @SuppressWarnings("deprecation")
    public A populateTimes(Calendar date) {
        // Called before attached to activity?
        ZmanimLocations locations = this.locations;
        if (locations == null)
            return null;
        GeoLocation gloc = locations.getGeoLocation();
        // Have we been destroyed?
        if (gloc == null)
            return null;

        A adapter = getAdapter();
        adapter.setCalendar(date);
        adapter.setGeoLocation(gloc);
        adapter.setInIsrael(locations.inIsrael());
        adapter.populate(false);

        ViewGroup list = this.list;
        if (list == null)
            return adapter;
        bindViews(list, adapter);
        return adapter;
    }

    /**
     * Bind the times to a list.
     *
     * @param list
     *         the list.
     * @param adapter
     *         the list adapter.
     */
    protected void bindViews(ViewGroup list, A adapter) {
        if (list == null)
            return;
        final int count = adapter.getCount();
        list.removeAllViews();

        ZmanimItem item;
        View row;

        Context context = getContext();
        Calendar date = adapter.getCalendar().getCalendar();
        JewishDate jewishDate = new JewishDate(date);
        CharSequence dateHebrew = adapter.formatDate(context, jewishDate);
        JewishCalendar jcal = adapter.getJewishCalendar();

        int position = 0;

        if (position < count) {
            item = adapter.getItem(position);
            if (item.titleId == R.string.hour) {
                row = adapter.getView(position, null, list);
                bindView(list, position, row, item);
                position++;
            }

            bindViewGrouping(list, position, dateHebrew);

            while (position < count) {
                item = adapter.getItem(position);
                row = adapter.getView(position, null, list);
                bindView(list, position, row, item);

                // Start of the next Hebrew day.
                if (item.titleId == R.string.sunset) {
                    jewishDate.forward();
                    jcal.forward();

                    dateHebrew = adapter.formatDate(context, jewishDate);
                    bindViewGrouping(list, position, dateHebrew);

                    // Sefirat HaOmer?
                    int omer = jcal.getDayOfOmer();
                    if (omer >= 1) {
                        CharSequence omerLabel = adapter.formatOmer(context, omer);
                        if (!TextUtils.isEmpty(omerLabel)) {
                            bindViewGrouping(list, position, omerLabel);
                        }
                    }
                }

                position++;
            }
        }
    }

    /**
     * Bind the time to a list.
     *
     * @param list
     *         the list.
     * @param position
     *         the position index.
     * @param row
     *         the row view.
     * @param item
     *         the item.
     */
    protected void bindView(ViewGroup list, int position, View row, ZmanimItem item) {
        setOnClickListener(row, item);
        inflater.inflate(R.layout.divider, list);
        list.addView(row);
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list
     *         the list.
     * @param position
     *         the position index.
     * @param label
     *         the formatted Hebrew date label.
     */
    @SuppressLint("InflateParams")
    protected void bindViewGrouping(ViewGroup list, int position, CharSequence label) {
        if (position > 0)
            inflater.inflate(R.layout.divider, list);
        ViewGroup row = (ViewGroup) inflater.inflate(R.layout.date_group, null);
        TextView text = (TextView) row.findViewById(R.id.date_hebrew);
        text.setText(label);
        list.addView(row);
    }

    protected void setOnClickListener(View view, ZmanimItem item) {
        final int id = item.titleId;
        boolean clickable = view.isEnabled() && (id != R.string.molad);
        view.setOnClickListener(clickable ? onClickListener : null);
        view.setClickable(clickable);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        onClickListener = listener;
    }

    /**
     * Get the background for the selected item.
     *
     * @return the background.
     */
    private Drawable getSelectedBackground() {
        if (highlightBackground == null) {
            highlightBackground = getResources().getDrawable(R.drawable.list_selected);
        }
        return highlightBackground;
    }

    /**
     * Mark the selected row as unselected.
     */
    public void unhighlight() {
        unhighlight(highlightRow);
    }

    /**
     * Mark the row as unselected.
     *
     * @param view
     *         the row view.
     */
    @SuppressWarnings("deprecation")
    private void unhighlight(View view) {
        Drawable bg = unhighlightBackground;
        if ((view == null) || (bg == null))
            return;

        // Workaround for Samsung ICS bug where the highlight lingers.
        if (bg instanceof StateListDrawable)
            bg = bg.getConstantState().newDrawable();
        view.setBackgroundDrawable(bg);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        unhighlightBackground = null;
    }

    /**
     * Mark the row as selected.
     *
     * @param itemId
     *         the row id.
     */
    @SuppressWarnings("deprecation")
    public void highlight(int itemId) {
        // Find the view that matches the item id (the view that was clicked).
        final A adapter = this.adapter;
        if (adapter == null)
            return;
        final ViewGroup list = this.list;
        if (list == null)
            return;
        View view = null;
        View child;
        ZmanimItem item;
        final int count = list.getChildCount();
        for (int i = 0; i < count; i++) {
            child = list.getChildAt(i);
            item = (ZmanimItem) child.getTag(R.id.time);
            // Maybe row divider?
            if (item == null)
                continue;
            if (item.titleId == itemId) {
                view = child;
                break;
            }
        }
        if (view == null)
            return;

        unhighlightBackground = view.getBackground();
        paddingLeft = view.getPaddingLeft();
        paddingTop = view.getPaddingTop();
        paddingRight = view.getPaddingRight();
        paddingBottom = view.getPaddingBottom();
        view.setBackgroundDrawable(getSelectedBackground());
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        highlightRow = view;
    }

    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if ((gestureDetector != null) && gestureDetector.onTouchEvent(event))
            return true;
        return super.onInterceptTouchEvent(event);
    }

    /**
     * Set the gesture detector.
     *
     * @param gestureDetector
     *         the gesture detector.
     */
    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }
}
