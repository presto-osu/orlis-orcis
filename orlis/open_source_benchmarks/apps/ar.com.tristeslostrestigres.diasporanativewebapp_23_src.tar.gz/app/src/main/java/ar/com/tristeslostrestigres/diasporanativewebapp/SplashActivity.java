/*
    This file is part of the Diaspora Native WebApp.

    Diaspora Native WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora Native WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package ar.com.tristeslostrestigres.diasporanativewebapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends AppCompatActivity {

    ImageView imgSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgSplash = (ImageView) findViewById(R.id.imgSplash);

        TypedArray images = getResources().obtainTypedArray(R.array.splash_images);
        int choice = (int) (Math.random() * images.length());
        imgSplash.setImageResource(images.getResourceId(choice, R.drawable.splash1));
        images.recycle();

        final SharedPreferences config = getSharedPreferences("PodSettings", MODE_PRIVATE);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent i;
                if (config.getString("podDomain", null) != null) {
                    i = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    i = new Intent(SplashActivity.this, PodsActivity.class);
                }
                startActivity(i);
                finish();
            }
        }, 1500);

    }

}
