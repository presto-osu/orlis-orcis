/*
 * Copyright (C) 2015 75py
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nagopy.android.disablemanager2;

import android.app.ActivityManager;
import android.support.annotation.IntDef;

public class Constants {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String MINE_TYPE_TEXT_PLAIN = "text/plain";

    private Constants() {
    }

    @IntDef({
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
            , ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            , ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE
            , ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
            , ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
            , ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY})
    public @interface RunningProcessImportance {
    }

}
