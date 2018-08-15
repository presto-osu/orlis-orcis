/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
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
package org.pixmob.freemobile.netstat.feature;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;

/**
 * Honeycomb {@link StrictModeFeature} implementation.
 * @author Pixmob
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class HoneycombStrictModeFeature implements StrictModeFeature {
    @Override
    public void enable() {
        final StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder()
                .detectDiskWrites().detectNetwork().detectCustomSlowCalls()
                .penaltyLog().penaltyFlashScreen().build();
        final StrictMode.VmPolicy vmPolicy = new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks().detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects().penaltyLog().build();
        
        StrictMode.setThreadPolicy(threadPolicy);
        StrictMode.setVmPolicy(vmPolicy);
    }
}
