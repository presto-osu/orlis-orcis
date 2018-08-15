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
import android.view.View;
import android.view.ViewGroup;

import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import java.util.Date;

/**
 * Adapter for all opinions of an halachic time.
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsAdapter extends ZmanimAdapter {

    private int itemId;

    public ZmanimDetailsAdapter(Context context, ZmanimSettings settings, int itemId) {
        super(context, settings);
        this.itemId = itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.times_detail);
    }

    @Override
    public void populate(boolean remote) {
        prePopulate();

        final int id = itemId;
        if (id == R.string.hour) {
            populateHour(calendar);
        } else if (id == R.string.dawn) {
            populateDawn(calendar);
        } else if (id == R.string.tallis) {
            populateTallis(calendar);
        } else if (id == R.string.sunrise) {
            populateSunrise(calendar);
        } else if (id == R.string.shema) {
            populateShema(calendar);
        } else if (id == R.string.prayers) {
            populatePrayers(calendar);
        } else if (id == R.string.midday) {
            populateMidday(calendar);
        } else if (id == R.string.earliest_mincha) {
            populateEarliestMincha(calendar);
        } else if (id == R.string.mincha) {
            populateMincha(calendar);
        } else if (id == R.string.plug_hamincha) {
            populatePlugHamincha(calendar);
        } else if (id == R.string.sunset) {
            populateSunset(calendar);
        } else if (id == R.string.twilight) {
            populateTwilight(calendar);
        } else if (id == R.string.nightfall) {
            populateNightfall(calendar);
        } else if (id == R.string.midnight) {
            populateMidnight(calendar);
        } else if (id == R.string.levana_earliest) {
            populateEarliestKiddushLevana(calendar);
        } else if (id == R.string.levana_latest) {
            populateLatestKiddushLevana(calendar);
        } else if (id == R.string.eat_chametz) {
            populateEatChametz(calendar);
        } else if (id == R.string.burn_chametz) {
            populateBurnChametz(calendar);
        }
        sort();
    }

    private void populateHour(ComplexZmanimCalendar cal) {
        long time;
        int title;
        // Offset is added back when formatted.
        long offset = cal.getCalendar().getTimeZone().getRawOffset();

        time = cal.getShaahZmanis120Minutes();
        title = R.string.hour_120;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis120MinutesZmanis();
        title = R.string.hour_120_zmanis;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis16Point1Degrees();
        title = R.string.hour_16;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis18Degrees();
        title = R.string.hour_18;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis19Point8Degrees();
        title = R.string.hour_19;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis26Degrees();
        title = R.string.hour_26;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis60Minutes();
        title = R.string.hour_60;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis72Minutes();
        title = R.string.hour_72;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis72MinutesZmanis();
        title = R.string.hour_72_zmanis;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis90Minutes();
        title = R.string.hour_90;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis90MinutesZmanis();
        title = R.string.hour_90_zmanis;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis96Minutes();
        title = R.string.hour_96;
        add(title, 0, time - offset);

        time = cal.getShaahZmanis96MinutesZmanis();
        title = R.string.hour_96_zmanis;
        add(title, 0, time - offset);

        time = cal.getShaahZmanisAteretTorah();
        title = R.string.hour_ateret;
        add(title, 0, time - offset);

        time = cal.getShaahZmanisGra();
        title = R.string.hour_gra;
        add(title, 0, time - offset);

        time = cal.getShaahZmanisMGA();
        title = R.string.hour_mga;
        add(title, 0, time - offset);
    }

    private void populateDawn(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getAlos19Point8Degrees();
        title = R.string.dawn_19;
        add(title, 0, date);

        date = cal.getAlos120();
        title = R.string.dawn_120;
        add(title, 0, date);

        date = cal.getAlos120Zmanis();
        title = R.string.dawn_120_zmanis;
        add(title, 0, date);

        date = cal.getAlos18Degrees();
        title = R.string.dawn_18;
        add(title, 0, date);

        date = cal.getAlos26Degrees();
        title = R.string.dawn_26;
        add(title, 0, date);

        date = cal.getAlos16Point1Degrees();
        title = R.string.dawn_16;
        add(title, 0, date);

        date = cal.getAlos96();
        title = R.string.dawn_96;
        add(title, 0, date);

        date = cal.getAlos90Zmanis();
        title = R.string.dawn_96_zmanis;
        add(title, 0, date);

        date = cal.getAlos90();
        title = R.string.dawn_90;
        add(title, 0, date);

        date = cal.getAlos90Zmanis();
        title = R.string.dawn_90_zmanis;
        add(title, 0, date);

        date = cal.getAlos72();
        title = R.string.dawn_72;
        add(title, 0, date);

        date = cal.getAlos72Zmanis();
        title = R.string.dawn_72_zmanis;
        add(title, 0, date);

        date = cal.getAlos60();
        title = R.string.dawn_60;
        add(title, 0, date);
    }

    private void populateTallis(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getMisheyakir10Point2Degrees();
        title = R.string.tallis_10;
        add(title, 0, date);

        date = cal.getMisheyakir11Degrees();
        title = R.string.tallis_11;
        add(title, 0, date);

        date = cal.getMisheyakir11Point5Degrees();
        title = R.string.tallis_summary;
        add(title, 0, date);
    }

    private void populateSunrise(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSeaLevelSunrise();
        title = R.string.sunrise_sea;
        add(title, 0, date);

        date = cal.getSunrise();
        title = R.string.sunrise_summary;
        add(title, 0, date);
    }

    private void populateShema(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSofZmanShmaAlos16Point1ToSunset();
        title = R.string.shema_16_sunset;
        add(title, 0, date);

        date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
        title = R.string.shema_7;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA19Point8Degrees();
        title = R.string.shema_19;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA120Minutes();
        title = R.string.shema_120;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA18Degrees();
        title = R.string.shema_18;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA96Minutes();
        title = R.string.shema_96;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA96MinutesZmanis();
        title = R.string.shema_96_zmanis;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA16Point1Degrees();
        title = R.string.shema_16;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA90Minutes();
        title = R.string.shema_90;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA90MinutesZmanis();
        title = R.string.shema_90_zmanis;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA72Minutes();
        title = R.string.shema_72;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA72MinutesZmanis();
        title = R.string.shema_72_zmanis;
        add(title, 0, date);

        date = cal.getSofZmanShmaMGA();
        title = R.string.shema_mga;
        add(title, 0, date);

        date = cal.getSofZmanShmaAteretTorah();
        title = R.string.shema_ateret;
        add(title, 0, date);

        date = cal.getSofZmanShma3HoursBeforeChatzos();
        title = R.string.shema_3;
        add(title, 0, date);

        date = cal.getSofZmanShmaFixedLocal();
        title = R.string.shema_fixed;
        add(title, 0, date);

        date = cal.getSofZmanShmaGRA();
        title = R.string.shema_gra;
        add(title, 0, date);
    }

    private void populatePrayers(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSofZmanTfilaMGA120Minutes();
        title = R.string.prayers_120;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA96Minutes();
        title = R.string.prayers_96;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA96MinutesZmanis();
        title = R.string.prayers_96_zmanis;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA19Point8Degrees();
        title = R.string.prayers_19;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA90Minutes();
        title = R.string.prayers_90;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA90MinutesZmanis();
        title = R.string.prayers_90_zmanis;
        add(title, 0, date);

        date = cal.getSofZmanTfilahAteretTorah();
        title = R.string.prayers_ateret;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA18Degrees();
        title = R.string.prayers_18;
        add(title, 0, date);

        date = cal.getSofZmanTfilaFixedLocal();
        title = R.string.prayers_fixed;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA16Point1Degrees();
        title = R.string.prayers_16;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA72Minutes();
        title = R.string.prayers_72;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA72MinutesZmanis();
        title = R.string.prayers_72_zmanis;
        add(title, 0, date);

        date = cal.getSofZmanTfila2HoursBeforeChatzos();
        title = R.string.prayers_2;
        add(title, 0, date);

        date = cal.getSofZmanTfilaGRA();
        title = R.string.prayers_gra;
        add(title, 0, date);

        date = cal.getSofZmanTfilaMGA();
        title = R.string.prayers_mga;
        add(title, 0, date);
    }

    private void populateMidday(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getFixedLocalChatzos();
        title = R.string.midday_fixed;
        add(title, 0, date);

        date = cal.getChatzos();
        title = R.string.midday_summary;
        add(title, 0, date);
    }

    private void populateEarliestMincha(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getMinchaGedola16Point1Degrees();
        title = R.string.earliest_mincha_16;
        add(title, 0, date);

        date = cal.getMinchaGedola30Minutes();
        title = R.string.earliest_mincha_30;
        add(title, 0, date);

        date = cal.getMinchaGedolaAteretTorah();
        title = R.string.earliest_mincha_ateret;
        add(title, 0, date);

        date = cal.getMinchaGedola72Minutes();
        title = R.string.earliest_mincha_72;
        add(title, 0, date);

        date = cal.getMinchaGedola();
        title = R.string.earliest_mincha_summary;
        add(title, 0, date);
    }

    private void populateMincha(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getMinchaKetana16Point1Degrees();
        title = R.string.mincha_16;
        add(title, 0, date);

        date = cal.getMinchaKetana72Minutes();
        title = R.string.mincha_72;
        add(title, 0, date);

        date = cal.getMinchaKetanaAteretTorah();
        title = R.string.mincha_ateret;
        add(title, 0, date);

        date = cal.getMinchaKetana();
        title = R.string.mincha_summary;
        add(title, 0, date);
    }

    private void populatePlugHamincha(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getPlagAlosToSunset();
        title = R.string.plug_hamincha_16_sunset;
        add(title, 0, date);

        date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
        title = R.string.plug_hamincha_16_alos;
        add(title, 0, date);

        date = cal.getPlagHaminchaAteretTorah();
        title = R.string.plug_hamincha_ateret;
        add(title, 0, date);

        date = cal.getPlagHamincha60Minutes();
        title = R.string.plug_hamincha_60;
        add(title, 0, date);

        date = cal.getPlagHamincha72Minutes();
        title = R.string.plug_hamincha_72;
        add(title, 0, date);

        date = cal.getPlagHamincha72MinutesZmanis();
        title = R.string.plug_hamincha_72_zmanis;
        add(title, 0, date);

        date = cal.getPlagHamincha16Point1Degrees();
        title = R.string.plug_hamincha_16;
        add(title, 0, date);

        date = cal.getPlagHamincha18Degrees();
        title = R.string.plug_hamincha_18;
        add(title, 0, date);

        date = cal.getPlagHamincha90Minutes();
        title = R.string.plug_hamincha_90;
        add(title, 0, date);

        date = cal.getPlagHamincha90MinutesZmanis();
        title = R.string.plug_hamincha_90_zmanis;
        add(title, 0, date);

        date = cal.getPlagHamincha19Point8Degrees();
        title = R.string.plug_hamincha_19;
        add(title, 0, date);

        date = cal.getPlagHamincha96Minutes();
        title = R.string.plug_hamincha_96;
        add(title, 0, date);

        date = cal.getPlagHamincha96MinutesZmanis();
        title = R.string.plug_hamincha_96_zmanis;
        add(title, 0, date);

        date = cal.getPlagHamincha120Minutes();
        title = R.string.plug_hamincha_120;
        add(title, 0, date);

        date = cal.getPlagHamincha120MinutesZmanis();
        title = R.string.plug_hamincha_120_zmanis;
        add(title, 0, date);

        date = cal.getPlagHamincha26Degrees();
        title = R.string.plug_hamincha_26;
        add(title, 0, date);

        date = cal.getPlagHamincha();
        title = R.string.plug_hamincha_gra;
        add(title, 0, date);
    }

    private void populateSunset(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSeaLevelSunset();
        title = R.string.sunset_sea;
        add(title, 0, date);

        date = cal.getSunset();
        title = R.string.sunset_summary;
        add(title, 0, date);
    }

    private void populateTwilight(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
        title = R.string.twilight_7_083;
        add(title, 0, date);

        date = cal.getBainHasmashosRT58Point5Minutes();
        title = R.string.twilight_58;
        add(title, 0, date);

        date = cal.getBainHasmashosRT13Point24Degrees();
        title = R.string.twilight_13;
        add(title, 0, date);

        date = cal.getBainHasmashosRT2Stars();
        title = R.string.twilight_2stars;
        add(title, 0, date);
    }

    private void populateNightfall(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getTzais120();
        title = R.string.nightfall_120;
        add(title, 0, date);

        date = cal.getTzais120Zmanis();
        title = R.string.nightfall_120_zmanis;
        add(title, 0, date);

        date = cal.getTzais16Point1Degrees();
        title = R.string.nightfall_16;
        add(title, 0, date);

        date = cal.getTzais18Degrees();
        title = R.string.nightfall_18;
        add(title, 0, date);

        date = cal.getTzais19Point8Degrees();
        title = R.string.nightfall_19;
        add(title, 0, date);

        date = cal.getTzais26Degrees();
        title = R.string.nightfall_26;
        add(title, 0, date);

        date = cal.getTzais60();
        title = R.string.nightfall_60;
        add(title, 0, date);

        date = cal.getTzais72();
        title = R.string.nightfall_72;
        add(title, 0, date);

        date = cal.getTzais72Zmanis();
        title = R.string.nightfall_72_zmanis;
        add(title, 0, date);

        date = cal.getTzais90();
        title = R.string.nightfall_90;
        add(title, 0, date);

        date = cal.getTzais90Zmanis();
        title = R.string.nightfall_90_zmanis;
        add(title, 0, date);

        date = cal.getTzais96();
        title = R.string.nightfall_96;
        add(title, 0, date);

        date = cal.getTzais96Zmanis();
        title = R.string.nightfall_96_zmanis;
        add(title, 0, date);

        date = cal.getTzaisAteretTorah();
        title = R.string.nightfall_ateret;
        add(title, 0, date);

        date = cal.getTzaisGeonim3Point65Degrees();
        title = R.string.nightfall_3_65;
        add(title, 0, date);

        date = cal.getTzaisGeonim3Point676Degrees();
        title = R.string.nightfall_3_676;
        add(title, 0, date);

        date = cal.getTzaisGeonim4Point37Degrees();
        title = R.string.nightfall_4_37;
        add(title, 0, date);

        date = cal.getTzaisGeonim4Point61Degrees();
        title = R.string.nightfall_4_61;
        add(title, 0, date);

        date = cal.getTzaisGeonim4Point8Degrees();
        title = R.string.nightfall_4_8;
        add(title, 0, date);

        date = cal.getTzaisGeonim5Point88Degrees();
        title = R.string.nightfall_5_88;
        add(title, 0, date);

        date = cal.getTzaisGeonim5Point95Degrees();
        title = R.string.nightfall_5_95;
        add(title, 0, date);

        date = cal.getTzaisGeonim7Point083Degrees();
        title = R.string.nightfall_7;
        add(title, 0, date);

        date = cal.getTzaisGeonim8Point5Degrees();
        title = R.string.nightfall_8;
        add(title, 0, date);

        date = cal.getTzais();
        title = R.string.nightfall_3stars;
        add(title, 0, date);
    }

    private void populateMidnight(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = getMidday(cal);
        if (date != null)
            date.setTime(date.getTime() + TWELVE_HOURS);
        title = R.string.midnight_12;
        add(title, 0, date);

        date = getNightfall(cal);
        if (date != null)
            date.setTime(date.getTime() + SIX_HOURS);
        title = R.string.midnight_6;
        add(title, 0, date);

        date = cal.getSolarMidnight();
        title = R.string.midnight_summary;
        add(title, 0, date);
    }

    private void populateEatChametz(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSofZmanAchilasChametzGRA();
        title = R.string.prayers_gra;
        add(title, 0, date);

        date = cal.getSofZmanAchilasChametzMGA16Point1Degrees();
        title = R.string.prayers_16;
        add(title, 0, date);

        date = cal.getSofZmanAchilasChametzMGA72Minutes();
        title = R.string.prayers_72;
        add(title, 0, date);
    }

    private void populateBurnChametz(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
        title = R.string.burn_chametz_16;
        add(title, 0, date);

        date = cal.getSofZmanBiurChametzMGA72Minutes();
        title = R.string.burn_chametz_72;
        add(title, 0, date);

        date = cal.getSofZmanBiurChametzGRA();
        title = R.string.burn_chametz_gra;
        add(title, 0, date);
    }

    private void populateEarliestKiddushLevana(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getTchilasZmanKidushLevana3Days();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getTchilasZmanKidushLevana3Days();
        }
        title = R.string.levana_earliest_summary;
        add(title, 0, date);

        date = cal.getTchilasZmanKidushLevana7Days();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getTchilasZmanKidushLevana7Days();
        }
        title = R.string.levana_7;
        add(title, 0, date);
    }

    private void populateLatestKiddushLevana(ComplexZmanimCalendar cal) {
        Date date;
        int title;

        date = cal.getSofZmanKidushLevanaBetweenMoldos();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getSofZmanKidushLevanaBetweenMoldos();
        }
        title = R.string.levana_latest_summary;
        add(title, 0, date);

        date = cal.getSofZmanKidushLevana15Days();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getSofZmanKidushLevana15Days();
        }
        title = R.string.levana_15;
        add(title, 0, date);
    }
}
