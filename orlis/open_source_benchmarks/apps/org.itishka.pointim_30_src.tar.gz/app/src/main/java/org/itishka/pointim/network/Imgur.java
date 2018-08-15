package org.itishka.pointim.network;

import org.itishka.pointim.model.imgur.Image;
import org.itishka.pointim.model.imgur.UploadResult;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

/**
 * Created by Tishka17 on 30.12.2014.
 */
public interface Imgur {
    @Multipart
    @POST("/upload")
    UploadResult uploadFile(@Part("image") TypedFile resource);


    @GET("/image/{id}")
    Image getImageInfo(@Path("id") String id);

    @DELETE("/image/{id}")
    void deleteImage(@Path("id") String id, Callback<Void> callback);
}
