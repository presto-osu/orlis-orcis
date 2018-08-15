package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class BookmarksFragment extends PostListFragment {

    @Override
    protected PostListRequest createRequest() {
        return new BookmarksRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new BookmarksRequest(before);
    }


    public static class BookmarksRequest extends PostListRequest {
        public BookmarksRequest(long before) {
            super(before);
        }

        public BookmarksRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getBookmarks();
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getBookmarks(before);
        }

    }
}
