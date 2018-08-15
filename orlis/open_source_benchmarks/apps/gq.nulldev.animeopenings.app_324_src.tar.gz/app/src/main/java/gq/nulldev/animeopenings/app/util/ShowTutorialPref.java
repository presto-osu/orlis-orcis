package gq.nulldev.animeopenings.app.util;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import gq.nulldev.animeopenings.app.SettingsActivity;

/**
 * Project: AnimeOpenings
 * Created: 07/03/16
 * Author: nulldev
 */
public class ShowTutorialPref extends Preference {
    public ShowTutorialPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShowTutorialPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowTutorialPref(Context context) {
        super(context);
    }

    @Override protected void onClick() {
        super.onClick();

        if(getContext() instanceof SettingsActivity) {
            SettingsActivity activity = (SettingsActivity) getContext();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(TutorialView.PREF_SHOW_CONTROLS_TUTORIAL, true).commit();
            activity.finish();
        }
    }
}
