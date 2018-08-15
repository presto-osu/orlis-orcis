package com.freezingwind.animereleasenotifier.ui.adapter;

import android.util.Log;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import com.freezingwind.animereleasenotifier.R;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.freezingwind.animereleasenotifier.controller.AppController;
import com.freezingwind.animereleasenotifier.data.Anime;
import com.freezingwind.animereleasenotifier.helpers.NetworkManager;

import android.graphics.Bitmap;
import android.graphics.Color;

public class AnimeAdapter extends ArrayAdapter<Anime> {
	protected int layout;

	// View lookup cache
	private static class ViewHolder {
		View listItem;
		TextView title;
		TextView airingDate;
		ImageView image;
	}

	public AnimeAdapter(Context context, int layout, ArrayList<Anime> anime) {
		super(context, layout, anime);

		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		final Anime anime = getItem(position);

		// Check if an existing view is being reused, otherwise inflate the view
		final ViewHolder viewHolder; // view lookup cache stored in tag

		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(this.layout, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.title);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
			viewHolder.airingDate = (TextView) convertView.findViewById(R.id.airingDate);
			viewHolder.listItem = convertView.findViewById(R.id.listItem);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// Populate the data into the template view using the data object
		viewHolder.title.setText(anime.title);
		viewHolder.airingDate.setText(anime.airingTimeRemaining);

		// Mark as new
		if(anime.watched < anime.available - anime.offset) {
			viewHolder.listItem.setBackgroundColor(Color.argb(127, 80, 255, 80));
			viewHolder.listItem.setAlpha(1.0f);
		} else if(anime.status == "completed") {
			viewHolder.listItem.setBackgroundColor(Color.TRANSPARENT);
			viewHolder.listItem.setAlpha(1.0f);
		} else {
			viewHolder.listItem.setBackgroundColor(Color.TRANSPARENT);
			viewHolder.listItem.setAlpha(0.2f);
		}

		// Load image from memory cache
		if(anime.image == null)
			anime.image = AppController.imageCache.get(anime.imageURL);

		// Image
		if(anime.image != null) {
			viewHolder.image.setImageBitmap(anime.image);
			viewHolder.image.setVisibility(View.VISIBLE);
		} else if(anime.imageRequest == null) {
			viewHolder.image.setVisibility(View.INVISIBLE);

			anime.imageRequest = new ImageRequest(anime.imageURL,
					new Response.Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap bitmap) {
							anime.image = bitmap;
							AppController.imageCache.put(anime.imageURL, bitmap);

							notifyDataSetChanged();
						}
					}, 0, 0, null,
					new Response.ErrorListener() {
						public void onErrorResponse(VolleyError error) {
							// TODO: ...
							//viewHolder.image.setImageResource(R.drawable.image_load_error);
							anime.imageRequest = null;
						}
					});

			// Execute request
			NetworkManager.getRequestQueue().add(anime.imageRequest);
		} else {
			viewHolder.image.setVisibility(View.INVISIBLE);
		}

		// Return the completed view to render on screen
		return convertView;
	}
}