package com.idunnololz.utils;

import android.util.Log;

public class LogUtils {
	private final static boolean LOG_ENABLED = false;
	public static void d(String tag, String msg) {
		if (LOG_ENABLED)
			Log.d(tag, msg);
	}
	
	public static void e(String tag, String msg) {
		if (LOG_ENABLED)
			Log.e(tag, msg);
	}
	
	public static void e(String tag, String msg, Throwable e) {
		if (LOG_ENABLED)
			Log.e(tag, msg, e);
	}
}
