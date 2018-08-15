package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class OutgoingFragment extends PostListFragment {


    @Override
    protected PostListRequest createRequest() {
        return new OutgoingRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new OutgoingRequest(before);
    }

    public static class OutgoingRequest extends PostListRequest {
        public OutgoingRequest(long before) {
            super(before);
        }

        public OutgoingRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getOutgoing();
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getOutgoing(before);
        }

    }
}
