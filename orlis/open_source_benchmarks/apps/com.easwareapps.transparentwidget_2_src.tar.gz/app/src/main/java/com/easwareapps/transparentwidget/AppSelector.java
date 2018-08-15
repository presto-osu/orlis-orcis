package com.easwareapps.transparentwidget;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;


import java.util.Collections;
import java.util.List;

/**
 * ॐ
 * लोकाः समस्ताः सुखिनो भवन्तु॥
 * <p/>
 * EmptyWidget
 * Copyright (C) 2016  vishnu
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class AppSelector extends AppCompatActivity{

    ListView lv;
    List pkgAppsList;
    ProgressBar pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_selector);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.select_app);
        setSupportActionBar(toolbar);

        pb = (ProgressBar) findViewById(R.id.toolbar_progress_bar);

        lv = (ListView) findViewById(R.id.apps_list);
        new AppsLoader().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResolveInfo info = (ResolveInfo) pkgAppsList.get(position);
                String result = new String(info.activityInfo.packageName + "," + info.activityInfo.name);
                Intent resultValue = new Intent();
                resultValue.putExtra("app", result);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });


    }

    public class AppsLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
//            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//            pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);
            //return null;

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> packageList = packageManager.queryIntentActivities(
                    mainIntent, 0);
            Collections.sort(packageList, new ResolveInfo.DisplayNameComparator(
                    packageManager));
            pkgAppsList = packageList;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pb.setVisibility(View.GONE);
            lv.setAdapter(new AppListAdapter(getApplicationContext(), pkgAppsList));
            super.onPostExecute(aVoid);
        }
    }
}
