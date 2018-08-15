/*
 * EpubReaderFragment.java is a part of DailybRead
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

package tk.elevenk.dailybread.fragment.reader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.IOUtil;
import tk.elevenk.dailybread.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Epub specific reader fragment
 *
 * Created by John Krause on 12/26/14.
 */
public class EpubReaderFragment extends Fragment implements BookReaderFragment {

    private Book book;
    private WebView pageText;
    private boolean pageLoaded;
    private int currentPage = 0, pageToLoad;
    private String tempStorageDir;
    private ProgressBar progress;

    private OnPageLoadedListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_epub_reader, container, false);
        progress = (ProgressBar) root.findViewById(R.id.page_loading_spinner);
        pageText = (WebView) root.findViewById(R.id.page_text);
        pageText.getSettings().setBlockNetworkLoads(true);
        pageText.getSettings().setDisplayZoomControls(true);
        pageText.getSettings().setSupportZoom(true);
        pageText.getSettings().setBuiltInZoomControls(true);
        pageText.getSettings().setAppCacheEnabled(false);
        pageText.clearCache(true);
        pageText.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        pageText.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        pageText.setWebViewClient(new EpubReaderWebViewClient());
        return root;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        if (args.containsKey("epub")) {
            this.book = (Book) args.get("epub");
        }
    }

    private boolean loadPage(int page) {
        if (page >= 0 && page < book.getContents().size()) {
            pageToLoad = page;
            new PageLoader().execute(pageToLoad);
            return true;
        }
        return false;

    }

    private class PageLoader extends AsyncTask<Integer,String,String[]> {

        @Override
        protected void onPreExecute() {
            pageLoaded = false;
            progress.setVisibility(View.VISIBLE);
            pageText.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String[] doInBackground(Integer... params) {
            return loadPage(book.getSpine().getResource(params[0]));
        }

        private String[] loadPage(Resource resource) {
            String pageData = "<h1>ERROR LOADING</h4>";
            String mediaType = "text/html";
            String encoding = null;
            try {
                pageData = new String(resource.getData(), resource.getInputEncoding()).replace("../", "");
                mediaType = resource.getMediaType().getName();
                pageLoaded = true;
            } catch (Exception e) {
                // TODO
                Log.e("", "Error loading page " + resource.getHref(), e);
            }
            return new String[] {pageData, mediaType, encoding};
        }

        @Override
        protected void onPostExecute(String[] params) {
            super.onPostExecute(params);
            try {
                pageText.loadDataWithBaseURL("file://" + tempStorageDir + "/", params[0], params[1], params[2], null);
            } catch (Exception e) {
                // TODO
                pageLoaded = false;
                Log.e("", "", e);
            }
        }
    }

    @Override
    public int load(Object book) {
        tempStorageDir = this.getActivity().getCacheDir().getAbsolutePath();
        this.book = (Book) book;
        storeEpubImages();
        loadPage(currentPage);
        return currentPage;
    }

    @Override
    public boolean next() {
        return loadPage(currentPage + 1);
    }

    @Override
    public boolean previous() {
        return loadPage(currentPage - 1);
    }

    @Override
    public boolean gotoPage(int page) {
        return loadPage(page);
    }

    private void storeEpubImages() {
        try {

            Resources rst = book.getResources();
            Collection<Resource> clrst = rst.getAll();
            Iterator<Resource> itr = clrst.iterator();

            while (itr.hasNext()) {
                Resource rs = itr.next();

                if ((rs.getMediaType() == MediatypeService.JPG)
                        || (rs.getMediaType() == MediatypeService.PNG)
                        || (rs.getMediaType() == MediatypeService.GIF)) {

                    File oppath1 = new File(tempStorageDir, rs.getHref().replace("OEBPS/", ""));
                    oppath1.delete();

                    oppath1.getParentFile().mkdirs();
                    oppath1.createNewFile();

                    System.out.println("Path : " + oppath1.getParentFile().getAbsolutePath());


                    FileOutputStream fos1 = new FileOutputStream(oppath1);
                    byte[] data = rs.getData();
                    if(data.length > 0)
                        fos1.write(data);
                    else {
                        IOUtil.copy(getResources().openRawResource(R.raw.no_image), new FileOutputStream(oppath1));
                    }
                    fos1.close();

                } else if (rs.getMediaType() == MediatypeService.CSS) {

                    File oppath = new File(tempStorageDir, rs.getHref());

                    oppath.getParentFile().mkdirs();
                    oppath.createNewFile();

                    FileOutputStream fos = new FileOutputStream(oppath);
                    fos.write(rs.getData());
                    fos.close();

                }

            }


        } catch (Exception e) {
            Log.e("", "Unable to store ePub images", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!pageLoaded && book != null) {
            loadPage(currentPage);
        }
    }

    @Override
    public OnPageLoadedListener getListener() {
        return listener;
    }

    @Override
    public void setListener(OnPageLoadedListener listener) {
        this.listener = listener;
    }

    private class EpubReaderWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            //Log.i("epubLoader", "loading resource: " + url);
            //if (url.contains("image")) {
            //    view.getSettings().setLoadWithOverviewMode(true);
            //    view.getSettings().setUseWideViewPort(true);
            //}
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(pageLoaded){
                currentPage = pageToLoad;
            }
            if(listener != null){
                listener.onPageLoaded(currentPage);
            }
            pageText.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("epubLoader", "Error: " + errorCode + " - " + description + " - " + failingUrl);
            pageLoaded = false;
            if(listener != null){
                listener.onPageLoaded(currentPage);
            }
        }
    }
}
