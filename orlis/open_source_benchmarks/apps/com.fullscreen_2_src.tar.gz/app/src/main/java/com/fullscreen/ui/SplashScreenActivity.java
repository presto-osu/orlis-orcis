package com.fullscreen.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import com.example.webview.R;
import com.fullscreen.utils.Constants;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        getSplashScreenCountDownTimer().start();
    }

    private CountDownTimer getSplashScreenCountDownTimer() {
        return new CountDownTimer(Constants.SPLASH_SCREEN_DURATION, Constants.SPLASH_SCREEN_INTERVAL) {

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }
}
