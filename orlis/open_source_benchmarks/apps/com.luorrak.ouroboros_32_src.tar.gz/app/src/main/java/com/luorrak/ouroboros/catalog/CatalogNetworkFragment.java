package com.luorrak.ouroboros.catalog;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.SettingsHelper;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class CatalogNetworkFragment extends Fragment {
    private Activity activity;
    private InsertCatalogIntoDatabase insertCatalogIntoDatabaseTask;

    public void beginTask(JsonArray jsonArray, InfiniteDbHelper infiniteDbHelper, String boardName, CatalogAdapter catalogAdapter){
        insertCatalogIntoDatabaseTask = new InsertCatalogIntoDatabase(activity, infiniteDbHelper, boardName, catalogAdapter);
        insertCatalogIntoDatabaseTask.execute(jsonArray);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(insertCatalogIntoDatabaseTask != null && insertCatalogIntoDatabaseTask.getStatus() == AsyncTask.Status.RUNNING){
            insertCatalogIntoDatabaseTask.cancel(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        if (insertCatalogIntoDatabaseTask != null){
            insertCatalogIntoDatabaseTask.onAttach(activity);
        }

    }

    @Override
    public void onDetach() {
        if (insertCatalogIntoDatabaseTask != null){
            insertCatalogIntoDatabaseTask.onDetach();
        }
        super.onDetach();
    }

    public void cancelTask(){
        insertCatalogIntoDatabaseTask.cancel(true);
    }

    public AsyncTask.Status getStatus(){
        if (insertCatalogIntoDatabaseTask != null){
            return insertCatalogIntoDatabaseTask.getStatus();
        } else {
            return null;
        }
    }

    public class InsertCatalogIntoDatabase extends AsyncTask<JsonArray, Void, Void> {
        Activity activity;
        InfiniteDbHelper infiniteDbHelper;
        String boardName;
        CatalogAdapter catalogAdapter;
        SwipeRefreshLayout swipeRefreshLayout;

        public InsertCatalogIntoDatabase(Activity activity, InfiniteDbHelper infiniteDbHelper, String boardName,
                                         CatalogAdapter catalogAdapter){
            this.activity = activity;
            this.infiniteDbHelper = infiniteDbHelper;
            this.boardName = boardName;
            this.catalogAdapter = catalogAdapter;
        }
        @Override
        protected Void doInBackground(JsonArray... params) {
            JsonParser jsonParser = new JsonParser();
            infiniteDbHelper.deleteCatalogCache();

            for (JsonElement page : params[0]) {
                JsonArray threads = page.getAsJsonObject().getAsJsonArray("threads");
                //loop through each post on the catalog and submit the results to the database for caching.
                for (JsonElement threadElement : threads) {
                    if (isCancelled()) break;
                    JsonObject thread = threadElement.getAsJsonObject();

                    infiniteDbHelper.insertCatalogEntry(
                            boardName,
                            jsonParser.getCatalogNo(thread),
                            jsonParser.getCatalogFilename(thread),
                            jsonParser.getCatalogTim(thread),
                            jsonParser.getCatalogExt(thread),
                            jsonParser.getCatalogSub(thread),
                            jsonParser.getCatalogCom(thread),
                            jsonParser.getCatalogReplies(thread),
                            jsonParser.getCatalogImageReplyCount(thread),
                            jsonParser.getCatalogSticky(thread),
                            jsonParser.getCatalogLocked(thread),
                            jsonParser.getCatalogEmbed(thread)
                    );
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor(SettingsHelper.getSortByMethod(getContext())));
            swipeRefreshLayout = (SwipeRefreshLayout) activity.findViewById(R.id.catalog_swipe_container);
            ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
            if(swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (progressBar != null){
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        public void onDetach(){
            this.activity = null;
        }

        public void onAttach(Activity activity){
            this.activity = activity;
        }
    }
}
