package org.itishka.pointim.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.itishka.pointim.R;
import org.itishka.pointim.activities.ImageViewActivity;

import java.util.List;

/**
 * Created by Tishka17 on 01.01.2015.
 */
public class ImageList extends FrameLayout {

    private static final int TAG_INDEX = R.id.imageView0;
    SharedPreferences mPreferences;

    private static final int[] sImageIds = new int[]{
            R.id.imageView0,
            R.id.imageView1,
            R.id.imageView2,
            R.id.imageView3,
            R.id.imageView4,
            R.id.imageView5,
            R.id.imageView6,
            R.id.imageView7,
            R.id.imageView8,
            R.id.imageView9
    };

    private final ImageView[] mImageViews = new ImageView[sImageIds.length];
    private String[] mUrls = null;
    private final OnClickListener imageClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) view.getTag(R.id.imageView)));
            browserIntent.putExtra(ImageViewActivity.EXTRA_URLS, mUrls);
            browserIntent.putExtra(ImageViewActivity.EXTRA_INDEX, (int) view.getTag(TAG_INDEX));
            browserIntent.setClass(getContext(), ImageViewActivity.class);
            getContext().startActivity(browserIntent);
        }
    };

    public ImageList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageList(Context context) {
        super(context);
        init();
    }

    public ImageList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPreferences = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        inflate(getContext(), R.layout.image_list, this);
        for (int i = 0; i < sImageIds.length; i++) {
            mImageViews[i] = (ImageView) findViewById(sImageIds[i]);
            mImageViews[i].setVisibility(GONE);
            mImageViews[i].setOnClickListener(imageClickListener);
            mImageViews[i].setTag(TAG_INDEX, i);
        }
    }

    public void setImageUrls(List<String> urls, List<String> files) {
        if (!mPreferences.getBoolean("loadImages", true)) {
            for (int i = 0; i < sImageIds.length; i++)
                mImageViews[i].setVisibility(GONE);
            return;
        }
        int urlCount = urls == null ? 0 : urls.size();
        int fileCount = files == null ? 0 : files.size();
        mUrls = new String[urlCount + fileCount];
        for (int i = 0; i < sImageIds.length; i++) {
            String url = null;
            if (i < urlCount) {
                url = urls.get(i);
            } else if (i - urlCount < fileCount) {
                url = files.get(i - urlCount);
            }
            if (url != null) {
                mUrls[i] = url;
                mImageViews[i].setVisibility(VISIBLE);
                mImageViews[i].setTag(R.id.imageView, url);
                Glide.with(getContext())
                        .load(url)
                        .dontAnimate()
                        .into(mImageViews[i]);
            } else {
                mImageViews[i].setVisibility(GONE);
            }
        }
    }
}
