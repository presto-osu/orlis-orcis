package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.Chapter;

public class MarkReadOnlineAdapter extends BaseCursorAdapter {
    public MarkReadOnlineAdapter(Context context) {
        super(context, Chapter.class);
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
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_mark_read, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        Chapter currentChapter = (Chapter) getItem(position);
        viewHolder.renderView(currentChapter);

        return currentView;
    }

    private static class ViewHolder {
        private CheckedTextView mCheckedTextView;

        public ViewHolder(View itemView) {
            mCheckedTextView = (CheckedTextView) itemView.findViewById(R.id.checkedTextView);
        }

        public void renderView(Chapter chapter) {
            mCheckedTextView.setText(chapter.getName());
        }
    }
}
