package com.freezingwind.animereleasenotifier.updater;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freezingwind.animereleasenotifier.data.Anime;
import com.freezingwind.animereleasenotifier.helpers.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AnimeUpdater {
	protected ArrayList<Anime> animeList;
	protected boolean completedOnly;

	public AnimeUpdater(boolean fetchCompletedOnly) {
		animeList = new ArrayList<Anime>();
		completedOnly = fetchCompletedOnly;
	}

	// GetAnimeList
	public ArrayList<Anime> getAnimeList() {
		return animeList;
	}

	public void update(String response, final Context context, final AnimeListUpdateCallBack callBack) {
		try {
			JSONObject responseObject = new JSONObject(response);
			update(responseObject, context, callBack);
		} catch(JSONException e) {
			Log.d("AnimeUpdater", e.toString());
		}
	}

	public void update(JSONObject response, final Context context, final AnimeListUpdateCallBack callBack) {
		try {
			animeList.clear();

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPrefs.edit();

			JSONArray watchingList = response.getJSONArray("watching");
			for (int i = 0; i < watchingList.length(); i++) {
				JSONObject animeJSON = watchingList.getJSONObject(i);
				JSONObject episodes = animeJSON.getJSONObject("episodes");
				JSONObject airingDate = animeJSON.getJSONObject("airingDate");

				JSONObject animeProvider = null;

				try {
					animeProvider = animeJSON.getJSONObject("animeProvider");
				} catch(JSONException e) {
					//Log.d("AnimeUpdater", "No anime provider available");
				}

				Anime anime = new Anime(
						animeJSON.getString("title"),
						animeJSON.getString("image"),
						animeJSON.getString("url"),
						animeProvider != null ? animeProvider.getString("url") : "",
						animeProvider != null ? animeProvider.getString("nextEpisodeUrl") : "",
						animeProvider != null ? animeProvider.getString("videoUrl") : "",
						episodes.getInt("watched"),
						episodes.getInt("available"),
						episodes.getInt("max"),
						episodes.getInt("offset"),
						airingDate.getString("remainingString"),
						completedOnly ? "completed" : "watching"
				);

				// Load cached episode count
				String key = anime.title + ":episodes-available";
				int availableCached = sharedPrefs.getInt(key, -1);

				anime.notify = anime.available > availableCached && availableCached != -1;

				// Save data in preferences
				editor.putInt(anime.title + ":episodes-available", anime.available);

				// Add to list
				animeList.add(anime);
			}

			// Write preferences
			editor.apply();
		} catch (JSONException e) {
			Log.d("AnimeUpdater", "Error parsing JSON: " + e.toString());
		} finally {
			callBack.execute();
		}
	}

	// Update
	public void updateByUser(String userName, final Context context, final AnimeListUpdateCallBack callBack) {
		String apiUrl = "https://animereleasenotifier.com/api/animelist/" + userName;

		if(completedOnly)
			apiUrl += "&completed=1";

		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		apiUrl += "&animeProvider=" + sharedPrefs.getString("animeProvider", "KissAnime");

		//Toast.makeText(activity, "Loading anime list of " + userName, Toast.LENGTH_SHORT).show();

		final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, (String)null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				update(response, context, callBack);

				// Cache it
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("cachedAnimeListJSON" + (completedOnly ? "Completed" : ""), response.toString());
				editor.apply();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				System.out.println("Error: " + error.toString());
			}
		});

		NetworkManager.getRequestQueue().add(jsObjRequest);
	}
}
