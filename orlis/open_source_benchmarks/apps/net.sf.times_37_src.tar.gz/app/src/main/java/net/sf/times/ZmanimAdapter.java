/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for halachic times list.
 * <p>
 * See also Wikipedia article on <a
 * href="http://en.wikipedia.org/wiki/Zmanim">Zmanim</a>.
 *
 * @author Moshe Waisberg
 */
public class ZmanimAdapter extends ArrayAdapter<ZmanimItem> {

    /** 12 hours (half of a full day). */
    protected static final long TWELVE_HOURS = DateUtils.DAY_IN_MILLIS >> 1;
    /** 6 hours (quarter of a full day). */
    protected static final long SIX_HOURS = DateUtils.DAY_IN_MILLIS >> 2;

    /** Holiday id for Shabbath. */
    public static final int SHABBATH = 100;

    /** No candles to light. */
    private static final int CANDLES_NONE = 0;
    /** Number of candles to light for Shabbath. */
    private static final int CANDLES_SHABBATH = 2;
    /** Number of candles to light for a festival. */
    private static final int CANDLES_FESTIVAL = 2;
    /** Number of candles to light for Yom Kippurim. */
    private static final int CANDLES_YOM_KIPPUR = 1;

    /** Flag indicating lighting times before sunset. */
    private static final int BEFORE_SUNSET = 0x00000000;
    /** Flag indicating lighting times at sunset. */
    private static final int AT_SUNSET = 0x10000000;
    /** Flag indicating lighting times at twilight. */
    private static final int AT_TWILIGHT = 0x20000000;
    /** Flag indicating lighting times after nightfall. */
    private static final int AT_NIGHT = 0x40000000;
    /** Flag indicating lighting times after Shabbath. */
    private static final int MOTZE_SHABBATH = AT_NIGHT;

    protected static final int CANDLES_MASK = 0x0000000F;
    protected static final int HOLIDAY_MASK = 0x000000FF;
    protected static final int MOTZE_MASK = 0xF0000000;

    protected static final String OPINION_10_2 = "10.2";
    protected static final String OPINION_11 = "11";
    protected static final String OPINION_12 = "12";
    protected static final String OPINION_120 = "120";
    protected static final String OPINION_120_ZMANIS = "120_zmanis";
    protected static final Object OPINION_13 = "13.24";
    protected static final String OPINION_15 = "15";
    protected static final String OPINION_16_1 = "16.1";
    protected static final String OPINION_16_1_ALOS = "16.1_alos";
    protected static final String OPINION_16_1_SUNSET = "16.1_sunset";
    protected static final String OPINION_18 = "18";
    protected static final String OPINION_19_8 = "19.8";
    protected static final String OPINION_2 = "2";
    protected static final String OPINION_26 = "26";
    protected static final String OPINION_3 = "3";
    protected static final String OPINION_3_65 = "3.65";
    protected static final String OPINION_3_676 = "3.676";
    protected static final String OPINION_30 = "30";
    protected static final String OPINION_4_37 = "4.37";
    protected static final String OPINION_4_61 = "4.61";
    protected static final String OPINION_4_8 = "4.8";
    protected static final String OPINION_5_88 = "5.88";
    protected static final String OPINION_5_95 = "5.95";
    protected static final Object OPINION_58 = "58.5";
    protected static final String OPINION_6 = "6";
    protected static final String OPINION_60 = "60";
    protected static final String OPINION_7 = "7";
    protected static final String OPINION_7_083 = "7.083";
    protected static final String OPINION_72 = "72";
    protected static final String OPINION_72_ZMANIS = "72_zmanis";
    protected static final String OPINION_8_5 = "8.5";
    protected static final String OPINION_90 = "90";
    protected static final String OPINION_90_ZMANIS = "90_zmanis";
    protected static final String OPINION_96 = "96";
    protected static final String OPINION_96_ZMANIS = "96_zmanis";
    protected static final String OPINION_ATERET = "AT";
    protected static final String OPINION_GRA = "GRA";
    protected static final String OPINION_MGA = "MGA";
    protected static final String OPINION_FIXED = "fixed";
    protected static final String OPINION_LEVEL = "level";
    protected static final String OPINION_SEA = "sea";
    private static final String OPINION_TWILIGHT = "twilight";
    private static final String OPINION_NIGHT = "nightfall";

    /** The day of the month as a decimal number (range 01 to 31). */
    private static final String DAY_PAD_VAR = "%d";
    /** The day of the month as a decimal number (range 1 to 31). */
    private static final String DAY_VAR = "%-e";
    /** The full month name according to the current locale. */
    private static final String MONTH_VAR = "%B";
    /** The year as a decimal number including the century. */
    private static final String YEAR_VAR = "%Y";

    /** Unknown date. */
    public static final long NEVER = Long.MIN_VALUE;

    protected final LayoutInflater inflater;
    protected final ZmanimSettings settings;
    protected final ComplexZmanimCalendar calendar;
    protected boolean inIsrael;
    protected long now = System.currentTimeMillis();
    protected boolean summaries;
    protected boolean elapsed;
    private DateFormat timeFormat;
    private Comparator<ZmanimItem> comparator;
    private HebrewDateFormatter hebrewDateFormatter;
    private String[] monthNames;
    private String monthDayYear;
    private String omerFormat;

    /**
     * Time row item.
     */
    protected static class ZmanimItem implements Comparable<ZmanimItem> {

        /** The row id. */
        public int rowId;
        /** The title id. */
        public int titleId;
        /** The summary. */
        public CharSequence summary;
        /** The time text id. */
        public int timeId;
        /** The time. */
        public long time;
        /** The time label. */
        public CharSequence timeLabel;
        /** Has the time elapsed? */
        public boolean elapsed;
        /** Emphasize? */
        public boolean emphasis;

        /** Creates a new row item. */
        public ZmanimItem() {
        }

        @Override
        public int compareTo(ZmanimItem that) {
            long t1 = this.time;
            long t2 = that.time;
            if (t1 != t2)
                return (t1 < t2) ? -1 : +1;
            int c = this.rowId - that.rowId;
            if (c != 0)
                return c;
            return this.titleId - that.titleId;
        }
    }

    /**
     * Compare two time items.
     */
    protected static class ZmanimComparator implements Comparator<ZmanimItem> {
        @Override
        public int compare(ZmanimItem lhs, ZmanimItem rhs) {
            return lhs.compareTo(rhs);
        }
    }

    /**
     * Creates a new adapter.
     *
     * @param context
     *         the context.
     * @param settings
     *         the application settings.
     */
    public ZmanimAdapter(Context context, ZmanimSettings settings) {
        super(context, R.layout.times_item);
        this.inflater = LayoutInflater.from(context);
        this.settings = settings;
        this.calendar = new ComplexZmanimCalendar();

        if (settings.isSeconds()) {
            boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
            String pattern = context.getString(time24 ? R.string.twenty_four_hour_time_format : R.string.twelve_hour_time_format);
            this.timeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        } else {
            this.timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        }
    }

    /**
     * Get the calendar.
     *
     * @return the calendar.
     */
    public ComplexZmanimCalendar getCalendar() {
        return calendar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.times_item);
    }

    /**
     * Bind the item to the view.
     *
     * @param position
     *         the row index.
     * @param convertView
     *         the view.
     * @param parent
     *         the parent view.
     * @param resource
     *         the resource layout.
     * @return the item view.
     */
    protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        ZmanimItem item = getItem(position);
        boolean enabled = !item.elapsed;

        View view = convertView;
        ViewHolder holder;
        TextView title;
        TextView summary;
        TextView time;

        if (view == null) {
            view = inflater.inflate(resource, parent, false);

            title = (TextView) view.findViewById(android.R.id.title);
            summary = (TextView) view.findViewById(android.R.id.summary);
            time = (TextView) view.findViewById(R.id.time);

            holder = new ViewHolder(title, summary, time);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            title = holder.title;
            summary = holder.summary;
            time = holder.time;
        }
        view.setEnabled(enabled);
        view.setTag(R.id.time, item);

        title.setText(item.titleId);
        title.setEnabled(enabled);
        if (item.emphasis) {
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * 1.25f);
        }

        if (summary != null) {
            summary.setText(item.summary);
            summary.setEnabled(enabled);
            if (!summaries || (item.summary == null))
                summary.setVisibility(View.GONE);
        }

        time.setText(item.timeLabel);
        time.setEnabled(enabled);
        if (item.emphasis) {
            time.setTypeface(time.getTypeface(), Typeface.BOLD);
            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, time.getTextSize() * 1.25f);
        }

        return view;
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time.
     */
    public void add(int titleId, int summaryId, Date time) {
        add(titleId, summaryId, time == null ? NEVER : time.getTime());
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time in milliseconds..
     */
    public void add(int titleId, int summaryId, long time) {
        add(titleId, (summaryId == 0) ? null : getContext().getText(summaryId), time);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summary
     *         the summary label.
     * @param time
     *         the time
     */
    public void add(int titleId, CharSequence summary, Date time) {
        add(titleId, summary, time == null ? NEVER : time.getTime());
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summary
     *         the summary label.
     * @param time
     *         the time in milliseconds.
     */
    public void add(int titleId, CharSequence summary, long time) {
        add(0, titleId, summary, titleId, time);
    }

    /**
     * Adds the item to the array for a valid date.
     *
     * @param rowId
     *         the row id for remote views?
     * @param titleId
     *         the row layout id.
     * @param timeId
     *         the time text id to set for remote views.
     * @param time
     *         the time.
     */
    public void add(int rowId, int titleId, int timeId, Date time) {
        add(rowId, titleId, timeId, time == null ? NEVER : time.getTime());
    }

    /**
     * Adds the item to the array for a valid date.
     *
     * @param rowId
     *         the row id for remote views?
     * @param titleId
     *         the row layout id.
     * @param timeId
     *         the time text id to set for remote views.
     * @param time
     *         the time in milliseconds..
     */
    public void add(int rowId, int titleId, int timeId, long time) {
        add(rowId, titleId, null, timeId, time);
    }

    /**
     * Adds the item to the array for a valid date.
     *
     * @param rowId
     *         the row id for remote views?
     * @param titleId
     *         the row layout id.
     * @param summary
     *         the summary label.
     * @param timeId
     *         the time text id to set for remote views.
     * @param time
     *         the time in milliseconds..
     */
    private void add(int rowId, int titleId, CharSequence summary, int timeId, long time) {
        ZmanimItem item = new ZmanimItem();
        item.rowId = rowId;
        item.titleId = titleId;
        item.summary = summary;
        item.timeId = timeId;
        item.time = time;
        item.emphasis = settings.isEmphasis(titleId);

        if (time == NEVER) {
            item.timeLabel = null;
            item.elapsed = true;
        } else {
            item.timeLabel = timeFormat.format(time);
            if (rowId != 0)
                item.elapsed = (time < now);
            else
                item.elapsed = (elapsed || (titleId == R.string.hour)) ? false : (time < now);
        }

        if ((time != NEVER) || (rowId != 0)) {
            add(item);
        }
    }

    protected void prePopulate() {
        clear();

        ZmanimSettings settings = this.settings;

        summaries = settings.isSummaries();
        elapsed = settings.isPast();

        Context context = getContext();
        if (settings.isSeconds()) {
            boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
            String pattern = context.getString(time24 ? R.string.twenty_four_hour_time_format : R.string.twelve_hour_time_format);
            timeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        } else {
            timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        }

        now = System.currentTimeMillis();
    }

    /**
     * Populate the list of times.
     *
     * @param remote
     *         is for remote views?
     */
    public void populate(boolean remote) {
        prePopulate();

        ComplexZmanimCalendar cal = getCalendar();
        Calendar gcal = cal.getCalendar();
        JewishCalendar jcal = getJewishCalendar();
        int candlesOffset = settings.getCandleLightingOffset();
        int candles = getCandles(jcal);
        int candlesCount = candles & CANDLES_MASK;
        boolean hasCandles = candlesCount > 0;
        int candlesHow = candles & MOTZE_MASK;
        int holidayTomorrow = (candles >> 4) & HOLIDAY_MASK;
        int holidayToday = (candles >> 12) & HOLIDAY_MASK;
        Date dateCandles;

        Date date;
        int summary;
        String opinion;
        final Resources res = getContext().getResources();

        if (!remote && settings.isHour()) {
            long time;
            opinion = settings.getHour();
            if (OPINION_19_8.equals(opinion)) {
                time = cal.getShaahZmanis19Point8Degrees();
                summary = R.string.hour_19;
            } else if (OPINION_120.equals(opinion)) {
                time = cal.getShaahZmanis120Minutes();
                summary = R.string.hour_120;
            } else if (OPINION_120_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis120MinutesZmanis();
                summary = R.string.hour_120_zmanis;
            } else if (OPINION_18.equals(opinion)) {
                time = cal.getShaahZmanis18Degrees();
                summary = R.string.hour_18;
            } else if (OPINION_26.equals(opinion)) {
                time = cal.getShaahZmanis26Degrees();
                summary = R.string.hour_26;
            } else if (OPINION_16_1.equals(opinion)) {
                time = cal.getShaahZmanis16Point1Degrees();
                summary = R.string.hour_16;
            } else if (OPINION_96.equals(opinion)) {
                time = cal.getShaahZmanis96Minutes();
                summary = R.string.hour_96;
            } else if (OPINION_96_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis96MinutesZmanis();
                summary = R.string.hour_96_zmanis;
            } else if (OPINION_90.equals(opinion)) {
                time = cal.getShaahZmanis90Minutes();
                summary = R.string.hour_90;
            } else if (OPINION_90_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis90MinutesZmanis();
                summary = R.string.hour_90_zmanis;
            } else if (OPINION_72.equals(opinion)) {
                time = cal.getShaahZmanis72Minutes();
                summary = R.string.hour_72;
            } else if (OPINION_72_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis72MinutesZmanis();
                summary = R.string.hour_72_zmanis;
            } else if (OPINION_60.equals(opinion)) {
                time = cal.getShaahZmanis60Minutes();
                summary = R.string.hour_60;
            } else if (OPINION_ATERET.equals(opinion)) {
                time = cal.getShaahZmanisAteretTorah();
                summary = R.string.hour_ateret;
            } else if (OPINION_MGA.equals(opinion)) {
                time = cal.getShaahZmanisMGA();
                summary = R.string.hour_mga;
            } else {
                time = cal.getShaahZmanisGra();
                summary = R.string.hour_gra;
            }
            // Offset is added back when formatted.
            add(R.string.hour, summary, time - gcal.getTimeZone().getRawOffset());
        }

        opinion = settings.getDawn();
        if (OPINION_19_8.equals(opinion)) {
            date = cal.getAlos19Point8Degrees();
            summary = R.string.dawn_19;
        } else if (OPINION_120.equals(opinion)) {
            date = cal.getAlos120();
            summary = R.string.dawn_120;
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getAlos120Zmanis();
            summary = R.string.dawn_120_zmanis;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getAlos18Degrees();
            summary = R.string.dawn_18;
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getAlos26Degrees();
            summary = R.string.dawn_26;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getAlos16Point1Degrees();
            summary = R.string.dawn_16;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getAlos96();
            summary = R.string.dawn_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getAlos90Zmanis();
            summary = R.string.dawn_96_zmanis;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getAlos90();
            summary = R.string.dawn_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getAlos90Zmanis();
            summary = R.string.dawn_90_zmanis;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getAlos72();
            summary = R.string.dawn_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getAlos72Zmanis();
            summary = R.string.dawn_72_zmanis;
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getAlos60();
            summary = R.string.dawn_60;
        } else {
            date = cal.getAlosHashachar();
            summary = R.string.dawn_16;
        }
        if (date == null) {
            date = cal.getAlos120Zmanis();
            summary = R.string.dawn_120_zmanis;
        }
        if (remote)
            add(R.id.dawn_row, R.string.dawn, R.id.dawn_time, date);
        else
            add(R.string.dawn, summary, date);
        if ((holidayToday == JewishCalendar.SEVENTEEN_OF_TAMMUZ) || (holidayToday == JewishCalendar.FAST_OF_GEDALYAH) || (holidayToday == JewishCalendar.TENTH_OF_TEVES)
                || (holidayToday == JewishCalendar.FAST_OF_ESTHER)) {
            add(R.string.fast_begins, null, date);
        }

        opinion = settings.getTallis();
        if (OPINION_10_2.equals(opinion)) {
            date = cal.getMisheyakir10Point2Degrees();
            summary = R.string.tallis_10;
        } else if (OPINION_11.equals(opinion)) {
            date = cal.getMisheyakir11Degrees();
            summary = R.string.tallis_11;
        } else {
            date = cal.getMisheyakir11Point5Degrees();
            summary = R.string.tallis_summary;
        }
        if (remote)
            add(R.id.tallis_row, R.string.tallis, R.id.tallis_time, date);
        else
            add(R.string.tallis, summary, date);

        opinion = settings.getSunrise();
        if (OPINION_SEA.equals(opinion)) {
            date = cal.getSeaLevelSunrise();
            summary = R.string.sunrise_sea;
        } else {
            date = cal.getSunrise();
            summary = R.string.sunrise_summary;
        }
        if (remote)
            add(R.id.sunrise_row, R.string.sunrise, R.id.sunrise_time, date);
        else
            add(R.string.sunrise, summary, date);

        opinion = settings.getLastShema();
        if (OPINION_16_1_SUNSET.equals(opinion)) {
            date = cal.getSofZmanShmaAlos16Point1ToSunset();
            summary = R.string.shema_16_sunset;
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
            summary = R.string.shema_7;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getSofZmanShmaMGA19Point8Degrees();
            summary = R.string.shema_19;
        } else if (OPINION_120.equals(opinion)) {
            date = cal.getSofZmanShmaMGA120Minutes();
            summary = R.string.shema_120;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getSofZmanShmaMGA18Degrees();
            summary = R.string.shema_18;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getSofZmanShmaMGA96Minutes();
            summary = R.string.shema_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanShmaMGA96MinutesZmanis();
            summary = R.string.shema_96_zmanis;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getSofZmanShmaMGA16Point1Degrees();
            summary = R.string.shema_16;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getSofZmanShmaMGA90Minutes();
            summary = R.string.shema_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanShmaMGA90MinutesZmanis();
            summary = R.string.shema_90_zmanis;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getSofZmanShmaMGA72Minutes();
            summary = R.string.shema_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanShmaMGA72MinutesZmanis();
            summary = R.string.shema_72_zmanis;
        } else if (OPINION_MGA.equals(opinion)) {
            date = cal.getSofZmanShmaMGA();
            summary = R.string.shema_mga;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getSofZmanShmaAteretTorah();
            summary = R.string.shema_ateret;
        } else if (OPINION_3.equals(opinion)) {
            date = cal.getSofZmanShma3HoursBeforeChatzos();
            summary = R.string.shema_3;
        } else if (OPINION_FIXED.equals(opinion)) {
            date = cal.getSofZmanShmaFixedLocal();
            summary = R.string.shema_fixed;
        } else if (OPINION_GRA.equals(opinion)) {
            date = cal.getSofZmanShmaGRA();
            summary = R.string.shema_gra;
        } else {
            date = cal.getSofZmanShmaMGA();
            summary = R.string.shema_mga;
        }
        if (remote)
            add(R.id.shema_row, R.string.shema, R.id.shema_time, date);
        else
            add(R.string.shema, summary, date);

        opinion = settings.getLastTfila();
        if (OPINION_120.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA120Minutes();
            summary = R.string.prayers_120;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA96Minutes();
            summary = R.string.prayers_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA96MinutesZmanis();
            summary = R.string.prayers_96_zmanis;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA19Point8Degrees();
            summary = R.string.prayers_19;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA90Minutes();
            summary = R.string.prayers_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA90MinutesZmanis();
            summary = R.string.prayers_90_zmanis;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getSofZmanTfilahAteretTorah();
            summary = R.string.prayers_ateret;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA18Degrees();
            summary = R.string.prayers_18;
        } else if (OPINION_FIXED.equals(opinion)) {
            date = cal.getSofZmanTfilaFixedLocal();
            summary = R.string.prayers_fixed;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA16Point1Degrees();
            summary = R.string.prayers_16;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA72Minutes();
            summary = R.string.prayers_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA72MinutesZmanis();
            summary = R.string.prayers_72_zmanis;
        } else if (OPINION_2.equals(opinion)) {
            date = cal.getSofZmanTfila2HoursBeforeChatzos();
            summary = R.string.prayers_2;
        } else if (OPINION_GRA.equals(opinion)) {
            date = cal.getSofZmanTfilaGRA();
            summary = R.string.prayers_gra;
        } else {
            date = cal.getSofZmanTfilaMGA();
            summary = R.string.prayers_mga;
        }
        if (remote)
            add(R.id.prayers_row, R.string.prayers, R.id.prayers_time, date);
        else {
            add(R.string.prayers, summary, date);
            if (holidayToday == JewishCalendar.EREV_PESACH)
                add(R.string.eat_chametz, summary, date);
        }

        if (!remote && (holidayToday == JewishCalendar.EREV_PESACH)) {
            opinion = settings.getBurnChametz();
            if (OPINION_16_1.equals(opinion)) {
                date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
                summary = R.string.burn_chametz_16;
            } else if (OPINION_72.equals(opinion)) {
                date = cal.getSofZmanBiurChametzMGA72Minutes();
                summary = R.string.burn_chametz_72;
            } else {
                date = cal.getSofZmanBiurChametzGRA();
                summary = R.string.burn_chametz_gra;
            }
            add(R.string.burn_chametz, summary, date);
        }

        opinion = settings.getMidday();
        if (OPINION_FIXED.equals(opinion)) {
            date = cal.getFixedLocalChatzos();
            summary = R.string.midday_fixed;
        } else {
            date = cal.getChatzos();
            summary = R.string.midday_summary;
        }
        if (remote)
            add(R.id.midday_row, R.string.midday, R.id.midday_time, date);
        else
            add(R.string.midday, summary, date);
        Date midday = date;

        opinion = settings.getEarliestMincha();
        if (OPINION_16_1.equals(opinion)) {
            date = cal.getMinchaGedola16Point1Degrees();
            summary = R.string.earliest_mincha_16;
        } else if (OPINION_30.equals(opinion)) {
            date = cal.getMinchaGedola30Minutes();
            summary = R.string.earliest_mincha_30;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getMinchaGedolaAteretTorah();
            summary = R.string.earliest_mincha_ateret;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getMinchaGedola72Minutes();
            summary = R.string.earliest_mincha_72;
        } else {
            date = cal.getMinchaGedola();
            summary = R.string.earliest_mincha_summary;
        }
        if (remote)
            add(R.id.earliest_mincha_row, R.string.earliest_mincha, R.id.earliest_mincha_time, date);
        else
            add(R.string.earliest_mincha, summary, date);

        opinion = settings.getMincha();
        if (OPINION_16_1.equals(opinion)) {
            date = cal.getMinchaKetana16Point1Degrees();
            summary = R.string.mincha_16;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getMinchaKetana72Minutes();
            summary = R.string.mincha_72;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getMinchaKetanaAteretTorah();
            summary = R.string.mincha_ateret;
        } else {
            date = cal.getMinchaKetana();
            summary = R.string.mincha_summary;
        }
        if (remote)
            add(R.id.mincha_row, R.string.mincha, R.id.mincha_time, date);
        else
            add(R.string.mincha, summary, date);

        opinion = settings.getPlugHamincha();
        if (OPINION_16_1_SUNSET.equals(opinion)) {
            date = cal.getPlagAlosToSunset();
            summary = R.string.plug_hamincha_16_sunset;
        } else if (OPINION_16_1_ALOS.equals(opinion)) {
            date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
            summary = R.string.plug_hamincha_16_alos;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getPlagHaminchaAteretTorah();
            summary = R.string.plug_hamincha_ateret;
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getPlagHamincha60Minutes();
            summary = R.string.plug_hamincha_60;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getPlagHamincha72Minutes();
            summary = R.string.plug_hamincha_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha72MinutesZmanis();
            summary = R.string.plug_hamincha_72_zmanis;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getPlagHamincha16Point1Degrees();
            summary = R.string.plug_hamincha_16;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getPlagHamincha18Degrees();
            summary = R.string.plug_hamincha_18;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getPlagHamincha90Minutes();
            summary = R.string.plug_hamincha_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha90MinutesZmanis();
            summary = R.string.plug_hamincha_90_zmanis;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getPlagHamincha19Point8Degrees();
            summary = R.string.plug_hamincha_19;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getPlagHamincha96Minutes();
            summary = R.string.plug_hamincha_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha96MinutesZmanis();
            summary = R.string.plug_hamincha_96_zmanis;
        } else if (OPINION_120.equals(opinion)) {
            date = cal.getPlagHamincha120Minutes();
            summary = R.string.plug_hamincha_120;
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha120MinutesZmanis();
            summary = R.string.plug_hamincha_120_zmanis;
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getPlagHamincha26Degrees();
            summary = R.string.plug_hamincha_26;
        } else {
            date = cal.getPlagHamincha();
            summary = R.string.plug_hamincha_gra;
        }
        if (remote)
            add(R.id.plug_hamincha_row, R.string.plug_hamincha, R.id.plug_hamincha_time, date);
        else
            add(R.string.plug_hamincha, summary, date);

        opinion = settings.getSunset();
        if (OPINION_LEVEL.equals(opinion)) {
            date = cal.getSunset();
            summary = R.string.sunset_summary;
        } else {
            date = cal.getSeaLevelSunset();
            summary = R.string.sunset_sea;
        }
        if (hasCandles && (candlesHow == BEFORE_SUNSET)) {
            dateCandles = cal.getTimeOffset(date, -candlesOffset * DateUtils.MINUTE_IN_MILLIS);
            if (remote) {
                add(R.id.candles_row, R.string.candles, R.id.candles_time, dateCandles);
            } else {
                String summaryText;
                if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                    summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                } else {
                    summaryText = res.getQuantityString(R.plurals.candles_summary, candlesOffset, candlesOffset);
                }
                add(R.string.candles, summaryText, dateCandles);
            }
        } else if (remote) {
            add(R.id.candles_row, R.string.candles, R.id.candles_time, null);
        }

        if (hasCandles && (candlesHow == AT_SUNSET)) {
            if (remote) {
                add(R.id.candles_row, R.string.candles, R.id.candles_time, date);
            } else if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                add(R.string.candles, summaryText, date);
            } else {
                add(R.string.candles, summary, date);
            }
        } else if (remote) {
            add(R.id.candles_row, R.string.candles, R.id.candles_time, null);
        }
        if ((holidayTomorrow == JewishCalendar.TISHA_BEAV) || (holidayTomorrow == JewishCalendar.YOM_KIPPUR)) {
            add(R.string.fast_begins, null, date);
        }
        if (remote)
            add(R.id.sunset_row, R.string.sunset, R.id.sunset_time, date);
        else
            add(R.string.sunset, summary, date);

        opinion = settings.getTwilight();
        if (OPINION_7_083.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
            summary = R.string.twilight_7_083;
        } else if (OPINION_58.equals(opinion)) {
            date = cal.getBainHasmashosRT58Point5Minutes();
            summary = R.string.twilight_58;
        } else if (OPINION_13.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point24Degrees();
            summary = R.string.twilight_13;
        } else {
            date = cal.getBainHasmashosRT2Stars();
            summary = R.string.twilight_2stars;
            if (date == null) {
                date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
                summary = R.string.twilight_7_083;
            }
        }
        if (hasCandles && (candlesHow == AT_TWILIGHT)) {
            if (remote) {
                add(R.id.candles_twilight_row, R.string.twilight, R.id.candles_twilight_time, date);
            } else if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                add(R.string.candles, summaryText, date);
            }
        } else if (remote) {
            add(R.id.candles_twilight_row, R.string.candles, R.id.candles_twilight_time, null);
        }
        if (remote)
            add(R.id.twilight_row, R.string.twilight, R.id.twilight_time, date);
        else
            add(R.string.twilight, summary, date);
        if ((holidayToday == JewishCalendar.SEVENTEEN_OF_TAMMUZ) || (holidayToday == JewishCalendar.TISHA_BEAV) || (holidayToday == JewishCalendar.FAST_OF_GEDALYAH)
                || (holidayToday == JewishCalendar.TENTH_OF_TEVES) || (holidayToday == JewishCalendar.FAST_OF_ESTHER)) {
            add(R.string.fast_ends, null, date);
        }

        opinion = settings.getNightfall();
        if (OPINION_120.equals(opinion)) {
            date = cal.getTzais120();
            summary = R.string.nightfall_120;
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getTzais120Zmanis();
            summary = R.string.nightfall_120_zmanis;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getTzais16Point1Degrees();
            summary = R.string.nightfall_16;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getTzais18Degrees();
            summary = R.string.nightfall_18;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getTzais19Point8Degrees();
            summary = R.string.nightfall_19;
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getTzais26Degrees();
            summary = R.string.nightfall_26;
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getTzais60();
            summary = R.string.nightfall_60;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getTzais72();
            summary = R.string.nightfall_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getTzais72Zmanis();
            summary = R.string.nightfall_72_zmanis;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getTzais90();
            summary = R.string.nightfall_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getTzais90Zmanis();
            summary = R.string.nightfall_90_zmanis;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getTzais96();
            summary = R.string.nightfall_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getTzais96Zmanis();
            summary = R.string.nightfall_96_zmanis;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getTzaisAteretTorah();
            summary = R.string.nightfall_ateret;
        } else if (OPINION_3_65.equals(opinion)) {
            date = cal.getTzaisGeonim3Point65Degrees();
            summary = R.string.nightfall_3_65;
        } else if (OPINION_3_676.equals(opinion)) {
            date = cal.getTzaisGeonim3Point676Degrees();
            summary = R.string.nightfall_3_676;
        } else if (OPINION_4_37.equals(opinion)) {
            date = cal.getTzaisGeonim4Point37Degrees();
            summary = R.string.nightfall_4_37;
        } else if (OPINION_4_61.equals(opinion)) {
            date = cal.getTzaisGeonim4Point61Degrees();
            summary = R.string.nightfall_4_61;
        } else if (OPINION_4_8.equals(opinion)) {
            date = cal.getTzaisGeonim4Point8Degrees();
            summary = R.string.nightfall_4_8;
        } else if (OPINION_5_88.equals(opinion)) {
            date = cal.getTzaisGeonim5Point88Degrees();
            summary = R.string.nightfall_5_88;
        } else if (OPINION_5_95.equals(opinion)) {
            date = cal.getTzaisGeonim5Point95Degrees();
            summary = R.string.nightfall_5_95;
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getTzaisGeonim7Point083Degrees();
            summary = R.string.nightfall_7;
        } else if (OPINION_8_5.equals(opinion)) {
            date = cal.getTzaisGeonim8Point5Degrees();
            summary = R.string.nightfall_8;
        } else {
            date = cal.getTzais();
            summary = R.string.nightfall_3stars;
        }
        if (remote)
            add(R.id.nightfall_row, R.string.nightfall, R.id.nightfall_time, date);
        else
            add(R.string.nightfall, summary, date);
        if (holidayToday == JewishCalendar.YOM_KIPPUR) {
            add(R.string.fast_ends, null, date);
        }
        if (hasCandles && (candlesHow == AT_NIGHT)) {
            if (remote) {
                add(R.id.candles_nightfall_row, R.string.nightfall, R.id.candles_nightfall_time, date);
            } else if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                add(R.string.candles, summaryText, date);
            } else {
                add(R.string.candles, summary, date);
            }
        } else if (remote) {
            add(R.id.candles_nightfall_row, R.string.nightfall, R.id.candles_nightfall_time, null);
        }
        Date nightfall = date;

        opinion = settings.getMidnight();
        if (OPINION_12.equals(opinion)) {
            date = midday;
            if (date != null)
                date.setTime(date.getTime() + TWELVE_HOURS);
            summary = R.string.midnight_12;
        } else if (OPINION_6.equals(opinion)) {
            date = nightfall;
            if (date != null)
                date.setTime(date.getTime() + SIX_HOURS);
            summary = R.string.midnight_6;
        } else {
            date = cal.getSolarMidnight();
            summary = R.string.midnight_summary;
        }
        if (remote)
            add(R.id.midnight_row, R.string.midnight, R.id.midnight_time, date);
        else
            add(R.string.midnight, summary, date);

        if (!remote) {
            final int jDayOfMonth = jcal.getJewishDayOfMonth();
            // Molad.
            if ((jDayOfMonth <= 2) || (jDayOfMonth >= 25)) {
                int y = gcal.get(Calendar.YEAR);
                int m = gcal.get(Calendar.MONTH);
                int d = gcal.get(Calendar.DAY_OF_MONTH);
                jcal.forward();// Molad is always of the previous month.
                JewishDate molad = jcal.getMolad();
                int moladYear = molad.getGregorianYear();
                int moladMonth = molad.getGregorianMonth();
                int moladDay = molad.getGregorianDayOfMonth();
                if ((moladYear == y) && (moladMonth == m) && (moladDay == d)) {
                    double moladSeconds = (molad.getMoladChalakim() * 10.0) / 3.0;
                    double moladSecondsFloor = Math.floor(moladSeconds);
                    Calendar calMolad = (Calendar) gcal.clone();
                    calMolad.set(moladYear, moladMonth, moladDay, molad.getMoladHours(), molad.getMoladMinutes(), (int) moladSecondsFloor);
                    calMolad.set(Calendar.MILLISECOND, (int) (DateUtils.SECOND_IN_MILLIS * (moladSeconds - moladSecondsFloor)));
                    summary = R.string.molad_summary;
                    add(R.string.molad, summary, calMolad.getTime());
                }
            }
            // First Kiddush Levana.
            else if ((jDayOfMonth >= 2) && (jDayOfMonth <= 8)) {
                opinion = settings.getEarliestKiddushLevana();
                if (OPINION_7.equals(opinion)) {
                    date = cal.getTchilasZmanKidushLevana7Days();
                    summary = R.string.levana_7;
                } else {
                    date = cal.getTchilasZmanKidushLevana3Days();
                    summary = R.string.levana_earliest_summary;
                }
                add(R.string.levana_earliest, summary, date);
            }
            // Last Kiddush Levana.
            else if ((jDayOfMonth > 10) && (jDayOfMonth < 20)) {
                opinion = settings.getLatestKiddushLevana();
                if (OPINION_15.equals(opinion)) {
                    date = cal.getSofZmanKidushLevana15Days();
                    summary = R.string.levana_15;
                } else {
                    date = cal.getSofZmanKidushLevanaBetweenMoldos();
                    summary = R.string.levana_latest_summary;
                }
                add(R.string.levana_latest, summary, date);
            }
        }

        sort();
    }

    /**
     * Get the number of candles to light.
     *
     * @param jcal
     *         the Jewish calendar.
     * @return the number of candles to light, the holiday, and when to light.
     */
    protected int getCandles(JewishCalendar jcal) {
        final int dayOfWeek = jcal.getDayOfWeek();

        // Check if the following day is special, because we can't check EREV_CHANUKAH.
        int holidayToday = jcal.getYomTovIndex();
        jcal.forward();
        int holidayTomorrow = jcal.getYomTovIndex();
        int count = CANDLES_NONE;
        int flags = BEFORE_SUNSET;

        switch (holidayTomorrow) {
            case JewishCalendar.PESACH:
            case JewishCalendar.SHAVUOS:
            case JewishCalendar.ROSH_HASHANA:
            case JewishCalendar.SUCCOS:
            case JewishCalendar.SHEMINI_ATZERES:
            case JewishCalendar.SIMCHAS_TORAH:
                count = CANDLES_FESTIVAL;
                break;
            case JewishCalendar.YOM_KIPPUR:
                count = CANDLES_YOM_KIPPUR;
                break;
            case JewishCalendar.CHANUKAH:
                count = jcal.getDayOfChanukah();
                if ((dayOfWeek != Calendar.FRIDAY) && (dayOfWeek != Calendar.SATURDAY)) {
                    String opinion = settings.getChanukkaCandles();
                    if (OPINION_TWILIGHT.equals(opinion)) {
                        flags = AT_TWILIGHT;
                    } else if (OPINION_NIGHT.equals(opinion)) {
                        flags = AT_NIGHT;
                    } else {
                        flags = AT_SUNSET;
                    }
                }
                break;
            default:
                if (dayOfWeek == Calendar.FRIDAY) {
                    holidayTomorrow = SHABBATH;
                    count = CANDLES_SHABBATH;
                }
                break;
        }

        // Forbidden to light candles during Shabbath.
        switch (dayOfWeek) {
            case Calendar.FRIDAY:
                // Probably never happens that Yom Kippurim falls on a Friday.
                // Prohibited to light candles on Yom Kippurim for Shabbath.
                if (holidayToday == JewishCalendar.YOM_KIPPUR) {
                    count = CANDLES_NONE;
                }
                break;
            case Calendar.SATURDAY:
                if (holidayToday == -1) {
                    holidayToday = SHABBATH;
                }
                flags = MOTZE_SHABBATH;
                break;
            default:
                // During a holiday, we can light for the next day from an existing
                // flame.
                switch (holidayToday) {
                    case JewishCalendar.ROSH_HASHANA:
                    case JewishCalendar.SUCCOS:
                    case JewishCalendar.SHEMINI_ATZERES:
                    case JewishCalendar.SIMCHAS_TORAH:
                    case JewishCalendar.PESACH:
                    case JewishCalendar.SHAVUOS:
                        flags = AT_SUNSET;
                        break;
                }
                break;
        }

        return flags | ((holidayToday & HOLIDAY_MASK) << 12) | ((holidayTomorrow & HOLIDAY_MASK) << 4) | (count & CANDLES_MASK);
    }

    /**
     * Sort.
     */
    protected void sort() {
        if (comparator == null) {
            comparator = new ZmanimComparator();
        }
        sort(comparator);
    }

    /**
     * View holder for zman row item.
     *
     * @author Moshe W
     */
    private static class ViewHolder {

        public final TextView title;
        public final TextView summary;
        public final TextView time;

        public ViewHolder(TextView title, TextView summary, TextView time) {
            this.title = title;
            this.summary = summary;
            this.time = time;
        }
    }

    /**
     * Format the Hebrew date.
     *
     * @param context
     *         the context.
     * @param jewishDate
     *         the date.
     * @return the formatted date.
     */
    public CharSequence formatDate(Context context, JewishDate jewishDate) {
        int jewishDay = jewishDate.getJewishDayOfMonth();
        int jewishMonth = jewishDate.getJewishMonth();
        int jewishYear = jewishDate.getJewishYear();
        if ((jewishMonth == JewishDate.ADAR) && jewishDate.isJewishLeapYear()) {
            jewishMonth = 14; // return Adar I, not Adar in a leap year
        }

        String[] monthNames = this.monthNames;
        if (monthNames == null) {
            monthNames = context.getResources().getStringArray(R.array.hebrew_months);
            this.monthNames = monthNames;
        }
        String format = monthDayYear;
        if (format == null) {
            format = context.getString(R.string.month_day_year);
            monthDayYear = format;
        }

        String yearStr;
        String monthStr = monthNames[jewishMonth - 1];
        String dayStr;
        String dayPadded;

        if (ZmanimLocations.isLocaleRTL()) {
            HebrewDateFormatter formatter = getHebrewDateFormatter();

            yearStr = formatter.formatHebrewNumber(jewishYear);
            dayStr = formatter.formatHebrewNumber(jewishDay);
            dayPadded = dayStr;
        } else {
            yearStr = String.valueOf(jewishYear);
            dayStr = String.valueOf(jewishDay);
            dayPadded = (jewishDay < 10) ? "0" + dayStr : dayStr;
        }

        String formatted = format.replaceAll(YEAR_VAR, yearStr);
        formatted = formatted.replaceAll(MONTH_VAR, monthStr);
        formatted = formatted.replaceAll(DAY_VAR, dayStr);
        formatted = formatted.replaceAll(DAY_PAD_VAR, dayPadded);

        return formatted;
    }

    /**
     * Set the calendar.
     *
     * @param calendar
     *         the calendar.
     */
    public void setCalendar(Calendar calendar) {
        this.calendar.setCalendar(calendar);
    }

    /**
     * Set the calendar time.
     *
     * @param time
     *         the time in milliseconds.
     */
    public void setCalendar(long time) {
        Calendar cal = calendar.getCalendar();
        cal.setTimeInMillis(time);
    }

    /**
     * Sets the {@link GeoLocation}.
     *
     * @param geoLocation
     *         the location.
     */
    public void setGeoLocation(GeoLocation geoLocation) {
        this.calendar.setGeoLocation(geoLocation);
    }

    /**
     * Sets whether to use Israel holiday scheme or not.
     *
     * @param inIsrael
     *         set to {@code true} for calculations for Israel.
     */
    public void setInIsrael(boolean inIsrael) {
        this.inIsrael = inIsrael;
    }

    protected Date getMidday(ComplexZmanimCalendar cal) {
        Date date;
        String opinion = settings.getMidday();
        if (OPINION_FIXED.equals(opinion)) {
            date = cal.getFixedLocalChatzos();
        } else {
            date = cal.getChatzos();
        }
        return date;
    }

    protected Date getNightfall(ComplexZmanimCalendar cal) {
        Date date;
        String opinion = settings.getNightfall();
        if (OPINION_120.equals(opinion)) {
            date = cal.getTzais120();
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getTzais120Zmanis();
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getTzais16Point1Degrees();
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getTzais18Degrees();
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getTzais19Point8Degrees();
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getTzais26Degrees();
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getTzais60();
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getTzais72();
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getTzais72Zmanis();
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getTzais90();
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getTzais90Zmanis();
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getTzais96();
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getTzais96Zmanis();
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getTzaisAteretTorah();
        } else if (OPINION_3_65.equals(opinion)) {
            date = cal.getTzaisGeonim3Point65Degrees();
        } else if (OPINION_3_676.equals(opinion)) {
            date = cal.getTzaisGeonim3Point676Degrees();
        } else if (OPINION_4_37.equals(opinion)) {
            date = cal.getTzaisGeonim4Point37Degrees();
        } else if (OPINION_4_61.equals(opinion)) {
            date = cal.getTzaisGeonim4Point61Degrees();
        } else if (OPINION_4_8.equals(opinion)) {
            date = cal.getTzaisGeonim4Point8Degrees();
        } else if (OPINION_5_88.equals(opinion)) {
            date = cal.getTzaisGeonim5Point88Degrees();
        } else if (OPINION_5_95.equals(opinion)) {
            date = cal.getTzaisGeonim5Point95Degrees();
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getTzaisGeonim7Point083Degrees();
        } else if (OPINION_8_5.equals(opinion)) {
            date = cal.getTzaisGeonim8Point5Degrees();
        } else {
            date = cal.getTzais();
        }

        return date;
    }


    /**
     * Get the Jewish calendar.
     *
     * @return the calendar.
     */
    public JewishCalendar getJewishCalendar() {
        Calendar gcal = getCalendar().getCalendar();
        JewishCalendar jcal = new JewishCalendar(gcal);
        jcal.setInIsrael(inIsrael);
        return jcal;
    }

    /**
     * Format the number of omer days.
     *
     * @param context
     *         the context.
     * @param days
     *         the number of days.
     * @return the formatted count.
     */
    public CharSequence formatOmer(Context context, int days) {
        if (days <= 0) {
            return null;
        }
        String suffix = settings.getOmerSuffix();
        if (TextUtils.isEmpty(suffix)) {
            return null;
        }
        if (ZmanimSettings.OMER_B.equals(suffix)) {
            suffix = context.getString(R.string.omer_b);
        } else if (ZmanimSettings.OMER_L.equals(suffix)) {
            suffix = context.getString(R.string.omer_l);
        }

        String format;
        if (days == 33) {
            format = context.getString(R.string.omer_33);
            return String.format(format, suffix);
        }

        format = omerFormat;
        if (format == null) {
            format = context.getString(R.string.omer_format);
            omerFormat = format;
        }

        String dayStr;

        if (ZmanimLocations.isLocaleRTL()) {
            HebrewDateFormatter formatter = getHebrewDateFormatter();

            dayStr = formatter.formatHebrewNumber(days);
        } else {
            dayStr = String.valueOf(days);
        }

        return String.format(format, dayStr, suffix);
    }

    protected HebrewDateFormatter getHebrewDateFormatter() {
        HebrewDateFormatter formatter = hebrewDateFormatter;
        if (formatter == null) {
            formatter = new HebrewDateFormatter();
            formatter.setHebrewFormat(true);
            hebrewDateFormatter = formatter;
        }
        return formatter;
    }
}
