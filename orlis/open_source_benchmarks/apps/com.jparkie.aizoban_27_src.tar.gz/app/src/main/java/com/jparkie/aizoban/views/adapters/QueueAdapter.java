package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.utils.DownloadUtils;

public class QueueAdapter extends BaseCursorAdapter {
    public QueueAdapter(Context context) {
        super(context, DownloadChapter.class);
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
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_queue, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        DownloadChapter currentDownloadChapter = (DownloadChapter) getItem(position);
        viewHolder.renderView(mContext, currentDownloadChapter);

        return currentView;
    }

    private static class ViewHolder {
        private TextView mNameTextView;
        private TextView mSourceTextView;
        private TextView mFlagTextView;

        public ViewHolder(View itemView) {
            mNameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            mSourceTextView = (TextView) itemView.findViewById(R.id.sourceTextView);
            mFlagTextView = (TextView) itemView.findViewById(R.id.flagTextView);
        }

        public void renderView(Context context, DownloadChapter downloadChapter) {
            mNameTextView.setText(downloadChapter.getName());
            mSourceTextView.setText(downloadChapter.getSource());

            if (downloadChapter.getFlag() == DownloadUtils.FLAG_FAILED) {
                mFlagTextView.setText(context.getResources().getString(R.string.flag_failed));
            } else if (downloadChapter.getFlag() == DownloadUtils.FLAG_PAUSED) {
                mFlagTextView.setText(context.getResources().getString(R.string.flag_paused));
            } else if (downloadChapter.getFlag() == DownloadUtils.FLAG_PENDING) {
                if (downloadChapter.getTotalPages() != 0) {
                    mFlagTextView.setText(context.getResources().getString(R.string.flag_pending) + ": " + downloadChapter.getCurrentPage() + "/" + downloadChapter.getTotalPages());
                } else {
                    mFlagTextView.setText(context.getResources().getString(R.string.flag_pending));
                }
            } else if (downloadChapter.getFlag() == DownloadUtils.FLAG_RUNNING) {
                if (downloadChapter.getTotalPages() != 0) {
                    mFlagTextView.setText(context.getResources().getString(R.string.flag_running_downloading) + ": " + downloadChapter.getCurrentPage() + "/" + downloadChapter.getTotalPages());
                } else {
                    mFlagTextView.setText(context.getResources().getString(R.string.flag_running_fetching));
                }
            } else if (downloadChapter.getFlag() == DownloadUtils.FLAG_COMPLETED) {
                mFlagTextView.setText(context.getResources().getString(R.string.flag_completed));
            } else if (downloadChapter.getFlag() == DownloadUtils.FLAG_CANCELED) {
                mFlagTextView.setText(context.getResources().getString(R.string.flag_canceled));
            }
        }
    }
}
