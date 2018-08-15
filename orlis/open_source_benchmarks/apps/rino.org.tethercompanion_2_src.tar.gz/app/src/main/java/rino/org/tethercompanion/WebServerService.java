package rino.org.tethercompanion;
/*
 * This is the source code of Tether companion for Android.
 * It is licensed under GNU GPL v. 3 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Rinat Kurmaev, 2015-2016.
 */
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.IOException;

public class WebServerService extends Service {
    MyHttpServer ws;

    public WebServerService() {
    }

    @Override
    public void onCreate()
    {
        ws = new MyHttpServer(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            ws.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        makeNotify(ws.getWifiApIpAddress()+":8000", true);
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy()
    {
        ws.stop();
        makeNotify(null, false);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void makeNotify(String ip, boolean start)
    {
        int NOTIFY_ID = 101;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification;
        NotificationManagerCompat notificationManager;
        if(start) {

            // оставим только самое необходимое
            builder.setContentIntent(null)
                    .setSmallIcon(R.drawable.accesspoint)
                    .setContentTitle("Tether Companion")
                    .setContentText(ip)
                    .setOngoing(true);

            notification = builder.build();

            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFY_ID, notification);
        }
        else
        {
            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(NOTIFY_ID);
        }
    }
}
