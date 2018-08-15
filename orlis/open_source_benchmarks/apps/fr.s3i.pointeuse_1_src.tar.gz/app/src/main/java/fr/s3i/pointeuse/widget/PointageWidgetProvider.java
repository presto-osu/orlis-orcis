/*
 * Oburo.O est un programme destinée à saisir son temps de travail sur un support Android.
 *
 *     This file is part of Oburo.O
 *     Oburo.O is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package fr.s3i.pointeuse.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import fr.s3i.pointeuse.R;
import fr.s3i.pointeuse.persistance.DatabaseHelper;
import fr.s3i.pointeuse.service.Rafraichissement;

public class PointageWidgetProvider extends AppWidgetProvider {
    public static String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
    public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
    public static String ACTION_START_REFRESH_WIDGET = "ACTION_START_REFRESH_WIDGET";

    String message;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Cursor dernierEnregistrement;

    private static PendingIntent pendingIntent;
    private static AlarmManager alarmManager;
    final static public int PERIODE = 60;

    private boolean pointageEnCours = false;
    int[] appWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.appWidgetIds = appWidgetIds;
        int N = appWidgetIds.length;
        android.util.Log.d("onUpdate=true", "onUpdate N=");
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        pointageEnCours = false;

    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prepare widget views
        android.util.Log.d("UPDATEAPP", "début de l'update app widget");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
        Intent active = new Intent(context, PointageWidgetProvider.class);
        active.setAction(ACTION_WIDGET_RECEIVER);

        active.putExtra("msg", "Pointage");
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
        remoteViews.setOnClickPendingIntent(R.id.monbouttonwidget, actionPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        // démarrage du service
        Intent monService;
        monService = new Intent(context, Rafraichissement.class);
        context.startService(monService);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();

//        if (alarmManager == null) {
//            monService = new Intent(context, Rafraichissement.class);
//            pendingIntent = PendingIntent.getService(context, 0, monService, 0);
//            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        }

        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            final int appWidgetId = intent.getExtras().getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                this.onDeleted(context, new int[]{appWidgetId});
            }
        } else {
            if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
                if (pointageEnCours) {
                    return;
                }
                pointageEnCours = true;
                pointe(context);

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                Notification notification = builder.setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.icon).setTicker(message).setWhen(System.currentTimeMillis())
                        .setAutoCancel(true).setContentTitle("Notice")
                        .setContentText(message).build();
                notificationManager.notify(1, notification);

                pointageEnCours = false;

            } else {
                android.util.Log.d("RECEIVE", "démarrage ??");

            }
            Intent monService = new Intent(context, Rafraichissement.class);
            pendingIntent = PendingIntent.getService(context, 0, monService, 0);
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Intent intent = new Intent(context, PointageWidgetProvider.class);
            intent.setAction(ACTION_START_REFRESH_WIDGET);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pi);
            // redémarrage du service
            monService = new Intent(context, Rafraichissement.class);
            context.startService(monService);


        }
    }


    public void pointe(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        dernierEnregistrement = dbHelper.getLastEnregistrementPointage(db);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        try {
            if (dernierEnregistrement.getCount() == 0) {
                android.util.Log.d("pointage widget", "tentative d'insertion base vide");
                dbHelper.insereNouveauPointage(db, dateFormat.format(date), "");
                dateFormat = new SimpleDateFormat("HH:mm");
                message = context.getString(R.string.debutpointage) + " " + dateFormat.format(date);

            } else if (dernierEnregistrement.getString(2).length() != 0) {
                android.util.Log.d("pointage widget", "tentative d'insertion tous pointage clos");
                dbHelper.insereNouveauPointage(db, dateFormat.format(date), "");
                dateFormat = new SimpleDateFormat("HH:mm");
                message = context.getString(R.string.debutpointage) + " " + dateFormat.format(date);
            } else {
                android.util.Log.d("pointage widget", "tentative d'update de cloture pointage");
                dbHelper.updateEnregistrementPointage(db, dernierEnregistrement.getLong(0), dbHelper.DATE_FIN, dateFormat.format(date));
                dateFormat = new SimpleDateFormat("HH:mm");
                message = context.getString(R.string.finpointage) + " " + dateFormat.format(date);
            }
        } catch (Exception e) {
            android.util.Log.w("Exception Pointage=", "message = " + e.getMessage());
        }

        if (!dernierEnregistrement.isClosed()) {
            dernierEnregistrement.close();
        }

        db.close();
        dbHelper.close();

    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        context.stopService(new Intent(context, Rafraichissement.class));
        //alarmManager.cancel(pendingIntent);
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {

        context.stopService(new Intent(context, Rafraichissement.class));
        super.onDisabled(context);
    }


}
