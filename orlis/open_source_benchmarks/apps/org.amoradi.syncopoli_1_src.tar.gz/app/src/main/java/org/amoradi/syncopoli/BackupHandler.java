package org.amoradi.syncopoli;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BackupHandler implements IBackupHandler {
    private List<BackupItem> mBackupItems;
    Context mContext;

    public BackupHandler(Context ctx) {
        mContext = ctx;
        updateBackupList();
    }

    public void addBackup(BackupItem item) {
        if (item.source.equals("") || item.name.equals("") || item.destination.equals("")) {
            return;
        }

        BackupSyncOpenHelper dbHelper = new BackupSyncOpenHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BackupSyncSchema.COLUMN_TYPE, "backup");
        values.put(BackupSyncSchema.COLUMN_NAME, item.name);
        values.put(BackupSyncSchema.COLUMN_SOURCE, item.source);
        values.put(BackupSyncSchema.COLUMN_DESTINATION, item.destination);
        values.put(BackupSyncSchema.COLUMN_LAST_UPDATE, "");

        db.insert(BackupSyncSchema.TABLE_NAME, null, values);
        db.close();
        dbHelper.close();

        updateBackupList();
    }

    public List<BackupItem> getBackups() {
        return mBackupItems;
    }

    public void updateBackupList() {
        List<BackupItem> bl = new ArrayList<>();

        BackupSyncOpenHelper dbHelper = new BackupSyncOpenHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        /*
        String[] proj = {BackupSyncSchema.COLUMN_NAME,
                BackupSyncSchema.COLUMN_SOURCE,
                BackupSyncSchema.COLUMN_DESTINATION,
                BackupSyncSchema.COLUMN_LAST_UPDATE};
        */

        Cursor c = db.query(
                BackupSyncSchema.TABLE_NAME,
                null, //proj
                "type = 'backup'",
                null,
                null,
                null,
                BackupSyncSchema.COLUMN_NAME + " DESC",
                null
        );

        if (c.getCount() <= 0) {
            c.close();
            db.close();
            dbHelper.close();
            mBackupItems = bl;
            return;
        }

        c.moveToFirst();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        do {
            BackupItem x = new BackupItem();
            x.name = c.getString(c.getColumnIndex(BackupSyncSchema.COLUMN_NAME));
            x.source = c.getString(c.getColumnIndex(BackupSyncSchema.COLUMN_SOURCE));
            x.destination = c.getString(c.getColumnIndex(BackupSyncSchema.COLUMN_DESTINATION));

            try {
                x.lastUpdate = df.parse(c.getString(c.getColumnIndex(BackupSyncSchema.COLUMN_LAST_UPDATE)));
            } catch (ParseException e) {
                x.lastUpdate = null;
            }

            x.logFileName = "log_" + x.name.replace(" ", "_");
            bl.add(x);
        } while(c.moveToNext());

        c.close();
        db.close();
        dbHelper.close();

        mBackupItems = bl;
    }

    public void updateBackupTimestamp(BackupItem b) {
        BackupSyncOpenHelper dbHelper = new BackupSyncOpenHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BackupSyncSchema.COLUMN_TYPE, "backup");
        values.put(BackupSyncSchema.COLUMN_NAME, b.name);
        values.put(BackupSyncSchema.COLUMN_SOURCE, b.source);
        values.put(BackupSyncSchema.COLUMN_DESTINATION, b.destination);

        b.lastUpdate = new Date();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        values.put(BackupSyncSchema.COLUMN_LAST_UPDATE, df.format(b.lastUpdate));

        db.update(BackupSyncSchema.TABLE_NAME, values, "name='" + b.name + "'", null);
        db.close();
        dbHelper.close();
    }

    public int runBackup(BackupItem b) {
        try {
            String rsyncPath = new File(mContext.getFilesDir(), "rsync").getAbsolutePath();
            String sshPath = new File(mContext.getFilesDir(), "ssh").getAbsolutePath();

            File f = new File(rsyncPath);
            FileOutputStream logFile = mContext.openFileOutput(b.logFileName, Context.MODE_PRIVATE);

            updateBackupTimestamp(b);
            logFile.write((b.lastUpdate.toString() + " \n\n").getBytes());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String rsync_username = prefs.getString(SettingsFragment.KEY_RSYNC_USERNAME, "");

            if (rsync_username.equals("")) {
                logFile.write("ERROR: Username not specified. Please set username in settings.".getBytes());
                return -1;
            }

            String rsync_options = prefs.getString(SettingsFragment.KEY_RSYNC_OPTIONS, "");
            String rsync_password = prefs.getString(SettingsFragment.KEY_RSYNC_PASSWORD, "");

            String server_address = prefs.getString(SettingsFragment.KEY_SERVER_ADDRESS, "");

            if (server_address.equals("")) {
                logFile.write("ERROR: Server address not specified. Please set Server address in settings.".getBytes());
                return -1;
            }

            String protocol = prefs.getString(SettingsFragment.KEY_PROTOCOL, "SSH");
            String private_key = prefs.getString(SettingsFragment.KEY_PRIVATE_KEY, "");
            String port = prefs.getString(SettingsFragment.KEY_PORT, "22");

            if (port.equals("")) {
                logFile.write("ERROR: Port not specified. Please set Port in settings.".getBytes());
                return -1;
            }

            /*
             * BUILD ARGUMENTS
             */

            List<String> args = new ArrayList<>();

            args.add(f.getAbsolutePath());

            if (!rsync_options.equals("")) {
                Collections.addAll(args, rsync_options.split(" "));
            }

            if (protocol.equals("SSH")) {
                if (private_key.equals("")) {
                    logFile.write("ERROR: Private key is not specified while SSH protocol is in use.".getBytes());
                    return -1;
                }

                args.add("-e");
                args.add(sshPath + " -y -p " + port + " -i " + private_key);
                args.add(b.source);
                args.add(rsync_username + "@" + server_address + ":" + b.destination);
            } else if (protocol.equals("Rsync")) {
                args.add(b.source);
                args.add(rsync_username + "@" + server_address + "::" + b.destination);
            }

            /*
             * BUILD PROCESS
             */

            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(mContext.getFilesDir());
            //pb.redirectErrorStream(true);

            if (protocol.equals("Rsync") && !rsync_password.equals("")) {
                Map<String, String> env = pb.environment();
                env.put("RSYNC_PASSWORD", rsync_password);
            }

            /*
             * RUN PROCESS
             */

            Process process = pb.start();

            /*
             * GET STDOUT/STDERR
             */

            int read;
            BufferedReader reader;
            char[] buffer = new char[4096];

            /* STDOUT */
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((read = reader.read(buffer)) > 0) {
                StringBuffer output = new StringBuffer();
                output.append(buffer, 0, read);
                logFile.write(output.toString().getBytes());
            }
            reader.close();

            /* STDERR */
            StringBuilder stderr = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while(reader.read(buffer) > 0) {
                stderr.append(buffer);
            }

            // Waits for the command to finish.
            process.waitFor();

            if (process.exitValue() != 0) {
                logFile.write("Error text:\n".getBytes());
                logFile.write(stderr.toString().getBytes());
            }

            return process.exitValue();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void syncBackups() {}
    public void showLog(BackupItem b) {}
}
