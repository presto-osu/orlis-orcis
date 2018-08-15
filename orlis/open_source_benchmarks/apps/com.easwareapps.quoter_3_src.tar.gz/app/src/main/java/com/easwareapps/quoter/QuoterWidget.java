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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class QuoterWidget extends AppWidgetProvider {

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        //called when widgets are deleted
        //see that you get an array of widgetIds which are deleted
        //so handle the delete of multiple widgets in an iteration
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //runs when all of the instances of the widget are deleted from
        //the home screen
        //here you can do some setup
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //runs when all of the first instance of the widget are placed
        //on the home screen
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {


        for(int currentWidgetId: appWidgetIds){
            ComponentName widget = new ComponentName(context, QuoterWidget.class);
            appWidgetManager.updateAppWidget(widget, new QuoteProvider(context).getViewAt(currentWidgetId));


        }


        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }






}
