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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import org.pixmob.freemobile.netstat.content.NetstatContract.Events;

import java.util.Calendar;

import static org.pixmob.freemobile.netstat.Constants.TAG;

/**
 * This broadcast receiver removes old data from the database.
 * @author Pixmob
 */
public class DatabaseCleanup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // The database cleanup is done in a background thread so that the main
        // thread is not blocked.
        new DatabaseCleanupTask(context.getApplicationContext()).start();
    }
    
    /**
     * Internal thread for executing database cleanup.
     * @author Pixmob
     */
    private static class DatabaseCleanupTask extends Thread {
        private final Context context;
        
        public DatabaseCleanupTask(final Context context) {
            this.context = context;
        }
        
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            
            try {
                cleanupDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Failed to cleanup database", e);
            }
        }
        
        private void cleanupDatabase() throws Exception {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            
            Log.i(TAG, "Deleting events older than " + cal.getTime());
            
            // Delete oldest events.
            final long timestampLimit = cal.getTimeInMillis();
            final int deletedEvents = context.getContentResolver().delete(
                Events.CONTENT_URI, Events.TIMESTAMP + "<?",
                new String[] { String.valueOf(timestampLimit) });
            
            Log.i(TAG, deletedEvents + " events deleted");
        }
    }
}
