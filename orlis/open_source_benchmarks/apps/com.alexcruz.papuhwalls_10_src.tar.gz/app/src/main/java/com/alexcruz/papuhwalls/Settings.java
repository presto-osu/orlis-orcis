package com.alexcruz.papuhwalls;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;

public class Settings extends ActionBarActivity implements View.OnClickListener {

    private RadioButton minute, hour;
    private NumberPicker numberpicker;
    private Preferences mPrefs;

    Preferences Preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.Preferences = new Preferences(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.muzei);

        mPrefs = new Preferences(Settings.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.muzei_settings));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);


        com.alexcruz.papuhwalls.Preferences.themeMe(this, toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        numberpicker = (NumberPicker) findViewById(R.id.number_picker);
        numberpicker.setMaxValue(100);
        numberpicker.setMinValue(1);
        setDividerColor(numberpicker);

        minute = (RadioButton) findViewById(R.id.minute);
        hour = (RadioButton) findViewById(R.id.hour);
        minute.setOnClickListener(this);
        hour.setOnClickListener(this);

        if (mPrefs.isRotateMinute()) {
            hour.setChecked(false);
            minute.setChecked(true);
            numberpicker.setValue(ConvertMiliToMinute(mPrefs.getRotateTime()));
        } else {
            hour.setChecked(true);
            minute.setChecked(false);
            numberpicker.setValue(ConvertMiliToMinute(mPrefs.getRotateTime()) / 60);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.muzei, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:

                int rotate_time;
                if (minute.isChecked()) {
                    rotate_time = ConvertMinuteToMili(numberpicker.getValue());
                    mPrefs.setRotateMinute(true);
                    mPrefs.setRotateTime(rotate_time);
                } else {
                    rotate_time = ConvertMinuteToMili(numberpicker.getValue()) * 60;
                    mPrefs.setRotateMinute(false);
                    mPrefs.setRotateTime(rotate_time);
                }

                Intent intent = new Intent(Settings.this, ArtSource.class);
                intent.putExtra("service", "restarted");
                startService(intent);
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.minute:
                if (minute.isChecked()) {
                    hour.setChecked(false);
                    minute.setChecked(true);
                }
                break;
            case R.id.hour:
                if (hour.isChecked()) {
                    minute.setChecked(false);
                    hour.setChecked(true);
                }
                break;
        }
    }

    public static int tint(int color, double factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }

    private int ConvertMinuteToMili(int minute) {
        return minute * 60 * 1000;
    }

    private int ConvertMiliToMinute(int mili) {
        return mili / 60 / 1000;
    }

    private void setDividerColor(NumberPicker picker) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, getResources().getDrawable(R.drawable.numberpicker));
                } catch (IllegalArgumentException | IllegalAccessException | Resources.NotFoundException e) {
                }
                break;
            }
        }
    }
}