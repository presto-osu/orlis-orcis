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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends Activity {
    public final static String _ClassName = MainActivity.class.getSimpleName();
    private final int DELAY_MEANING = 1000;   //Delay in showing meaning (millisec)
    private final String welcomeScreenShownPref = "welcomeScreenShown";
    private final String textColorPref = "textColor";
    //Class Variables
    private TextView textview_word;
    private TextView textview_meaning;
    private AsyncTask meaningDelayedTask;
    /* Engines */
    private WordEngine wordEngine;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        wordEngine = new WordEngine(this);
//      databaseEngine = new DatabaseEngine(this);
//      databaseEngine.populateDatabase(wordEngine.getMapFromXml());

        textview_word = (TextView) findViewById(R.id.textview_word);
        textview_meaning = (TextView) findViewById(R.id.textview_meaning);
        color_change(mPrefs.getString(textColorPref, "green"));

        // second argument is the default to use if the preference can't be found
        Boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);

        if (!welcomeScreenShown) {
            showWelcomeScreen();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.commit(); // Very important to save the preference
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_color_blue) {
            color_change("blue");
        } else if (id == R.id.action_color_green) {
            color_change("green");
        } else if (id == R.id.action_color_orange) {
            color_change("orange");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * My Functions
     */

    public void search_word(View v) {
        String search_query = textview_word.getText().toString();
        search_query = search_query.replace(' ','+');
        Uri uri = Uri.parse("http://www.google.com/#q=define:"+search_query);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    //TODO: Make sure no need of throw now.
    public void nextRandom(View v) throws XmlPullParserException, IOException {
        if (meaningDelayedTask != null &&
                meaningDelayedTask.getStatus() == AsyncTask.Status.RUNNING)
            meaningDelayedTask.cancel(true);
        WordPair wordPair = nextRandom();
        textview_word.setText(wordPair.getWord());
        textview_meaning.setText("");   //Empty, if user waits then show else move on
        String meaning; //should be passed to task probably
        meaning = wordPair.getMeaning();

        //Run on thread with a delay of DELAY_MEANING
        meaningDelayedTask = new LongRunningTask().execute(meaning);
    } //end nextRandom

    public WordPair nextRandom() {
        return wordEngine.getRandomWord();
    }

    public void color_change(String color) {
        TextView tw_word = (TextView)findViewById(R.id.textview_word);
        TextView tw_meaning = (TextView)findViewById(R.id.textview_meaning);
        int color_word = tw_word.getCurrentTextColor();
        int color_meaning = tw_meaning.getCurrentTextColor();
        if (color.equals("blue")) {
            color_word = getResources().getColor(R.color.holo_blue_light);
            color_meaning = getResources().getColor(R.color.holo_blue_light);
        } else if (color.equals("green")) {
            color_word = getResources().getColor(R.color.holo_green_light);
            color_meaning = getResources().getColor(R.color.holo_green_light);
        } else if (color.equals("orange")) {
            color_word = getResources().getColor(R.color.holo_orange_light);
            color_meaning = getResources().getColor(R.color.holo_orange_light);
        } else if (color.equals("red")) {
            color_word = getResources().getColor(R.color.holo_red_light);
            color_meaning = getResources().getColor(R.color.holo_red_light);
        } else if (color.equals("purple") || color.equals("voilet")) {
            color_word = color_meaning = getResources().getColor(R.color.holo_purple);
        }
        tw_word.setTextColor(color_word);
        tw_meaning.setTextColor(color_meaning);
        //Store in preferences
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(textColorPref, color);
        editor.commit(); // Very important to save the preference
    }

    public void color_red(View v) { color_change("red"); }
    public void color_blue(View v) { color_change("blue"); }
    public void color_green(View v) { color_change("green"); }
    public void color_orange(View v) { color_change("orange"); }
    public void color_purple(View v) { color_change("purple"); }

    private void showWelcomeScreen() {
        // here you can launch another activity if you like
        // the code below will display a popup
        String welcomeTitle = getResources().getString(R.string.welcomeTitle);
        String welcomeText = getResources().getString(R.string.welcomeText);
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_menu_help).setTitle(welcomeTitle).setMessage(welcomeText).setPositiveButton(
                R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    private class LongRunningTask extends AsyncTask<String, Void, Boolean> {
        String meaning;

        @Override
        protected Boolean doInBackground(String... incomingStrings) {
            meaning = incomingStrings[0]; //FIXME: Probably not best
            try {
                Thread.sleep(DELAY_MEANING);
            } catch (InterruptedException e) {
                /* No need of stacktrace here. The exception is intentional. */
                //Log.i("Sleep","Skipping this meaning: "+meaning);
                //e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            textview_meaning.setText(meaning);
        }
    }
}
// vim: set ts=4 sw=4 tw=0 et :
