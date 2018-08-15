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

import com.rastating.droidbeard.Application;
import com.rastating.droidbeard.Preferences;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

public class HttpClientManager {
    private HttpClient mClient;

    public final static HttpClientManager INSTANCE = new HttpClientManager();

    private HttpClientManager() {
        invalidateClient();
    }

    public HttpClient getClient() {
        return mClient;
    }

    private void setupHttpCredentials() {
        try {
            Preferences preferences = new Preferences(Application.getContext());
            Credentials credentials = new UsernamePasswordCredentials(preferences.getHttpUsername(), preferences.getHttpPassword());
            ((AbstractHttpClient) mClient).getCredentialsProvider().setCredentials(new AuthScope(preferences.getAddress(), preferences.getPort()), credentials);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DefaultHttpClient createThreadSafeClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager manager = client.getConnectionManager();
        HttpParams params = client.getParams();
        ThreadSafeClientConnManager threadSafeManager = new ThreadSafeClientConnManager(params, manager.getSchemeRegistry());
        return new DefaultHttpClient(threadSafeManager, params);
    }

    public void invalidateClient() {
        Preferences preferences = new Preferences(com.rastating.droidbeard.Application.getContext());
        boolean trustAllCertificates = preferences.getTrustAllCertificatesFlag();
        mClient = createThreadSafeClient();
        SchemeRegistry schemeRegistry = mClient.getConnectionManager().getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", new TlsSocketFactory(trustAllCertificates), 443));

        setupHttpCredentials();
    }
}