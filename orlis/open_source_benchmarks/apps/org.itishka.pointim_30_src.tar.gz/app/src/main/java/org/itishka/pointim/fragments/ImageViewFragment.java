package org.itishka.pointim.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.itishka.pointim.R;
import org.itishka.pointim.activities.ToolbarActivity;
import org.itishka.pointim.widgets.HideAnimationHelper;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewFragment extends SpicedFragment {

    private static final String ARG_URL = "url";
    private String mUrl;
    private ImageView mImageView;
    private HideAnimationHelper mHideAnimationHelper;
    private PhotoViewAttacher mAttacher;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    public ImageViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_view, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                mAttacher.update();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAttacher.cleanup();
        mImageView.setImageDrawable(null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mAttacher = new PhotoViewAttacher(mImageView);
        Glide.with(this)
                .load(mUrl)
                .fitCenter()
                .into(new GlideDrawableImageViewTarget(mImageView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        mAttacher.update();
                    }
                });

        mHideAnimationHelper = new HideAnimationHelper(((ToolbarActivity) getActivity()).getToolbar());
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                mHideAnimationHelper.toggleView();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
        }
        setHasOptionsMenu(true);
    }

    public static Fragment newInstance(String url) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_image_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.weblink) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            startActivity(browserIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
