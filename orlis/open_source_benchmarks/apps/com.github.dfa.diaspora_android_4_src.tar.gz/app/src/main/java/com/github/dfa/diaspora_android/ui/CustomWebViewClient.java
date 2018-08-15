package com.github.dfa.diaspora_android.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.dfa.diaspora_android.App;

/**
 * Created by Gregor Santner (gsantner) on 04.06.16.
 */
public class CustomWebViewClient extends WebViewClient {
    private App app;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;

    public CustomWebViewClient(App app, SwipeRefreshLayout swipeRefreshLayout, WebView webView) {
        this.app = app;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.webView = webView;
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!url.contains(app.getSettings().getPodDomain())) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.getApplicationContext().startActivity(i);
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        swipeRefreshLayout.setEnabled(true);
        if(url.contains(app.getSettings().getPodDomain()+"/conversations/") || url.endsWith("status_messages/new")){
            swipeRefreshLayout.setEnabled(false);
        }
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        swipeRefreshLayout.setRefreshing(false);

        final CookieManager cookieManager = app.getCookieManager();
        String cookies = cookieManager.getCookie(url);
        //Log.d(App.TAG, "All the cookies in a string:" + cookies);

        if (cookies != null) {
            cookieManager.setCookie(url, cookies);
            cookieManager.setCookie("https://" + app.getSettings().getPodDomain(), cookies);
            //for (String c : cookies.split(";")) {
            // Log.d(App.TAG, "Cookie: " + c.split("=")[0] + " Value:" + c.split("=")[1]);
            //}
            //new ProfileFetchTask(app).execute();
        }
    }

}
