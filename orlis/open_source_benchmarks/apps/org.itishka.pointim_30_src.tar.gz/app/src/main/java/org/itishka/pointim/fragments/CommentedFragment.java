package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class CommentedFragment extends PostListFragment {

    @Override
    protected PostListRequest createRequest() {
        return new CommentedRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new CommentedRequest(before);
    }

    public static class CommentedRequest extends PostListRequest {
        public CommentedRequest(long before) {
            super(before);
        }

        public CommentedRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getCommented();
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getCommented(before);
        }

    }
}
