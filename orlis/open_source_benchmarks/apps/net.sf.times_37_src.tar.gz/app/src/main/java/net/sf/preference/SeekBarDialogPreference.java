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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarDialogPreference extends DialogPreference implements OnSeekBarChangeListener {

    /** Android namespace. */
    private static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";

    private int progress;
    private int max;
    private boolean progressSet;
    private int summaryFormat;
    private SeekBar seekBar;
    private TextView summaryView;

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        max = attrs.getAttributeIntValue(NS_ANDROID, "max", 100);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        max = attrs.getAttributeIntValue(NS_ANDROID, "max", 100);
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
     * Gets the progress from the {@link SharedPreferences}.
     *
     * @return the progress.
     */
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

    /**
     * Get the maximum progress.
     *
     * @return the upper range of this progress bar.
     */
    public int getMax() {
        return max;
    }

    /**
     * Saves the progress to the {@link SharedPreferences}.
     *
     * @param progress
     *         the progress.
     */
    public void setProgress(int progress) {
        // Always persist/notify the first time; don't assume the field's
        // default value.
        final boolean changed = this.progress != progress;
        if (changed || !progressSet) {
            this.progress = progress;
            progressSet = true;
            persistInt(progress);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBar seek = (SeekBar) view.findViewById(android.R.id.edit);
        seek.setMax(max);
        seek.setOnSeekBarChangeListener(this);
        seekBar = seek;

        summaryView = (TextView) view.findViewById(android.R.id.summary);

        seek.setProgress(getProgress());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int progress = seekBar.getProgress();
            if (callChangeListener(progress) && (summaryFormat != 0)) {
                Resources res = getContext().getResources();
                CharSequence summary = res.getQuantityString(summaryFormat, progress, progress);
                setSummary(summary);

                setProgress(progress);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if ((this.seekBar == seekBar) && (summaryFormat != 0)) {
            Resources res = getContext().getResources();
            CharSequence summary = res.getQuantityString(summaryFormat, progress, progress);
            summaryView.setText(summary);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Set the summary format.
     *
     * @param pluralId
     *         the plural id for quantity.
     */
    public void setSummaryFormat(int pluralId) {
        summaryFormat = pluralId;
    }
}
