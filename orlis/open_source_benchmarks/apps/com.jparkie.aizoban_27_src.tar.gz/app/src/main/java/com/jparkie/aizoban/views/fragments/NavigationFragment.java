package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.NavigationPresenter;
import com.jparkie.aizoban.presenters.NavigationPresenterImpl;
import com.jparkie.aizoban.presenters.mapper.NavigationMapper;
import com.jparkie.aizoban.views.NavigationView;

public class NavigationFragment extends Fragment implements NavigationView, NavigationMapper {
    public static final String TAG = NavigationFragment.class.getSimpleName();

    public static final String POSITION_ARGUMENT_KEY = TAG + ":" + "PositionArgumentKey";

    private NavigationPresenter mNavigationPresenter;

    private ListView mListView;

    private View mHeaderView;
    private ImageView mThumbnailImageView;
    private TextView mSourceTextView;

    public static NavigationFragment newInstance(int initialPosition) {
        NavigationFragment newInstance = new NavigationFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(POSITION_ARGUMENT_KEY, initialPosition);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNavigationPresenter = new NavigationPresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View menuView = inflater.inflate(R.layout.fragment_navigation, container, false);

        mListView = (ListView) menuView.findViewById(R.id.listView);

        mHeaderView = inflater.inflate(R.layout.header_navigation, null);
        mThumbnailImageView = (ImageView)mHeaderView.findViewById(R.id.thumbnailImageView);
        mSourceTextView = (TextView)mHeaderView.findViewById(R.id.sourceTextView);

        return menuView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mNavigationPresenter.restoreState(savedInstanceState);
        } else {
            mNavigationPresenter.handleInitialArguments(getArguments());
        }

        mNavigationPresenter.initializeViews();

        mNavigationPresenter.initializeNavigationFromResources();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mNavigationPresenter.destroyAllSubscriptions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mNavigationPresenter.saveState(outState);
    }

    // NavigationView:

    @Override
    public void initializeAbsListView() {
        if (mListView != null) {
            if (mHeaderView != null) {
                mListView.addHeaderView(mHeaderView, null, false);
            }

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int actualPosition = position - mListView.getHeaderViewsCount();

                    mNavigationPresenter.onNavigationItemClick(actualPosition);
                }
            });
        }
    }

    @Override
    public void scrollToTop() {
        // Do Nothing.
    }

    @Override
    public void initializeSourceTextView(String source) {
        if (mSourceTextView != null) {
            mSourceTextView.setText(source.toUpperCase());
        }
    }

    @Override
    public void setThumbnail(String url) {
        if (mThumbnailImageView != null) {
            mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

            Glide.with(getActivity())
                    .load(url)
                    .animate(android.R.anim.fade_in)
                    .into(new GlideDrawableImageViewTarget(mThumbnailImageView) {
                              @Override
                              public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                  super.onResourceReady(resource, animation);
                                  mThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                  mThumbnailImageView.setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
                              }
                          }
                    );
        }
    }

    @Override
    public void highlightPosition(int position) {
        if (mListView != null) {
            mListView.setItemChecked(position + mListView.getHeaderViewsCount(), true);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // NavigationMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }
}
