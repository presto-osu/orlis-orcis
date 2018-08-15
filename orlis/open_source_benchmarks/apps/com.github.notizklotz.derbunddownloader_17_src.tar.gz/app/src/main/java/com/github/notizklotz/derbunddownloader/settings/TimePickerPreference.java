/*
 * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
 * Copyright (C) 2013 Adrian Gygax
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see {http://www.gnu.org/licenses/}.
 */

package com.github.notizklotz.derbunddownloader.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.github.notizklotz.derbunddownloader.common.DateHandlingUtils;

import org.springframework.util.StringUtils;

@SuppressWarnings("WeakerAccess")
public class TimePickerPreference extends DialogPreference {

    private String currentTime;
    private TimePicker tp;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static Integer[] toHourMinuteIntegers(String timeString) {
        if (!StringUtils.hasText(timeString)) {
            throw new IllegalArgumentException("timeString must not be blank");
        }

        String[] values = timeString.split(":|/");
        Integer hour = Integer.valueOf(values[0]);
        Integer minute = Integer.valueOf(values[1]);

        return new Integer[]{hour, minute};
    }

    @Override
    protected View onCreateDialogView() {
        tp = new TimePicker(getContext());
        tp.setIs24HourView(true);
        return tp;
    }

    @Override
    protected void onBindDialogView(@SuppressWarnings("NullableProblems") View view) {
        super.onBindDialogView(view);
        Integer[] time = toHourMinuteIntegers(currentTime);
        tp.setCurrentHour(time[0]);
        tp.setCurrentMinute(time[1]);
    }

    private void setTime(String text) {
        currentTime = text;

        if (isPersistent()) {
            persistString(text);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            tp.clearFocus();  // to get value of number if edited in text field, and clicking OK without clicking outside the field first (bug in NumberPicker)
            String newtime = DateHandlingUtils.toHH_MMString(tp.getCurrentHour(), tp.getCurrentMinute());
            if (callChangeListener(newtime)) {
                setTime(newtime);
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        if (restorePersistedValue) {
            setTime(this.getPersistedString(DateHandlingUtils.toHH_MMString(6, 0)));
        } else {
            // Set default state from the XML attribute
            setTime(defaultValue.toString());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

}