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

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nagopy.android.disablemanager2.support.DimenUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LicenseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_license);
        // ツールバーをアクションバーとしてセット
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout parent = (LinearLayout) findViewById(R.id.license_parent);

        AssetManager assetManager = getAssets();
        InputStream is = null;
        BufferedReader br = null;
        try {
            try {
                String[] filePaths = assetManager.list("license");
                for (String filePath : filePaths) {
                    is = assetManager.open("license/" + filePath);
                    br = new BufferedReader(new InputStreamReader(is));

                    // １行ずつ読み込み、改行を付加する
                    String title = br.readLine();
                    String str;
                    StringBuilder sb = new StringBuilder();
                    while ((str = br.readLine()) != null) {
                        Log.d("XXXXX", str);
                        sb.append(str);
                        sb.append('\n');
                    }

                    TextView titleView = new TextView(getApplicationContext());
                    titleView.setText(title);
                    titleView.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
                    titleView.setTextColor(Color.BLACK);
                    parent.addView(titleView);

                    TextView bodyView = new TextView(getApplicationContext());
                    bodyView.setText(sb.toString());
                    bodyView.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
                    bodyView.setBackgroundColor(Color.LTGRAY);
                    bodyView.setTextColor(Color.BLACK);
                    int tenDp = DimenUtil.getPixelFromDp(getApplicationContext(), 10);
                    bodyView.setPadding(tenDp, tenDp, tenDp, tenDp);
                    parent.addView(bodyView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
}
