package com.phg.constellations;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DetailsActivity extends Activity {
    private static String TAG = "Constellations-app-Detail";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        String constellation_name = getIntent().getExtras().getString("Constellation","");
        Log.d(TAG, "got name: " + constellation_name);
        setTitle(constellation_name);
        constellation_name = TextUtils.join("_", constellation_name.toLowerCase().split(" "));

        StrictMode.allowThreadDiskReads();
        TextView textView = (TextView) findViewById(R.id.details);
        StringBuilder textBuilder = new StringBuilder();
        InputStream is = getResources().openRawResource(getResources().getIdentifier(constellation_name, "raw", getPackageName()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                textBuilder.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        textView.setText(textBuilder.toString());
        StrictMode.enableDefaults();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
