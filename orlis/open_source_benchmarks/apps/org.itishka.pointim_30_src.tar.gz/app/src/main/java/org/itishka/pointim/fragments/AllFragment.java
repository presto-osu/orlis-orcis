package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class AllFragment extends PostListFragment {
    @Override
    protected PostListRequest createRequest() {
        return new AllRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new AllRequest(before);
    }


    public static class AllRequest extends PostListRequest {
        public AllRequest(long before) {
            super(before);
        }

        public AllRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getAll();
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getAll(before);
        }

    }
}
