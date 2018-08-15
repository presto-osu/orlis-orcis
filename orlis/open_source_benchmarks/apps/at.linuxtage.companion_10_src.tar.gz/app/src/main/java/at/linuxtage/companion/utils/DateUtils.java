package at.linuxtage.companion.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	private static final TimeZone AUSTRIA_TIME_ZONE = TimeZone.getTimeZone("Europe/Vienna");

	private static final DateFormat TIME_DATE_FORMAT = withAustriaTimeZone(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault()));

	public static TimeZone getAustriaTimeZone() {
		return AUSTRIA_TIME_ZONE;
	}

	public static DateFormat withAustriaTimeZone(DateFormat format) {
		format.setTimeZone(AUSTRIA_TIME_ZONE);
		return format;
	}

	public static DateFormat getTimeDateFormat() {
		return TIME_DATE_FORMAT;
	}
}
