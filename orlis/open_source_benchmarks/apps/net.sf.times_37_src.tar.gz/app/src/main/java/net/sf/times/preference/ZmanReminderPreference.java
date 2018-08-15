package net.sf.times.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Zman reminder preference.
 */
public class ZmanReminderPreference extends ListPreference {

    public ZmanReminderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZmanReminderPreference(Context context) {
        super(context);
    }

    @Override
    public boolean shouldDisableDependents() {
        return super.shouldDisableDependents() || isOff();
    }

    public boolean isOff() {
        return TextUtils.isEmpty(getValue());
    }
}
