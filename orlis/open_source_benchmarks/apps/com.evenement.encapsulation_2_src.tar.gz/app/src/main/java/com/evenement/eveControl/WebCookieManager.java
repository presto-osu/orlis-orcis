package com.evenement.eveControl;

import android.util.Log;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Cookie manager to two-way sync cookies between HttpUrlConnection(java.net.CookieManager= and WebView(android.webkit.CookieManager)
 */
public class WebCookieManager extends CookieManager
{
    private final android.webkit.CookieManager webkitCookieManager;

    public WebCookieManager()
    {
        this(null, null);
    }

    public WebCookieManager(CookieStore store, CookiePolicy cookiePolicy)
    {
        super(null, cookiePolicy);

        this.webkitCookieManager = android.webkit.CookieManager.getInstance();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException
    {
        // make sure our args are valid
        if ((uri == null) || (responseHeaders == null)) return;

        // save our url once
        String url = uri.toString();

        // go over the headers
        for (String headerKey : responseHeaders.keySet())
        {
            // ignore headers which aren't cookie related
            if ((headerKey == null) || !(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey.equalsIgnoreCase("Set-Cookie"))) continue;

            // process each of the headers
            for (String headerValue : responseHeaders.get(headerKey))
            {
                Log.d("aa", "putCookie" + headerValue);
                this.webkitCookieManager.setCookie(url, headerValue);
            }
        }
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException
    {
        // make sure our args are valid
        if ((uri == null) || (requestHeaders == null)) throw new IllegalArgumentException("Argument is null");

        // save our url once
        String url = uri.toString();

        // prepare our response
        Map<String, List<String>> res = new java.util.HashMap<String, List<String>>();

        // get the cookie
        String cookie = this.webkitCookieManager.getCookie(url);
Log.d("aa", "getCookie" + cookie);
        // return it
        if (cookie != null) res.put("Cookie", Collections.singletonList(cookie));
        return res;
    }
}

