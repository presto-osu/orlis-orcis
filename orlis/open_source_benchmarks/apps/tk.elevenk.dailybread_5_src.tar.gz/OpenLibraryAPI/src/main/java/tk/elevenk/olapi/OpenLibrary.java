/*
 * OpenLibrary.java is a part of DailybRead
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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import tk.elevenk.olapi.books.BookQuery;
import tk.elevenk.olapi.data.BookData;
import tk.elevenk.olapi.data.Credential;
import tk.elevenk.olapi.data.LibraryPreferences;
import tk.elevenk.olapi.logging.Log;
import tk.elevenk.olapi.read.ReadQuery;
import tk.elevenk.olapi.search.SearchQuery;
import tk.elevenk.olapi.search.SearchResults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Implementation of the OpenLibrary functionality
 *
 * Created by John Krause on 12/26/14.
 */
public class OpenLibrary implements Library {

    private static final String AND_USER_AGENT = "Mozilla/5.0 (Android;) DailybRead/0.1";
    private static final String LOGIN_COOKIE_PARAMETER = "Cookie";


    private static LibraryApi api;
    private static HttpClient client;
    private LibraryPreferences preferences;
    private boolean cancelled;

    public static OpenLibrary genericLibrary(LibraryPreferences preferences) {
        client = new DefaultHttpClient();
        ((DefaultHttpClient) client).setRedirectHandler(getRedirectHandler());
        return new OpenLibrary(new OpenLibApi(client), preferences);
    }

    public static OpenLibrary androidLibrary(LibraryPreferences preferences) {
        //TODO make android client redirect
        // client = AndroidHttpClient.newInstance(AND_USER_AGENT);
        client = new DefaultHttpClient();
        ((DefaultHttpClient) client).setRedirectHandler(getRedirectHandler());
        return new OpenLibrary(new OpenLibApi(client), preferences);
    }

    @Override
    public SearchResults search(SearchQuery search) {
        return new SearchResults(api.search(search));
    }

    @Override
    public BookData getBookDetails(BookQuery bookQuery) {
        return BookData.bookFromBookApi(api.books(bookQuery));
    }

    @Override
    public BookData getReadingDetails(ReadQuery readQuery) {
        return BookData.bookFromReadingApi(api.read(readQuery));
    }

    @Override
    public Object getEbook(BookData bookData) {
        Object ebook = null;
        switch (preferences.getPreferredEbookType()) {
            case EPUB:
                ebook = getEpub(bookData);
                if (ebook != null)
                    break;
            case DJVU:
                break;
            case PDF:
                break;
            case TXT:
                break;
            default:
                break;
        }
        return ebook;
    }

    private Object getEpub(BookData bookData) {
        Object ebook = null;
        try {
            bookData.addBookDetails(this);
            String fileUrl = bookData.getEpubUrl().toString();
            ebook = downloadTempEbook(fileUrl, "epub");
        } catch (Exception e) {
            //TODO
            Log.e("", e);
        }
        return ebook;
    }

    private File downloadTempEbook(String url, String extension) {
        HttpResponse response = null;
        File temp = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            response = client.execute(httpGet);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }

        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                temp = File.createTempFile("openlib" + new Random().nextInt(), extension);
                response.getEntity().writeTo(new FileOutputStream(temp));
            } catch (IOException e) {
                Log.e("", e);
            }
        }
        return temp;
    }

    @Override
    public BookData findRandomBook(LibraryCallbacks callback) {
        cancelled = false;
        // get random word to use in query
        SearchResults results;
        BookData book = null;
        boolean bookFound = false;
        do {
            HttpGet get = new HttpGet("http://randomword.setgetgo.com/get.php");
            String word = "science"; // TODO "diabology" causes a lot of loops, look into it
            if (cancelled) break;
            try {
                HttpResponse resp = client.execute(get);
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    word = ApiHelpers.convertStreamToString(resp.getEntity().getContent()).trim();
                }
            } catch (Exception e) {
                Log.e("", e);
            }
            callback.onSearchUpdate(word);
            SearchQuery query = new SearchQuery();
            query.hasFullText();
            query.query(word);
            query.subject("Accessible+book");
            query.sort("editions");
            Log.d("Searching for " + word);
            if (cancelled) break;
            results = search(query);
            int numBooks = results.getNumberOfResults();
            Log.d("Found this many: " + numBooks);
            if (numBooks > 0) {
                book = results.getBooks().get(0);
                if (cancelled) break;
                book.addBookDetails(this);
                if (cancelled) break;
                book.addReadingDetails(this);
                Object epubUrlData = book.getEpubUrl();
                Log.d("Got book " + epubUrlData.toString());
                if (cancelled) break;
                if (epubUrlData instanceof List) {
                    List olidList = (List) epubUrlData;
                    for (Object item : olidList) {
                        Log.d("Trying a new book" + item.toString());
                        BookQuery bq = new BookQuery();
                        bq.olid(item.toString());
                        if (cancelled) break;
                        book = this.getBookDetails(bq);
                        if (book.hasEpubUrl()) {
                            bookFound = true;
                            break;
                        }
                    }
                } else if (book.hasEpubUrl()) {
                    bookFound = true;
                }
            }
        } while (!bookFound && !cancelled);
        if (!cancelled) {
            Log.d("Found good book, loading");
            return book;
        } else return null;
    }

    @Override
    public boolean login(Credential creds) {
        boolean loggedIn = false;
        Map<String, String> data = new HashMap<String, String>();
        data.put("username", creds.getUsername());
        data.put("password", creds.getPassword());
        Cookie sessionCookie = api.login(data);

        if (sessionCookie != null) {
            // Set client to use cookie
            HttpParams params = client.getParams();
            params.setParameter(LOGIN_COOKIE_PARAMETER, sessionCookie.getName() + "=" + sessionCookie.getValue());
            loggedIn = true;
        }
        return loggedIn;
    }

    @Override
    public Map getCoverUrls(Object key, Object value) {
        Map<String, String> data = new HashMap<String, String>();
        if (key != null && value != null) {
            data.put(key.toString(), value.toString());
            JSONObject object = api.covers(data);
            data.clear();
            Iterator iterator = object.keys();
            while (iterator.hasNext()) {
                String objKey = iterator.next().toString();
                data.put(objKey, object.get(objKey).toString());
            }
        }
        return data;
    }

    private static RedirectHandler getRedirectHandler() {
        // TODO may break post requests
        return new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                boolean isRedirect = super.isRedirectRequested(response, context);
                if (!isRedirect) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode == 301 || responseCode == 302) {
                        return true;
                    }
                }
                return isRedirect;
            }
        };
    }

    private OpenLibrary() {
        // just to disable use of new OpenLibrary();
    }

    private OpenLibrary(LibraryApi lapi, LibraryPreferences preferences) {
        this();
        api = lapi;
        this.preferences = preferences;
    }

    @Override
    public void cancelRequest() {
        this.cancelled = true;
        api.cancelRequest();
    }

    @Override
    public String getBaseUrl() {
        return api.getBaseUrl();
    }
}
