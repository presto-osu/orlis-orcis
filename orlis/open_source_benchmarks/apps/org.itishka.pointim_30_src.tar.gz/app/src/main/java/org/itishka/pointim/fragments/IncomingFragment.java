package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class IncomingFragment extends PostListFragment {


    @Override
    protected PostListRequest createRequest() {
        return new IncomingRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new IncomingRequest(before);
    }

    public static class IncomingRequest extends PostListRequest {
        public IncomingRequest(long before) {
            super(before);
        }

        public IncomingRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getIncoming();
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getIncoming(before);
        }

    }
}
