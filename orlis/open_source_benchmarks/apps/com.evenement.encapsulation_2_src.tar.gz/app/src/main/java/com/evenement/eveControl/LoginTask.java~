package com.evenement.eveControl;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.webkit.WebView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * AsyncTask to Login to e-venement instance access control
 */
public class LoginTask extends AsyncTask<String, String, Boolean> {

    private HttpsURLConnection connection = null;
    private final WebView webView;
    private String csrfToken;
    private final Context context;
    private ProgressDialog dialog;
    private String formAction;
    private final String username;
    private final String password;
    private final String server;
    private final DrawerLayout drawerLayout;
    private final String uri = "/tck.php/ticket/control";


    /**
     * @param webView      to load access control page
     * @param context      app context for Toast calls
     * @param server       e-venement instance host name
     * @param username     e-venement instance username
     * @param password     e-venement instance password
     * @param drawerLayout to control navDrawer open/close
     */
    public LoginTask(WebView webView, Context context, String server, String username, String password, DrawerLayout drawerLayout) {

        this.webView = webView;
        this.context = context;
        this.username = username;
        this.password = password;
        this.server = server;
        this.drawerLayout = drawerLayout;
    }

    /**
     * show progress dialog before executing background task
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        showProgressDialog();
    }

    /**
     * actual background task
     *
     * @param params
     * @return boolean success
     */
    @Override
    protected Boolean doInBackground(String... params) {

        //connect twice in case cookie sync failed
        login(server + uri);

        return login(server + uri);
    }

    /**
     * Callback function to manipulate UI thread
     *
     * @param statut success of login via HttpsUrlConnection
     */
    @Override
    protected void onPostExecute(Boolean statut) {
        super.onPostExecute(statut);

        //connection ok, load access contrl page in webview
        if (statut) {
            webView.loadUrl(server + uri);
            hideProgressDialog();
            //connection failed,
        } else {
            hideProgressDialog();
            this.cancel(true);
            Toast.makeText(context, "Connexion impossible Veuillez vérifier vos informations de connexion", Toast.LENGTH_LONG).show();
            drawerLayout.openDrawer(GravityCompat.START);

        }
    }

    /**
     * read inputStream from getConnectionStream()
     *
     * @param stream
     * @return String, html returned by HttpsUrlConnection
     */
    private String readStream(InputStream stream) {

        if (stream != null) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return buffer.toString();
        }
        return null;
    }

    /**
     * Connect to host and retrieve html of 401
     *
     * @param uri
     * @return
     */
    private InputStream getConnectionStream(String uri) {

        int statusCode;

        if (username != null & password != null & server != null) {

            URL url = null;
            try {

                url = new URL(uri);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            assignTrustManager();

            InputStream stream = null;
            try {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostVerifier());
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setRequestMethod("GET");

                connection.connect();

                try {
                    statusCode = connection.getResponseCode();

                } catch (IOException e) {
                    statusCode = connection.getResponseCode();
                }

                switch (statusCode) {

                    case 200:
                        stream = connection.getInputStream();

                        break;

                    case 401:
                        stream = connection.getErrorStream();
                        break;

                    default:
                        stream = connection.getErrorStream();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return stream;
        }
        return null;
    }

    /**
     * initialize then assign a  x509TrustManager to HttpsUrlConnection for ssl handshake
     */
    private void assignTrustManager() {

        TrustManager manager = new TrustManager();

        TrustManager[] trustAllCerts = new TrustManager[]{manager};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse csrf token from html of 401 page.
     *
     * @param html content returned from getConnectionStream()
     */
    private void parseResponse(String html) {

        Document doc = Jsoup.parse(html);

        Elements tokenTag = doc.select("#signin__csrf_token");
        csrfToken = tokenTag.attr("value");

        Elements formTag = doc.select(".login form");
        formAction = formTag.attr("action");
    }

    /**
     * Login to server with HttpsUrlConnection
     *
     * @param uri server host name
     * @return boolean success
     * @see login()
     */
    private boolean postLogin(String uri) {

        URL url;

        try {
            url = new URL(uri + formAction);

            assignTrustManager();

            connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.write(getQuery());
            writer.flush();

            connection.connect();

            if (connection.getResponseCode() == 200) {

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * @return encoded query string for postLogin()
     */
    private String getQuery() {

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("signin[username]", username)
                .appendQueryParameter("signin[password]", password)
                .appendQueryParameter("signin[_csrf_token]", csrfToken);

        return builder.build().getEncodedQuery();
    }

    /**
     * @param url
     * @return
     */
    private boolean login(String url) {

        String html = readStream(getConnectionStream(url));

        if (html != null) {
            parseResponse(html);

            return postLogin(url);
        }
        return false;
    }

    /* progress dialog utils */
    private void showProgressDialog() {

        if (dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setTitle(context.getString(R.string.progressDialogTitle));
            dialog.setMessage(context.getString(R.string.progressDialogMessage));
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    private void hideProgressDialog() {

        if (dialog.isShowing()) {

            dialog.hide();
        }
    }
}//taskClass
