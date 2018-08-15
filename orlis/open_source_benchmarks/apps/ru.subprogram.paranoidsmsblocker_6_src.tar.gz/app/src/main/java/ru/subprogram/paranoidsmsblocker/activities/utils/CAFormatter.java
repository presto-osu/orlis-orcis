package ru.subprogram.paranoidsmsblocker.activities.utils;

import ru.subprogram.paranoidsmsblocker.R;
import android.content.Context;

public class CAFormatter {

	private final static int[] UNITS = {R.string.units_b, R.string.units_kb, R.string.units_mb, R.string.units_gb, R.string.units_tb, R.string.units_pb};

	public static String getFileSize(Context context, Integer bytesCount) {
		
		float res = (float)bytesCount;
		int dividedTimes = 0;
		
		while((res > 1000) && (dividedTimes < UNITS.length - 1)) {
			res /= 1024f;
			dividedTimes++;
		}
		
		String format;
		if(dividedTimes == 0) {
			format = "%.0f %s";
		} else {
			format = "%.2f %s";
		}

		return String.format(format, res, context.getString(UNITS[dividedTimes]));
	}
	
}
