package com.jenzz.materialpreference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.alexcruz.papuhwalls.Preferences;

public class SwitchPreferenceThemeable extends SwitchPreference {

    public SwitchPreferenceThemeable(Context context) {
        super(context);
    }

    public SwitchPreferenceThemeable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPreferenceThemeable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwitchPreferenceThemeable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Preferences preferences = new Preferences(getContext());

        titleView.setTextColor(preferences.PrimaryText());
        summaryView.setTextColor(preferences.SecondaryText());
    }
}
