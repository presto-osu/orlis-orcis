/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.pixmob.freemobile.netstat.R;

/**
 * Activity showing an HTML document.
 * @author Pixmob
 */
@SuppressLint("NewApi")
public class DocumentBrowser extends Activity {
    /**
     * Intent key for setting a document URL to display.
     */
    public static final String INTENT_EXTRA_URL = "url";
    public static final String INTENT_EXTRA_HIDE_BUTTON_BAR = "hideButtonBar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        String url = getIntent().getStringExtra(INTENT_EXTRA_URL);
        if (TextUtils.isEmpty(url)) {
            url = "file:///android_asset/CHANGELOG.html";
        } else if (!url.startsWith("file://") || !url.startsWith("http://") || !url.startsWith("https://")) {
            url = "file:///android_asset/" + url;
        }

        setContentView(R.layout.document_browser);

        final boolean buttonBarInvisible = getIntent().getBooleanExtra(INTENT_EXTRA_HIDE_BUTTON_BAR, false);
        findViewById(R.id.button_bar).setVisibility(buttonBarInvisible ? View.GONE : View.VISIBLE);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.doc_progress);

        final WebView browser = (WebView) findViewById(R.id.browser);
        // Fix white scrollbar: http://stackoverflow.com/a/2766399/422906
        browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Open links using the system browser application.
                final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
        });
        browser.loadUrl(url);
    }

    public void onOK(View v) {
        finish();
    }
}
