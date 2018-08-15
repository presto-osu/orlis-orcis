package org.itishka.pointim.network;

import org.itishka.pointim.model.point.LoginResult;

import retrofit.RequestInterceptor;

/**
 * Created by Tishka17 on 04.02.2015.
 */
public class PointImRequestInterceptor implements RequestInterceptor {
    private LoginResult mLoginResult;

    PointImRequestInterceptor() {
    }

    public void setAuthorization(LoginResult loginResult) {
        mLoginResult = loginResult;
    }

    @Override
    public void intercept(RequestInterceptor.RequestFacade requestFacade) {
        requestFacade.addHeader("Authorization", mLoginResult.token);
        requestFacade.addHeader("X-CSRF", mLoginResult.csrf_token);
    }
}
