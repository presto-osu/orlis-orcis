package com.freezingwind.animereleasenotifier.data;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageRequest;

public class Anime {
	public String title;
	public String imageURL;
	public Bitmap image;
	public ImageRequest imageRequest;
	public String url;

	public String animeProviderURL;
	public String nextEpisodeURL;
	public String videoURL;

	public int watched;
	public int available;
	public int max;
	public int offset;

	public String airingTimeRemaining;

	public String status;

	public boolean notify;

	public Anime(
			String title,
			String imageURL,
			String url,
			String animeProviderURL,
			String nextEpisodeURL,
			String videoURL,
			int watched,
			int available,
			int max,
			int offset,
			String airingTimeRemaining,
	        String status
	) {
		this.title = title;
		this.imageURL = imageURL;
		this.url = url;

		this.videoURL = videoURL;
		this.animeProviderURL = animeProviderURL;
		this.nextEpisodeURL = nextEpisodeURL;

		this.watched = watched;
		this.available = available;
		this.max = max;
		this.offset = offset;

		this.airingTimeRemaining = airingTimeRemaining;
		this.status = status;

		this.image = null;
		this.imageRequest = null;
	}

	@Override
	public String toString() {
		return title;
	}
}