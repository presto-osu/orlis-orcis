/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Pair;

import com.rastating.droidbeard.Application;
import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLHandshakeException;

public abstract class SickbeardAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private Context mContext;
    private List<ApiResponseListener<Result>> mResponseListeners;
    private List<AsyncTaskCompleteListener> mCompleteListeners;
    private SickBeardException mLastException;

    public SickbeardAsyncTask(Context context) {
        mContext = context;
        mResponseListeners = new ArrayList<ApiResponseListener<Result>>();
        mCompleteListeners = new ArrayList<AsyncTaskCompleteListener>();
    }

    public void addResponseListener(ApiResponseListener<Result> listener) {
        if (!mResponseListeners.contains(listener)) {
            mResponseListeners.add(listener);
        }
    }

    public void addCompleteListener(AsyncTaskCompleteListener listener) {
        mCompleteListeners.add(listener);
    }

    public void removeResponseListener(ApiResponseListener<Result> listener) {
        mResponseListeners.remove(listener);
    }

    protected Bitmap getDefaultBanner() {
        return BitmapFactory.decodeResource(Application.getContext().getResources(), R.drawable.banner);
    }

    protected Bitmap getShowBanner(long tvdbid, int cachedInSB) {
        Bitmap banner;
        File cachedBanner = new File(getContext().getCacheDir(), String.valueOf(tvdbid) + ".png");
        if (cachedBanner.exists() && !cachedBanner.isDirectory()) {
            banner = BitmapFactory.decodeFile(cachedBanner.getAbsolutePath());
        }
        else {
            ArrayList<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>();
            params.add(new Pair<String, Object>("tvdbid", tvdbid));
            banner = getBitmap("show.getbanner", params);

            if (cachedInSB == 1) {
                if (banner != null) {
                    try {
                        FileOutputStream stream = new FileOutputStream(cachedBanner);
                        banner.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.flush();
                        stream.close();
                    }
                    catch (Exception e) {
                        banner = getDefaultBanner();
                    }
                }
            }

            if (banner == null) {
                banner = getDefaultBanner();
            }
        }

        return banner;
    }

    protected Bitmap getBitmap(String cmd, List<Pair<String, Object>> params) {
        Bitmap bitmap = null;
        Preferences preferences = new Preferences(mContext);
        String format = "%sapi/%s/?cmd=%s";

        String uri = String.format(format, preferences.getSickbeardUrl(), preferences.getApiKey(), cmd);
        if (params != null) {
            for (Pair<String, Object> pair : params) {
                uri += "&" + pair.first + "=" + pair.second.toString();
            }
        }

        try {
            HttpClient client = HttpClientManager.INSTANCE.getClient();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();

            if (status.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } catch (MalformedURLException e) {
            mLastException = new SickBeardException("", e);
            e.printStackTrace();
        } catch (IOException e) {
            mLastException = new SickBeardException("", e);
            e.printStackTrace();
        }

        return bitmap;
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getJson(String cmd) throws SSLHandshakeException {
        return getJson(cmd, null);
    }

    protected String getJson(String cmd, String paramKey, Object paramValue) {
        List<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>();
        params.add(new Pair<String, Object>(paramKey, paramValue));
        return getJson(cmd, params);
    }

    protected String getJson(String cmd, List<Pair<String, Object>> params) {
        String uri = null;
        String body = null;
        String format = "%sapi/%s/?cmd=%s";
        Preferences preferences = new Preferences(mContext);
        HttpClient client = HttpClientManager.INSTANCE.getClient();

        uri = String.format(format, preferences.getSickbeardUrl(), preferences.getApiKey(), cmd);
        if (params != null) {
            for (Pair<String, Object> pair : params) {
                uri += "&" + pair.first + "=" + pair.second.toString();
            }
        }

        try {
            HttpGet request = new HttpGet(uri);
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();

            if (status.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                HttpEntity entity = response.getEntity();
                entity.writeTo(stream);
                stream.close();
                entity.consumeContent();
                body = stream.toString();
                stream.close();
            } else {
                HttpEntity entity = response.getEntity();
                entity.getContent().close();
                entity.consumeContent();
                throw new IOException(status.getReasonPhrase());
            }
        }
        catch (SSLHandshakeException e) {
            setLastException("", e);
            return null;
        }
        catch (Exception e) {
            setLastException("", e);
            e.printStackTrace();
            return null;
        }

        return body;
    }

    protected List<ApiResponseListener<Result>> getResponseListeners() {
        return mResponseListeners;
    }

    @Override
    protected void onPostExecute(Result result) {
        List<ApiResponseListener<Result>> listeners = getResponseListeners();
        for (ApiResponseListener<Result> listener : listeners) {
            listener.onApiRequestFinished(this, result);
        }

        for (AsyncTaskCompleteListener listener : mCompleteListeners) {
            listener.onAsyncTaskComplete(this);
        }
    }

    public void start(Executor executor, Params... args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
        }
        else {
            this.execute(args);
        }
    }

    public void start(Params... args) {
        this.start(AsyncTask.THREAD_POOL_EXECUTOR, args);
    }

    protected void setLastException(String data, Exception e) {
        mLastException = new SickBeardException(data, e);
    }

    public SickBeardException getLastException() {
        return mLastException;
    }
}