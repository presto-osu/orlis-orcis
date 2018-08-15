package com.eibriel.reddot;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.graphics.Color;


public class RedDot extends ActionBarActivity {
    DrawDot drawDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_dot);

        drawDot = new DrawDot(this);
        drawDot.setBackgroundColor(Color.BLACK);
        setContentView(drawDot);

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {
                // do stuff then
                // can call h again after work!
                drawDot.invalidate();

                time += 1000;
                //Log.d("TimerExample", "Going for... " + time);
                h.postDelayed(this, 15);
            }
        }, 15); // 1 second delay (takes millis)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_red_dot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
