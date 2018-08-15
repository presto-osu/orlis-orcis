package com.jparkie.aizoban.views.fragments;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.utils.FitRenderBoundsTransformation;
import com.jparkie.aizoban.views.widgets.GestureImageView;

public class PageFragment extends Fragment {
    public static final String TAG = PageFragment.class.getSimpleName();

    public static final String URL_ARGUMENT_KEY = TAG + ":" + "UrlArgumentKey";
    public static final String POSITION_ARGUMENT_KEY = TAG + ":" + "PositionArgumentKey";

    private GestureImageView mGestureImageView;

    private String mUrl;
    private int mPosition;

    public static PageFragment newInstance(String url, int position) {
        PageFragment newInstance = new PageFragment();

        Bundle arguments = new Bundle();
        arguments.putString(URL_ARGUMENT_KEY, url);
        arguments.putInt(POSITION_ARGUMENT_KEY, position);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(URL_ARGUMENT_KEY)) {
                mUrl = arguments.getString(URL_ARGUMENT_KEY);
            }
            if (arguments.containsKey(POSITION_ARGUMENT_KEY)) {
                mPosition = arguments.getInt(POSITION_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageView = inflater.inflate(R.layout.fragment_page, container, false);

        mGestureImageView = (GestureImageView) pageView.findViewById(R.id.gestureImageView);

        return pageView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGestureImageView.setScaleType(ImageView.ScaleType.CENTER);

        Drawable placeHolderDrawable = getResources().getDrawable(R.drawable.ic_image_white_48dp);
        placeHolderDrawable.setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
        Drawable errorHolderDrawable = getResources().getDrawable(R.drawable.ic_error_white_48dp);
        errorHolderDrawable.setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);

        Glide.with(this)
                .load(mUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeHolderDrawable)
                .error(errorHolderDrawable)
                .animate(android.R.anim.fade_in)
                .transform(new FitRenderBoundsTransformation(getActivity(), mGestureImageView))
                .into(new GlideDrawableImageViewTarget(mGestureImageView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);

                        mGestureImageView.setTag(PageFragment.TAG + ":" + mPosition);
                        mGestureImageView.initializeView();
                    }
                });
    }
}
