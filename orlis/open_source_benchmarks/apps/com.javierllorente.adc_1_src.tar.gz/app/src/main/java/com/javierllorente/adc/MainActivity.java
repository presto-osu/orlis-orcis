/*
 * Copyright (C) 2012-2016 Javier Llorente <javier@opensuse.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.javierllorente.adc;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    WebView mWebView = null;
    WebFragment webFragment = null;
    SearchView searchView = null;
    private static final String PAG_AYUDA = "file:///android_asset/ayuda.html";
    ProgressBar progressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        App.setContext(this);

        webFragment = (WebFragment) getFragmentManager().findFragmentById(R.id.webFragment);
        mWebView = webFragment.getWebView();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setProgress(0);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.search_hint));
        expandSearchView();
        searchView.setFocusable(true);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.requestFocusFromTouch();

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchView.getQuery().toString();
                webFragment.query(query);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                webFragment.query(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_help:
                mWebView.loadUrl(PAG_AYUDA);
                return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Prevent the system from restarting this activity
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        } else if (newConfig.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
        } else if (newConfig.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
        }

        expandSearchView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the BACK key and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        // Defaults to system behaviour if there's no history
        return super.onKeyDown(keyCode, event);
    }

    public void expandSearchView() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 10,
                getResources().getDisplayMetrics());
        searchView.setMaxWidth(screenWidth - (int) px);
    }

}
