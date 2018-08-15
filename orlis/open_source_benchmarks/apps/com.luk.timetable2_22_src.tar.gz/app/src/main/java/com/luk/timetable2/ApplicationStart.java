package com.luk.timetable2;

import android.app.Application;
import android.content.Intent;

import com.luk.timetable2.services.RegisterReceivers;

/**
 * Created by luk on 10/15/15.
 */
public class ApplicationStart extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Start services
        sendBroadcast(new Intent(this, RegisterReceivers.class));
    }
}
