package com.evenement.eveControl;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

/**
 * Custom WebViewClient to handle SSl errors caused by self signed SSL certificates
 */
public class WebViewClient extends android.webkit.WebViewClient {

    public WebViewClient() {
    }


    @Override
    public void onReceivedSslError(final WebView view, final SslErrorHandler handler, SslError error) {

        handler.proceed();

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }
}