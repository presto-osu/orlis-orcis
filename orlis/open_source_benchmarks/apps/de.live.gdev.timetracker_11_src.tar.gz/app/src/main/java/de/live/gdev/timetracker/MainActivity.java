package de.live.gdev.timetracker;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    public static final boolean LOAD_IN_DESKTOP_MODE = true;

    @Bind(R.id.web_view)
    WebView webView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        profile = Profile.getDefaultProfile(this);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (profile.isAcceptAllSsl()) {
                    handler.proceed();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.ssl_toast_error, Snackbar.LENGTH_SHORT).show();
                    webView.loadData(getString(R.string.ssl_webview_error_str), "text/html", "UTF-16");
                }
            }
        });


        // Apply web settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);

        if (LOAD_IN_DESKTOP_MODE) {
            settings.setBuiltInZoomControls(true);
            settings.setSupportZoom(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SettingsActivity.ACTIVITY_ID);
                return true;
            case R.id.action_login:
                loadWebapp(true);
                return true;
            case R.id.action_info:
                startActivity(new Intent(this,InfoActivity.class));
                return true;
            case R.id.action_exit:
                webView.clearCache(true);
                webView.clearFormData();
                webView.clearHistory();
                webView.clearMatches();
                webView.clearSslPreferences();
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsActivity.ACTIVITY_ID &&
                resultCode == SettingsActivity.RESULT.CHANGED) {
            profile = Profile.getDefaultProfile(this);
        }
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent e) {
        if ((key == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(key, e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        profile.reloadSettings();
        loadWebapp(profile.isAutoLogin());
    }

    public void loadWebapp(boolean doLogin) {
        Uri url;
        try {
            url = Uri.parse(profile.getFullPath());
        } catch (Exception e) {
            webView.loadData(getString(R.string.no_valid_path), "text/html", "UTF-16");
            return;
        }

        String url_s = url.toString();
        if (TextUtils.isEmpty(url_s) || url_s.equals("index.php")) {
            webView.loadData(getString(R.string.no_valid_path), "text/html", "UTF-16");
        } else {
            webView.loadUrl(url_s);
            if (doLogin) {
                url_s += "?a=checklogin";
                String postData = "name=" + profile.getUsername() + "&password=" + profile.getPassword();
                this.webView.postUrl(url_s, EncodingUtils.getBytes(postData, "base64"));
            }
        }
    }
}
