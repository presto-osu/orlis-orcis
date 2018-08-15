package com.idunnololz.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.content.Context;

public class Utils {
	
	public static int convertToPixels(Context context, int dps) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dps * scale + 0.5f);
	}
}
