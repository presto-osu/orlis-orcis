/**
 * Copyright (C) 2015 Damien Chazoule
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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public class FlashDevice {

	// Declaring your view and variables
    private static final String TAG = "FlashDevice";
    private static int mValueOff;
    private static int mValueOn;
	private static String mFlashDevice;
    private static boolean mUseCameraInterface;
	private WakeLock mWakeLock;
    public static final int STROBE = -1;
    public static final int OFF = 0;
    public static final int ON = 1;
    private static FlashDevice mInstance;
	private static boolean mSurfaceCreated = false;
    private static SurfaceTexture mSurfaceTexture = null;
	private FileWriter mFlashDeviceWriter = null;
    private int mFlashMode = OFF;
    private Camera mCamera = null;
    private Camera.Parameters mParams;
	private Context mContext;
    private static SharedPreferences mPreferences;

    private FlashDevice(Context context) {
		mContext = context;
        mValueOff = context.getResources().getInteger(R.integer.valueOff);
        mValueOn = context.getResources().getInteger(R.integer.valueOn);
        mFlashDevice = context.getResources().getString(R.string.flashDevice);
        mUseCameraInterface = context.getResources().getBoolean(R.bool.useCameraInterface);
		if (mUseCameraInterface) {
            PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Torch");
        }
    }

    public static synchronized FlashDevice getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FlashDevice(context);
        }
        return mInstance;
    }

    public synchronized void setFlashMode(int mode) {

        Log.v(TAG, mode+"");
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean mPrefDevice = mPreferences.getBoolean("mPrefDevice", false);

        if (mPrefDevice) {
            try {
                if (mUseCameraInterface) {
                    if (mCamera == null) {
                        mCamera = Camera.open();
                    }
                    if (mode == OFF || mode == STROBE) {
                        mParams = mCamera.getParameters();
                        mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(mParams);
                        if (mode != STROBE) {
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;
                            mSurfaceCreated = false;
                        }
						if (mWakeLock.isHeld()) {
							mWakeLock.release();
						}
                    } else {
                        if (!mSurfaceCreated) {
							int[] mTextures = new int[1];
							// Generate one texture pointer and bind it as an external texture.
							GLES20.glGenTextures(1, mTextures, 0);
							GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[0]);
							// No mip-mapping with camera source.
							GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 
									GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
							GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 
									GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
							// Clamp to edge is only option.
							GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 
									GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
							GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 
									GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

							FlashDevice.mSurfaceTexture = new SurfaceTexture(mTextures[0]);
							mCamera.setPreviewTexture(FlashDevice.mSurfaceTexture);
							mSurfaceCreated = true;
                            mCamera.startPreview();
                        }
                        mParams = mCamera.getParameters();
                        mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(mParams);
						if (!mWakeLock.isHeld()) { // Only get the wakelock if we don't have it already
							mWakeLock.acquire(); // We don't want to go to sleep while cam is up
						}
                    }
                } else {
                    // Devices with sysfs toggle
                    if (mFlashDeviceWriter == null) {
                        mFlashDeviceWriter = new FileWriter(mFlashDevice);
                    }
                    if (mode == OFF) {
                        mFlashDeviceWriter.write(String.valueOf(mValueOff));
                        mFlashDeviceWriter.close();
                        mFlashDeviceWriter = null;
                    } else {
                        mFlashDeviceWriter.write(String.valueOf(mValueOn));
                        mFlashDeviceWriter.flush();
                    }
                }
                mFlashMode = mode;
            } catch (IOException e) { // No flash ?
                throw new RuntimeException("Can't open flash device", e);
            }
        } else {
            mFlashMode = mode;
        }
    }

    public int getFlashMode() {
        return mFlashMode;
    }
}
