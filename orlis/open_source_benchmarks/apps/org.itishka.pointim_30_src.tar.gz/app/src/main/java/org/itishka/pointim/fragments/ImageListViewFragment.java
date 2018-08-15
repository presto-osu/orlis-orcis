package org.itishka.pointim.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.itishka.pointim.R;

public class ImageListViewFragment extends SpicedFragment {
    private static final String ARG_URLS = "urls";
    private static final String ARG_INDEX = "index";

    // TODO: Rename and change types of parameters
    private String[] mUrls;
    private int mIndex;

    private ViewPager mPager;

    public ImageListViewFragment() {
        // Required empty public constructor
    }

    public static ImageListViewFragment newInstance(String[] urls, int index) {
        ImageListViewFragment fragment = new ImageListViewFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_URLS, urls);
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_INDEX, mIndex);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrls = getArguments().getStringArray(ARG_URLS);
            mIndex = getArguments().getInt(ARG_INDEX);
        }
        if (savedInstanceState != null) {
            mIndex = savedInstanceState.getInt(ARG_INDEX, mIndex);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ImageViewFragment.newInstance(mUrls[position]);
            }

            @Override
            public int getCount() {
                return mUrls.length;
            }
        });
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mIndex = position;
                updateTitle();
            }
        });
        mPager.setCurrentItem(mIndex);
        mPager.setOffscreenPageLimit(3);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTitle();
    }

    private void updateTitle() {
        if (!isDetached()) {
            if (mUrls == null || mUrls.length == 0)
                getActivity().setTitle("[]");
            else
                getActivity().setTitle(String.format("[%s/%s] %s", mIndex + 1, mUrls.length, mUrls[mIndex]));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_imagelist_view, container, false);
    }
}
