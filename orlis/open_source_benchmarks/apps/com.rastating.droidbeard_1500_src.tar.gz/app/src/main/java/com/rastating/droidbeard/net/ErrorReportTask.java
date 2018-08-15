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

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class ErrorReportTask extends AsyncTask<JSONObject, Void, Integer> {

    public JSONObject postData(String url, JSONObject obj) {
        HttpClient client = new DefaultHttpClient();
        String json = obj.toString();

        try {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-type", "application/json");

            StringEntity se = new StringEntity(obj.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);

            HttpResponse response = client.execute(post);
            String retval = EntityUtils.toString(response.getEntity());

            return new JSONObject(retval);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Integer doInBackground(JSONObject... exceptions) {
        String url = "http://blog.rastating.com:5050/exception";

        try {
            JSONObject result = postData(url, exceptions[0]);
            if (result != null && result.has("number")) {
                return result.getInt("number");
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {

    }
}