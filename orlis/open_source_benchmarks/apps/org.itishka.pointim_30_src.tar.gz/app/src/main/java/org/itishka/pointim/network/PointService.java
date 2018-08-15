package org.itishka.pointim.network;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

import org.itishka.pointim.model.point.TextWithImages;
import org.itishka.pointim.utils.DateDeserializer;
import org.itishka.pointim.utils.TextParser;
import org.itishka.pointim.utils.TextSerializer;

import java.util.Date;

import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

public class PointService extends RetrofitGsonSpiceService {
    public static final String BASE_URL = "https://point.im";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private PointImRequestInterceptor mRequestInterceptor = new PointImRequestInterceptor();

    public PointService() {
    }

    @Override
    protected Converter createConverter() {
        Gson gson = new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(TextWithImages.class, new TextParser())
                .registerTypeAdapter(TextWithImages.class, new TextSerializer())
                .create();
        return new GsonConverter(gson);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //FIXME
        mRequestInterceptor.setAuthorization(PointConnectionManager.getInstance().loginResult);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {
        return super.createRestAdapterBuilder()
                .setRequestInterceptor(mRequestInterceptor);
    }

    @Override
    public int getThreadCount() {
        return 2;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(PointIm.class);
    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }
}
