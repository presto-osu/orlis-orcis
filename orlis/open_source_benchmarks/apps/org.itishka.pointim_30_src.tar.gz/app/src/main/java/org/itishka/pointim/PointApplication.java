package org.itishka.pointim;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.itishka.pointim.network.ImgurConnectionManager;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.utils.ImageSearchHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tishka17 on 03.08.2015.
 */
public class PointApplication extends Application {
    private OkHttpClient mOkHttpClient;
    String USER_AGENT = "Tishka17 Point.im Client";
    private static final int MAX_CACHE_SIZE = 50 * 1024 * 1024; //50 MiB

    public class UserAgentInterceptor implements Interceptor {

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.interceptors().add(new UserAgentInterceptor());
        mOkHttpClient.setCache(new Cache(getExternalCacheDir(), MAX_CACHE_SIZE));

        Glide
                .get(this)
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mOkHttpClient));
        ImageSearchHelper.initCache(this);
        PointConnectionManager.getInstance().init(this);
        ImgurConnectionManager.getInstance().init(this);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
