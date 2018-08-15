package org.itishka.pointim.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import org.itishka.pointim.model.point.ExtendedUser;
import org.itishka.pointim.network.PointIm;

/**
 * Created by Tishka17 on 08.02.2015.
 */
public class UserInfoRequest extends RetrofitSpiceRequest<ExtendedUser, PointIm> {
    private final String mUserName;

    public UserInfoRequest(String userName) {
        super(ExtendedUser.class, PointIm.class);
        mUserName = userName;
    }

    @Override
    final public ExtendedUser loadDataFromNetwork() throws Exception {
        return getService().getUserInfo(mUserName);
    }

    public String getCacheName() {
        return getClass().getCanonicalName() + "-" + mUserName;
    }
}
