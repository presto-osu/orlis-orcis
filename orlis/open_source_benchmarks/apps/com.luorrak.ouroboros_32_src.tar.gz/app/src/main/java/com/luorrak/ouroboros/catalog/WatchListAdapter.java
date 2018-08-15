package com.luorrak.ouroboros.catalog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.thread.ThreadActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.TouchHelperInterface;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;

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
public class WatchListAdapter extends CursorRecyclerAdapter implements TouchHelperInterface{
    private InfiniteDbHelper infiniteDbHelper;
    private DrawerLayout drawerLayout;

    public WatchListAdapter(Cursor cursor, DrawerLayout drawerLayout, InfiniteDbHelper infiniteDbHelper) {
        super(cursor);
        this.drawerLayout = drawerLayout;
        this.infiniteDbHelper = infiniteDbHelper;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        WatchListViewHolder watchlistViewHolder = (WatchListViewHolder) holder;

        watchlistViewHolder.watchlistObject.title = cursor.getString(cursor.getColumnIndex(DbContract.WatchlistEntry.COLUMN_TITLE));
        watchlistViewHolder.watchlistObject.board = cursor.getString(cursor.getColumnIndex(DbContract.WatchlistEntry.COLUMN_BOARD));
        watchlistViewHolder.watchlistObject.no = cursor.getString(cursor.getColumnIndex(DbContract.WatchlistEntry.COLUMN_NO));
        watchlistViewHolder.watchlistObject.serializedMediaList = cursor.getBlob(cursor.getColumnIndex(DbContract.WatchlistEntry.COLUMN_MEDIA_FILES));
        watchlistViewHolder.watchlistTitle.setText(watchlistViewHolder.watchlistObject.title);

        if (watchlistViewHolder.watchlistObject.serializedMediaList != null){
            ArrayList<Media> deserializedMediaList = (ArrayList<Media>) Util.deserializeObject(watchlistViewHolder.watchlistObject.serializedMediaList);
            Ion.with(watchlistViewHolder.watchlistThumbnail)
                    .load(ChanUrls.getThumbnailUrl(watchlistViewHolder.watchlistObject.board, deserializedMediaList.get(0).fileName))
                    .withBitmapInfo();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.watchlist_item, parent, false);
        return new WatchListViewHolder(view);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        infiniteDbHelper.swapWatchlistOrder(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        infiniteDbHelper.removeWatchlistEntry(position);
        changeCursor(infiniteDbHelper.getWatchlistCursor());
    }

    class WatchListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView watchlistThumbnail;
        public Button watchlistTitle;

        public watchlistObject watchlistObject;

        public WatchListViewHolder(View itemView) {
            super(itemView);
            watchlistThumbnail = (ImageView) itemView.findViewById(R.id.watchlist_thumbnail);
            watchlistTitle = (Button) itemView.findViewById(R.id.watchlist_title);

            watchlistObject = new watchlistObject();

            watchlistTitle.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.watchlist_title: {
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ThreadActivity.class);
                    intent.putExtra(Util.INTENT_THREAD_NO, watchlistObject.no);
                    intent.putExtra(Util.INTENT_BOARD_NAME, watchlistObject.board);
                    context.startActivity(intent);
                    break;
                }
            }
        }
    }

    // Cursor Object ///////////////////////////////////////////////////////////////////////////////

    class watchlistObject {
        public String title;
        public String board;
        public String no;
        public byte[] serializedMediaList;
    }
}
