package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.downloads.DownloadChapter;

import java.util.ArrayList;
import java.util.List;

public class DownloadChapterListingsAdapter extends BaseCursorAdapter {
    private List<String> mRecentChapterUrls;

    private int mColor;

    public DownloadChapterListingsAdapter(Context context) {
        super(context, DownloadChapter.class);

        mRecentChapterUrls = new ArrayList<>();

        mColor = mContext.getResources().getColor(R.color.primaryBlue500);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mCursor == null) {
            throw new IllegalStateException("Null Cursor");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Can't Move Cursor to Position " + position);
        }

        ViewHolder viewHolder;
        View currentView = convertView;

        if (currentView == null) {
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_download_chapter, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        DownloadChapter currentDownloadChapter = (DownloadChapter) getItem(position);
        if (mRecentChapterUrls != null && mRecentChapterUrls.contains(currentDownloadChapter.getUrl())) {
            viewHolder.renderView(currentDownloadChapter, mContext.getResources().getColor(R.color.secondaryText), true);
        } else {
            viewHolder.renderView(currentDownloadChapter, mColor, false);
        }

        return currentView;
    }

    public List<String> getRecentChapterUrls() {
        return mRecentChapterUrls;
    }

    public void setRecentChapterUrls(List<String> recentChapterUrls) {
        mRecentChapterUrls = recentChapterUrls;

        notifyDataSetChanged();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    private static class ViewHolder {
        private TextView mNameTextView;

        public ViewHolder(View itemView) {
            mNameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        }

        public void renderView(DownloadChapter downloadChapter, int color, boolean alreadyRead) {
            mNameTextView.setText(downloadChapter.getName());
            mNameTextView.setTextColor(color);

            if (alreadyRead) {
                mNameTextView.setTypeface(mNameTextView.getTypeface(), Typeface.BOLD_ITALIC);
            } else {
                mNameTextView.setTypeface(mNameTextView.getTypeface(), Typeface.BOLD);
            }
        }
    }
}
