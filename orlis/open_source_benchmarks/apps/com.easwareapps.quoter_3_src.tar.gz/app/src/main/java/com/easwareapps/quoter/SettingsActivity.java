/**
 ************************************** ॐ ***********************************
 ***************************** लोकाः समस्ताः सुखिनो भवन्तु॥**************************
 * <p/>
 * Quoter is a Quotes collection with daily notification and widget
 * Copyright (C) 2016  vishnu
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.easwareapps.quoter;


import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;


public class SettingsActivity extends AppCompatActivity {

    Switch switchNotification;
    TimePicker timePicker;

    public static String ENABLE_DAILY_NOTIFICATION = "daily_notification";

   @TargetApi(23)
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.settings_layout);

       switchNotification = (Switch)findViewById(R.id.switch_daily_quote);
       timePicker = (TimePicker)findViewById(R.id.time_picker);
       try {
           timePicker.setIs24HourView(true);
       }catch (Exception e){

       }
       final SharedPreferences pref = getSharedPreferences(getPackageName(), MODE_PRIVATE);

       if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
           timePicker.setCurrentHour(pref.getInt("hour", 7));
           timePicker.setCurrentMinute(pref.getInt("minute", 0));
       }else {
           timePicker.setHour(pref.getInt("hour", 7));
           timePicker.setMinute(pref.getInt("minute", 0));
       }


       timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
           @Override
           public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

               SharedPreferences.Editor editor = pref.edit();
               editor.putInt("hour", hourOfDay);
               editor.putInt("minute", minute);
               editor.apply();
               setAlarm();

           }
       });




       switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("daily_notification", isChecked);
                editor.apply();
                changeTimePickerState(isChecked);
           }
       });

       changeTimePickerState(pref.getBoolean(ENABLE_DAILY_NOTIFICATION, true));
       Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);

       toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
       toolbar.setNavigationOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {
               // TODO Auto-generated method stub
               finish();
           }
       });


       //new EAReceiver().showNotification(getApplicationContext());
   }

    private void changeTimePickerState(boolean state){
        switchNotification.setChecked(state);
        if(!state){
            timePicker.setVisibility(View.GONE);
            new EANotificationManager().cancelAlarm(getApplicationContext());
        }else{
            timePicker.setVisibility(View.VISIBLE);
            setAlarm();
        }
    }

    private void setAlarm(){
        EANotificationManager eanm = new EANotificationManager();
        eanm.cancelAlarm(getApplicationContext());
        eanm.setAlarm(getApplicationContext());
    }

}
