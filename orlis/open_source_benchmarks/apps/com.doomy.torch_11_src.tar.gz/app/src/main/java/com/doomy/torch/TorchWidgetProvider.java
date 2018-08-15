/**
 * Copyright (C) 2014 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.torch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class TorchWidgetProvider extends AppWidgetProvider {

	// Declaring your view and variables
	private static final String TAG = "TorchWidgetProvider";
    private static TorchWidgetProvider mInstance;

    public void disableWidget(Context context) {
        ComponentName mComponentName = new ComponentName(context, TorchWidgetProvider.class);
        PackageManager mPackageManager = context.getApplicationContext().getPackageManager();
        mPackageManager.setComponentEnabledSetting(mComponentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    static synchronized TorchWidgetProvider getInstance() {
        if (mInstance == null) {
            mInstance = new TorchWidgetProvider();
        }
        return mInstance;
    }

    private enum widgetState {
        OFF(R.drawable.widget_off), ON(R.drawable.widget_on);

        /**
         * The drawable resources associated with this widget state.
         */
        private final int mDrawImgRes;

        private widgetState(int myDrawImgRes) {
            mDrawImgRes = myDrawImgRes;
        }

        public int getImgDrawable() {
            return mDrawImgRes;
        }
    }

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            this.updateState(context, appWidgetId);
    }

    private static PendingIntent getLaunchPendingIntent(Context context, int appWidgetId,
                                                        int buttonId) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean mPrefScreen = mPreferences.getBoolean(SettingsActivity.KEY_SCREEN, false);
        if (mPrefScreen) {
            Intent mIntent = new Intent(context, MainActivity.class);
            PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
            return mPendingIntent;
        } else {
            Intent mLaunchIntent = new Intent();
            mLaunchIntent.setClass(context, TorchWidgetProvider.class);
            mLaunchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            mLaunchIntent.setData(Uri.parse("custom:" + appWidgetId + "/" + buttonId));
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, mLaunchIntent, 0);
            return mPendingIntent;
        }
    }

	@Override
    public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
        super.onReceive(context, intent);
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            Uri mData = intent.getData();
            int mButtonId;
            int mWidgetId;
            mWidgetId = Integer.parseInt(mData.getSchemeSpecificPart().split("/")[0]);
            mButtonId = Integer.parseInt(mData.getSchemeSpecificPart().split("/")[1]);

            if (mButtonId == 0) {
                Intent mPendingIntent = new Intent(TorchSwitch.TOGGLE_FLASHLIGHT);
                mPendingIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                mPendingIntent.putExtra("sos",
                        mPreferences.getBoolean("widget_sos" + mWidgetId, false));
                context.sendBroadcast(mPendingIntent);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO auto-generated catch block
                e.printStackTrace();
            }
            this.updateAppWidget(context);
        } else if (intent.getAction().equals(TorchSwitch.TORCH_STATE_CHANGED)) {
            this.updateAppWidget(context);
        }
    }

    public void updateAppWidget(Context context) {
        AppWidgetManager mWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = mWidgetManager.getAppWidgetIds(
                new ComponentName(context, getClass()));
        for (int appWidgetId : appWidgetIds)
            this.updateState(context, appWidgetId);
    }

    public void updateState(Context context, int appWidgetId) {
        RemoteViews mViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent mStateIntent = context.registerReceiver(null,
                new IntentFilter(TorchSwitch.TORCH_STATE_CHANGED));
        boolean on = mStateIntent != null && mStateIntent.getIntExtra("state", 0) != 0;

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String mPrefColor = mPreferences.getString(WidgetActivity.KEY_WIDGET_COLOR, context.getString(R.string.red));
        mViews.setInt(R.id.shape, "setColorFilter", Utils.getPrefColor(context, mPrefColor));

        mViews.setOnClickPendingIntent(R.id.button, getLaunchPendingIntent(context, appWidgetId, 0));

        if (on) {
            mViews.setImageViewResource(R.id.widget, widgetState.ON.getImgDrawable());
        } else {
            mViews.setImageViewResource(R.id.widget, widgetState.OFF.getImgDrawable());
        }

        AppWidgetManager mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetManager.updateAppWidget(appWidgetId, mViews);
    }
}
