package org.itishka.pointim.network;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import org.itishka.pointim.PointApplication;

import retrofit.client.OkClient;

/**
 * Created by Tishka17 on 31.08.2015.
 */
public abstract class ConnectionManager {
    private static final String KEY = "AuthorizationData";
    private OkClient mOkClient;

    protected abstract Gson getGson();

    public abstract void updateAuthorization(Context context, Object loginResult);

    @CallSuper
    public void init(PointApplication application) {
        mOkClient = new OkClient(application.getOkHttpClient());
    }

    public abstract boolean isAuthorized();

    public abstract void resetAuthorization(Context context);

    protected abstract void createService();

    protected void saveAuthorization(Context context, String preference, @Nullable Object data) {
        context.getSharedPreferences(preference, Context.MODE_PRIVATE).edit().putString(KEY, getGson().toJson(data)).commit();
    }

    protected <T> T loadAuthorization(Context context, String preference, Class<T> clazz) {
        String v = context.getSharedPreferences(preference, Context.MODE_PRIVATE).getString(KEY, null);
        if (v == null) {
            return null;
        } else {
            return getGson().fromJson(v, clazz);
        }
    }

    public OkClient getOkClient() {
        return mOkClient;
    }
}
