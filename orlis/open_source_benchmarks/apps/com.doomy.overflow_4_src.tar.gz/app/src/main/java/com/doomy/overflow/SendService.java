/**
 * Copyright (C) 2013 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.overflow;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

public class SendService extends Service{

    private static final String TAG = "SendService";
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private String mFullName;
    private String mPhoneNumber;
    private String mMessage;
    private int mQuantity;
    private int mDelay;

    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle mExtra = intent.getExtras();

        mFullName = mExtra.getString("fullname");
        mPhoneNumber = mExtra.getString("phonenumber");
        mMessage = mExtra.getString("message");
        mQuantity = mExtra.getInt("quantity");
        mDelay = mExtra.getInt("delay");

        Intent mIntent = new Intent(SendService.this, MainActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(SendService.this, 0, mIntent, 0);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.sending))
                .setSmallIcon(R.drawable.ic_overflow)
                .setColor(getResources().getColor(R.color.blueGrey))
                .setContentIntent(mPendingIntent)
                .setAutoCancel(true);

        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    mBuilder.setProgress(0, 0, true);
                    mNotifyManager.notify(0, mBuilder.build());
                    for (int i = 0; i < mQuantity; i++) {
                        try {
                            Thread.sleep(mDelay * 1000);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Fail");
                        }
                        sendMessage(mPhoneNumber, mMessage);
                        mBuilder.setContentInfo(i+1+"");
                        mNotifyManager.notify(0, mBuilder.build());
                    }
                    mBuilder.setContentText(getString(R.string.go) + " " + mFullName)
                            .setContentInfo(mQuantity+"")
                            .setProgress(0, 0, false);
                    mNotifyManager.notify(0, mBuilder.build());
                }
            }
        ).start();

        return START_STICKY;
    }

    private void sendMessage(String myPhoneNumber, String myMessage) {

        try {
            SmsManager mSmsManager = SmsManager.getDefault();
            mSmsManager.sendTextMessage(myPhoneNumber, null, myMessage, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}