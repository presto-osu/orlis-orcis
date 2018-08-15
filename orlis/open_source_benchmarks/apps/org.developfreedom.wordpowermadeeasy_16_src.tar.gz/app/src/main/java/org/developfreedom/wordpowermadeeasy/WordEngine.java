/**
 *   Word Power Made Easy - A vocabulary building application
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *
 *   This file is part of WordPowerMadeEasy.
 *
 *   WordPowerMadeEasy is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   WordPowerMadeEasy is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with WordPowerMadeEasy.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.developfreedom.wordpowermadeeasy;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordEngine {

    private static int count = 0;
    private final Context context;
    private final SQLiteOpenHelper myDatabaseHelper;
    private SQLiteDatabase database;
    private Cursor cursor;
    private Map<String, String> word_map;

    WordEngine(Context ctx) {
        this.context = ctx;
        this.myDatabaseHelper = new DatabaseOpenHelper(this.context);
        try {
            //XXX do this once ever
            this.word_map = readXml();
            populateDatabase(this.word_map);
            WordEngine.count += 1;
            System.out.println("Count ");
            System.out.println(WordEngine.count);
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void populateDatabase(Map<String, String> word_map) {
        try {
            int DEFAULT_SCORE = 50;
            String word, meaning;
            List<String> keys = new ArrayList<String>(word_map.keySet());

            for (String key : keys) {
                word = key;
                meaning = word_map.get(word);

                database = myDatabaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
//              values.put(DatabaseOpenHelper.KEY_INDEX, i);
                values.put(DatabaseOpenHelper.KEY_WORD, word);
                values.put(DatabaseOpenHelper.KEY_MEANING, meaning);
                values.put(DatabaseOpenHelper.KEY_SCORE, DEFAULT_SCORE);

                database.insert(DatabaseOpenHelper.TABLE_WORDLIST, null, values);
                database.close();   //FIXME Is it right to open and close everytime
            }//end for loop
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end populateDatabase


    void check_database() {
        try {
            //Checking db
            String[] columns = new String[]{
//                      DatabaseOpenHelper.KEY_INDEX,
                    DatabaseOpenHelper.KEY_WORD,
                    DatabaseOpenHelper.KEY_MEANING,
                    DatabaseOpenHelper.KEY_SCORE
            };
            cursor = database.query(DatabaseOpenHelper.TABLE_WORDLIST, columns,
                    null, null, null, null, null);
            Log.v("DE", "Cursor Object" + DatabaseUtils.dumpCursorToString(cursor));
            Log.d("DE", "database connected and values inserted with primary key");
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WordPair getRandomWord() {
        Random rand = new Random();
        List<String> keys = new ArrayList<String>(word_map.keySet());
        String randomKey = keys.get(rand.nextInt(keys.size()));
        String value = word_map.get(randomKey);
        //WordPair new_word = new WordPair(randomKey, value);
        return (new WordPair(randomKey, value));
    }

    public Map<String, String> readXml() throws XmlPullParserException, IOException {
        XmlResourceParser xrp = context.getResources().getXml(R.xml.word_list);

        xrp.next();
        int eventType = xrp.getEventType();
        String word = null, meaning = null;
        Map<String, String> word_pair_map = new HashMap<String, String>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG
                    && xrp.getName().equalsIgnoreCase("pair")) {
                //NOTE: In our xml file meaning comes before word and that matters
                eventType = xrp.next();
                if (eventType == XmlPullParser.START_TAG
                        && xrp.getName().equalsIgnoreCase("meaning")) {
                    eventType = xrp.next();
                    if (eventType == XmlPullParser.TEXT) {
                        //TODO: Handle situation when either word or meaning is missing
                        meaning = xrp.getText();
//                      Log.i("M","Meaning: "+meaning);
                    }
                }
                xrp.next();                 //End tag meaning
                eventType = xrp.next();     //Start tag word
                if (eventType == XmlPullParser.START_TAG
                        && xrp.getName().equalsIgnoreCase("word")) {
                    eventType = xrp.next();
                    if (eventType == XmlPullParser.TEXT) {
                        word = xrp.getText();
//                      Log.i("W","Word: "+word);
                    }
                }//end if elif
            }//end if START_TAG
            else if (eventType == XmlPullParser.END_TAG && xrp.getName().equalsIgnoreCase("pair")) {
                if (word != null && meaning != null && word.length() != 0 && meaning.length() != 0) {
                    if (word_pair_map.get(word) == null) {
//                      Log.d("map",word+meaning);
                        word_pair_map.put(word, meaning);
                    }
                }
            }
            eventType = xrp.next();
        }//end while
        return word_pair_map;
    }//end readXml

    Map<String, String> getMapFromXml() {
        try {
            return readXml();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    List<WordPair> getWeekPairs(int week_number) {
        int WEEK_COUNT = 50;
        //XXX week_number starts from 0
        week_number += 1;
        List<WordPair> output_list = new ArrayList<WordPair>();
        String word, meaning;
        Log.i("WeekPairs", String.valueOf(week_number));
        List<String> keys = new ArrayList<String>(word_map.keySet());
        for (int i = WEEK_COUNT * week_number; i < WEEK_COUNT * (week_number + 1); ++i) {
            word = keys.get(i);
            meaning = word_map.get(word);
            output_list.add(new WordPair(word, meaning));
        }
        //Check
        for (WordPair wp : output_list)
            Log.i("WeekPairs", wp.getWord() + wp.getMeaning());
        return output_list;
    }

}
// vim: set ts=4 sw=4 tw=0 et :
