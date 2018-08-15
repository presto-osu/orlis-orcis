package org.itishka.pointim.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.network.PointIm;

/**
 * Created by Tishka17 on 08.02.2015.
 */
public class SinglePostRequest extends RetrofitSpiceRequest<Post, PointIm> {
    private final String mPostId;

    public SinglePostRequest(String postId) {
        super(Post.class, PointIm.class);
        mPostId = postId;
    }

    @Override
    final public Post loadDataFromNetwork() throws Exception {
        return getService().getPost(mPostId);
    }

    public String getCacheName() {
        return getClass().getCanonicalName() + "-" + mPostId;
    }
}
