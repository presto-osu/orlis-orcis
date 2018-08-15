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

package com.github.redpanal.android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.redpanal.android.helpers.Constants;
import com.github.redpanal.android.helpers.Helpers;
import com.github.redpanal.android.receivers.NetworkChangeReceiver;


public class MainActivity extends CustomActionBarActivity {


    private static final String TAG = "RedPanal Main";
    private Toolbar app_bar;
    private WebView webView;
    private ProgressDialog progressDialog;
    private BroadcastReceiver networkStateReceiver;
    private boolean networkStateReceiverIsRegistered;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);

        regNetworkStateChangeReceiver();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getString(R.string.espere));
        progressDialog.setMessage(getString(R.string.cargando));
        progressDialog.setMax(75);

        if (Helpers.isOnline(MainActivity.this)) {
            if (Helpers.isUsingMobile(MainActivity.this)) {
                Toast.makeText(MainActivity.this, "Atención: conectado con datos móviles", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Atención: Sin conexión a Internet", Toast.LENGTH_SHORT).show();
        }

        webView = (WebView)findViewById(R.id.webView);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
        WebSettings wSettings = webView.getSettings();
        wSettings.setJavaScriptEnabled(true);
        wSettings.setBuiltInZoomControls(true);
        wSettings.setUseWideViewPort(true);
        wSettings.setLoadWithOverviewMode(true);

        if (android.os.Build.VERSION.SDK_INT >= 21)
            wSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        WebViewClient wc = new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, url);
                if (!url.contains("redpanal.org")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else {
                    if (!progressDialog.isShowing()) progressDialog.show();
                    return false;
                }
            }

            public void onPageFinished(WebView view, String url) {
//                Log.i(TAG, "Terminó de cargar: " + url);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                Log.e(TAG, "Error: " + description);
                if (progressDialog.isShowing()) progressDialog.dismiss();

                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(description)
                        .setPositiveButton("Cerrar", null)
                        .show();
            }
        };


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.setWebViewClient(wc);
        if (savedInstanceState == null) {
            if (Helpers.isOnline(MainActivity.this)) {
                progressDialog.show();
                webView.loadUrl(Constants.URL_PANAL);
            } else {
                Toast.makeText(
                        MainActivity.this,
                        getString(R.string.sin_internet),
                        Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public void onBackPressed() {
        if (!progressDialog.isShowing()) progressDialog.show();
        webView.goBack();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkStateReceiverIsRegistered)
            unregisterReceiver(networkStateReceiver);
    }

    private void regNetworkStateChangeReceiver() {
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras.getString(NetworkChangeReceiver.CONNECTION_STATE_CHANGE).equals("Wifi enabled") ||
                        extras.getString(NetworkChangeReceiver.CONNECTION_STATE_CHANGE).equals("Mobile data enabled")) {
                    // Conexión establecida
                } else {
                    // Conexión perdida
                }
            }
        };
        registerReceiver(networkStateReceiver, new IntentFilter(NetworkChangeReceiver.CONNECTION_STATE_CHANGE));
        networkStateReceiverIsRegistered = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.recargar) {
            if (Helpers.isOnline(MainActivity.this)) {

                if(Helpers.isUsingMobile(MainActivity.this))
                    Helpers.warningMobile(MainActivity.this);

                if (!progressDialog.isShowing()) progressDialog.show();
                webView.reload();
                return true;
            } else {
                Toast.makeText(
                        MainActivity.this,
                        getString(R.string.sin_internet),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (id == R.id.salir) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(getString(R.string.confirmar_salida))
                    .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}
