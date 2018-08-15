package com.easwareapps.transparentwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


/**
 * ॐ
 * लोकाः समस्ताः सुखिनो भवन्तु॥
 * <p/>
 * EmptyWidget
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
public class ConfigureActivity extends AppCompatActivity{

    int mAppWidgetId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_widget);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.select_action));
        setSupportActionBar(toolbar);




    }

    public void selectAction(View v){
        if (v == findViewById(R.id.nothing)){
            saveWidgetAction(DoActionActivity.DO_NOTHING);
        } else if (v == findViewById(R.id.lock_screen)){
            saveWidgetAction(DoActionActivity.LOCK_SCREEN);
        } else if (v == findViewById(R.id.open_app)){
            Intent appSeclector = new Intent(this, AppSelector.class);
            startActivityForResult(appSeclector, DoActionActivity.OPEN_APP);
        } else if (v == findViewById(R.id.toggle_flash)){
            saveWidgetAction(DoActionActivity.TOGGLE_FLASH);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DoActionActivity.OPEN_APP && resultCode == RESULT_OK){

            SharedPreferences.Editor editor = getSharedPreferences(
                    getPackageName(), MODE_PRIVATE).edit();
            String app = data.getStringExtra("app");
            editor.putString("app_" + mAppWidgetId, app);
            editor.apply();

            saveWidgetAction(DoActionActivity.OPEN_APP);
            editor.apply();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveWidgetAction(int action) {

        SharedPreferences.Editor editor = getSharedPreferences(
                getPackageName(), MODE_PRIVATE).edit();
        editor.putInt("action_" + mAppWidgetId, action);
        editor.apply();


        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }




}
