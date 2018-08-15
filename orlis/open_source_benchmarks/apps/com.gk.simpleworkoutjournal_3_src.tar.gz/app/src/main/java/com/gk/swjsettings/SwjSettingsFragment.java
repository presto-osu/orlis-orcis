package com.gk.swjsettings;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.gk.datacontrol.DBClass;
import com.gk.simpleworkoutjournal.R;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class SwjSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;
    final PreferenceFragment prefFrag = this;

    boolean createBackupConfirmed = false;
    boolean restoreBackupConfirmed = false;

    public boolean copyFile( String from, String to ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SwjSettingsFragment :: copyFile" );
        boolean success = true;
        //File source  = new File( from );
        FileInputStream inp = null;
        FileOutputStream out = null;
        int len;
        byte[] buf = new byte[ 128 ];

        try
        {
            inp = new FileInputStream( from );
            out = new FileOutputStream( to );

            while ((len = inp.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.flush();
            Toast.makeText(getActivity(), getResources().getString( R.string.db_copied_to ) + " " +  to, Toast.LENGTH_LONG).show();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            success = false;
        }
        finally
        {
            try
            {
                if ( out != null ) out.close();
                if ( inp != null ) inp.close();
            }
            catch(IOException ioe)
            {
                success = false;
            }
        }
        return success;
    }

    public boolean copyDB( boolean isRestore, String pathArg ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SwjSettingsFragment :: copyDB" );
        //find own DB
        String inAppDbDir = "/data/data/com.gk.simpleworkoutjournal/databases/";
        File[] files = new File( inAppDbDir ).listFiles();
        String inAppDbPath = "";


        for (File fl : files ) {
            if (fl.isFile() && fl.getName().equals( DBClass.DB_NAME ) ) {
                inAppDbPath = inAppDbDir + DBClass.DB_NAME;
                break;
            }
        }

        if ( inAppDbPath.isEmpty() ) {
            Log.e(APP_NAME,"SwjSettingsFragment :: copyDB :: failed to find own DB file location.");
            return false;
        }

        pathArg += "/swj.db";
        File passedFile = new File( pathArg );
        if( passedFile.isDirectory() )
        {
            Log.e(APP_NAME,"SwjSettingsFragment :: copyDB :: provided file path is a directory.");
            return false;
        }

        if ( !passedFile.exists() )
        {

            if ( isRestore )
            {
                Log.e(APP_NAME,"SwjSettingsFragment :: copyDB :: source file not found.");
                return false;
            }
            else
            {
               try
               {
                   passedFile.createNewFile();
               }
               catch ( IOException e )
               {
                   Log.e(APP_NAME,"SwjSettingsFragment :: copyDB :: failed to create file.");
                   return false;
               }
            }
        }

        String from;
        String to;

        if ( isRestore ) {
            from = pathArg;
            to = inAppDbPath;
        } else {
            from = inAppDbPath;
            to = pathArg;
        }

        return copyFile( from, to );
    }

    public boolean contactDev() {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SwjSettingsFragment :: contactDev" );
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("message/rfc822");
        emailIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"geokrock@gmail.com"}); // recipients
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SimpleWorkoutJournal");

        startActivity(createEmailOnlyChooserIntent(emailIntent, getResources().getString( R.string.send_message ) ));
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SwjSettingsFragment :: onPreferenceClick" );
        boolean res = false;
        if ( pref.getKey().equals("writemail") )
        {
            res = contactDev();

        }
        else if ( pref.getKey().equals("create_backup") )
        {
            if ( createBackupConfirmed )
            {
                createBackupConfirmed = false;
                res = copyDB(false, Environment.getExternalStorageDirectory().getAbsolutePath() );
                if ( !res )
                {
                    Toast.makeText(getActivity(), getResources().getText( R.string.backup_create_failed_toast ), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getActivity(), getResources().getText(R.string.press_again_to_confirm), Toast.LENGTH_SHORT).show();
                res = true;
                createBackupConfirmed = true;
                restoreBackupConfirmed = false;
            }

        }
        else if ( pref.getKey().equals("restore_backup") )
        {
            if ( restoreBackupConfirmed )
            {
                restoreBackupConfirmed = false;
                res = copyDB(true, Environment.getExternalStorageDirectory().getAbsolutePath());
                if (!res) {
                    Toast.makeText(getActivity(), getResources().getText(R.string.backup_restore_failed_toast), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getActivity(), getResources().getText(R.string.press_again_to_confirm), Toast.LENGTH_SHORT).show();
                res = true;
                restoreBackupConfirmed = true;
                createBackupConfirmed = false;
            }

        }

        return res;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.swjprefs);


        Preference mailPref = findPreference("writemail");
        Preference createBackupPref = findPreference("create_backup");
        Preference restoreBackupPref = findPreference("restore_backup");

        createBackupPref.setSummary( getString( R.string.in_path) + Environment.getExternalStorageDirectory().getAbsolutePath() );
        restoreBackupPref.setSummary( getString( R.string.from_path ) + Environment.getExternalStorageDirectory().getAbsolutePath() );

        mailPref.setOnPreferenceClickListener( this );
        createBackupPref.setOnPreferenceClickListener( this );
        restoreBackupPref.setOnPreferenceClickListener( this );

        createBackupConfirmed = false;
        restoreBackupConfirmed = false;

    }

    public Intent createEmailOnlyChooserIntent(Intent source,
                                               CharSequence chooserTitle) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SwjSettingsFragment :: createEmailOnlyChooserIntent" );

        Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                "info@domain.com", null));
        List<ResolveInfo> activities = prefFrag.getActivity().getPackageManager()
                .queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            target.setPackage(ri.activityInfo.packageName);
            intents.add(target);
        }

        if(!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new Parcelable[intents.size()]));

            return chooserIntent;
        } else {
            return Intent.createChooser(source, chooserTitle);
        }
    }


}
