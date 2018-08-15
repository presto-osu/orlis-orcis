package rinor;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.orhanobut.logger.Logger;

public class Stats_Com {
    private static Stats_Com instance = null;
    // Supported kinds of counters
    public static final int EVENTS_SEND = 1;
    public static final int EVENTS_RCV = 2;
    public static final int STATS_SEND = 3;
    public static final int STATS_RCV = 4;
    //Cumulative counters
    public static int cumul_events_sent_packets = 0;
    public static int cumul_events_recv_packets = 0;
    public static int cumul_events_sent_bytes = 0;
    public static int cumul_events_recv_bytes = 0;

    public static int cumul_stats_sent_packets = 0;
    public static int cumul_stats_recv_packets = 0;
    public static int cumul_stats_sent_bytes = 0;
    public static int cumul_stats_recv_bytes = 0;

    //periodic counters
    public static int periodic_events_sent_packets = 0;
    public static int periodic_events_recv_packets = 0;
    public static int periodic_events_sent_bytes = 0;
    public static int periodic_events_recv_bytes = 0;

    public static int periodic_stats_sent_packets = 0;
    public static int periodic_stats_recv_packets = 0;
    public static int periodic_stats_sent_bytes = 0;
    public static int periodic_stats_recv_bytes = 0;

    private static int elapsed_period = 0;
    private static int cumul_period = 0;
    private static int period;                // Number of seconds between periodic clears
    private static Timer timer = null;
    private static Boolean sleeping = false;

    /*******************************************************************************
     * Internal Constructor
     *******************************************************************************/
    private Stats_Com() {
        super();
        if (period < 10)
            period = 10 * 60;    //10 minutes by default
        cumul_period = 0;
        Timer();
    }

    public static Stats_Com getInstance() {
        if (instance == null) {
            instance = new Stats_Com();
        }

        return instance;

    }

    public void add(int type, int count) {
        switch (type) {
            case EVENTS_SEND:
                periodic_events_sent_packets++;    //1 packet more
                cumul_events_sent_packets++;
                periodic_events_sent_bytes += count;    //And N bytes more
                cumul_events_sent_bytes += count;
                break;
            case EVENTS_RCV:
                periodic_events_recv_packets++;    //1 packet more
                cumul_events_recv_packets++;
                periodic_events_recv_bytes += count;    //And N bytes more
                cumul_events_recv_bytes += count;
                break;
            case STATS_SEND:
                periodic_stats_sent_packets++;    //1 packet more
                cumul_stats_sent_packets++;
                periodic_stats_sent_bytes += count;    //And N bytes more
                cumul_stats_sent_bytes += count;
                break;
            case STATS_RCV:
                periodic_stats_recv_packets++;    //1 packet more
                cumul_stats_recv_packets++;
                periodic_stats_recv_bytes += count;    //And N bytes more
                cumul_stats_recv_bytes += count;
                break;
        }
    }

    private void clear() {
        periodic_events_sent_packets = 0;
        periodic_events_recv_packets = 0;
        periodic_events_sent_bytes = 0;
        periodic_events_recv_bytes = 0;

        periodic_stats_sent_packets = 0;
        periodic_stats_recv_packets = 0;
        periodic_stats_sent_bytes = 0;
        periodic_stats_recv_bytes = 0;
        elapsed_period = 0;
    }

    private String get_Period_String(int seconds) {
        String result = "";
        int hours = 0;
        int minutes = 0;
        int reste = 0;

        if (seconds != 0) {

            if (seconds <= 60) {
                result = seconds + " secs";
            } else if (seconds > 3600) {
                hours = seconds / 3600;
                result += hours + " h ";
                reste = seconds - (hours * 3600);
                if (reste <= 60) {
                    result += "0 mn " + reste + " sec";
                } else {
                    minutes = reste / 60;
                    reste = reste - (minutes * 60);
                    result += minutes + " mn " + reste + " sec";
                }
            } else if (seconds > 60) {
                minutes = seconds / 60;
                reste = seconds - (minutes * 60);
                result = minutes + " mn " + reste + " sec";
            }
        }
        return result;
    }

    public String get_elapsed_period() {
        return get_Period_String(elapsed_period);
    }

    public String get_cumul_period() {
        return get_Period_String(cumul_period);
    }

    public void set_sleeping() {
        sleeping = true;
    }

    public void wakeup() {
        sleeping = false;
    }

    public void cancel() {
        com.orhanobut.logger.Logger.init("Stats_Com").methodCount(0);
        Logger.w("cancel requested !");

        if (timer != null)
            timer.cancel();
        try {
            this.finalize();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void Timer() {
        timer = new Timer();

        TimerTask doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    elapsed_period++;
                    if (!sleeping)
                        cumul_period++;

                    if (elapsed_period >= period) {
                        clear();
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }
        };
        if (timer != null) {
            timer.schedule(doAsynchronousTask, 0, 1000);    // Once per second

        }
    }
}
