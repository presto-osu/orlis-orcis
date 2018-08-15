package org.toulibre.cdl.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	private static final TimeZone FRENCH_TIME_ZONE = TimeZone.getTimeZone("GMT+1");

	private static final DateFormat TIME_DATE_FORMAT = withFrenchTimeZone(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault()));

	public static TimeZone getFrenchTimeZone() {
		return FRENCH_TIME_ZONE;
	}

	public static DateFormat withFrenchTimeZone(DateFormat format) {
		format.setTimeZone(FRENCH_TIME_ZONE);
		return format;
	}

	public static DateFormat getTimeDateFormat() {
		return TIME_DATE_FORMAT;
	}
}
