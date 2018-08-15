package com.jparkie.aizoban.controllers.networks;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;

public class MangaService {
    public static final int CONNECT_TIMEOUT = 10;
    public static final int WRITE_TIMEOUT = 10;
    public static final int READ_TIMEOUT = 30;

    private static MangaService sInstance;

    private OkHttpClient mClient;

    private MangaService() {
        mClient = new OkHttpClient();
        mClient.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        mClient.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        mClient.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
    }

    public static MangaService getPermanentInstance() {
        if (sInstance == null) {
            sInstance = new MangaService();
        }

        return sInstance;
    }

    public static MangaService getTemporaryInstance() {
        return new MangaService();
    }

    public Observable<Response> getResponse(final String url) {
        return Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)")
                            .build();

                    subscriber.onNext(mClient.newCall(request).execute());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<Response> getCustomResponse(final String url, final Headers headers) {
        return Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .headers(headers)
                            .build();

                    subscriber.onNext(mClient.newCall(request).execute());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<String> mapResponseToString(final Response response) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
