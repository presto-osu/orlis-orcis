/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * A special thanks go to Chteuteu as he allow us to re-use parts of his
 * code from Munin-for-Android (https://github.com/chteuchteu/Munin-for-Android)
 *
 *
 */
package rinor;

import android.content.SharedPreferences;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import misc.tracerengine;


public class Rest_com {
    private static final String mytag = "Rest_com";
    private static boolean alreadyTriedAuthenticating = false;

    @SuppressWarnings("null")
    public static JSONObject connect_jsonobject(tracerengine Tracer, String url, final String login, final String password, int timeout, boolean SSL) {

        JSONObject json = null;
        if (!SSL) {
            try {
                // Set timeout
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
                HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
                httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
                HttpGet httpget = new HttpGet(url);
                HttpResponse response;
                String result = null;
                response = httpclient.execute(httpget);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream instream = entity.getContent();
                        result = Abstract.httpsUrl.convertStreamToString(instream);
                        json = new JSONObject(result);
                        instream.close();
                    }
                } else if (response.getStatusLine().getStatusCode() == 204) {
                    //TODO need to adapt for 0.4 since rest answer now with standard code
                    //204,400,404 and else
                    json = new JSONObject();
                    json.put("status", "204 NO CONTENT");
                } else {
                    Tracer.d(mytag, "Resource not available>");
                }
            } catch (UnknownHostException e) {
                Tracer.e(mytag, "Unable to resolve host");
            } catch (ConnectTimeoutException e) {
                Tracer.e(mytag, "Timeout connecting to domogik");
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
            return json;
        } else {
            try {
                Tracer.d(mytag, "Start https connection");
                if (url.startsWith("http://")) {
                    url = url.replace("http://", "https://");
                }
                Tracer.d(mytag, "Url=" + url);
                HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url);
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
                String result = null;
                InputStream instream = urlConnection.getInputStream();
                result = Abstract.httpsUrl.convertStreamToString(instream);
                json = new JSONObject(result);
                instream.close();
                //} catch (HttpHostConnectException e) {
                //    e.printStackTrace();
            } catch (UnknownHostException e) {
                Tracer.e(mytag, "Unable to resolve host");
            } catch (ConnectTimeoutException e) {
                Tracer.e(mytag, "Timeout connecting to domogik");
            } catch (IOException | JSONException e) {
                Tracer.e(mytag, e.toString());
            }
            return json;

        }

    }

    @SuppressWarnings("null")
    public static JSONArray connect_jsonarray(tracerengine Tracer, String url, final String login, final String password, int timeout, boolean SSL) {
        JSONArray json = null;

        if (!SSL) {
            try {
                // Set timeout
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
                HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
                httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
                HttpGet httpget = new HttpGet(url);
                HttpResponse response;
                String result = null;
                response = httpclient.execute(httpget);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream instream = entity.getContent();
                        result = Abstract.httpsUrl.convertStreamToString(instream);
                        json = new JSONArray(result);
                        instream.close();
                    }
                } else {
                    Tracer.d(mytag, "Resource not available>");
                }

            } catch (UnknownHostException e) {
                json = new JSONArray();
                json.put("Error ; Unknown host");
                Tracer.e(mytag, "Unable to resolve host");
            } catch (HttpHostConnectException e) {
                Tracer.e(mytag, e.toString());
            } catch (ConnectTimeoutException e) {
                Tracer.e(mytag, "Timeout connecting to domogik");
            } catch (IOException | JSONException e) {
                Tracer.e(mytag, e.toString());
            }
            return json;
        } else {
            try {
                Tracer.d(mytag, "Start https connection");
                if (url.startsWith("http://")) {
                    url = url.replace("http://", "https://");
                }
                Tracer.d(mytag, "Url=" + url);
                HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url);
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
                String result = null;
                InputStream instream = urlConnection.getInputStream();
                result = Abstract.httpsUrl.convertStreamToString(instream);
                json = new JSONArray(result);
                instream.close();
                //} catch (HttpHostConnectException e) {
                //    e.printStackTrace();
            } catch (UnknownHostException e) {
                Tracer.e(mytag, "Unable to resolve host");
            } catch (ConnectTimeoutException e) {
                Tracer.e(mytag, "Timeout connecting to domogik");
            } catch (IOException | JSONException e) {
                Tracer.e(mytag, e.toString());
            }
            return json;

        }
    }

    public void setParams(SharedPreferences params) {
        SharedPreferences params1 = params;
    }

}