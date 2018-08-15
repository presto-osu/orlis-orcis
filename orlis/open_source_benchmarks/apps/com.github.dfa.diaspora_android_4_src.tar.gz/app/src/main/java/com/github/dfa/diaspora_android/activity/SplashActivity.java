/*
    This file is part of the Diaspora for Android.

    Diaspora for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora for Android.

    If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.dfa.diaspora_android.activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.util.Helpers;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SplashActivity extends AppCompatActivity {
    private App app;

    @BindView(R.id.splash__splashimage)
    public ImageView imgSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash__activity);
        ButterKnife.bind(this);
        app = (App) getApplication();

        TypedArray images = getResources().obtainTypedArray(R.array.splash_images);
        int choice = (int) (Math.random() * images.length());
        imgSplash.setImageResource(images.getResourceId(choice, R.drawable.splashscreen1));
        images.recycle();

        int delay = getResources().getInteger(R.integer.splash_delay);
        new Handler().postDelayed(startActivityRunnable, delay);
    }

    final Runnable startActivityRunnable = new Runnable() {
        public void run() {
            boolean hasPodDomain = app.getSettings().hasPodDomain();
            Helpers.animateToActivity(SplashActivity.this,
                    hasPodDomain ? MainActivity.class : PodSelectionActivity.class,
                    true
            );
        }
    };
}
