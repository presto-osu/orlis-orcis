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
package org.pixmob.freemobile.netstat;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.Build;

import static org.pixmob.freemobile.netstat.Constants.SP_NAME;

/**
 * Copy application preferences to a remote "cloud" storage, using the Android
 * backup provider.
 * @author Pixmob
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class BackupAgent extends BackupAgentHelper {
    @Override
    public void onCreate() {
        super.onCreate();

        final SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(this, SP_NAME);
        addHelper("prefs", prefsHelper);
    }
}
