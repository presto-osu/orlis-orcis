package com.claha.showtimeremote.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseSettings {

    private final Resources resources;
    private final SharedPreferences sharedPreferences;

    protected BaseSettings(Context context) {
        resources = context.getResources();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getStringResource(int id) {
        return resources.getString(id);
    }

    private int getInt(String key) {
        return sharedPreferences.getInt(key, -1);
    }

    protected int getInt(int id) {
        return getInt(getStringResource(id));
    }

    private void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    protected void putInt(int id, int value) {
        putInt(getStringResource(id), value);
    }

    private boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    protected boolean getBoolean(int id) {
        return getBoolean(getStringResource(id));
    }

    private String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    protected String getString(int id) {
        return getString(getStringResource(id));
    }

    private void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    protected void putString(int id, String value) {
        putString(getStringResource(id), value);
    }

    private Set<String> getStringSet(String key) {
        return sharedPreferences.getStringSet(key, new HashSet<String>());
    }

    private void putStringSet(String key, Set<String> values) {
        sharedPreferences.edit().putStringSet(key, values).apply();
    }

    private List<String> getStringList(String key) {
        Set<String> temp = getStringSet(key);
        List<String> values = new ArrayList<>(temp);
        Collections.sort(values);
        return values;
    }

    protected List<String> getStringList(int id) {
        return getStringList(getStringResource(id));
    }

    private void putStringList(String key, List<String> values) {
        putStringSet(key, new HashSet<>(values));
    }

    protected void putStringList(int id, List<String> values) {
        putStringList(getStringResource(id), values);
    }
}
