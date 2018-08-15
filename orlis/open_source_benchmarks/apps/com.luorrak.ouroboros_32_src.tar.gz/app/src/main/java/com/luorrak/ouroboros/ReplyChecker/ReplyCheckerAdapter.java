package com.luorrak.ouroboros.ReplyChecker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.thread.ThreadActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Util;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
public class ReplyCheckerAdapter extends CursorRecyclerAdapter{

    InfiniteDbHelper infiniteDbHelper;
    public ReplyCheckerAdapter(Cursor cursor, InfiniteDbHelper infiniteDbHelper) {
        super(cursor);
        this.infiniteDbHelper = infiniteDbHelper;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        ReplyCheckerViewHolder replyCheckerViewHolder = (ReplyCheckerViewHolder) holder;
        createReplyCheckerObject(replyCheckerViewHolder, cursor);
        setViewVisibility(replyCheckerViewHolder);

        String threadTitle = "/" + replyCheckerViewHolder.replyCheckerObject.boardName + "/" +
                replyCheckerViewHolder.replyCheckerObject.resto;

        replyCheckerViewHolder.rcThreadName.setText(threadTitle);
        replyCheckerViewHolder.rcSubjectText.setText(replyCheckerViewHolder.replyCheckerObject.subject);
        replyCheckerViewHolder.rcCommentText.setText(replyCheckerViewHolder.replyCheckerObject.comment);
        replyCheckerViewHolder.rcReplyCountText.setText(replyCheckerViewHolder.replyCheckerObject.replyCount + " Replies");
    }

    private void setViewVisibility(ReplyCheckerViewHolder replyCheckerViewHolder){
        if (replyCheckerViewHolder.replyCheckerObject.subject.equals("")){
            replyCheckerViewHolder.rcSubjectText.setVisibility(View.GONE);
        }
        if (replyCheckerViewHolder.replyCheckerObject.comment.equals("")){
            replyCheckerViewHolder.rcCommentText.setVisibility(View.GONE);
        }
    }

    private void createReplyCheckerObject(ReplyCheckerViewHolder replyCheckerViewHolder, Cursor cursor){
        if((cursor != null) && (cursor.getCount() > 0)){
            replyCheckerViewHolder.replyCheckerObject.id = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts._ID));
            replyCheckerViewHolder.replyCheckerObject.boardName = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_BOARDS));
            replyCheckerViewHolder.replyCheckerObject.resto = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_RESTO));
            replyCheckerViewHolder.replyCheckerObject.subject = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_SUBJECT));
            replyCheckerViewHolder.replyCheckerObject.comment = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_COMMENT));
            replyCheckerViewHolder.replyCheckerObject.replyCount = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_NUMBER_OF_REPLIES));
            replyCheckerViewHolder.replyCheckerObject.position = cursor.getInt(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_POSITION));
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReplyCheckerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_checker_list_item, parent, false));
    }

    class ReplyCheckerObject {
        public String id = "";
        public String boardName = "";
        public String resto = "";
        public String subject = "";
        public String comment = "";
        public String replyCount = "";
        public int position = 0;
    }

    private class ReplyCheckerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView rcCard;
        public TextView rcThreadName;
        public TextView rcSubjectText;
        public TextView rcCommentText;
        public TextView rcReplyCountText;
        public ImageButton rcMarkAsReadButton;
        public ReplyCheckerObject replyCheckerObject;

        public ReplyCheckerViewHolder(View itemView) {
            super(itemView);
            rcCard = (CardView) itemView.findViewById(R.id.reply_checker_card);
            rcThreadName = (TextView) itemView.findViewById(R.id.reply_checker_thread_name);
            rcSubjectText = (TextView) itemView.findViewById(R.id.reply_checker_sub_text);
            rcCommentText = (TextView) itemView.findViewById(R.id.reply_checker_com_text);
            rcReplyCountText = (TextView) itemView.findViewById(R.id.reply_checker_reply_count);
            rcMarkAsReadButton = (ImageButton) itemView.findViewById(R.id.reply_checker_mark_as_read_button);
            replyCheckerObject = new ReplyCheckerObject();

            rcThreadName.setTypeface(rcThreadName.getTypeface(), Typeface.BOLD);
            rcReplyCountText.setTypeface(rcReplyCountText.getTypeface(), Typeface.BOLD);

            rcMarkAsReadButton.setOnClickListener(this);

            rcCard.setOnClickListener(this);
            rcThreadName.setOnClickListener(this);
            rcSubjectText.setOnClickListener(this);
            rcCommentText.setOnClickListener(this);
            rcReplyCountText.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.reply_checker_mark_as_read_button:{
                    Snackbar.make(v, "Thread Marked As Read", Snackbar.LENGTH_LONG).show();
                    infiniteDbHelper.removeUserPostFlag(replyCheckerObject.id);
                    changeCursor(infiniteDbHelper.getFlaggedUserPostsCursor());
                    break;
                }
                default:{
                    // TODO: 2/8/16 Open thread in new intent and mark as read
                    Intent intent = new Intent(v.getContext(), ThreadActivity.class);
                    intent.putExtra(Util.INTENT_THREAD_NO, replyCheckerObject.resto);
                    intent.putExtra(Util.INTENT_BOARD_NAME, replyCheckerObject.boardName);
                    intent.putExtra(Util.INTENT_THREAD_POSITION, replyCheckerObject.position);
                    v.getContext().startActivity(intent);
                    break;
                }
            }
        }
    }
}
