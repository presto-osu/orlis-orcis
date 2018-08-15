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
package net.sf.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * SeekBar preference.
 *
 * @author Moshe Waisberg
 */
public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

    /** Android namespace. */
    private static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";
    /** Delay in milliseconds to wait for user to finish changing the seek bar. */
    private static final long PERSIST_DELAY = 650;

    private final Context context;
    private SeekBar seekBar;
    private int progress;
    private int max = 100;
    private Timer timer;
    private PersistTask task;
    private Toast toast;

    /**
     * Creates a new seek bar preference.
     *
     * @param context
     *         the context.
     */
    public SeekBarPreference(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Creates a new seek bar preference.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the attributes.
     */
    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        max = attrs.getAttributeIntValue(NS_ANDROID, "max", 100);
    }

    /**
     * Creates a new seek bar preference.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the attributes.
     * @param defStyle
     *         the default style.
     */
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        max = attrs.getAttributeIntValue(NS_ANDROID, "max", 100);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        TextView title = (TextView) view.findViewById(android.R.id.title);
        RelativeLayout host = (RelativeLayout) title.getParent();

        seekBar = new SeekBar(getContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(RelativeLayout.BELOW, android.R.id.summary);
        host.addView(seekBar, lp);

        return view;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        final int max = this.max;
        final int progress = this.progress;
        if (max != seekBar.getMax())
            seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(this);
        if (progress != seekBar.getProgress())
            seekBar.setProgress(progress);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? getPersistedInt(progress) : (Integer) defaultValue);
    }

    /**
     * Set the progress state and saves it to the {@link SharedPreferences}.
     *
     * @param progress
     *         the progress.
     */
    public void setProgress(int progress) {
        if (seekBar == null) {
            // Save this for when the seek bar is created.
            this.progress = progress;
        } else {
            // Calls onProgressChanged -> persistProgress
            seekBar.setProgress(progress);
        }
    }

    public int getProgress() {
        return progress;
    }

    /**
     * Set the range of the progress bar to {@code 0}...{@code max}.
     *
     * @param max
     *         the upper range of this progress bar.
     */
    public void setMax(int max) {
        this.max = max;
        if (seekBar != null)
            seekBar.setMax(max);
    }

    public int getMax() {
        return seekBar.getMax();
    }

    /**
     * Set the progress state and saves it to the {@link SharedPreferences}.
     *
     * @param progress
     *         the progress.
     */
    protected void persistProgress(int progress) {
        this.progress = progress;
        // Postpone persisting until user finished dragging.
        if (task != null)
            task.cancel();
        task = new PersistTask(progress);
        if (timer == null)
            timer = new Timer();
        timer.schedule(task, PERSIST_DELAY);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (this.seekBar == seekBar) {
            if (this.progress != progress) {
                // FIXME print the progress on the bar instead of toasting.
                if (toast == null)
                    toast = Toast.makeText(context, String.valueOf(progress), Toast.LENGTH_SHORT);
                else {
                    toast.setText(String.valueOf(progress));
                    toast.show();
                }
                persistProgress(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Timed task to persist the preference.
     *
     * @author Moshe
     */
    private class PersistTask extends TimerTask {

        private final int mProgress;

        /**
         * Constructs a new task.
         *
         * @param progress
         *         the progress to save.
         */
        public PersistTask(int progress) {
            mProgress = progress;
        }

        @Override
        public void run() {
            if (callChangeListener(mProgress)) {
                persistInt(mProgress);
            }
        }
    }
}
