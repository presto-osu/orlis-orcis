package gq.nulldev.animeopenings.app.util;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import gq.nulldev.animeopenings.app.ActivityPlaylistBuilder;

/**
 * Project: AnimeOpenings
 * Created: 13/11/15
 * Author: nulldev
 */
public class OpenPBPref extends Preference {

    public OpenPBPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OpenPBPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpenPBPref(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        super.onClick();

        Intent openPeIntent = new Intent(this.getContext(), ActivityPlaylistBuilder.class);
        this.getContext().startActivity(openPeIntent);
    }
}
