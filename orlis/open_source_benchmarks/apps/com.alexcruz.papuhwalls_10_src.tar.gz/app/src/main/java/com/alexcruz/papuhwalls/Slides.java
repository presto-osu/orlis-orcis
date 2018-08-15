package com.alexcruz.papuhwalls;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by mazda on 9/9/15.
 */
public class Slides extends AppIntro {

    private View decorView;

    @Override
    public void init(Bundle bundle) {

        addSlide(AppIntroFragment.newInstance(getString(R.string.first_slide_title), getString(R.string.first_slide_description), R.drawable.ic_intro_first, getResources().getColor(R.color.first_slide)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.second_slide_title), getString(R.string.second_slide_description), R.drawable.ic_intro_second, getResources().getColor(R.color.second_slide)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.third_slide_title), getString(R.string.third_slide_description), R.drawable.ic_intro_third, getResources().getColor(R.color.third_slide)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.fourth_slide_title), getString(R.string.fourth_slide_description), R.drawable.ic_intro_fourth, getResources().getColor(R.color.fourth_slide)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.last_slide_title), getString(R.string.last_slide_description), R.drawable.ic_intro_last, getResources().getColor(R.color.last_slide)));

        showDoneButton(true);
        showSkipButton(true);
        setDepthAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        decorView = getWindow().getDecorView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onDonePressed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean(Preferences.IS_FIRST__RUN, false).commit();
        Intent intent = new Intent(Slides.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSkipPressed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean(Preferences.IS_FIRST__RUN, false).commit();
        Intent intent = new Intent(Slides.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}