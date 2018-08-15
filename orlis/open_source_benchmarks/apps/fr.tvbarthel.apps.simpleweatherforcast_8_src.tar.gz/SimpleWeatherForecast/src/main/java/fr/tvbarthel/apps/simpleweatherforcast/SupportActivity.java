package fr.tvbarthel.apps.simpleweatherforcast;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class SupportActivity extends fr.tvbarthel.apps.billing.SupportActivity {

    public static final String EXTRA_BG_COLOR = "fr.tvbarthel.apps.simpleweatherforcast.SupportActivity.Extra.BG_COLOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_BG_COLOR)) {
            final int backgroundColor = intent.getIntExtra(EXTRA_BG_COLOR, getResources().getColor(android.R.color.white));
            getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
        }

    }
}
