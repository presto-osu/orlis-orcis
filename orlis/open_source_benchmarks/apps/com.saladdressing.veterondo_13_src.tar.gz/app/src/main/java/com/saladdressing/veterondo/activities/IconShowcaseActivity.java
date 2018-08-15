package com.saladdressing.veterondo.activities;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.saladdressing.veterondo.R;
import com.saladdressing.veterondo.utils.Constants;
import com.saladdressing.veterondo.utils.SPS;

public class IconShowcaseActivity extends AppCompatActivity {

    SPS sps;
    RelativeLayout showcaseLayout;
    TextView description;
    ImageView iconShow;
    Typeface robotoSlab;
    Typeface ailerons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sps = new SPS(this);
        int sentIcon = sps.getPrefs().getInt(Constants.ICON_TO_SHOW, R.drawable.cloud_refresh);
        String sentDescription = sps.getPrefs().getString(Constants.DESC_TO_SHOW, "nothing to see here");

        setContentView(R.layout.activity_icon_showcase);

        robotoSlab = Typeface.createFromAsset(getAssets(), "fonts/RobotoSlab-Light.ttf");
        ailerons  = Typeface.createFromAsset(getAssets(), "fonts/Ailerons-Typeface.otf");

        description = (TextView) findViewById(R.id.weather_description);
        iconShow = (ImageView) findViewById(R.id.showcased_icon);
        showcaseLayout = (RelativeLayout) findViewById(R.id.showcase_layout);

        description.setTypeface(ailerons);

        iconShow.setImageResource(sentIcon);
        description.setText(sentDescription);

        iconShow.animate().setStartDelay(1000).setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator()).alpha(1.0f).start();
        description.animate().setStartDelay(1500).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).alpha(1.0f).start();
        showcaseLayout.animate().setStartDelay(16000).setDuration(500).alpha(0.0f).setInterpolator(new AccelerateDecelerateInterpolator()).start();

        Handler handler = new Handler();

        makeFullscreen();
        keepScreenOn();

        Runnable launchMainActivityRunnable = new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(IconShowcaseActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(intent);
                overridePendingTransition(0,0);

            }
        };

        handler.postDelayed(launchMainActivityRunnable, 20 * 1000);


    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void makeFullscreen() {
        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());

    }


}
