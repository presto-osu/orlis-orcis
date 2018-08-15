package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.controllers.databases.LibraryContract;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.PaletteBitmapTarget;
import com.jparkie.aizoban.utils.PaletteBitmapTranscoder;
import com.jparkie.aizoban.utils.PaletteUtils;
import com.jparkie.aizoban.utils.wrappers.PaletteBitmapWrapper;

public class LatestMangaAdapter extends BaseCursorAdapter {
    private OnLatestPositionListener mOnLatestPositionListener;

    public LatestMangaAdapter(Context context) {
        super(context, Manga.class);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mCursor == null) {
            throw new IllegalStateException("Null Cursor");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Can't Move Cursor to Position " + position);
        }

        if (mOnLatestPositionListener != null) {
            mOnLatestPositionListener.onLatestPosition(position);
        }

        ViewHolder viewHolder;
        View currentView = convertView;

        if (currentView == null) {
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_latest_manga, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        Manga currentManga = new Manga();
        currentManga.setName(mCursor.getString(mCursor.getColumnIndex(LibraryContract.Manga.COLUMN_NAME)));
        currentManga.setThumbnailUrl(mCursor.getString(mCursor.getColumnIndex(LibraryContract.Manga.COLUMN_THUMBNAIL_URL)));
        currentManga.setUpdateCount(mCursor.getInt(mCursor.getColumnIndex(LibraryContract.Manga.COLUMN_UPDATE_COUNT)));

        viewHolder.renderView(mContext, currentManga);

        return currentView;
    }

    public void setOnLatestPositionListener(OnLatestPositionListener onLatestPositionListener) {
        mOnLatestPositionListener = onLatestPositionListener;
    }

    public interface OnLatestPositionListener {
        public void onLatestPosition(int position);
    }

    private static class ViewHolder {
        private ImageView mThumbnailImageView;
        private View mMaskView;
        private TextView mNumberTextView;
        private TextView mNameTextView;
        private LinearLayout mFooterView;

        private int mDefaultPrimary = -1;
        private int mDefaultAccent = -1;
        private Drawable mPlaceHolderDrawable;
        private Drawable mErrorHolderDrawable;

        public ViewHolder(View itemView) {
            mThumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnailImageView);
            mMaskView = itemView.findViewById(R.id.maskImageView);
            mNumberTextView = (TextView) itemView.findViewById(R.id.numberTextView);
            mNameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            mFooterView = (LinearLayout) itemView.findViewById(R.id.footerLinearLayout);
        }

        public void renderView(Context context, Manga manga) {
            if (mDefaultPrimary < 0) {
                mDefaultPrimary = context.getResources().getColor(R.color.primaryBlue500);
            }
            if (mDefaultAccent < 0) {
                mDefaultAccent = context.getResources().getColor(R.color.accentPinkA200);
            }
            if (mPlaceHolderDrawable == null) {
                mPlaceHolderDrawable = context.getResources().getDrawable(R.drawable.ic_image_white_48dp);
                mPlaceHolderDrawable.setColorFilter(mDefaultAccent, PorterDuff.Mode.MULTIPLY);
            }
            if (mErrorHolderDrawable == null) {
                mErrorHolderDrawable = context.getResources().getDrawable(R.drawable.ic_error_white_48dp);
                mErrorHolderDrawable.setColorFilter(mDefaultAccent, PorterDuff.Mode.MULTIPLY);
            }

            setName(manga.getName());
            setNumber(manga.getUpdateCount());
            setMask(mDefaultPrimary);
            setFooter(mDefaultPrimary);
            setThumbnail(context, manga.getThumbnailUrl(), mDefaultAccent);
        }

        private void setThumbnail(Context context, String thumbnailUrl, final int defaultColor) {
            mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

            Glide.with(context)
                    .load(thumbnailUrl)
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(), PaletteBitmapWrapper.class)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(android.R.anim.fade_in)
                    .placeholder(mPlaceHolderDrawable)
                    .error(mErrorHolderDrawable)
                    .fitCenter()
                    .into(new PaletteBitmapTarget(mThumbnailImageView) {
                        @Override
                        public void onResourceReady(PaletteBitmapWrapper resource, GlideAnimation<? super PaletteBitmapWrapper> glideAnimation) {
                            mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            super.onResourceReady(resource, glideAnimation);

                            int color = PaletteUtils.getColorWithDefault(resource.getPalette(), defaultColor);
                            setMask(color);
                            setFooter(color);
                        }
                    });
        }

        private void setMask(int color) {
            GradientDrawable maskDrawable = new GradientDrawable();
            maskDrawable.setColor(color);
            mMaskView.setBackgroundDrawable(maskDrawable);
        }

        private void setNumber(int number) {
            mNumberTextView.setText(Integer.toString(number));
        }

        private void setName(String name) {
            mNameTextView.setText(name);
        }

        private void setFooter(int color) {
            GradientDrawable footerDrawable = new GradientDrawable();
            footerDrawable.setCornerRadii(new float[]{0.0f, 0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f, 4.0f});
            footerDrawable.setColor(color);
            mFooterView.setBackgroundDrawable(footerDrawable);
        }
    }
}
