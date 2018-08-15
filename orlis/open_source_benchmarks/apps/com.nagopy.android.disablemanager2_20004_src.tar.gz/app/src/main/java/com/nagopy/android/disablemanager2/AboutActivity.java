/*
 * Copyright (C) 2015 75py
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
package com.nagopy.android.disablemanager2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nagopy.android.disablemanager2.support.DebugUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        // ツールバーをアクションバーとしてセット
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView versionName = (TextView) findViewById(R.id.version_name);
        versionName.setText("Version: " + BuildConfig.VERSION_NAME);

        LinearLayout parentView = (LinearLayout) findViewById(R.id.parent);

        AssetManager assetManager = getAssets();
        InputStream is = null;
        BufferedReader br = null;
        try {
            try {
                is = assetManager.open("about/about.txt");
                br = new BufferedReader(new InputStreamReader(is));

                TextView title = null;
                TextView body = null;
                String str;
                StringBuilder sb = new StringBuilder();
                while ((str = br.readLine()) != null) {
                    DebugUtil.debugLog(str);
                    if (TextUtils.isEmpty(str)) {
                        sb.append("<br>");
                    } else {
                        if (str.startsWith("# ")) {
                            if (body != null) {
                                String html = sb.toString();
                                while (html.endsWith("<br>")) {
                                    html = html.substring(0, html.length() - 4);
                                }
                                body.setText(Html.fromHtml(html));
                                parentView.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                parentView.addView(body, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                sb.setLength(0);
                            }
                            title = (TextView) View.inflate(getApplicationContext(), R.layout.view_title, null);
                            body = (TextView) View.inflate(this, R.layout.view_body, null);
                            title.setText(str.replace("# ", ""));
                        } else if (str.startsWith("> ")) {
                            str = "> " + str.replace("> ", "<i>") + "</i>";
                            sb.append(str);
                            sb.append("<br>");
                        } else {
                            sb.append(str);
                            sb.append("<br>");
                        }
                    }
                }
                if (body != null) {
                    body.setText(Html.fromHtml(sb.toString()));
                    parentView.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    parentView.addView(body, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    sb.setLength(0);
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_open_source_license:
                Intent licenseActivity = new Intent(getApplicationContext(), LicenseActivity.class);
                startActivity(licenseActivity);
                break;
        }
    }
}
