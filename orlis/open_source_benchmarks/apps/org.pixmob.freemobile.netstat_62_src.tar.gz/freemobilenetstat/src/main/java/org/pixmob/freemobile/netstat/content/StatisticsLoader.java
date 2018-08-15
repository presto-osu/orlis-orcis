package org.pixmob.freemobile.netstat.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.pixmob.freemobile.netstat.Event;
import org.pixmob.freemobile.netstat.MobileOperator;
import org.pixmob.freemobile.netstat.NetworkClass;
import org.pixmob.freemobile.netstat.content.NetstatContract.Events;

import java.util.Calendar;
import java.util.Date;

import static org.pixmob.freemobile.netstat.BuildConfig.DEBUG;
import static org.pixmob.freemobile.netstat.Constants.INTERVAL_ONE_MONTH;
import static org.pixmob.freemobile.netstat.Constants.INTERVAL_ONE_WEEK;
import static org.pixmob.freemobile.netstat.Constants.INTERVAL_ONE_DAY;
import static org.pixmob.freemobile.netstat.Constants.SP_KEY_TIME_INTERVAL;
import static org.pixmob.freemobile.netstat.Constants.SP_NAME;
import static org.pixmob.freemobile.netstat.Constants.TAG;

/**
 * {@link Loader} implementation for loading events from the database, and
 * computing statistics.
 * @author Pixmob
 */
public class StatisticsLoader extends AsyncTaskLoader<Statistics> {
    public StatisticsLoader(final Context context) {
        super(context);

        if (DEBUG) {
            Log.d(TAG, "New StatisticsLoader");
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();

        if (DEBUG) {
            Log.d(TAG, "StatisticsLoader.onStartLoading()");
        }
    }

    @Override
    public Statistics loadInBackground() {
        if (DEBUG) {
            Log.d(TAG, "StatisticsLoader.loadInBackground()");
        }

        final long now = System.currentTimeMillis();

        final SharedPreferences prefs = getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        final int interval = prefs.getInt(SP_KEY_TIME_INTERVAL, 0);
        final long fromTimestamp;
        if (interval == INTERVAL_ONE_MONTH) {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            cal.add(Calendar.MONTH, -1);
            fromTimestamp = cal.getTimeInMillis();
        } else if (interval == INTERVAL_ONE_WEEK) {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            cal.add(Calendar.DATE, -7);
            fromTimestamp = cal.getTimeInMillis();
        } else if (interval == INTERVAL_ONE_DAY) {
            final long one_day_in_millis = 86400000; // 24*60*60*1000
            fromTimestamp = now - one_day_in_millis;
        } else {
            fromTimestamp = now - SystemClock.elapsedRealtime();
        }

        Log.i(TAG, "Loading statistics from " + new Date(fromTimestamp) + " to now");

        final Statistics s = new Statistics();

        final TelephonyManager tm = (TelephonyManager) getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        s.mobileOperatorCode = tm.getNetworkOperator();
        s.mobileOperator = MobileOperator.fromString(s.mobileOperatorCode);
        if (s.mobileOperator == null) {
            s.mobileOperatorCode = null;
        }

        long connectionTimestamp = 0;

        Cursor c = null;
        try {
            c = getContext().getContentResolver().query(
                    Events.CONTENT_URI,
                    new String[] { Events.TIMESTAMP, Events.SCREEN_ON, Events.WIFI_CONNECTED,
                            Events.MOBILE_CONNECTED, Events.MOBILE_NETWORK_TYPE, Events.MOBILE_OPERATOR,
                            Events.BATTERY_LEVEL, Events.POWER_ON, Events.FEMTOCELL, Events.FIRST_INSERT }, Events.TIMESTAMP + ">?",
                    new String[] { String.valueOf(fromTimestamp) }, Events.TIMESTAMP + " ASC");
            final int rowCount = c.getCount();

            s.events = new Event[rowCount];
            Event e = null, // current event
                  e0 = new Event(); // next event

            if (c.moveToNext()) {
                e0 = new Event();
                e0.read(c);
            }

            for (int i = 0; i < rowCount; ++i) {
                e = e0;
                s.events[i] = e;
                if (c.moveToNext()) {
                    e0 = new Event();
                    e0.read(c);
                }
                else { // Last event
                    e0 = new Event(e);
                    e0.timestamp = now;
                    e0.firstInsert = false;
                }

                if (e0.firstInsert) {
                    // If the next event is the first event added to the database since close,
                    // we can't predict how much time has passed on the given network.
                    continue;
                }

                final long dt = e0.timestamp - e.timestamp;

                final MobileOperator op = MobileOperator.fromString(e.mobileOperator);
                final NetworkClass nc = NetworkClass.getNetworkClass(e.mobileNetworkType);
                if (op != null) {
                    if (MobileOperator.ORANGE.equals(op)) {
                        s.orangeTime += dt;
                        if (nc != null) {
                            if (NetworkClass.NC_2G.equals(nc)) {
                                s.orange2GTime += dt;
                            } else if (NetworkClass.NC_3G.equals(nc)) {
                                s.orange3GTime += dt;
                            }
                        }
                    } else if (MobileOperator.FREE_MOBILE.equals(op)) {
                        s.freeMobileTime += dt;
                        if (nc != null) {
                            if (NetworkClass.NC_3G.equals(nc)) {
                                if (e.femtocell) {
                                    s.femtocellTime += dt;
                                } else {
                                    s.freeMobile3GTime += dt;
                                }
                            } else if (NetworkClass.NC_4G.equals(nc)) {
                                s.freeMobile4GTime += dt;
                            }
                        }
                    }
                }

                if (e.mobileConnected)
                    connectionTimestamp = e.timestamp;
                else
                    connectionTimestamp = 0;

                if (e.wifiConnected)
                    s.wifiOnTime += dt;

                if (e.screenOn)
                    s.screenOnTime += dt;
            }

            if (e != null) {
                s.battery = e.batteryLevel;
            }

            final double sTime = s.orangeTime + s.freeMobileTime;
            final long orangeKnownNetworkClassTime = s.orange2GTime + s.orange3GTime;
            final long freeMobileKnownNetworkClassTime =  s.freeMobile3GTime + s.freeMobile4GTime + s.femtocellTime;

            final double[] freeMobileOrangeUsePercents = { (double)s.freeMobileTime / sTime * 100d, (double)s.orangeTime / sTime * 100d };
            Statistics.roundPercentagesUpTo100(freeMobileOrangeUsePercents);
            s.freeMobileUsePercent = (int) freeMobileOrangeUsePercents[0];
            s.orangeUsePercent = (int) freeMobileOrangeUsePercents[1];

            final double[] freeMobile3G4GFemtoUsePercents =
                    {
                        freeMobileKnownNetworkClassTime == 0 ? 0 : (double)s.freeMobile3GTime / freeMobileKnownNetworkClassTime * 100d,
                        freeMobileKnownNetworkClassTime == 0 ? 0 : (double)s.freeMobile4GTime / freeMobileKnownNetworkClassTime * 100d,
                        freeMobileKnownNetworkClassTime == 0 ? 0 : (double)s.femtocellTime / freeMobileKnownNetworkClassTime * 100d
                    };
            Statistics.roundPercentagesUpTo100(freeMobile3G4GFemtoUsePercents);
            s.freeMobile3GUsePercent = (int) freeMobile3G4GFemtoUsePercents[0];
            s.freeMobile4GUsePercent = (int) freeMobile3G4GFemtoUsePercents[1];
            s.freeMobileFemtocellUsePercent = (int) freeMobile3G4GFemtoUsePercents[2];

            final double[] orange2G3GUsePercents =
                    {
                        orangeKnownNetworkClassTime == 0 ? 0 : (double)s.orange2GTime / orangeKnownNetworkClassTime * 100,
                        orangeKnownNetworkClassTime == 0 ? 0 : (double)s.orange3GTime / orangeKnownNetworkClassTime * 100
                    };
            Statistics.roundPercentagesUpTo100(orange2G3GUsePercents);
            s.orange2GUsePercent = (int) orange2G3GUsePercents[0];
            s.orange3GUsePercent = (int) orange2G3GUsePercents[1];

            s.connectionTime = now - connectionTimestamp;

        } catch (Exception e) {
            Log.e(TAG, "Failed to load statistics", e);
            s.events = new Event[0];
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception ignore) {
            }
        }

        if (DEBUG) {
            final long end = System.currentTimeMillis();
            Log.d(TAG, "Statistics loaded in " + (end - now) + " ms");
        }

        return s;
    }


}