package com.freezingwind.animereleasenotifier.helpers;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkManager {
	private static RequestQueue mRequestQueue;

	public static void init(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
	}

	public static RequestQueue getRequestQueue() {
		if(mRequestQueue != null) {
			return mRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}
}
