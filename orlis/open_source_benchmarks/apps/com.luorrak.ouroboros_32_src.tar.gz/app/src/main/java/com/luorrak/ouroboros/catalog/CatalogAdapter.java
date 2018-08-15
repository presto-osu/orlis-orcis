package com.luorrak.ouroboros.catalog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.CommentParser;
import com.luorrak.ouroboros.thread.ThreadActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;

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
public class CatalogAdapter extends CursorRecyclerAdapter implements Filterable {
    private final String LOG_TAG = CatalogAdapter.class.getSimpleName();

    private final int LOCKED = 1;
    private final int STICKY = 1;

    private NetworkHelper networkHelper = new NetworkHelper();
    private CommentParser commentParser = new CommentParser();
    private String boardName;
    private InfiniteDbHelper infiniteDbHelper;
    private Context context;

    public CatalogAdapter(Cursor cursor, String boardName, InfiniteDbHelper infiniteDbHelper, Context context) {
        super(cursor);
        this.boardName = boardName;
        this.infiniteDbHelper = infiniteDbHelper;
        this.context = context;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        String imageUrl;
        String[] youtubeData;

        CatalogViewHolder catalogViewHolder = (CatalogViewHolder)holder;
        resetCatalogViewHolder(catalogViewHolder);
        createCatalogObject(catalogViewHolder, cursor);

        setLayoutText(catalogViewHolder);

        if (catalogViewHolder.catalogObject.locked == LOCKED) {
            catalogViewHolder.lockIcon.setVisibility(View.VISIBLE);
        }

        if (catalogViewHolder.catalogObject.sticky == STICKY) {
            catalogViewHolder.stickyIcon.setVisibility(View.VISIBLE);
        }

        if (catalogViewHolder.catalogObject.tim != null){
            imageUrl = ChanUrls.getThumbnailUrl(boardName, catalogViewHolder.catalogObject.tim);
            networkHelper.getImageNoCrossfade(catalogViewHolder.catalogPicture, imageUrl);
        } else if (catalogViewHolder.catalogObject.embed != null) {
            youtubeData = Util.parseYoutube(catalogViewHolder.catalogObject.embed);
            imageUrl = "https://" + youtubeData[1];
            networkHelper.getImageNoCrossfade(catalogViewHolder.catalogPicture, imageUrl);
        }
        //HIDDEN TAG ON COM TEXT TO HACK THREAD NUMBER INTO VIEW
        catalogViewHolder.catalogComText.setTag(cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_NO)));
    }


    private void setLayoutText(CatalogViewHolder catalogViewHolder){
        if (catalogViewHolder.catalogObject.sub != null){
            catalogViewHolder.catalogSubText.setVisibility(View.VISIBLE);
            catalogViewHolder.catalogObject.sub = Html.fromHtml(catalogViewHolder.catalogObject.sub).toString();
            catalogViewHolder.catalogSubText.setText(catalogViewHolder.catalogObject.sub);
        }

        if (catalogViewHolder.catalogObject.com != null){
            catalogViewHolder.catalogComText.setVisibility(View.VISIBLE);
            catalogViewHolder.catalogComText.setText(commentParser.parseCom(
                    catalogViewHolder.catalogObject.com,
                    CommentParser.CATALOG_VIEW,
                    "v",
                    "-1",
                    null,
                    infiniteDbHelper
            ));
        }

        if (catalogViewHolder.catalogObject.itemViewType == Util.CATALOG_LAYOUT_LIST){
            catalogViewHolder.catalogObject.replyCount += " Replies";
            catalogViewHolder.catalogObject.imageReplyCount += " Images";
        }

        catalogViewHolder.replyCount.setText(catalogViewHolder.catalogObject.replyCount);
        catalogViewHolder.imageReplyCount.setText(catalogViewHolder.catalogObject.imageReplyCount);
    }

    private void resetCatalogViewHolder(CatalogViewHolder catalogViewHolder){
        catalogViewHolder.catalogSubText.setVisibility(View.GONE);
        catalogViewHolder.catalogComText.setVisibility(View.GONE);
        catalogViewHolder.lockIcon.setVisibility(View.GONE);
        catalogViewHolder.stickyIcon.setVisibility(View.GONE);
        catalogViewHolder.catalogPicture.setImageDrawable(null);
    }

    private void createCatalogObject(CatalogViewHolder catalogViewHolder, Cursor cursor){
        catalogViewHolder.catalogObject.sub = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_SUB));
        catalogViewHolder.catalogObject.com = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_COM));
        catalogViewHolder.catalogObject.tim = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_TIM));
        catalogViewHolder.catalogObject.replyCount = String.valueOf(cursor.getInt(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_REPLIES)));
        catalogViewHolder.catalogObject.imageReplyCount = String.valueOf(cursor.getInt(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_IMAGES)));
        catalogViewHolder.catalogObject.locked = cursor.getInt(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_LOCKED));
        catalogViewHolder.catalogObject.sticky = cursor.getInt(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_STICKY));
        catalogViewHolder.catalogObject.embed = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_EMBED));
        catalogViewHolder.catalogObject.itemViewType = getItemViewType(cursor.getPosition());
    }

    @Override
    public int getItemViewType(int position) {
        return SettingsHelper.getCatalogView(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Util.CATALOG_LAYOUT_LIST: {
                return new CatalogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_list_item, parent, false));
            }
            default: {
                return new CatalogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_grid_item, parent, false));
            }
        }
    }


    class CatalogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView catalogSubText;
        public TextView catalogComText;
        public TextView replyCount;
        public TextView imageReplyCount;
        public ImageView catalogPicture;
        public ImageView lockIcon;
        public ImageView stickyIcon;
        public CatalogObject catalogObject;

        public CatalogViewHolder(View itemView) {
            super(itemView);
            catalogSubText = (TextView) itemView.findViewById(R.id.catalog_sub_text);
            catalogComText = (TextView) itemView.findViewById(R.id.catalog_com_text);
            replyCount = (TextView) itemView.findViewById(R.id.catalog_reply_count);
            imageReplyCount = (TextView) itemView.findViewById(R.id.catalog_image_reply_count);
            catalogPicture = (ImageView) itemView.findViewById(R.id.catalog_picture);
            lockIcon = (ImageView) itemView.findViewById(R.id.catalog_lock_icon);
            stickyIcon = (ImageView) itemView.findViewById(R.id.catalog_sticky_icon);
            catalogObject = new CatalogObject();

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, ThreadActivity.class);
            String threadNo = (String) catalogComText.getTag();
            intent.putExtra(Util.INTENT_THREAD_NO, threadNo);
            intent.putExtra(Util.INTENT_BOARD_NAME, boardName);
            context.startActivity(intent);
        }
    }

    //Catalog Object //////////////////////////////////////////////////////////////////////////////

    class CatalogObject {
        String sub;
        String com;
        String tim;
        String replyCount;
        String imageReplyCount;
        int locked;
        int sticky;
        String embed;
        int itemViewType;

    }
}
