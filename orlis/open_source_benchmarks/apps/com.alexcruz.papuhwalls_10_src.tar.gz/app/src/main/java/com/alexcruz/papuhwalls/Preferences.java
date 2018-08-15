package com.alexcruz.papuhwalls;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

public class Preferences {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String Theme = "Theme";
    public static final String NavBarTheme = "NavBarTheme";
    public static final String StatusBarTint = "StatusBarTint";
    public static final String NavigationTint = "NavigationTint";
    public static final String Background = "Background";
    public static final String PrimaryText = "PrimaryText";
    public static final String SecondaryText = "SecondaryText";
    public static final String Accent = "Accent";
    public static final String Drawer = "Drawer";
    public static final String DrawerText = "DrawerText";
    public static final String SelectedDrawerText = "SelectedDrawerText";
    public static final String DrawerSelector = "DrawerSelector";
    public static final String BadgeBackground = "BadgeBackground";
    public static final String BadgeText = "BadgeText";
    public static final String FABapply = "FABapply";
    public static final String FABsave = "FABsave";
    public static final String FABaddLW = "FABaddLW";
    public static final String FABcrop = "FABcrop";
    public static final String FABedit = "FABedit";
    public static final String FABshare = "FABshare";
    public static final String FABpressed = "FABpressed";
    public static final String FABbackground = "FABbackground";
    public static final String NormalIcon = "NormalIcon";
    public static final String SelectedIcon = "SelectedIcon";
    public static final String LWinterval = "LWinterval";
    public static final String LWtripleTapToJump = "LWtripleTapToJump";
    public static final String gridCount = "gridCount";

    private static final int defaultUpdateInterval = 300;
    public static final int pendingIntentUnique = 0107621;

    public int Theme() {
        return sharedPreferences.getInt(Theme, context.getResources().getColor(R.color.primary));
    }

    public int NavBarTheme() {
        return sharedPreferences.getInt(NavBarTheme, context.getResources().getColor(R.color.primary));
    }

    public Boolean getNavigationTint() {
        return sharedPreferences.getBoolean(NavigationTint, true);
    }

    public boolean StatusBarTint() {
        return sharedPreferences.getBoolean(StatusBarTint, true);
    }

    public int Background() {
        return sharedPreferences.getInt(Background, context.getResources().getColor(R.color.navigation_drawer_color));
    }

    public int PrimaryText() {
        return sharedPreferences.getInt(PrimaryText, context.getResources().getColor(R.color.primary));
    }

    public int SecondaryText() {
        return sharedPreferences.getInt(SecondaryText, context.getResources().getColor(R.color.semitransparent_white));
    }

    public int Accent() {
        return sharedPreferences.getInt(Accent, context.getResources().getColor(R.color.accent));
    }

    public int Drawer() {
        return sharedPreferences.getInt(Drawer, context.getResources().getColor(R.color.navigation_drawer_color));
    }

    public int NormalIcon() {
        return sharedPreferences.getInt(NormalIcon, context.getResources().getColor(R.color.white));
    }

    public int SelectedIcon() {
        return sharedPreferences.getInt(SelectedIcon, context.getResources().getColor(R.color.primary));
    }

    public int DrawerText() {
        return sharedPreferences.getInt(DrawerText, context.getResources().getColor(R.color.white));
    }

    public int SelectedDrawerText() {
        return sharedPreferences.getInt(SelectedDrawerText, context.getResources().getColor(R.color.primary));
    }

    public int DrawerSelector() {
        return sharedPreferences.getInt(DrawerSelector, context.getResources().getColor(R.color.selector_drawer_color));
    }

    public int BadgeBackground() {
        return sharedPreferences.getInt(BadgeBackground, context.getResources().getColor(R.color.primary));
    }

    public int BadgeText() {
        return sharedPreferences.getInt(BadgeText, context.getResources().getColor(R.color.white));
    }

    public int FABapply() {
        return sharedPreferences.getInt(FABapply, context.getResources().getColor(R.color.apply_color));
    }

    public int FABsave() {
        return sharedPreferences.getInt(FABsave, context.getResources().getColor(R.color.save_color));
    }

    public int FABaddLW() {
        return sharedPreferences.getInt(FABaddLW, context.getResources().getColor(R.color.add_lw_color));
    }

    public int FABcrop() {
        return sharedPreferences.getInt(FABcrop, context.getResources().getColor(R.color.crop_color));
    }

    public int FABedit() {
        return sharedPreferences.getInt(FABedit, context.getResources().getColor(R.color.edit_color));
    }

    public int FABshare() {
        return sharedPreferences.getInt(FABshare, context.getResources().getColor(R.color.share_color));
    }

    public int FABpressed() {
        return sharedPreferences.getInt(FABpressed, context.getResources().getColor(R.color.primary));
    }

    public int FABbackground() {
        return sharedPreferences.getInt(FABbackground, context.getResources().getColor(R.color.black_semi_transparent));
    }

    public int LWinterval() {
        return getSharedPreferences().getInt(LWinterval, defaultUpdateInterval);
    }

    public int gridCount(){
        return getSharedPreferences().getInt(gridCount, 2);
    }

    public void setGridCount(int count){
        getSharedPreferencesEditor().putInt(gridCount, count).apply();
    }

    String wall_name, wall_author, wall_url;

    public Preferences(String wall_name, String wall_author, String wall_url) {
        this.wall_name = wall_name;
        this.wall_author = wall_author;
        this.wall_url = wall_url;
    }

    public String getWallName() {
        return wall_name;
    }

    public String getWallAuthor() {
        return wall_author;
    }

    public String getWallURL() {
        return wall_url;
    }

    private static final String
            PREFERENCES_NAME = "PAPUH_PREFERENCES";

    private static final String
            ROTATE_MINUTE = "rotate_time_minute",
            ROTATE_TIME = "muzei_rotate_time";

    private Context context;

    public Preferences(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
    }

    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }

    public boolean isRotateMinute() {
        return getSharedPreferences().getBoolean(ROTATE_MINUTE, false);
    }

    public static final String IS_FIRST__RUN = "first_run";

    public int getRotateTime() {
        return getSharedPreferences().getInt(ROTATE_TIME, 900000);
    }

    public void setRotateTime(int time) {
        getSharedPreferencesEditor().putInt(ROTATE_TIME, time).apply();
    }

    public void setRotateMinute(boolean bool) {
        getSharedPreferencesEditor().putBoolean(ROTATE_MINUTE, bool).apply();
    }

    public void setLWUpdateInterval(int seconds){
        getSharedPreferencesEditor().putInt(LWinterval, seconds).apply();
    }

    public void setLWtripleTapToJumpActivated(boolean activated){
        getSharedPreferencesEditor().putBoolean(LWtripleTapToJump, activated).apply();
    }

    public boolean isTripleTapToJump(){
        return getSharedPreferences().getBoolean(LWtripleTapToJump, true);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void resetPrefs(Activity context) {

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putInt(Theme, context.getResources().getColor(R.color.primary))
                .putInt(NavBarTheme, context.getResources().getColor(R.color.primary))
                .putBoolean(NavigationTint, true)
                .putBoolean(StatusBarTint, true)
                .putInt(Background, context.getResources().getColor(R.color.navigation_drawer_color))
                .putInt(PrimaryText, context.getResources().getColor(R.color.primary))
                .putInt(SecondaryText, context.getResources().getColor(R.color.semitransparent_white))
                .putInt(Accent, context.getResources().getColor(R.color.accent))
                .putInt(Drawer, context.getResources().getColor(R.color.navigation_drawer_color))
                .putInt(NormalIcon, context.getResources().getColor(R.color.white))
                .putInt(SelectedIcon, context.getResources().getColor(R.color.primary))
                .putInt(DrawerText, context.getResources().getColor(R.color.white))
                .putInt(SelectedDrawerText, context.getResources().getColor(R.color.primary))
                .putInt(DrawerSelector, context.getResources().getColor(R.color.selector_drawer_color))
                .putInt(BadgeBackground, context.getResources().getColor(R.color.primary))
                .putInt(BadgeText, context.getResources().getColor(R.color.white))
                .putInt(FABapply, context.getResources().getColor(R.color.apply_color))
                .putInt(FABsave, context.getResources().getColor(R.color.save_color))
                .putInt(FABaddLW, context.getResources().getColor(R.color.add_lw_color))
                .putInt(FABcrop, context.getResources().getColor(R.color.crop_color))
                .putInt(FABedit, context.getResources().getColor(R.color.edit_color))
                .putInt(FABshare, context.getResources().getColor(R.color.share_color))
                .putInt(FABpressed, context.getResources().getColor(R.color.primary))
                .putInt(FABbackground, context.getResources().getColor(R.color.black_semi_transparent))
                .commit();

    }

    public static void themeMe(Activity activity, Toolbar toolbar) {

        Preferences preferences = new Preferences(activity);

        toolbar.setBackgroundColor(preferences.Theme());
        activity.getWindow().getDecorView().setBackgroundColor(preferences.Background());


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(preferences.Theme());
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (preferences.getNavigationTint()) {
                activity.getWindow().setNavigationBarColor(preferences.NavBarTheme());
            } else {
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.navigation_drawer_color));
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (preferences.StatusBarTint()) {
                activity.getWindow().setStatusBarColor(tint(preferences.Theme(), 0.8));
            } else {
                activity.getWindow().setStatusBarColor(preferences.Theme());
            }
        }
    }

    public static int tint(int color, double factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }

    public ArrayList<String> getLiveWalls() {
        File saveWallLoc = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + context.getResources().getString(R.string.walls_save_location));
        ArrayList<String> liveWalls = new ArrayList<>();
        File file[] = saveWallLoc.listFiles();
        if(file != null) {
            for (File wall : file) {
                if (wall.getName().startsWith("PapuhLive")) {
                    liveWalls.add(wall.getAbsolutePath());
                }
            }
        }
        return liveWalls;
    }

    public boolean isWallAddedToLWList(String wall){
        return getLiveWalls().contains(wall);
    }


}
