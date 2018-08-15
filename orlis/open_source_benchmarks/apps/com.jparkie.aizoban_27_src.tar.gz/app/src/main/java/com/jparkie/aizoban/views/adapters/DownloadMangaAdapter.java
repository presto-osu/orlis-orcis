package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.downloads.DownloadManga;
import com.makeramen.RoundedImageView;

public class DownloadMangaAdapter extends BaseCursorAdapter {
    public DownloadMangaAdapter(Context context) {
        super(context, DownloadManga.class);
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
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_download_manga, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        DownloadManga currentDownloadManga = (DownloadManga) getItem(position);
        viewHolder.renderView(mContext, currentDownloadManga);

        return currentView;
    }

    private static class ViewHolder {
        private TextView mNameTextView;
        private TextView mSourceTextView;
        private RoundedImageView mThumbnailImageView;

        public ViewHolder(View itemView) {
            mNameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            mSourceTextView = (TextView) itemView.findViewById(R.id.sourceTextView);
            mThumbnailImageView = (RoundedImageView) itemView.findViewById(R.id.avatarImageView);
        }

        public void renderView(Context context, DownloadManga downloadManga) {
            setName(downloadManga.getName());
            setSource(downloadManga.getSource());
            setThumbnail(context, downloadManga.getThumbnailUrl(), context.getResources().getColor(R.color.accentPinkA200));
        }

        private void setThumbnail(Context context, String thumbnailUrl, int defaultColor) {
            mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);
            mThumbnailImageView.setColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY);

            Drawable placeHolderDrawable = context.getResources().getDrawable(R.drawable.ic_image_white_48dp);
            Drawable errorHolderDrawable = context.getResources().getDrawable(R.drawable.ic_error_white_48dp);

            Glide.with(context)
                    .load(thumbnailUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(android.R.anim.fade_in)
                    .placeholder(placeHolderDrawable)
                    .error(errorHolderDrawable)
                    .fitCenter()
                    .into(new GlideDrawableImageViewTarget(mThumbnailImageView) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);

                            mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            mThumbnailImageView.setColorFilter(null);
                        }
                    });
        }

        private void setName(String name) {
            mNameTextView.setText(name);
        }

        private void setSource(String source) {
            mSourceTextView.setText(source);
        }
    }
}
