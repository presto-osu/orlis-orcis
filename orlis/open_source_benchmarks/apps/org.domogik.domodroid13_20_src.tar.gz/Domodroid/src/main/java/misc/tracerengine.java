package misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import database.WidgetUpdate;

public class tracerengine {
    private static tracerengine instance;
    private static Boolean to_Android = true;
    private static Boolean to_txtFile = false;
    private static Boolean to_screen = false;
    private static Boolean txtappend = false;
    private static String logpath = null;
    private static String logname = null;
    private static Boolean Debug = true;
    private static Boolean Info = false;
    private static Boolean Error = false;
    private static Boolean Verbose = false;
    private static Boolean Warning = false;
    private static Context context;
    private static SharedPreferences settings = null;

    /*
     * It's not elegant, but Tracer will store the reference to widgetupdate instance,
     * and will offer to all users using Tracer to also retrieve instance
     * of state engine.....
     */
    private static WidgetUpdate state_engine = null;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private static FileWriter txtFile = null;

    public Boolean DBEngine_running = false;
    public Boolean force_Main = false;
    public Boolean Map_as_main = false;
    /*
     * This class is fully static
	 * Only one instance will be started (by Main) and used by all other components
	 */

    /*******************************************************************************
     * Internal Constructor
     *******************************************************************************/
    private tracerengine(Context context) {
        super();
        tracerengine.context = context;
        force_Main = false;
        Map_as_main = false;
    }

    public static tracerengine getInstance(SharedPreferences params, Context context) {
        com.orhanobut.logger.Logger.init("tracerengine").methodCount(0);
        if (instance == null) {
            Logger.d("Creating instance........................");
            instance = new tracerengine(context);
        }
        settings = params;
        set_profile(params);
        return instance;

    }

    public static tracerengine getInstance(Context context) {
        com.orhanobut.logger.Logger.init("tracerengine").methodCount(0);
        if (instance == null) {
            Logger.d("Creating instance........................");
            instance = new tracerengine(context);
        }
        return instance;

    }

    public void close() {
        if (txtFile != null) {
            try {
                txtFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            txtFile = null;
        }
    }

    public void d(String tag, String msg) {
        if (Debug)
            choose_log(0, tag, msg);
    }

    public void e(String tag, String msg) {
        if (Error)
            choose_log(1, tag, msg);
    }

    public void i(String tag, String msg) {
        if (Info)
            choose_log(2, tag, msg);
    }

    public void v(String tag, String msg) {
        if (Verbose)
            choose_log(3, tag, msg);
    }

    public void w(String tag, String msg) {
        if (Warning)
            choose_log(4, tag, msg);
    }

    public void json(String tag, String msg) {
        if (Verbose)
            choose_log(5, tag, msg);
    }

    /*
     *
     * Configure Tracer profile
     */
    public static void set_profile(SharedPreferences params) {
        settings = params;
        get_settings();

    }

    public static void refresh_settings() {
        if (settings != null) {
            logpath = settings.getString("LOGPATH", "");
            logname = settings.getString("LOGNAME", "");
            to_Android = settings.getBoolean("SYSTEMLOG", false);
            to_txtFile = settings.getBoolean("TEXTLOG", false);
            to_screen = settings.getBoolean("SCREENLOG", false);
            txtappend = settings.getBoolean("LOGAPPEND", false);

            Debug = settings.getBoolean("LOG_DEBUG", true);
            Info = settings.getBoolean("LOG_INFO", true);
            Error = settings.getBoolean("LOG_ERROR", true);
            Verbose = settings.getBoolean("LOG_VERBOSE", true);
            Warning = settings.getBoolean("LOG_WARNING", true);

        }

    }

    private static void get_settings() {
        if (settings != null) {
            Boolean changed = settings.getBoolean("LOGCHANGED", true);
            if (changed) {
                refresh_settings();
                if (txtFile != null) {
                    txtFile = null;    //close object
                }
                if (to_txtFile) {
                    if (logname.equals("")) {
                        // If no filename given, no log to file, nor in append !
                        to_txtFile = false;
                        txtappend = false;

                    } else {
                        // file path given : try to open it....
                        try {
                            txtFile = new FileWriter(logpath + logname, txtappend);
                            txtlog(2, " ", "Starting log session");
                        } catch (Exception e) {
                            txtFile = null;
                            to_txtFile = false;
                            txtappend = false;
                        }

                    }
                }
                SharedPreferences.Editor prefEditor = settings.edit();
                prefEditor.putBoolean("LOGCHANGED", false);
                prefEditor.putBoolean("TEXTLOG", to_txtFile);    //In case open fails.... don't retry till next change !
                prefEditor.commit();

            }
            // Nothing changed
        }
    }

    /*
     * all modes use this common method, to decide wich kind of logging is configured
     */
    private static void choose_log(int type, String tag, String msg) {

        // if needed, log to Android
        if (to_Android)
            syslog(type, tag, msg);
        // if needed, log to text file
        if (to_txtFile)
            txtlog(type, tag, msg);
        if (to_screen) {
            screenlog(tag, msg);
        }
    }

    /*
     * Method writing messages to screen view
     */
    private static void screenlog(String tag, String msg) {
        com.orhanobut.logger.Logger.init("tracerengine").methodCount(0);
        try {
            Toast.makeText(context, tag + ":" + msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logger.e("Tracerengine ScreenLog: " + e.toString());
        }
    }

    /*
     * Method writing messages to text file
     */
    private static void txtlog(int type, String tag, String msg) {
        if (txtFile != null) {
            Date now = new Date();
            String typeC = " ";
            String dateS = sdf.format(now);
            String tagS = String.format("%-26s", tag);

            int tid = android.os.Process.myTid();
            String tids = Integer.toString(tid);
            /*
             int pid = android.os.Process.myPid();
			 String pids = Integer.toString(pid);
			 */
            switch (type) {
                case 0:
                    typeC = "D";
                    break;
                case 1:
                    typeC = "E";
                    break;
                case 2:
                    typeC = "I";
                    break;
                case 3:
                    typeC = "V";
                    break;
                case 4:
                    typeC = "W";
                    break;
                case 5:
                    typeC = "V";
                    break;
            }
            try {
                String line = typeC + " | " + dateS + " | " + tids + " | " + tagS + " | " + msg;
                //String line = typeC+" | "+dateS+" | "+tagS+" | "+msg;
                //Log.w(tag,line);
                txtFile.write(line + "\n");
                //txtFile.flush(); 	//To improve performances, don't flush on each write
            } catch (IOException i) {
                txtFile = null;        //Abort log to text file in future
                to_txtFile = false;
                Logger.e("Tracerengine FileLog: " + i.toString());
            }
        }
    }

    /*
     * method sending messages to system log ( for Eclipse, Android Studio or CatLog )
     * It use the https://github.com/orhanobut/logger log system.
     */
    private static void syslog(int type, String tag, String msg) {
        if (tag == null)
            tag = "Null tag";
        com.orhanobut.logger.Logger.init(tag).methodCount(5).hideThreadInfo().methodOffset(0);
        switch (type) {
            case 0:
                //Log.d(tag, msg);
                Logger.d(msg);
                break;
            case 1:
                //Log.e(tag, msg);
                Logger.e(msg);
                break;
            case 2:
                //Log.i(tag, msg);
                Logger.i(msg);
                break;
            case 3:
                //Log.v(tag, msg);
                Logger.v(msg);
                break;
            case 4:
                //Log.w(tag, msg);
                Logger.v(msg);
                break;
            case 5:
                //Log.w(tag, msg);
                Logger.json(msg);
                break;

        }
    }

    public WidgetUpdate get_engine() {
        return state_engine;
    }

    public static void set_engine(WidgetUpdate engine) {
        state_engine = engine;
    }

}
