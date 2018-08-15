package org.itishka.pointim.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * A placeholder fragment containing a simple view.
 */
public class TagViewFragment extends PostListFragment {

    public static final String ARG_USER = "user";
    public static final String ARG_TAG = "tag";
    private String mTag;
    private String mUser;

    public static TagViewFragment newInstance(String user, String tag) {
        TagViewFragment fragment = new TagViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        args.putString(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString(ARG_TAG);
        mUser = getArguments().getString(ARG_USER);
    }

    @Override
    protected PostListRequest createRequest() {
        return new TagRequest(mUser, mTag);
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new TagRequest(mUser, before, mTag);
    }

    public static class TagRequest extends PostListRequest {
        private final String mTag;
        private final String mUser;

        public TagRequest(String user, long before, String tag) {
            super(before);
            mUser = user;
            mTag = tag;
        }

        public TagRequest(String user, String tag) {
            super();
            mUser = user;
            mTag = tag;
        }

        @Override
        public String getCacheName() {
            return super.getCacheName() + "-" + ((mUser == null) ? "" : mUser) + "-" + mTag;
        }

        @Override
        public PostList load() throws Exception {
            if (TextUtils.isEmpty(mUser))
                return getService().getPostsByTag(mTag);
            else
                return getService().getPostsByUserTag(mUser, mTag);
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            if (TextUtils.isEmpty(mUser))
                return getService().getPostsByTag(before, mTag);
            else
                return getService().getPostsByUserTag(before, mUser, mTag);
        }

    }
}
