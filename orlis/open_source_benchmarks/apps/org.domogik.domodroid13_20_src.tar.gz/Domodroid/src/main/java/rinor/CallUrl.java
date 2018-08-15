package rinor;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by fritz on 07/09/15.
 * Call with url,login,password,timeout
 * all those parameters should be String
 */
public class CallUrl extends AsyncTask<String, Void, String> {
    private final String mytag = this.getClass().getName();
    private boolean alreadyTriedAuthenticating = false;

    @Override
    protected String doInBackground(String... uri) {
        // TODO : use non deprecated functions
        String url = uri[0];
        final String login = uri[1];
        final String password = uri[2];
        int timeout = Integer.parseInt(uri[3]);
        Boolean SSL = Boolean.valueOf(uri[4]);
        if (!SSL) {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
            HttpConnectionParams.setSoTimeout(httpParameters, timeout);
            DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(url));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else {
                    //Closes the connection.
                    try {
                        response.getEntity().getContent().close();
                    } catch (Exception e1) {
                        //TODO Handle problems..
                    }
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (IOException e) {
                //TODO Handle problems..
                // Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
                // Toast.makeText(context, "Rinor exception sending command", Toast.LENGTH_LONG).show();
            }
            return responseString;
        } else {
            String responseMessage = null;
            try {
                if (url.startsWith("http://")) {
                    url = url.replace("http://", "https://");
                }
                final HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url);
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
                String result = null;
                InputStream instream = urlConnection.getInputStream();
                // Read response headers
                int responseCode = urlConnection.getResponseCode();
                responseMessage = urlConnection.getResponseMessage();
                result = Abstract.httpsUrl.convertStreamToString(instream);
                instream.close();
                //} catch (HttpHostConnectException e) {
                //    e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseMessage;
        }
    }
/*
    @Override
    protected void onPreExecute() {
        // This method will called during doInBackground is in process
        // Here you can for example show a ProgressDialog
    }

    @Override
    protected void onPostExecute(Long result) {
        // onPostExecute is called when doInBackground finished
        // Here you can for example fill your Listview with the content loaded in doInBackground method

    }
*/
}
