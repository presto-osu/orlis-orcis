/*
 * SearchResultsFragment.java is a part of DailybRead
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

package tk.elevenk.dailybread.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import tk.elevenk.dailybread.R;
import tk.elevenk.dailybread.adapter.SearchResultsAdapter;
import tk.elevenk.dailybread.fragment.reader.MainReaderFragment;
import tk.elevenk.olapi.Library;
import tk.elevenk.olapi.OpenLibrary;
import tk.elevenk.olapi.data.BookData;
import tk.elevenk.olapi.data.BookList;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Results from search displayed using this fragment
 *
 * Created by John Krause on 1/1/15.
 */
public class SearchResultsFragment extends BooksListFragment {

    private BookList books;
    private Library library;
    private LoadEbook currentTask;
    private SearchResultsAdapter searchResultsAdapter;
    private List<AsyncTask> tasks;
    private ProgressDialog progress;

    public void populateSearchResults(Library library, BookList bookList) {
        this.books = bookList;
        this.library = library;
        this.tasks = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter(books, this.getActivity());
        this.setListAdapter(searchResultsAdapter);
        for (BookData data : bookList) {
            tasks.add(new DownloadImage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, data));
        }
        if(bookList.isEmpty()){
            // TODO make better dialog/action for this
            Toast.makeText(getActivity(), "No Results Found!", Toast.LENGTH_LONG).show();
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reader = new MainReaderFragment(){
            @Override
            public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
                inflater.inflate(R.menu.reader_from_search, menu);
            }
        };
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        progress = ProgressDialog.show(this.getActivity(), "Loading book...", "Trying to load ebook. If none is found you will be taken to the book's web page.", true, false);
        BookData book = books.get(position);
        currentTask = new LoadEbook();
        currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, book);
    }

    private void cancelTasks() {
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }
        tasks.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTasks();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelTasks();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelTasks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTasks();
    }

    private class LoadEbook extends AsyncTask<BookData, String, Object> {

        private BookData bookData;

        @Override
        protected Object doInBackground(BookData... params) {
            bookData = params[0];
            Object book = null;
            bookData.addBookDetails(library);
            if (bookData.hasEpubUrl()) {
                book = library.getEbook(params[0]);
            }
            return book;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o != null) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.main_content_layout, reader);
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
                if (!reader.loadNewBook(o)) {
                    openBookInBrowser();
                } else {
                    progress.hide();
                }
            } else {
                openBookInBrowser();
                progress.hide();
            }
        }

        private void openBookInBrowser() {
            String url = bookData.getUrl();
            if (url != null && url.length() > 0) {
                if (url.startsWith("/")) {
                    url = library.getBaseUrl() + url;
                }
                Intent browserCall = new Intent(Intent.ACTION_VIEW);
                browserCall.setData(Uri.parse(url));
                startActivity(browserCall);
            }
        }
    }

    private class DownloadImage extends AsyncTask<Object, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(Object... args) {
            Bitmap bmp = null;
            if (args[0] != null) {
                BookData data = (BookData) args[0];
                if (data.getCoverImage() == null) {
                    if (isCancelled()) return null;
                    Library lib = OpenLibrary.androidLibrary(null);
                    if (isCancelled()) return null;
                    Object url = lib.getCoverUrls("id", data.getCoverId()).get("medium");
                    if (isCancelled()) return null;
                    if (url == null) {
                        url = lib.getCoverUrls("olid", data.getCoverEditionKey()).get("medium");
                    }
                    if (isCancelled()) return null;
                    if (url == null) {
                        url = lib.getCoverUrls("olid", data.getOlid()).get("medium");
                    }
                    if (isCancelled()) return null;
                    if (url != null) {
                        bmp = downloadImage(url.toString());
                        if (isCancelled()) return null;
                        data.setCoverImage(bmp);
                    }
                }
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            searchResultsAdapter.notifyDataSetChanged();
        }


        private Bitmap downloadImage(String _url) {
            //Prepare to download image
            Bitmap bMap = null;
            URL url;
            InputStream in;
            BufferedInputStream buf;

            //BufferedInputStream buf;
            try {
                url = new URL(_url);
                if (isCancelled()) return null;
                in = url.openStream();

                // Read the input stream
                if (isCancelled()) return null;
                buf = new BufferedInputStream(in);

                // Convert the BufferedInputStream to a Bitmap
                if (isCancelled()) return null;
                bMap = BitmapFactory.decodeStream(buf);
                in.close();
                buf.close();

            } catch (Exception e) {
                Log.e("Error reading file", e.toString());
            }

            return bMap;
        }

    }
}
