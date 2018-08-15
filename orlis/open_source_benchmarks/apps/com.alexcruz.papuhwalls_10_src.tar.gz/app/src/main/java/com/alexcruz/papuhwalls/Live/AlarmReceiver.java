package com.alexcruz.papuhwalls.Live;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Daniel Huber on 20.12.2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent updateBroadcast = new Intent(LiveWallpaperService.updateWallAction);
        context.sendBroadcast(updateBroadcast);
    }
}
