package yuku.ambilwarna.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.alexcruz.papuhwalls.Preferences;

public class AmbilWarnaPreferenceThemeable extends AmbilWarnaPreference {

    public AmbilWarnaPreferenceThemeable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Preferences preferences = new Preferences(getContext());

        int titleId = Resources.getSystem().getIdentifier("title", "id", "android");

        TextView titleView = (TextView) view.findViewById(titleId);
        if (titleView != null) {
            titleView.setTextColor(preferences.PrimaryText());
        }

        int summaryId = Resources.getSystem().getIdentifier("summary", "id", "android");

        TextView summaryView = (TextView) view.findViewById(summaryId);
        if (summaryView != null) {
            summaryView.setTextColor(preferences.SecondaryText());
        }

    }

}
