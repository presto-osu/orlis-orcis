package com.linuxcounter.lico_update_003;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alex on 17.05.15.
 */
public class StartBackgroundServiceAtBootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, UpdateInBackgroundService.class);
            context.startService(i);
        }
    }
}
