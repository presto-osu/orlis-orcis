package org.itishka.pointim.fragments;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.network.requests.PostListRequest;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class SelfFragment extends PostListFragment {

    @Override
    protected PostListRequest createRequest() {
        return new SelfRequest();
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new SelfRequest(before);
    }


    public static class SelfRequest extends PostListRequest {
        public SelfRequest(long before) {
            super(before);
        }

        public SelfRequest() {
            super();
        }

        @Override
        public PostList load() throws Exception {
            return getService().getBlog(PointConnectionManager.getInstance().loginResult.login);
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getBlog(before, PointConnectionManager.getInstance().loginResult.login);
        }

    }
}
