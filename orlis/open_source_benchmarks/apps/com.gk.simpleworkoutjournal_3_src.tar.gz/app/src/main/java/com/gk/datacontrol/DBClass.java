package com.gk.datacontrol;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBClass  {
    private static final boolean DEBUG_FLAG = false;
	private int exercisesInDay = 0, exerciseDays = 0, setsInDay = 0, setDays = 0; // used for simulating date change situation\
	public static final  String APP_NAME = "SWJournal";
	
	public static final String DB_NAME = "SWJournal";
	private static final int    DB_VERSION = 1;
	public static final long MS_IN_A_DAY = 86400000;
    public static final int EX_IN_PAST = -2;
	
	private static final String TABLE_EXERCISES = "exercises";
	private static final String TABLE_SETS_LOG = "sets_log";
	public  static final String TABLE_EXERCISE_LOG 	= "exercise_log";

	public  static final String KEY_ID 			= "_id";
    public  static final String KEY_EX_LOG_ID   = "ex_log_id";
	public  static final String KEY_NAME		= "name";
	public  static final String KEY_NOTE 		= "note";
	public  static final String KEY_EX_NAME  	= "exercise_name";
	public  static final String KEY_TIME 		= "time";
	public  static final String KEY_REPS    	= "reps";
	public  static final String KEY_WEIGHT  	= "weight";
	
	private static final String CREATE_EXERCISES_TABLE = "CREATE TABLE "+ TABLE_EXERCISES +" ("+
														  KEY_NAME + " TEXT PRIMARY KEY," +
														  KEY_NOTE + " TEXT"+
														  ");";
	
	private static final String CREATE_EXERCISE_LOG_TABLE = "CREATE TABLE "+TABLE_EXERCISE_LOG+" ("+
			KEY_ID      	   + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_EX_NAME  	   + " TEXT,"  +
		    KEY_TIME           + " INTEGER,"+
            KEY_NOTE           + " TEXT," +
			"FOREIGN KEY ("+KEY_EX_NAME+") REFERENCES "+ TABLE_EXERCISES +"("+KEY_NAME+")"+
			");";

    private static final String CREATE_SETS_LOG_TABLE = "CREATE TABLE "+ TABLE_SETS_LOG +" ("+
            KEY_ID	   + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_TIME   + " INTEGER,"+
            KEY_REPS   + " INTEGER," +
            KEY_WEIGHT + " REAL," +
            KEY_NOTE   + " TEXT,"+
            KEY_EX_LOG_ID + " INTEGER,"+
            KEY_EX_NAME + " INTEGER,"+
            "FOREIGN KEY ("+ KEY_EX_LOG_ID +") REFERENCES "+ TABLE_EXERCISE_LOG +"("+KEY_ID+"),"+
            "FOREIGN KEY ("+ KEY_EX_NAME +") REFERENCES "+ TABLE_EXERCISES +"("+KEY_NAME+")"+
            ");";

    private SQLiteDatabase realdb;
	private DBHelper dbHelper;
	private ContentValues values;
	
	public DBClass(Context context) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: DBHelper(Context context)");
		dbHelper = new DBHelper(context);
		values =  new ContentValues();
        open();
	}


	public void close() {
		if (dbHelper!=null) dbHelper.close();
	}

	public void open() {
		realdb = dbHelper.getWritableDatabase();
		realdb.execSQL("PRAGMA foreign_keys=ON;");
	}

	public String millisToDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		return sdf.format(time);
	}

    public long maximizeTimeOfDay( long millitime )
    {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTimeInMillis( millitime );
        cal.set( cal.get( GregorianCalendar.YEAR ), cal.get( GregorianCalendar.MONTH ),cal.get( GregorianCalendar.DATE ), 23, 59  );
        return cal.getTimeInMillis();
    }

    public boolean isSameDay(long tm1, long tm2) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: isSameDay time1: "+tm1+"time2: "+tm2);
        String date1 = millisToDate(tm1).split(" ")[0];
        String date2 = millisToDate(tm2).split(" ")[0];
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: isSameDay time1: "+date1+"time2: "+date2);

        return date1.equals( date2 );
    }

   /*
   * Will delete all logs related to exercise and exercise itself
   */
    public int deleteEx( Cursor exCursor ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: deleteEx started");

        String exToDelete = exCursor.getString( exCursor.getColumnIndex("exercise_name") );

        realdb.delete(TABLE_SETS_LOG,     KEY_EX_NAME + " = \"" + exToDelete + "\"", null);
        int affectedExLogs = realdb.delete(TABLE_EXERCISE_LOG, KEY_EX_NAME + " = \"" + exToDelete + "\"", null);
        realdb.delete(TABLE_EXERCISES,    KEY_NAME    + " = \"" + exToDelete + "\"", null);

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: deleteEx :: deleted ex log entries: "+ affectedExLogs );
        return affectedExLogs;

    }

    /*
     * @param[in] subject affects only on retval: in case of 0 will return affected ex amount, in case of 1 - sets amount
     */
    public int rmExLogEntry( long exLogId, int subject ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: rmExLogEntry started. ex id passed: "+ exLogId );

        int affectedSets = realdb.delete(TABLE_SETS_LOG,  KEY_EX_LOG_ID +" = "+exLogId, null);
        int affectedExs = realdb.delete(TABLE_EXERCISE_LOG, KEY_ID        +" = "+exLogId, null);

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: rmExLogEntry :: affected sets entries: "+ affectedSets+ " affected ex entries: "+affectedExs );
        return ( subject == 0 ) ? affectedExs : affectedSets;
    }

    public int rmSetLogEntry( Cursor setLogEntry ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: rmSetLogEntry started" );

        Long setLogId = setLogEntry.getLong( setLogEntry.getColumnIndex( KEY_ID ) );

        int affectedSets = realdb.delete(TABLE_SETS_LOG,  KEY_ID +" = "+setLogId, null);

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: rmExLogEntry :: affected sets entries: "+ affectedSets );
        return affectedSets;
    }

	public long insertExerciseNote( String exercise, String newNote )  {
		values.put( KEY_NOTE, newNote  );

		long res = realdb.update(TABLE_EXERCISES, values, KEY_NAME + "=\"" + exercise +"\"" , null);

		if (res != 1) {
			Log.e(APP_NAME, "DBClass :: insertNote for exercise :: failed. (name: "+exercise+")" );
		} else {
			if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: insertNote for exercise :: success for exercise "+exercise);
		}
		values.clear();
		return res;
	}

	public long insertSetNote( String setId, String newNote )  {
		values.put( KEY_NOTE, newNote );

		long res = realdb.update(TABLE_SETS_LOG, values, KEY_ID + "=" + setId , null);

		if (res != 1) {
			Log.e(APP_NAME, "DBClass :: OBSOLETE :: insertNote for set :: failed. (id: "+setId+")" );
		} else {
			Log.e(APP_NAME, "DBClass :: OBSOLETE :: insertNote for set :: success for set with id "+setId);
		}
		values.clear();
		return res;
	}

    public long getTimeForEx( String exLogId ) {
        Cursor entryCursor = realdb.rawQuery("SELECT "+KEY_TIME+" FROM "+ TABLE_EXERCISE_LOG +
                " WHERE "+KEY_ID+" = \""+exLogId+"\"", null );

        if ( entryCursor.getCount() != 1 ) {
            Log.e(APP_NAME, "DBClass :: getTimeForEx unexpected query result." );
            entryCursor.close();
            return -1;
        } else {
            entryCursor.moveToFirst();
            long val = entryCursor.getLong( entryCursor.getColumnIndex( KEY_TIME ) );
            entryCursor.close();
            return val;
        }

    }

    // if dates for set and exercise not match - set will be inserted only if ignoreDateDiff is set. otherwise EX_IN_PAST is returned.
	public long insertSet( String exName, String exLogId, String reps, String weight, boolean ignoreDateDiff ) {
		long time = System.currentTimeMillis();

        if ( DEBUG_FLAG ) {
            if (setsInDay % 4 == 0) {
                setDays++;
                setsInDay = 0;
            }
            time += (MS_IN_A_DAY * setDays); // number - ms in day
            setsInDay++;
        }

        //if set time is NOW and ex time is not -> we add set for day in the past
        long res;
        if ( !isSameDay( getTimeForEx( exLogId ), time ) )
        {
            res = (ignoreDateDiff) ? insertSet(exName, exLogId, null, reps, weight,  maximizeTimeOfDay( getTimeForEx( exLogId ) ) ) : EX_IN_PAST;
        }
        else
        {
            res =  insertSet(exName, exLogId, null, reps, weight, time);
        }

        return res;
	}

	public long insertSet( String exName, String exLogId, String setNote, String reps, String weight, long time) {
        if ( DEBUG_FLAG ) {
            Log.v(APP_NAME, "DBClass :: insertSet :: exName:  "+ exName);
            Log.v(APP_NAME, "DBClass :: insertSet :: exLogId: "+ exLogId);
            Log.v(APP_NAME, "DBClass :: insertSet :: setNote: "+ setNote);
            Log.v(APP_NAME, "DBClass :: insertSet :: reps: "+ reps);
            Log.v(APP_NAME, "DBClass :: insertSet :: weight: "+ weight);
            Log.v(APP_NAME, "DBClass :: insertSet :: time: "+ millisToDate(time) );
        }

        values.put(KEY_EX_NAME, exName);
    	values.put(KEY_EX_LOG_ID, exLogId);
    	values.put(KEY_NOTE, setNote);
    	values.put(KEY_REPS, reps );
    	values.put(KEY_WEIGHT,  weight);
    	values.put(KEY_TIME, time);


		long res = realdb.insert(TABLE_SETS_LOG, null, values);

		values.clear();
		if (res == -1) {
			Log.e(APP_NAME, "DBClass :: insertSet :: failed. (exName: "+exName+
                                                                "exLogId: "+exLogId+
																"; time: "+ millisToDate(time)+
																"; reps: "+reps+
																"; weight: "+weight+
																")" );
		} else {
			if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: insertSet :: success");
		}
		return res;
	}

	 public Cursor fetchSetsForExercise( String exerciseName ) {
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: fetchSetsForExercise for "+exerciseName);

         Cursor setsCursor = realdb.rawQuery("SELECT * FROM "+ TABLE_SETS_LOG +
				 							 " WHERE "+KEY_EX_NAME+" = \""+exerciseName+"\" ORDER BY "+KEY_TIME, null );

		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: fetchSetsForExercise for \""+exerciseName+"\" complete.");
		 return setsCursor;
	 }

	 public Cursor fetchExerciseHistory() {
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: fetchExerciseHistory begin");

         Cursor mCursor = realdb.rawQuery( "SELECT " +KEY_ID +"," + KEY_EX_NAME + "," + KEY_TIME + "," + TABLE_EXERCISES+"."+KEY_NOTE + " FROM " + TABLE_EXERCISE_LOG +
                 " LEFT OUTER JOIN "+TABLE_EXERCISES+" ON "
                 +TABLE_EXERCISE_LOG+ "." +KEY_EX_NAME+ " = "+TABLE_EXERCISES+"."+KEY_NAME +
                 "  ORDER BY " + KEY_TIME + " ASC", null);

		 if (mCursor != null) {
			 mCursor.moveToFirst();
		 }
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: fetchExerciseHistory complete");

		 return mCursor;
	 }

    public Cursor fetchExerciseNames( CharSequence constr ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: fetchExerciseNames begin");

        Cursor mCursor = realdb.rawQuery( "SELECT " + KEY_NAME  +" as "+KEY_ID+" FROM " + TABLE_EXERCISES + " WHERE " + KEY_ID + " LIKE '%" + constr + "%' ORDER BY "+KEY_ID, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: fetchExerciseNames complete");

        return mCursor;
    }

	 public boolean addExercise(String exercise) {
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: addExercise for \""+exercise+"\"");
		 long result = -1;
		 values.put(KEY_NAME, exercise);

		 //check if there already exist an exercise like this
		 Cursor tmpcs = realdb.rawQuery("SELECT "+KEY_NAME+" FROM "+ TABLE_EXERCISES + " WHERE "+KEY_NAME+ " = \"" + exercise + "\"", null );

		 if (tmpcs.getCount() == 0)
			 result = realdb.insert(TABLE_EXERCISES, null, values);

         tmpcs.close();
		 values.clear();
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: addExercise done");
		 return (result != -1);
	 }

	 public boolean logExercise(String exercise) {

		 long time = System.currentTimeMillis();

         if ( DEBUG_FLAG ) {
             if (exercisesInDay % 4 == 0) {
                 exerciseDays++;
                 exercisesInDay = 0;
             }
             time += (MS_IN_A_DAY * exerciseDays); // number - ms in day
             exercisesInDay++;
         }

		 return logExercise(exercise, time);
	 }

	 public boolean logExercise(String exercise, long time ) {
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: logExercise begin for \""+exercise+"\", time "+ millisToDate(time) );

		 //we use full timestamp
		 values.put(KEY_EX_NAME, exercise);
		 values.put(KEY_TIME, time);
		 //DEV-ONLY

		 //values.put(KEY_TIME, getUnixDay() );

		 long result = realdb.insert(TABLE_EXERCISE_LOG, null, values);

		 values.clear();
		 if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: logExercise done");
		 return (result != -1);

	 }

	 public boolean haveSetsWithExId( long exId ) {
         if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: haveSetsWithExId . id: "+ exId);
         Cursor setsWithExId = realdb.rawQuery( "SELECT "+KEY_ID+" FROM "+TABLE_SETS_LOG+" WHERE "+KEY_EX_LOG_ID+" = "+exId, null);

         boolean res = setsWithExId.getCount() > 0;
         setsWithExId.close();
         return res;
     }

    public boolean updateExercise( String origName, String newName) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: updateExercise . original name: "+ origName + " new name: "+newName);

        values.put( KEY_NAME, newName );
        if ( !addExercise( newName ) ) {
            if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: updateExercise . cannot rename since exercise \""+newName+"\" already exist");
            return false;
        }

        values.clear();
        values.put( KEY_EX_NAME, newName );
        int changedSets = realdb.update( TABLE_SETS_LOG,     values, KEY_EX_NAME+"=\""+origName+"\"", null );
        int changedExs  = realdb.update( TABLE_EXERCISE_LOG, values, KEY_EX_NAME+"=\""+origName+"\"", null );
        values.clear();

        //copy note
        Cursor noteCursor = realdb.rawQuery("SELECT "+KEY_NOTE+" FROM "+TABLE_EXERCISES+" WHERE "+KEY_NAME+"=\""+origName+"\"", null);
        if ( noteCursor.getCount() != 0 ) {
            noteCursor.moveToFirst();
            insertExerciseNote( newName, noteCursor.getString( noteCursor.getColumnIndex(KEY_NOTE)) );
        }

        noteCursor.close();
        //delete original exercise
        realdb.delete(TABLE_EXERCISES,KEY_NAME+"=\""+origName+"\"", null);

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: updateExercise : done. Affected exercise entries: "+ changedExs + "  Affected set entries: "+changedSets);
        return true;
    }

    public boolean updateSetLog( String id, String reps, String weight ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: updateSetLog . id: "+ id + " reps: "+reps+" weight: "+weight);

        values.put(KEY_REPS, reps);
        values.put(KEY_WEIGHT, weight);

        int res = realdb.update( TABLE_SETS_LOG, values, KEY_ID+"="+id,null  );

        values.clear();

        if ( res == 0 ) {
            Log.e(APP_NAME, "DBClass :: updateSetLog . No rows affected");
            return false;
        } else {
            return true;
        }

    }

    public void cleanAllTables() {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: dropAllTables : called");
        realdb.execSQL("DROP TABLE "+TABLE_SETS_LOG     );
        realdb.execSQL("DROP TABLE "+TABLE_EXERCISE_LOG );
        realdb.execSQL("DROP TABLE "+TABLE_EXERCISES    );

        realdb.execSQL(CREATE_EXERCISES_TABLE);
        realdb.execSQL(CREATE_EXERCISE_LOG_TABLE);
        realdb.execSQL(CREATE_SETS_LOG_TABLE);
    }

	 private class DBHelper extends SQLiteOpenHelper {
	
		    public DBHelper(Context context) {
		      super(context, DB_NAME, null, DB_VERSION);
		      //DEV-ONLY
		      //context.deleteDatabase(DB_NAME);
		    }
	
		    @Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL(CREATE_EXERCISES_TABLE);
				db.execSQL(CREATE_SETS_LOG_TABLE);
				if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: onCreate :: sets table created");
				db.execSQL(CREATE_EXERCISE_LOG_TABLE);
				if ( DEBUG_FLAG ) Log.v(APP_NAME, "DBClass :: onCreate :: exersises log table created");
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				// TODO Auto-generated method stub
				
			}
	  }

}
