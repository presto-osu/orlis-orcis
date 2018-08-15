package com.fullscreen.webviews;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by jackson on 02/11/15.
 */
public class DIOWebViewClient extends WebViewClient {

    private final Bundle savedInstanceState;
    private final ViewGroup viewLoading;
    private boolean isViewLoadingNotStarted = true;

    public DIOWebViewClient(Bundle savedInstanceState, ViewGroup viewLoading) {
        this.savedInstanceState = savedInstanceState;
        this.viewLoading = viewLoading;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (savedInstanceState == null)
            view.loadUrl(url);
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (isViewLoadingNotStarted) {
            viewLoading.setVisibility(View.VISIBLE);
            isViewLoadingNotStarted = false;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (!isViewLoadingNotStarted) {
            viewLoading.setVisibility(View.INVISIBLE);
            isViewLoadingNotStarted = true;
        }
        view.setVisibility(View.VISIBLE);
    }
}
