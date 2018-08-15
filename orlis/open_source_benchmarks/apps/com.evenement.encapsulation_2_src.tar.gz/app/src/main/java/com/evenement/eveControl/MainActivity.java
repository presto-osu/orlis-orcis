package com.evenement.eveControl;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.view.GravityCompat.START;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LoginTask loginTask;
    private String username;
    private String password;
    private String server;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSharedPreferences();

        setupCookieManager();

        //assign main content to frame
        setContentView(R.layout.activity_main);

        setupWebview();
        setupNavDrawer();

        loginTask = new LoginTask(webView, MainActivity.this, server, username, password, drawer);

        checkNetwork();
    }

    /**
     * Setup of the slide drawer (loginform)
     */
    private void setupNavDrawer() {

        //actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //DrawerLayout (used to control drawer animations)
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //DrawerToggle (hamburger button to toggle drawer open/close)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //DrawerFragment (drawer content)
        NavFragment navFragment = new NavFragment();
        navFragment.setDrawerLayout(drawer);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame_new, navFragment,
                        "NavFragment").commit();

        getSupportActionBar().hide();
    }


    /**
     * webview config
     */
    private void setupWebview() {

        webView = (WebView) findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString("evenement-android");

        webView.setWebViewClient(new WebViewClient());

    }


    /**
     * ccokie manager setup to sync cookies between HttpsUrlConnection and Webview
     */
    private void setupCookieManager() {

        CookieSyncManager.createInstance(MainActivity.this);

        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

        WebCookieManager coreCookieManager = new WebCookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);

        java.net.CookieHandler.setDefault(coreCookieManager);
    }

    /**
     * Check if network connection is available
     *
     * @return boolean isConnected
     */
    private boolean hasInternet() {

        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


    /**
     * Check network connection and saved credentials before executing login AsyncTask
     */
    private void checkNetwork() {

        if (!hasInternet()) {

            showDialog("Connexion réseau indisponible", "Réessayer");
        } else if (!checkPreferences()) {

            showDialog("Informations de connexion non configurées", "Configurer");

        } else {

            keepSessionAlive();

            loginTask.execute();
        }
    }

    /**
     * Execute a Login AsyncTask every 5mn to keep session alive in webview
     */
    private void keepSessionAlive() {

        long delay = 1000 * 60 * 5;

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                new KeepSessionTask(server, username, password).execute();
            }
        }, 0, delay);
    }

    private void setupSharedPreferences() {

        SharedPreferences preferences = MainActivity.this.getPreferences(Context.MODE_PRIVATE);

        username = preferences.getString("username", "");
        password = preferences.getString("password", "");
        server = preferences.getString("server", "");
    }

    /**
     * @return boolean credentials not null
     */
    private boolean checkPreferences() {

        if ("".equals(username) || username == null) {
            return false;
        }

        if ("".equals(password) || password == null) {
            return false;
        }

        if ("".equals(server) || server == null) {
            return false;
        }
        return true;
    }

    // Method to show network or credentials alert dialog accordingly
    private void showDialog(String message, String action) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        AlertDialog alertDialog = builder.create();
        final String actionParam = action;

        alertDialog.setCancelable(false);
        alertDialog.setTitle("Connexion impossible");
        alertDialog.setMessage(message);

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (actionParam.equals("Configurer")) {

                    drawer.openDrawer(START);
                } else {
                    checkNetwork();
                }
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Quitter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                System.exit(0);
            }
        });
        alertDialog.show();
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

        return super.onOptionsItemSelected(item);
    }
}
