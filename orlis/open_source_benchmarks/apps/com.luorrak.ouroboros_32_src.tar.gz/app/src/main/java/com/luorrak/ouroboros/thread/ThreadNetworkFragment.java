package com.luorrak.ouroboros.thread;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;

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

public class ThreadNetworkFragment extends Fragment {
    private Activity activity;
    private InsertThreadIntoDatabaseTask insertThreadIntoDatabaseTask;

    public void beginTask(JsonObject jsonObject, InfiniteDbHelper infiniteDbHelper, String boardName, String resto, int threadPosition, boolean firstRequest, RecyclerView recyclerView, ThreadAdapter threadAdapter){
        insertThreadIntoDatabaseTask = new InsertThreadIntoDatabaseTask(activity, infiniteDbHelper, boardName, resto, threadPosition, firstRequest, recyclerView, threadAdapter);
        insertThreadIntoDatabaseTask.execute(jsonObject);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(insertThreadIntoDatabaseTask != null && insertThreadIntoDatabaseTask.getStatus() == AsyncTask.Status.RUNNING){
            insertThreadIntoDatabaseTask.cancel(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        if (insertThreadIntoDatabaseTask != null){
            insertThreadIntoDatabaseTask.onAttach(activity);
        }

    }

    @Override
    public void onDetach() {
        if (insertThreadIntoDatabaseTask != null){
            insertThreadIntoDatabaseTask.onDetach();
        }
        super.onDetach();
    }

    public void cancelTask(){
        insertThreadIntoDatabaseTask.cancel(true);
    }

    public class InsertThreadIntoDatabaseTask extends AsyncTask<JsonObject, Void, Void> {
        private Activity activity;
        private InfiniteDbHelper infiniteDbHelper;
        private String boardName;
        private String resto;
        private ThreadAdapter threadAdapter;
        private RecyclerView recyclerView;
        private int threadPosition;
        private boolean firstRequest;

        public InsertThreadIntoDatabaseTask(Activity activity, InfiniteDbHelper infiniteDbHelper, String boardName, String resto, int threadPosition, boolean firstRequest, RecyclerView recyclerView, ThreadAdapter threadAdapter) {
            onAttach(activity);
            this.infiniteDbHelper = infiniteDbHelper;
            this.boardName = boardName;
            this.resto = resto;
            this.threadAdapter = threadAdapter;
            this.recyclerView = recyclerView;
            this.threadPosition = threadPosition;
            this.firstRequest = firstRequest;
        }

        public void onDetach(){
            this.activity = null;
        }

        public void onAttach(Activity activity){
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(JsonObject... params) {
            JsonParser jsonParser = new JsonParser();
            JsonArray posts = params[0].getAsJsonArray("posts");
            int position = 0;
            for (JsonElement postElement : posts) {
                if (isCancelled()) break;
                JsonObject post = postElement.getAsJsonObject();

                infiniteDbHelper.insertThreadEntry(
                        boardName,
                        jsonParser.getThreadResto(post),
                        jsonParser.getThreadNo(post),
                        jsonParser.getThreadSub(post),
                        jsonParser.getThreadCom(post),
                        jsonParser.getThreadEmail(post),
                        jsonParser.getThreadName(post),
                        jsonParser.getThreadTrip(post),
                        jsonParser.getThreadTime(post),
                        jsonParser.getThreadLastModified(post),
                        jsonParser.getThreadId(post),
                        jsonParser.getThreadEmbed(post),
                        jsonParser.getMediaFiles(post),
                        position
                );
                position++;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Cursor cursor = infiniteDbHelper.getThreadCursor(resto);
            String threadSubject = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_SUB));
            int replyCount = cursor.getCount();
            cursor.close();
            updateToolbar(threadSubject);
            newPostSnackBar(replyCount);
            threadAdapter.changeCursor(infiniteDbHelper.getThreadCursor(resto));

            if (firstRequest){
                recyclerView.scrollToPosition(threadPosition);
            }
        }

        private void updateToolbar(String threadSubject){
            activity.setTitle(threadSubject != null ? threadSubject : "/" + boardName + "/" + resto);

            ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
            if (progressBar != null){
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        private void newPostSnackBar(int replyCount){
            Cursor cursor = infiniteDbHelper.getThreadReplyCountCursor(resto);
            int oldReplycount = 0;
            if ((cursor != null) && (cursor.getCount() > 0)){
                oldReplycount = cursor.getInt(cursor.getColumnIndex(DbContract.ThreadReplyCountTracker.REPLY_COUNT));
            }
            cursor.close();

            if (replyCount > oldReplycount){
                int newReplies = replyCount - oldReplycount;
                final int finalOldReplycount = oldReplycount;
                Snackbar.make(recyclerView, String.valueOf(newReplies) + " New Replies", Snackbar.LENGTH_LONG)
                        .setAction("VIEW", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                recyclerView.scrollToPosition(finalOldReplycount);
                            }
                        }).show();
                infiniteDbHelper.updateThreadReplyCount(boardName, resto, replyCount);
            }
        }
    }
}
