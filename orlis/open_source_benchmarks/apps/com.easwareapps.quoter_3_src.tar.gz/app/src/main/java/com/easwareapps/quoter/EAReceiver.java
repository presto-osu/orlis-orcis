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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class EAReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            new EANotificationManager().setAlarmIfNeeded(context);
            return;
        }else if(intent.getAction()!=null && intent.getAction().equals("clear")){
            new EANotificationManager().clearNotification(context);
            return;
        }

        new EANotificationManager().showNotification(context);



    }



}