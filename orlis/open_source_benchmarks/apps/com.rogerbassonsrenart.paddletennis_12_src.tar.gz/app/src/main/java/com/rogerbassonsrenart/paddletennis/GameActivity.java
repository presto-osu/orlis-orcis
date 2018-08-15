package com.rogerbassonsrenart.paddletennis;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class GameActivity extends Activity {

    Game g_;
    GameView gv_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setFullscreen();

        g_ = new Game();
        gv_ = new GameView(this, g_);
        setContentView(gv_);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gv_.stopView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullscreen();
        gv_ = new GameView(this, g_);
        setContentView(gv_);
    }

    private void setFullscreen() {
        if (Build.VERSION.SDK_INT < 19) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}
