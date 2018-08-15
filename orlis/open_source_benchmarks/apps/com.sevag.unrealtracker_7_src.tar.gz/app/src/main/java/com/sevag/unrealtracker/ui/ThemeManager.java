package com.sevag.unrealtracker.ui;

import android.app.Activity;
import android.content.Context;

import java.io.*;

/**
 * Created by sevag on 3/28/15.
 */
public class ThemeManager {
    private static final String FILENAME = "unreal_tracker_file";

    public static void setTheme(int theme, Activity callingActivity) {
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fos = callingActivity.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            dos = new DataOutputStream(fos);
            dos.writeInt(theme);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (dos != null) {
                    dos.close();
                }
            } catch (Exception ex) {
                //code done fucked up now
            }
        }
    }

    public static int getTheme(Activity callingActivity) {
        FileInputStream fis = null;
        DataInputStream dis = null;
        int theme = 0;
        try {
            fis = callingActivity.openFileInput(FILENAME);
            dis = new DataInputStream(fis);
            theme = dis.readInt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (dis != null) {
                    dis.close();
                }
            } catch (Exception ex) {
                //code done fucked up now
            }
        }
        return theme;
    }
}
