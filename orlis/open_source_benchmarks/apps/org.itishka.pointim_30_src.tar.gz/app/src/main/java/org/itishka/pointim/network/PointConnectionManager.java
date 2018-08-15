package org.itishka.pointim.network;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.itishka.pointim.PointApplication;
import org.itishka.pointim.model.point.LoginResult;
import org.itishka.pointim.model.point.TextWithImages;
import org.itishka.pointim.utils.DateDeserializer;
import org.itishka.pointim.utils.TextParser;

import java.util.Date;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class PointConnectionManager extends ConnectionManager {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ENDPOINT = "https://point.im";
    private static final String PREFERENCE = "PointConnectionManager";
    private static final PointConnectionManager sInstance = new PointConnectionManager();
    private final Gson mGson = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .registerTypeAdapter(TextWithImages.class, new TextParser())
            .create();
    public PointIm pointIm = null;
    public PointImAuth pointAuthService = null;
    public LoginResult loginResult = null;

    private PointConnectionManager() {
    }

    public static PointConnectionManager getInstance() {
        return sInstance;
    }

    @Override
    protected Gson getGson() {
        return mGson;
    }

    @Override
    public void updateAuthorization(Context context, Object loginResult) {
        synchronized (this) {
            this.loginResult = (LoginResult) loginResult;
            saveAuthorization(context, PREFERENCE, loginResult);
            createService();
        }
    }

    @Override
    public void init(PointApplication application) {
        super.init(application);


        RestAdapter authRestAdapter = new RestAdapter.Builder()
                .setClient(getOkClient())
                .setEndpoint(ENDPOINT)
                .setConverter(new GsonConverter(mGson))
                .build();
        pointAuthService = authRestAdapter.create(PointImAuth.class);

        synchronized (this) {
            if (this.loginResult == null) {
                loginResult = loadAuthorization(application, PREFERENCE, LoginResult.class);
                createService();
            }
        }
    }

    @Override
    protected void createService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        requestFacade.addHeader("Authorization", loginResult.token);
                        requestFacade.addHeader("X-CSRF", loginResult.csrf_token);
                    }
                })
                .setClient(getOkClient())
                .setEndpoint(ENDPOINT)
                .setConverter(new GsonConverter(mGson))
                .build();
        pointIm = restAdapter.create(PointIm.class);
    }

    @Override
    synchronized public boolean isAuthorized() {
        return loginResult != null && !TextUtils.isEmpty(loginResult.csrf_token);
    }

    @Override
    public void resetAuthorization(Context context) {
        synchronized (this) {
            loginResult = null;
            saveAuthorization(context, PREFERENCE, loginResult);
            init((PointApplication) context.getApplicationContext());
        }
    }
}
