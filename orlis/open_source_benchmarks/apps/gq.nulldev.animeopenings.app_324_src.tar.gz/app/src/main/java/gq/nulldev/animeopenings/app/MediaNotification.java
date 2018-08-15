package gq.nulldev.animeopenings.app;

/**
 * Project: AnimeOpenings
 * Created: 19/11/15
 * Author: nulldev
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Music controls!
 */
public class MediaNotification {
    Context context;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder = null;
    RemoteViews remoteView;

    public MediaNotification(Context context, String title, String details, boolean paused) {
        this.context = context;
        //Get notification manager
        if (remoteView == null) {
            remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_music);
            setListeners(remoteView);
        }
        if (builder == null) {
            builder = new NotificationCompat.Builder(context)
                    .setContentTitle("Music Controller")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(false)
                    .setDeleteIntent(PendingIntent.getService(context, 3, new Intent(MediaService.ACTION_EXIT), PendingIntent.FLAG_UPDATE_CURRENT));
            //set the button listeners
            builder.setContent(remoteView);
        }

        remoteView.setTextViewText(R.id.title, title);
        remoteView.setTextViewText(R.id.details, details);
        if (!paused) {
            remoteView.setImageViewResource(R.id.btnPlayPause, R.drawable.ic_pause_24dp);
        } else {
            remoteView.setImageViewResource(R.id.btnPlayPause, R.drawable.ic_play_arrow_24dp);
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Register onclick to open main text
        Intent launchMainApp = new Intent(context, ActivityNewVideo.class);
        PendingIntent launchMainAppPE = PendingIntent.getActivity(context, 10, launchMainApp, 0);
        builder.setContentIntent(launchMainAppPE);

        notificationManager.notify(2, builder.build());
    }

    public void setListeners(RemoteViews view) {
        linkIntentToButton(view, MediaService.ACTION_PREV, 0, R.id.btnPrev);
        linkIntentToButton(view, MediaService.ACTION_PLAYPAUSE, 1, R.id.btnPlayPause);
        linkIntentToButton(view, MediaService.ACTION_NEXT, 2, R.id.btnNext);
    }

    void linkIntentToButton(RemoteViews view, String action, int requestCode, int button) {
        Intent intent = new Intent(action);
        PendingIntent pendingBtnIntent
                = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(button, pendingBtnIntent);
    }

    public void cancel() {
        notificationManager.cancel(2);
    }
}
