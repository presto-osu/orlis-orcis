package gq.nulldev.animeopenings.app.util;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import gq.nulldev.animeopenings.app.ActivityPE;

/**
 * Project: AnimeOpenings
 * Created: 13/11/15
 * Author: nulldev
 */
public class OpenPEPref extends Preference {

    public OpenPEPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OpenPEPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpenPEPref(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        super.onClick();

        Intent openPeIntent = new Intent(this.getContext(), ActivityPE.class);
        this.getContext().startActivity(openPeIntent);
    }
}
