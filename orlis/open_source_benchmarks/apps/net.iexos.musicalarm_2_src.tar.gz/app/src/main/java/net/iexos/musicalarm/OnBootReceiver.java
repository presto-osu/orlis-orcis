package net.iexos.musicalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {
    public OnBootReceiver() {
    }

    @Override
    public void onReceive(Context con, Intent intent) {
        if (!AlarmUtils.isAlarmSet(con)) return;
        AlarmUtils.setAlarm(con, AlarmUtils.getTriggerTime(con));
    }
}
