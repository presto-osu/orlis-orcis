package org.itishka.pointim.network;

import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.model.point.ExtendedUser;
import org.itishka.pointim.model.point.NewPostResponse;
import org.itishka.pointim.model.point.PointResult;
import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.model.point.TagList;
import org.itishka.pointim.model.point.UserList;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by atikhonov on 28.04.2014.
 */
public interface PointIm {
    @GET("/api/all")
    PostList getAll();

    @GET("/api/all")
    PostList getAll(@Query("before") long before);

    @GET("/api/bookmarks")
    PostList getBookmarks();

    @GET("/api/bookmarks")
    PostList getBookmarks(@Query("before") long before);

    @GET("/api/recent")
    PostList getRecent();

    @GET("/api/recent")
    PostList getRecent(@Query("before") long before);

    @GET("/api/comments")
    PostList getCommented();

    @GET("/api/comments")
    PostList getCommented(@Query("before") long before);

    @GET("/api/messages/incoming")
    PostList getIncoming();

    @GET("/api/messages/incoming")
    PostList getIncoming(@Query("before") long before);

    @GET("/api/messages/outgoing")
    PostList getOutgoing();

    @GET("/api/messages/outgoing")
    PostList getOutgoing(@Query("before") long before);

    @GET("/api/blog/login/{login}")
    PostList getBlog(@Path("login") String login);

    @GET("/api/blog/login/{login}")
    PostList getBlog(@Query("before") long before, @Path("login") String login);

    @GET("/api/user/login/{login}")
    ExtendedUser getUserInfo(@Path("login") String login);

    @GET("/api/user/login/{login}/subscriptions")
    UserList getUserSubscriptions(@Path("login") String login);

    @FormUrlEncoded
    @POST("/api/user/s/{login}")
    void subscribeUser(@Path("login") String login, @Field("text") String text, Callback<Void> callback);

    @DELETE("/api/user/s/{login}")
    void unsubscribeUser(@Path("login") String login, Callback<PointResult> callback);

    @FormUrlEncoded
    @POST("/api/user/sr/{login}")
    void subscribeUserRecommendations(@Path("login") String login, @Field("text") String text, Callback<Void> callback);

    @DELETE("/api/user/sr/{login}")
    void unsubscribeUserRecommendations(@Path("login") String login, Callback<PointResult> callback);

    @GET("/api/tags/login/{login}")
    TagList getTags(@Path("login") String login);

    @GET("/api/tags")
    PostList getPostsByTag(@Query("tag") String tag);

    @GET("/api/tags")
    PostList getPostsByTag(@Query("before") long before, @Query("tag") String tag);

    @GET("/api/tags/login/{login}")
    PostList getPostsByUserTag(@Path("login") String login, @Query("tag") String tag);

    @GET("/api/tags/login/{login}")
    PostList getPostsByUserTag(@Query("before") long before, @Path("login") String login, @Query("tag") String tag);

    @GET("/api/post/{id}")
    Post getPost(@Path("id") String id);

    @FormUrlEncoded
    @POST("/api/post/{id}/b")
    void addBookmark(@Path("id") String id, @Field("text") String text, Callback<PointResult> callback);

    @DELETE("/api/post/{id}/b")
    void deleteBookmark(@Path("id") String id, Callback<Void> callback);

    @FormUrlEncoded
    @POST("/api/post/{id}")
    void addComment(@Path("id") String id, @Field("text") String text, Callback<PointResult> callback);

    @FormUrlEncoded
    @POST("/api/post/{id}")
    void addComment(@Path("id") String id, @Field("text") String text, @Field("comment_id") String commentId, Callback<PointResult> callback);

    @FormUrlEncoded
    @POST("/api/post/{id}/r")
    void recommend(@Path("id") String id, @Field("text") String text, Callback<PointResult> callback);

    @DELETE("/api/post/{id}/r")
    void notRecommend(@Path("id") String id, Callback<PointResult> callback);

    @FormUrlEncoded
    @POST("/api/post/{id}/{cid}/r")
    void recommendCommend(@Path("id") String id, @Path("cid") long cid, @Field("text") String text, Callback<PointResult> callback);

    @DELETE("/api/post/{id}/{cid}/r")
    void notRecommendComment(@Path("id") String id, @Path("cid") long cid, Callback<PointResult> callback);

    @FormUrlEncoded
    @POST("/api/post/")
    void createPost(@Field("text") String text, @Field("tag") String[] tags, Callback<NewPostResponse> callback);

    @FormUrlEncoded
    @POST("/api/post/")
    void createPrivatePost(@Field("text") String text, @Field("tag") String[] tags, @Field("private") boolean reserved, Callback<NewPostResponse> callback);

    @FormUrlEncoded
    @PUT("/api/post/{id}")
    void editPost(@Path("id") String id, @Field("text") String text, @Field("tag") String[] tags, Callback<NewPostResponse> callback);

    @DELETE("/api/post/{id}")
    void deletePost(@Path("id") String id, Callback<PointResult> callback);

    @DELETE("/api/post/{id}/{cid}")
    void deleteComment(@Path("id") String id, @Path("cid") long cid, Callback<PointResult> callback);
}
