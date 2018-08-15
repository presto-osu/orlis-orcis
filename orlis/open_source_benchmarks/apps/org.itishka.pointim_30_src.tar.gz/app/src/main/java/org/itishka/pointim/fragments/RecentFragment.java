package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class RecentFragment extends PostListFragment {


    @Override
    protected PostListRequest createRequest() {
        return new RecentRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new RecentRequest(before);
    }

    public static class RecentRequest extends PostListRequest {
        public RecentRequest(long before) {
            super(before);
        }

        public RecentRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getRecent();
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getRecent(before);
        }

    }
}
