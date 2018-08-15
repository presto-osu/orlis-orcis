package com.jenzz.materialpreference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.alexcruz.papuhwalls.Preferences;

public class PreferenceCategoryThemeable extends PreferenceCategory {
    public PreferenceCategoryThemeable(Context context) {
        super(context);
    }

    public PreferenceCategoryThemeable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceCategoryThemeable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreferenceCategoryThemeable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Preferences preferences = new Preferences(getContext());

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setTextColor(preferences.Accent());
    }

}
