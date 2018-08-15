package org.itishka.pointim.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.PointIm;

/**
 * Created by Tishka17 on 08.02.2015.
 */
public abstract class PostListRequest extends RetrofitSpiceRequest<PostList, PointIm> {
    private final long mBefore;

    public PostListRequest() {
        super(PostList.class, PointIm.class);
        mBefore = 0;
    }

    public PostListRequest(long before) {
        super(PostList.class, PointIm.class);
        mBefore = before;
    }

    @Override
    final public PostList loadDataFromNetwork() throws Exception {
        if (mBefore == 0)
            return load();
        else
            return loadBefore(mBefore);
    }

    public abstract PostList load() throws Exception;

    public abstract PostList loadBefore(long before) throws Exception;

    public String getCacheName() {
        return getClass().getCanonicalName() + "-" + mBefore;
    }
}
