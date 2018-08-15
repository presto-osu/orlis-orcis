/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

/**
 * Created by Graham on 10/12/2014.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class BundleCache {

    private static BundleCache mInstance;
    private Context context;
    private static Gson GSON = new Gson();
    private String namePrefix;

    private BundleCache(Context context) {
        this.context = context;
        namePrefix = context.getPackageName() + "_bundleCache_";
    }

    public static BundleCache getInstance(Context applicationContext) {
        if (mInstance == null) {
            mInstance = new BundleCache(applicationContext);
        }
        return mInstance;
    }

    public synchronized void clearCache(String key) {
        clearCacheSub(key);
    }

    private void clearCacheSub(String key) {
        String fileName = namePrefix + key;
        try {
            context.deleteFile(fileName);
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("clearCache failed on: %s ex: %s", fileName, ex));
        }
    }

    public synchronized void putBundle(String key, Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("bundle is null");
        }
        if (key.equals("") || key == null) {
            throw new IllegalArgumentException("key is empty or null");
        }
        String fileName = namePrefix + key;
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            Parcel p = Parcel.obtain();
            bundle.writeToParcel(p, 0);
            byte[] buffer = p.marshall();
            fos.write(buffer);
            fos.flush();
            fos.close();
            Log.d(getClass().getName(), String.format("putBundle completed for, ex: %s", fileName));
        } catch (Exception ex) {
            clearCacheSub(key);
            Log.w(getClass().getName(), String.format("putBundle failed, on: %s ex: %s", fileName, ex));
        }
    }

    public synchronized Bundle getBundle(String key) {
        if (key.equals("") || key == null) {
            throw new IllegalArgumentException("key is empty or null");
        }
        String fileName = namePrefix + key;
        try {
            FileInputStream fis = context.openFileInput(fileName);
            byte[] buffer = new byte[16384];
            fis.read(buffer);
            Parcel p = Parcel.obtain();
            p.unmarshall(buffer, 0, buffer.length);
            p.setDataPosition(0);
            Bundle b = p.readBundle();
            Log.d(getClass().getName(), String.format("getBundle completed for, ex: %s", fileName));
            return b;
        } catch (Exception ex) {
            clearCacheSub(key);
            Log.w(getClass().getName(), String.format("getBundle failed on %s ex: %s", fileName, ex));
            return null;
        }
    }

}