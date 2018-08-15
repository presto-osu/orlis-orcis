package com.markusborg.logic;

import android.content.Context;
import android.util.Log;

import com.markusborg.ui.MainActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Markus Borg on 2015-08-08.
 */
public class LogHandler {

    private Context appContext;
    private static String[] history; // all LogHandlers should share the same history
    private int n;
    private ArrayList<Setting> settingList;

    public final int MAX_HISTORY = 100;

    public LogHandler(Context appContext) {
        this.appContext = appContext;
        history = new String[MAX_HISTORY];
        n = 0;
        settingList = new ArrayList<Setting>();

        // Try to populate the string array
        try {
            FileInputStream fis = appContext.openFileInput(MainActivity.FILENAME);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (n < MAX_HISTORY) {
                    history[n] = line;
                    settingList.add(new Setting(line));
                    n++;
                }
            }
        } catch (FileNotFoundException e) {

        } catch (UnsupportedEncodingException e) {

        } catch (IOException e) {

        }
    }

    /**
     * Add a new ghosting session to the log
     * @param theSetting
     */
    public void addSessionToLog(Setting theSetting) {
        // move all history items one position
        if (n == 1) {
            history[1] = history[0];
        }
        else if (n == MAX_HISTORY) {
            for (int i=MAX_HISTORY-1; i>=1; i--) {
                history[i] = history[i-1];
            }
        }
        else {
            for (int i=n; i>=1; i--) {
                history[i] = history[i-1];
            }
        }

        // add new item
        history[0] = theSetting.toString();
        settingList.add(new Setting(history[0]));
        if (n < MAX_HISTORY) {
            n++;
        }

        // print them to file
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(appContext.openFileOutput(MainActivity.FILENAME, Context.MODE_PRIVATE));

            for (int i=0; i<n; i++) {
                outputStreamWriter.write(history[i] + "\n");
            }

            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Get lines from the log as strings.
     * @param nbr number of previous sessions to extract
     * @return The String
     */
    public String getFromLog(int nbr) {
        StringBuffer sb = new StringBuffer();
        try {
            for (int i = 0; i < n && i < nbr; i++) {
                String[] split = history[i].split(";");
                String ms = split[2].trim();
                int millis = Integer.parseInt(ms);
                float s = ((float) millis / 1000); // turn ms to s
                sb.append(split[0] + ";" + split[1] + "; " + s + ";" + split[3] + "\n");
            }
        }
        catch (Exception e) {
            // TODO: Catch the right type of exception
        }
        return sb.toString();
    }

    /**
     * Get history as a list of strings.
     * @param nbr number of previous sessions to extract
     * @return The list
     */
    public ArrayList<String> getArrayList(int nbr) {
        ArrayList<String> historyList = new ArrayList<String>();
        for (int i=0; i<n && i<nbr; i++) {
            historyList.add(history[i]);
        }
        return historyList;
    }

    public ArrayList<Setting> getSettingList() {
        return settingList;
    }

}
