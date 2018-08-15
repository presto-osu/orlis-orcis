package com.saladdressing.veterondo.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SPS {

    public static final String FILENAME = "veterondoPrefs";
    public static final int MODE = Context.MODE_PRIVATE;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    Context context;

    /**
     * SPS is a SharedPreferences wrapper for less boilerplate.
     *
     * I think that I actually managed to INCREASE boilerplate using this class
     * but at least I'm feeling smart. For now.
     *
     * @param context
     */
    public SPS(Context context) {

        this.context = context;
        sharedPrefs = context.getSharedPreferences(FILENAME, MODE);
        editor = sharedPrefs.edit();


    }


    public SharedPreferences getPrefs() {
        return sharedPrefs;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }


}
