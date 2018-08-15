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

import android.net.SSLCertificateSocketFactory;
import android.util.Log;

import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TlsSocketFactory implements LayeredSocketFactory {

    final static HostnameVerifier hostnameVerifier = new StrictHostnameVerifier();
    private boolean mTrustAllCertificates;

    public TlsSocketFactory(boolean trustAllCertificates) {
        mTrustAllCertificates = trustAllCertificates;
    }

    @Override
    public Socket connectSocket(Socket s, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
        return null;
    }

    @Override
    public Socket createSocket() throws IOException {
        return null;
    }

    @Override
    public boolean isSecure(Socket s) throws IllegalArgumentException {
        return s instanceof SSLSocket && ((SSLSocket) s).isConnected();
    }

    @Override
    public Socket createSocket(Socket plainSocket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        // Create and connect SSL socket, but don't do hostname/certificate verification yet
        SSLCertificateSocketFactory sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);

        // Setup custom trust manager if we are trusting all certificates
        if (mTrustAllCertificates) {
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslSocketFactory.setTrustManagers(new TrustManager[] { tm });
        }

        SSLSocket ssl = (SSLSocket)sslSocketFactory.createSocket(InetAddress.getByName(host), port);

        // Enable TLSv1.1/1.2 if available
        // (see https://github.com/rfc2822/davdroid/issues/229)
        ssl.setEnabledProtocols(ssl.getSupportedProtocols());
        SSLSession session = ssl.getSession();

        // Verify hostname and certificate if we aren't trusting all certificates
        if (!mTrustAllCertificates) {
            if (!hostnameVerifier.verify(host, session))
                throw new SSLPeerUnverifiedException("Cannot verify hostname: " + host);
        }

        Log.i("droidbeard", "Established " + session.getProtocol() + " connection with " + session.getPeerHost() + " using " + session.getCipherSuite());
        return ssl;
    }
}