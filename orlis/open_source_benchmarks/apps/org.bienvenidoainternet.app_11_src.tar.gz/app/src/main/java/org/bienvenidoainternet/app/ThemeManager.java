package org.bienvenidoainternet.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *   BaiApp - Bienvenido a internet Android Application
 *   Copyright (C) 2016 Renard1911(https://github.com/Renard1911)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ThemeManager {
    private int currentThemeId;
    private int prefThemeId;
    private Activity activity;
    public ThemeManager(Activity activity){
        this.activity = activity;
        setCurrentThemeId();
    }

    public void setCurrentThemeId(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        int themeId = Integer.valueOf(settings.getString("pref_theme", "1"));
        prefThemeId = themeId;
        switch (themeId) {
            case 1:
                currentThemeId = R.style.AppTheme_NoActionBar;
                break;
            case 2:
                currentThemeId = R.style.AppTheme_Dark;
                break;
            case 3:
                currentThemeId = R.style.AppTheme_HeadLine;
//                setTheme(R.style.AppTheme_HeadLine_Activity);
                break;
            case 4:
                currentThemeId = R.style.AppTheme_Black;
//                setTheme(R.style.AppTheme_Black_Activity);
                break;
        }
        Log.d("ThemeManager", "isDarkTheme: " + isDarkTheme());
    }

    public int getSageColor(){
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.sageColor});
        return a.getColor(0, Color.CYAN);
    }

    public int getMarginColor(){
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.marginColor});
        return a.getColor(0, Color.CYAN);
    }

    public void updateThemeId(int id){
        this.currentThemeId = id;
    }

    public int getNameColor() {
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.nameColor});
        return a.getColor(0, Color.CYAN);
    }

    public int getTripcodeColor() {
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.tripcodeColor});
        return a.getColor(0, Color.CYAN);
    }

    public int getPrimaryColor(){
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.colorPrimary});
        return a.getColor(0, Color.CYAN);
    }
    public int getPrimaryDarkColor(){
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.colorPrimaryDark});
        return a.getColor(0, Color.CYAN);
    }

    public boolean isDarkTheme(){
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.isDarkTheme});
        return a.getBoolean(0, false);
    }

    public int getThemeForActivity(){
        int id = R.style.AppTheme;
        switch (prefThemeId) {
            case 1: // pseudoch
                id = R.style.AppTheme;
                break;
            case 2: // nightmode
                id = R.style.AppTheme;
                break;
            case 3: // photon
                id = R.style.AppTheme_HeadLineActionBar;
                break;
            case 4: // tomorrow
                id = R.style.AppTheme_BlackActionBar;
                break;
        }
        return id;
    }

    public int getThemeForMainActivity(){
        int id = R.style.AppTheme_NoActionBar;
        switch (prefThemeId) {
            case 1: // pseudoch
                id = R.style.AppTheme_NoActionBar;
                break;
            case 2: // nightmode
                id = R.style.AppTheme_NoActionBar;
                break;
            case 3: // photon
                id = R.style.AppTheme_HeadLineActionBar_NoActionBar;
                break;
            case 4: // tomorrow
                id = R.style.AppTheme_BlackActionBar_NoActionBar;
                break;
        }
        return id;
    }


    public int getCurrentThemeId() {
        return currentThemeId;
    }

    public int getQuoteColor() {
        TypedArray a = activity.getTheme().obtainStyledAttributes(currentThemeId, new int[]{R.attr.quoteColor});
        return a.getColor(0, Color.CYAN);
    }
}
