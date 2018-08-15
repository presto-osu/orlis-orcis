package org.itishka.pointim.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import org.itishka.pointim.model.point.UserList;
import org.itishka.pointim.network.PointIm;

/**
 * Created by Tishka17 on 06.08.2015.
 */
public class UserSubscriptionsRequest extends RetrofitSpiceRequest<UserList, PointIm> {

    private final String mUserName;

    public UserSubscriptionsRequest(String userName) {
        super(UserList.class, PointIm.class);
        mUserName = userName;
    }

    @Override
    final public UserList loadDataFromNetwork() throws Exception {
        return getService().getUserSubscriptions(mUserName);
    }

    public String getCacheName() {
        return getClass().getCanonicalName() + "-" + mUserName;
    }
}
