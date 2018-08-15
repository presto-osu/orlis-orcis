/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;

public class ComplexPreferences {

    private static ComplexPreferences complexPreferences;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static Gson GSON = new Gson();
    private String namePreferences;

    private ComplexPreferences(Context context, String namePreferences, int mode) {
        this.context = context;
        if (namePreferences == null || namePreferences.equals("")) {
//            namePreferences = context.getPackageName() + getVersion(context) + "_complexPreferences";
            namePreferences = context.getPackageName() + "211_complexPreferences";
            this.namePreferences = namePreferences;
        }
        preferences = context.getSharedPreferences(namePreferences, mode);
        editor = preferences.edit();
    }

    public static ComplexPreferences getComplexPreferences(Context context,
                                                           String namePreferences, int mode) {

        if (complexPreferences == null) {
            complexPreferences = new ComplexPreferences(context,
                    namePreferences, mode);
        }

        return complexPreferences;
    }

    public int getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }


    public void putObject(String key, Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }

        if (key.equals("") || key == null) {
            throw new IllegalArgumentException("key is empty or null");
        }

        editor.putString(key, GSON.toJson(object));
    }

    public void commit() {

        editor.commit();
//        for debug, save logs & settings to external
//        saveSharedPreferences();
    }

    public <T> T getObject(String key, Class<T> a) {

        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try {
                return GSON.fromJson(gson, a);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storaged with key " + key + " is instanceof other class");
            }
        }
    }
//    for debug, save logs & settings to external
//    private void saveSharedPreferences()
//    {
//        File myPath = new File(Environment.getExternalStorageDirectory().toString());
//        File myFile = new File(myPath, this.namePreferences);
//
//        try
//        {
//            FileWriter fw = new FileWriter(myFile);
//            PrintWriter pw = new PrintWriter(fw);
//
//            Map<String,?> prefsMap = preferences.getAll();
//
//            for(Map.Entry<String,?> entry : prefsMap.entrySet())
//            {
//                pw.println(entry.getKey() + ": " + entry.getValue().toString());
//            }
//
//            pw.close();
//            fw.close();
//        }
//        catch (Exception e)
//        {
//            // what a terrible failure...
//            Log.wtf(getClass().getName(), e.toString());
//        }
//    }
}