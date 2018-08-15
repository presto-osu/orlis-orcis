package com.easwareapps.transparentwidget;

import android.Manifest;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

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
public class EmptyWidgetAppProvider extends AppWidgetProvider {

    final int DELAY = 500;
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences pref = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        for(int appWidgetId: appWidgetIds) {
            editor.remove("action_" + appWidgetId);
            editor.remove("click_time_" + appWidgetId);
            try {
                editor.remove("action_" + appWidgetId);
            }catch (Exception e){

            }
            try {
                editor.remove("init_" + appWidgetId);
            }catch (Exception e){

            }
        }
        editor.apply();

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //runs when all of the first instance of the widget are placed
        //on the home screen
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("Click")) {
            Bundle extras = intent.getExtras();
            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            SharedPreferences pref = context.getSharedPreferences(context.getPackageName(),
                    Context.MODE_PRIVATE);
            long time = pref.getLong("click_time_" + widgetId, -1);
            if (time != -1) {
                if ((System.currentTimeMillis() - time < DELAY)) {
                    int action = pref.getInt("action_" +  widgetId, 0);
                    doAction(context, action, widgetId);

                }
            }
            pref.edit().putLong("click_time_" + widgetId, System.currentTimeMillis()).commit();
        }else if(intent.getAction().equals("DONE")) {
            Bundle extras = intent.getExtras();
            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            SharedPreferences pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            pref.edit().putBoolean("init_" + widgetId, true).apply();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, new int[]{widgetId});
        }

        super.onReceive(context, intent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        for(int appWidget: appWidgetIds) {


            SharedPreferences pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);


            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.empty_widget);

            Intent intent = new Intent(context, getClass());
            intent.setAction("Click");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidget);
            intent.putExtra("WIDGET_ID", appWidget);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidget,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.empty_view, pendingIntent);





            if ( !pref.getBoolean("init_" + appWidget, false)) {
                views.setViewVisibility(R.id.done, View.VISIBLE);
                Intent done = new Intent(context, getClass());
                done.setAction("DONE");
                done.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidget);
                done.putExtra("WIDGET_ID", appWidget);

                PendingIntent pendingDoneIntent = PendingIntent.getBroadcast(context, appWidget,
                        done, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.done, pendingDoneIntent);

            } else {
                views.setViewVisibility(R.id.done, View.GONE);
                if (pref.getBoolean("show_all", false)) {
                    views.setViewVisibility(R.id.image, View.VISIBLE);
                } else {
                    views.setViewVisibility(R.id.image, View.GONE);
                }
            }

            appWidgetManager.updateAppWidget(appWidget, views);
            super.onUpdate(context, appWidgetManager, new int[]{appWidget});


        }

    }

    private void doAction(Context context, int action, int widget){

        switch (action) {
            case DoActionActivity.TOGGLE_FLASH:
                if (toggleFlash(context)) return;
                break;
            case DoActionActivity.LOCK_SCREEN:
                if(lockNow(context))   return;
                break;
            case  DoActionActivity.DO_NOTHING:
                return;
            case  DoActionActivity.OPEN_APP:
                String option = context.getSharedPreferences(context.getPackageName(),
                        Context.MODE_PRIVATE).getString("app_" + widget, "");

                String packageName = option.split(",")[0];
                String activityName = "";
                if(option.split(",").length >= 2){
                    activityName = option.split(",")[1];
                }
                openActivity(context, packageName, activityName);
                return;
            default:
                break;
        }

        Intent actionIntent = new Intent(context, DoActionActivity.class);
        actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actionIntent.putExtra("action", action);
        context.startActivity(actionIntent);

    }

    private boolean toggleFlash(Context context){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int hasStoragePermission = context.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
                //No Permission
                return false;
            }
        }
        Intent i = new Intent(context, DoActionService.class);
        i.setAction(DoActionActivity.TOGGLE_FLASH + "");
        context.startService(i);
        return  true;

    }

    private boolean lockNow(Context context){

        ComponentName mAdminName = new ComponentName(context, AdminManageReceiver.class);;
        DevicePolicyManager mDPM  = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(mDPM.isAdminActive(mAdminName)){
            mDPM.lockNow();
            return  true;
        }else {
            return false;
        }
    }

    private void openActivity(Context context, String packageName, String activityName){
        try{
            Intent launchIntent = new Intent(Intent.ACTION_MAIN, null);
            launchIntent.setComponent(new ComponentName(packageName, activityName));
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        }catch (Exception e){

        }
    }

}


