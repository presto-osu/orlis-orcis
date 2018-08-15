/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Color;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.view.ScreenFilterView;

public class ColorSeekBarPreference extends Preference {
    private static final String TAG = "ColorSeekBarPreference";
    private static final boolean DEBUG = true;
    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    public static final int DEFAULT_VALUE = 10;

    public SeekBar mColorTempSeekBar;
    private int mProgress;
    private View mView;

    public ColorSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference_color_seekbar);
    }

    public void setProgress(int progress) {
        if (mColorTempSeekBar != null) {
            mColorTempSeekBar.setProgress(progress);
        } else {
            mProgress = progress;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mProgress = getPersistedInt(DEFAULT_VALUE);
        } else {
            mProgress = (Integer) defaultValue;
            persistInt(mProgress);
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        mView = view;

        mColorTempSeekBar = (SeekBar) view.findViewById(R.id.color_temp_seekbar);
        initLayout();
    }

    private void initLayout() {
        mColorTempSeekBar.setProgress(mProgress);

        mColorTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                persistInt(mProgress);

                updateMoonIconColor();
                updateProgressText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateMoonIconColor();
        updateProgressText();
    }

    private void updateMoonIconColor() {
        if (!isEnabled()) return;

        int color = ScreenFilterView.rgbFromColorProgress(mProgress);

        ImageView moonIcon = (ImageView) mView.findViewById(R.id.moon_icon_color);

        PorterDuffColorFilter colorFilter
            = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);

        moonIcon.setColorFilter(colorFilter);
    }

    private void updateProgressText() {
        int colorTemp = ScreenFilterView.getColorTempFromProgress(mProgress);

        TextView progressView = (TextView) mView.findViewById(R.id.current_color_temperature);

        String unit = "K";
        String temp = Integer.toString(colorTemp);

        progressView.setText(temp + unit);
    }
}
