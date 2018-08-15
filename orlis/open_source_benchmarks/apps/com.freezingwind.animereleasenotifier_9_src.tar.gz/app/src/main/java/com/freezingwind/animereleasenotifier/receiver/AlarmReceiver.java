package com.freezingwind.animereleasenotifier.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.freezingwind.animereleasenotifier.R;
import com.freezingwind.animereleasenotifier.ui.animelist.AnimeListActivity;
import com.freezingwind.animereleasenotifier.data.Anime;
import com.freezingwind.animereleasenotifier.updater.AnimeListUpdateCallBack;
import com.freezingwind.animereleasenotifier.updater.AnimeUpdater;
import com.freezingwind.animereleasenotifier.helpers.NetworkManager;

import java.util.ArrayList;

// AlarmReceiver
public class AlarmReceiver extends BroadcastReceiver {
	protected AnimeUpdater updater;

	public AlarmReceiver() {
		updater = new AnimeUpdater(false);
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String userName = sharedPrefs.getString("userName", "");

		//Toast.makeText(context, "Checking anime notifications for: " + userName, Toast.LENGTH_SHORT).show();

		updater.updateByUser(userName, context, new AnimeListUpdateCallBack() {
			@Override
			public void execute() {
				ArrayList<Anime> animeList = updater.getAnimeList();

				for(int i = 0; i < animeList.size(); i++) {
					final Anime anime = animeList.get(i);

					// Notify
					if(!anime.notify)
						continue;

					ImageRequest imageRequest = new ImageRequest(anime.imageURL,
							new Response.Listener<Bitmap>() {
								@Override
								public void onResponse(Bitmap bitmap) {
									Notification.Builder mBuilder =
											new Notification.Builder(context)
													.setSmallIcon(R.drawable.launcher_icon)
													.setAutoCancel(true)
													.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, AnimeListActivity.class), 0))
													.setDefaults(Notification.DEFAULT_SOUND)
													.setLargeIcon(bitmap)
													.setContentTitle(anime.title)
													.setContentText("Episode " + anime.available + " is now available!");

									// Gets an instance of the NotificationManager service
									NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

									int mNotificationId = anime.title.hashCode();
									mNotificationManager.notify(mNotificationId, mBuilder.build());
								}
							}, 0, 0, null,
							new Response.ErrorListener() {
								public void onErrorResponse(VolleyError error) {

								}
							});

					// Execute request
					NetworkManager.getRequestQueue().add(imageRequest);
				}
			}
		});
	}
}
