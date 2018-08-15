package com.pjuu.otterdroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {

    protected static final int REQUEST_CODE_FILE_PICKER = 1;

    private WebView webView;
    private ProgressBar prgrsBar;

    /** File upload callback for platform versions prior to Android 5.0 */
    protected ValueCallback<Uri> mFileUploadCallbackFirst;
    /** File upload callback for Android 5.0+ */
    protected ValueCallback<Uri[]> mFileUploadCallbackSecond;

    // Hostname extracted from R.string.app_url
    private String hostname;

    public class PjuuWebChromeClient extends WebChromeClient {

        // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, null);
        }

        // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, acceptType, null);
        }

        // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileInput(uploadMsg, null);
        }

        // file upload callback (Android 5.0 (API level 21) -- current) (public method)
        @SuppressWarnings("all")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            openFileInput(null, filePathCallback);
            return true;
        }

    }

    public class PjuuWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (hasConnection()) {
                // Pass the hostname from the requested URL
                String request_hostname = Uri.parse(url).getHost();
                if (request_hostname.equals(hostname)) {
                    // This is a Pjuu URL so open it in the WebView
                    view.loadUrl(url);
                } else {
                    // This is a non Pjuu URL. Open it in the default browser
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
            } else {
                // Doesn't look like you are connected to the internet
                Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            prgrsBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            prgrsBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // Will display a standard error message if the application can't connect
            view.loadUrl("file:///android_asset/error.html");
        }
    }

    public boolean hasConnection() {
        // Get a ConnectivityManager
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressLint("NewApi")
    protected void openFileInput(final ValueCallback<Uri> fileUploadCallbackFirst,
                                 final ValueCallback<Uri[]> fileUploadCallbackSecond) {
        if (mFileUploadCallbackFirst != null) {
            mFileUploadCallbackFirst.onReceiveValue(null);
        }
        mFileUploadCallbackFirst = fileUploadCallbackFirst;
        if (mFileUploadCallbackSecond != null) {
            mFileUploadCallbackSecond.onReceiveValue(null);
        }
        mFileUploadCallbackSecond = fileUploadCallbackSecond;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Choose a file"),
                    REQUEST_CODE_FILE_PICKER);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == REQUEST_CODE_FILE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    if (mFileUploadCallbackFirst != null) {
                        mFileUploadCallbackFirst.onReceiveValue(intent.getData());
                        mFileUploadCallbackFirst = null;
                    }
                    else if (mFileUploadCallbackSecond != null) {
                        Uri[] dataUris;
                        try {
                            dataUris = new Uri[] { Uri.parse(intent.getDataString()) };
                        }
                        catch (Exception e) {
                            dataUris = null;
                        }
                        mFileUploadCallbackSecond.onReceiveValue(dataUris);
                        mFileUploadCallbackSecond = null;
                    }
                }
            }
            else {
                if (mFileUploadCallbackFirst != null) {
                    mFileUploadCallbackFirst.onReceiveValue(null);
                    mFileUploadCallbackFirst = null;
                }
                else if (mFileUploadCallbackSecond != null) {
                    mFileUploadCallbackSecond.onReceiveValue(null);
                    mFileUploadCallbackSecond = null;
                }
            }
        }
    }

    @Override
    public void onBackPressed (){
        if (webView.isFocused() && webView.canGoBack()) {
            webView.goBack();
        }
        else {
            super.onBackPressed();
            finish();
        }
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Hide the image upload button is we are in KitKat (doesn't work) */
        @JavascriptInterface
        public boolean hideImageUpload() {
            return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Get the hostname from the URL defined in the resources
        // This will let us check all requests to redirect to the browser or not.
        hostname = Uri.parse(getString(R.string.app_url)).getHost();

        webView = (WebView)findViewById(R.id.webview);
        WebSettings browserSettings = webView.getSettings();
        // We need Javascript on the site
        browserSettings.setJavaScriptEnabled(true);
        // Inject out Java to Javascript stuff as "Android"
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new PjuuWebViewClient());
        webView.setWebChromeClient(new PjuuWebChromeClient());

        // Remove the cache on application startup
        webView.clearCache(true);

        // The URL is stored in values/strings.xml
        if (savedInstanceState == null) {
            webView.loadUrl(getString(R.string.app_url));
        }

        // Progress bar
        prgrsBar = (ProgressBar)findViewById(R.id.progress_bar);
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

}
