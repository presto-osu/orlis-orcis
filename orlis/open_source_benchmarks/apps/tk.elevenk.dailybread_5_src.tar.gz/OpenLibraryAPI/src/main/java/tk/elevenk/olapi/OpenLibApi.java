/*
 * OpenLibApi.java is a part of DailybRead
 *     Copyright (C) 2015  John Krause, Eleven-K Software
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.elevenk.olapi;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tk.elevenk.olapi.logging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of OpenLibrary API
 *
 * Created by John Krause on 12/25/14.
 */
public class OpenLibApi implements LibraryApi {

    // *************************************************
    // API URLS AND QUERY KEYS
    // *************************************************
    public static final String BASE_URL = "https://openlibrary.org";

    private static final String BOOKS_API_URL = BASE_URL + "/api/books";
    private static final String COVERS_API_URL = BASE_URL.replace("//", "//covers.") + "/b/";
    private static final String READ_API_URL = BASE_URL + "/api/volumes/brief/json/";
    private static final String SEARCH_API_URL = BASE_URL + "/search.json";
    private static final String SUBJECTS_API_URL = BASE_URL + "/subjects/<subject>.json";
    private static final String LOGIN_API_URL = BASE_URL + "/account/login";
    private static final Map BOOKS_API_KEYS = new HashMap<String, String>() {
        {
            put("bibkeys", null);
            put("format", "json");
            put("callback", null);
            put("jscmd", "data");
        }
    };
    private static final Map SEARCH_API_KEYS = new HashMap<String, String>() {
        {
            put("q", null);
            put("author", null);
            put("title", null);
            put("has_fulltext", null);
            put("isbn", null);
            put("subject", null);
            put("place", null);
            put("person", null);
            put("publisher", null);
            put("page", null);
            put("sort", null);
        }
    };
    private static final String SUBJECT_DATA_KEY = "subject";
    private static final Map SUBJECTS_API_KEYS = new HashMap<String, String>() {
        {
            put("details", "true");
            put("ebooks", "true");
            put("published_in", null);
            put("limit", null);
            put("offset", null);
        }
    };
    private static final String LOGIN_SET_COOKIE_HEADER = "Set-Cookie";

    // *************************************************
    // FIELDS
    // *************************************************

    private HttpClient httpClient;
    private HttpUriRequest currentRequest;

    public OpenLibApi() {
        this(new DefaultHttpClient());
    }

    public OpenLibApi(HttpClient client) {
        this.httpClient = client;
    }

    private String addQueryParams(String baseUrl, Map data, Map keys) {
        // TODO make this whole api use URI or URL instead of string
        baseUrl += "?";
        for (Object key : keys.keySet()) {
            if (data.containsKey(key) && data.get(key) != null && !data.get(key).equals("")) {
                baseUrl += key.toString() + "=" + data.get(key).toString().replaceAll(" ","+").replaceAll("[^\\w\\+]", "") + "&";
            } else if (keys.get(key) != null && !keys.get(key).equals("")) {
                baseUrl += key.toString() + "=" + keys.get(key).toString().replaceAll(" ", "+").replaceAll("[^\\w\\+]", "") + "&";
            }
        }
        Log.d("URL with query params: " + baseUrl);
        return baseUrl;
    }

    private JSONObject makeRequest(HttpUriRequest request) {
        currentRequest = request;
        JSONObject responseData = new JSONObject();
        if (request != null) {
            try {
                HttpResponse response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String responseBody = ApiHelpers.convertStreamToString(response.getEntity().getContent());
                    responseData = new JSONObject(responseBody);
                }
            } catch (Exception e) {
                Log.e("", e);
            }
        }
        return responseData;

    }

    private JSONObject makeApiCall(Map data, Map paramKeys, String pUrl) {
        HttpGet apiRequest;
        String url = pUrl.replaceAll(" ","%20");
        if (paramKeys != null && data != null) {
            apiRequest = new HttpGet(addQueryParams(url, data, paramKeys));
        } else {
            apiRequest = new HttpGet(url);
        }
        return makeRequest(apiRequest);
    }

    @Override
    public JSONObject books(Map data) {
        return makeApiCall(data, BOOKS_API_KEYS, BOOKS_API_URL);
    }

    @Override
    public JSONObject covers(Map data) {
        JSONObject imageData = new JSONObject();
        try {
            for (Object key : data.keySet()) {
                String coverUrl = COVERS_API_URL + key + "/" + data.get(key);
                imageData.put("small", coverUrl + "-S.jpg");
                imageData.put("medium", coverUrl + "-M.jpg");
                imageData.put("large", coverUrl + "-L.jpg");
            }
        } catch (JSONException e) {
            Log.e("", e);
        }
        return imageData;
    }

    @Override
    public JSONObject lists(Map data) {
        // TODO not yet implemented
        throw new NotImplementedException();
    }

    @Override
    public JSONObject read(Map data) {
        String requestUrl = READ_API_URL;
        for (Object key : data.keySet()) {
            requestUrl += key.toString() + ":" + data.get(key).toString() + "%7C";
        }
        return makeApiCall(null, null, requestUrl);
    }

    @Override
    public JSONObject recentChanges(Map data) {
        // TODO not yet implemented
        throw new NotImplementedException();
    }

    @Override
    public JSONObject search(Map data) {
        return makeApiCall(data, SEARCH_API_KEYS, SEARCH_API_URL);
    }

    @Override
    public JSONObject searchInside(Map data) {
        // TODO this is a bit complex
        throw new NotImplementedException();
    }

    @Override
    public JSONObject subjects(Map data) {
        String subject = data.get(SUBJECT_DATA_KEY).toString();
        String apiUrl = SUBJECTS_API_URL.replace("<subject>", subject);
        return makeApiCall(data, SUBJECTS_API_KEYS, apiUrl);
    }

    @Override
    public Cookie login(Map data) {
        HttpPost loginRequest = new HttpPost(LOGIN_API_URL);
        JSONObject jsonData = new JSONObject(data);
        Cookie cookie = null;

        try {
            StringEntity entity = new StringEntity(jsonData.toString());
            entity.setContentType("application/json");
            loginRequest.setEntity(entity);
            HttpResponse response = httpClient.execute(loginRequest);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Header setCookie = response.getFirstHeader(LOGIN_SET_COOKIE_HEADER);
                String[] headerVal = setCookie.getValue().split(";");
                String[] sessionData = headerVal[0].split("=");
                String[] pathData = headerVal[1].split("=");
                cookie = new BasicClientCookie(sessionData[0], sessionData[1]);
                ((BasicClientCookie) cookie).setPath(pathData[1]);
            }
        } catch (Exception e) {
            Log.e("", e);
        }

        return cookie;
    }

    @Override
    public void cancelRequest() {
        try {
            currentRequest.abort();
        } catch (Exception e) {
            Log.e("Unable to abort request", e);
        }
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
