package com.thefonz.ed_tool;

/**
 * Created by thefonz on 18/03/15.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thefonz.ed_tool.utils.Constants;
import com.thefonz.ed_tool.utils.Utils;

public class Tab_Reddit extends Fragment
{
    LinearLayout progressLayout;
    ProgressBar progressBar;
    TextView TextViewProgress;
    WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View myFragmentView = inflater.inflate(R.layout.tab_reddit, container, false);

        webView = (WebView) myFragmentView.findViewById(R.id.webView);
        progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.progressLayout);
        progressBar = (ProgressBar) myFragmentView.findViewById(R.id.progressBar);
        TextViewProgress = (TextView ) myFragmentView.findViewById(R.id.textViewProgress);

        final Button button_back = (Button) myFragmentView
                .findViewById(R.id.button_back);
        final Button button_forward = (Button) myFragmentView
                .findViewById(R.id.button_forward);
        final Button button_refresh = (Button) myFragmentView
                .findViewById(R.id.button_refresh);

        // Configure related browser settings
        WebSettings wv2 = webView.getSettings();
        wv2.setLoadsImagesAutomatically(true);
        wv2.setLightTouchEnabled(false);
        wv2.setPluginState(WebSettings.PluginState.ON);
        wv2.setJavaScriptEnabled(true);
        wv2.setLoadWithOverviewMode(true);
        wv2.setUseWideViewPort(true);
        wv2.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wv2.setBuiltInZoomControls(true);
        wv2.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        webView.setWebViewClient(new MyBrowser());
        // Load the initial URL
        webView.loadUrl(Constants.REDDITURL);

        // Define back,forward and refresh webview control buttons
        button_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(webView.canGoBack()){
                    progressLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    TextViewProgress.setVisibility(View.VISIBLE);
                    webView.goBack();
              }
            }
        });
        button_forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(webView.canGoForward()){
                    progressLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    TextViewProgress.setVisibility(View.VISIBLE);
                    webView.goForward();
              }
            }
        });
        button_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                progressLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                TextViewProgress.setVisibility(View.VISIBLE);
                String currentURL = webView.getUrl();
                webView.loadUrl(currentURL);
            }
        });
        return myFragmentView;
    }

    // Manages the behavior when URLs are loaded
    private class MyBrowser extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("youtube")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            } else {
                progressLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                TextViewProgress.setVisibility(View.VISIBLE);
                view.loadUrl(url);
                return true;
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            progressLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            TextViewProgress.setVisibility(View.GONE);
        }
        // WebView error handler
        public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
            final String LOGMETHOD = " onReceivedError ";
            if (errorCode == ERROR_AUTHENTICATION) {
                String LOGBODY = "ERROR_AUTHENTICATION";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_BAD_URL) {
                String LOGBODY = "ERROR_BAD_URL";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_CONNECT) {
                String LOGBODY = "ERROR_CONNECT";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_FAILED_SSL_HANDSHAKE) {
                String LOGBODY = "ERROR_FAILED_SSL_HANDSHAKE";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_FILE) {
                String LOGBODY = "ERROR_FILE";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_FILE_NOT_FOUND) {
                String LOGBODY = "ERROR_FILE_NOT_FOUND";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_HOST_LOOKUP) {
                String LOGBODY = "ERROR_HOST_LOOKUP";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_IO) {
                String LOGBODY = "ERROR_IO";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_PROXY_AUTHENTICATION) {
                String LOGBODY = "ERROR_PROXY_AUTHENTICATION";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_REDIRECT_LOOP) {
                String LOGBODY = "ERROR_REDIRECT_LOOP";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_TIMEOUT) {
                String LOGBODY = "ERROR_TIMEOUT";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_TOO_MANY_REQUESTS) {
                String LOGBODY = "ERROR_TOO_MANY_REQUESTS";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_UNKNOWN) {
                String LOGBODY = "ERROR_UNKNOWN";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_UNSUPPORTED_AUTH_SCHEME) {
                String LOGBODY = "ERROR_UNSUPPORTED_AUTH_SCHEME";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            if (errorCode == ERROR_UNSUPPORTED_SCHEME) {
                String LOGBODY = "ERROR_UNSUPPORTED_SCHEME";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
        }
    }
}