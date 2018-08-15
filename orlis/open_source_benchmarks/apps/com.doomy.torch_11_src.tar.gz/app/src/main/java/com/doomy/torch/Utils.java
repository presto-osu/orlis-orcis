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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

public class Utils {

    // Declaring your view and variables
	private static Boolean mPrefDevice;
    private static Camera mCamera = null;
    private static SharedPreferences mPreferences;

	/**
     * Check if the device has a camera flash.
     *
     * @return The preference value.
     */
    public static boolean deviceHasCameraFlash(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            mCamera = Camera.open();
            if (mCamera == null) {
                mPrefDevice = mPreferences.edit().putBoolean("mPrefDevice", false).commit();
                return mPrefDevice;
            }
            Camera.Parameters mParameters = mCamera.getParameters();
            if (mParameters.getFlashMode() == null) {
                mPrefDevice = mPreferences.edit().putBoolean("mPrefDevice", false).commit();
                return mPrefDevice;
            }
            List<String> mSupportedFlashModes = mParameters.getSupportedFlashModes();
            if (!mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                mPrefDevice = mPreferences.edit().putBoolean("mPrefDevice", false).commit();
                return mPrefDevice;
            }
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            Log.e("Error : ", e+"");
        }
        mPrefDevice = mPreferences.edit().putBoolean("mPrefDevice", true).commit();
        return mPrefDevice;
    }

	/**
     * Gets the preference color.
     *
     * @return The preference color.
     */
    public static int getPrefColor(Context context, String preference) {

        int mColor = context.getResources().getColor(R.color.red);
		
        if (preference.equals(context.getString(R.string.red))) {
            mColor = context.getResources().getColor(R.color.red);
            return mColor;
        } else if (preference.equals(context.getString(R.string.pink))) {
            mColor = context.getResources().getColor(R.color.pink);
            return mColor;
        } else if (preference.equals(context.getString(R.string.purple))) {
            mColor = context.getResources().getColor(R.color.purple);
            return mColor;
        } else if (preference.equals(context.getString(R.string.deepPurple))) {
            mColor = context.getResources().getColor(R.color.deepPurple);
            return mColor;
        } else if (preference.equals(context.getString(R.string.indigo))) {
            mColor = context.getResources().getColor(R.color.indigo);
            return mColor;
        } else if (preference.equals(context.getString(R.string.blue))) {
            mColor = context.getResources().getColor(R.color.blue);
            return mColor;
        } else if (preference.equals(context.getString(R.string.lightBlue))) {
            mColor = context.getResources().getColor(R.color.lightBlue);
            return mColor;
        } else if (preference.equals(context.getString(R.string.cyan))) {
            mColor = context.getResources().getColor(R.color.cyan);
            return mColor;
        } else if (preference.equals(context.getString(R.string.teal))) {
            mColor = context.getResources().getColor(R.color.teal);
            return mColor;
        } else if (preference.equals(context.getString(R.string.green))) {
            mColor = context.getResources().getColor(R.color.green);
            return mColor;
        } else if (preference.equals(context.getString(R.string.lightGreen))) {
            mColor = context.getResources().getColor(R.color.lightGreen);
            return mColor;
        } else if (preference.equals(context.getString(R.string.lime))) {
            mColor = context.getResources().getColor(R.color.lime);
            return mColor;
        } else if (preference.equals(context.getString(R.string.yellow))) {
            mColor = context.getResources().getColor(R.color.yellow);
            return mColor;
        } else if (preference.equals(context.getString(R.string.amber))) {
            mColor = context.getResources().getColor(R.color.amber);
            return mColor;
        } else if (preference.equals(context.getString(R.string.orange))) {
            mColor = context.getResources().getColor(R.color.orange);
            return mColor;
        } else if (preference.equals(context.getString(R.string.deepOrange))) {
            mColor = context.getResources().getColor(R.color.deepOrange);
            return mColor;
        } else if (preference.equals(context.getString(R.string.brown))) {
            mColor = context.getResources().getColor(R.color.brown);
            return mColor;
        } else if (preference.equals(context.getString(R.string.grey))) {
            mColor = context.getResources().getColor(R.color.grey);
            return mColor;
        } else if (preference.equals(context.getString(R.string.blueGrey))) {
            mColor = context.getResources().getColor(R.color.blueGrey);
            return mColor;
        }
        return mColor;
    }
	
	public static void setMainTheme(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.setTheme(R.style.MaterialTranslucent);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.setTheme(R.style.HoloTranslucent);
        } else {
            activity.setTheme(R.style.HoloWithoutBar);
        }
	}

    public static void setPreferenceTheme(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.setTheme(R.style.Material);
        } else {
            activity.setTheme(R.style.Holo);
        }
    }

    public static void colorizeBar(Activity activity, Context context, String preference) {

        Window mWindow = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ActionBar mActionBar = activity.getActionBar();

        if (preference.equals(context.getString(R.string.red))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.red));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.redDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.pink))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.pink));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.pinkDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.purple))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.purple));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.purpleDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.deepPurple))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.deepPurple));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.deepPurpleDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.indigo))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.indigo));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.indigoDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.blue))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.blue));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.blueDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.lightBlue))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.lightBlue));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.lightBlueDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.cyan))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.cyan));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.cyanDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.teal))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.teal));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.tealDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.green))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.green));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.greenDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.lightGreen))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.lightGreen));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.lightGreenDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.lime))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.lime));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.limeDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.yellow))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.yellow));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.yellowDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.amber))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.amber));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.amberDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.orange))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.orange));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.orangeDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.deepOrange))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.deepOrange));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.deepOrangeDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.brown))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.brown));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.brownDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.grey))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.grey));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.greyDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        } else if (preference.equals(context.getString(R.string.blueGrey))) {
            ColorDrawable mColorDrawable = new ColorDrawable(context.getResources().getColor(R.color.blueGrey));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(context.getResources().getColor(R.color.blueGreyDark));
            }
            mActionBar.setBackgroundDrawable(mColorDrawable);
        }
    }
}
