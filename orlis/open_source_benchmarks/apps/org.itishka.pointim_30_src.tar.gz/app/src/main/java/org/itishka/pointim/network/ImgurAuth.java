package org.itishka.pointim.network;

import org.itishka.pointim.model.imgur.Token;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by Tishka17 on 29.08.2015.
 */
public interface ImgurAuth {

    @FormUrlEncoded
    @POST("/token")
    void getToken(@Field("client_id") String client_id,
                  @Field("client_secret") String client_secret,
                  @Field("grant_type") String grant_type,
                  @Field("code") String code,
                  Callback<Token> callback);

    @FormUrlEncoded
    @POST("/token")
    Token refreshToken(@Field("client_id") String client_id,
                       @Field("client_secret") String client_secret,
                       @Field("grant_type") String grant_type,
                       @Field("refresh_token") String refresh_token);
}
