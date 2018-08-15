/*
    This file is part of the Diaspora Native WebApp.

    Diaspora Native WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora Native WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.diaspora.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetPodsService extends Service {
    public static final String MESSAGE = "de.baumann.diaspora.podsreceived";

    private static final String TAG = "Diaspora Pod Service";

    public GetPodsService() { }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getPods();
        return super.onStartCommand(intent, flags, startId);
    }

    private void getPods() {
        /*
         * Most of the code in this AsyncTask is from the file getPodlistTask.java
         * from the app "Diaspora Webclient".
         * A few modifications and adaptations were made by me.
         * Source:
         * https://github.com/voidcode/Diaspora-Webclient/blob/master/src/com/voidcode/diasporawebclient/getPodlistTask.java
         * Thanks to Terkel Sørensen
         */
        AsyncTask<Void, Void, String[]> getPodsAsync = new AsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... params) {

                // TODO: Update deprecated code

                StringBuilder builder = new StringBuilder();
                HttpClient client = new DefaultHttpClient();
                List<String> list = null;
                try {
                    HttpGet httpGet = new HttpGet("http://podupti.me/api.php?key=4r45tg&format=json");
                    HttpResponse response = client.execute(httpGet);
                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream content = entity.getContent();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(content));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } else {
                        //TODO  Notify User about failure
                        Log.e(TAG, "Failed to download list of pods");
                    }
                } catch (IOException e) {
                    //TODO handle json buggy feed
                    e.printStackTrace();
                }
                //Parse the JSON Data
                try {
                    JSONObject j = new JSONObject(builder.toString());
                    JSONArray jr = j.getJSONArray("pods");
                    Log.d(TAG, "Number of entries " + jr.length());
                    list = new ArrayList<String>();
                    for (int i = 0; i < jr.length(); i++) {
                        JSONObject jo = jr.getJSONObject(i);
                        Log.d(TAG, jo.getString("domain"));
                        String secure = jo.getString("secure");
                        if (secure.equals("true"))
                            list.add(jo.getString("domain"));
                    }

                } catch (Exception e) {
                    //TODO Handle Parsing errors here
                    e.printStackTrace();
                }
                if (list != null)
                    return list.toArray(new String[list.size()]);
                else
                    return null;
            }

            @Override
            protected void onPostExecute(String[] strings) {
                Intent broadcastIntent = new Intent(MESSAGE);
                if (strings != null)
                    broadcastIntent.putExtra("pods", strings);
                sendBroadcast(broadcastIntent);
                stopSelf();
            }
        };
        getPodsAsync.execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
