/*
 * TodaysbReadFragment.java is a part of DailybRead
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

import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;
import tk.elevenk.dailybread.R;
import tk.elevenk.dailybread.fragment.reader.MainReaderFragment;
import tk.elevenk.olapi.Library;
import tk.elevenk.olapi.data.BookData;

/**
 * Fragment for featured section of the app. Executes random searches.
 *
 * Created by John Krause on 12/31/14.
 */
public class TodaysbReadFragment extends MainReaderFragment {

    private String randomSearchWord = "";
    private RandomSearchTask currentTask;
    private Library library;

    public void randomSearch(Library library) {
        this.library = library;
        if (isSearching()) {
            Log.w(this.getClass().getName(), "Random search already executing");
        } else {
            showSearchingDialog();
            bookLoaded = false;
            currentTask = new RandomSearchTask();
            currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, library, searchQueryText);
        }
    }

    public String getRandomSearchWord() {
        return randomSearchWord;
    }

    private void cancelCurrentTask() {
        if (isSearching()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    currentTask.cancel(true);
                    library.cancelRequest();
                }
            }).run();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelCurrentTask();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelCurrentTask();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelCurrentTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelCurrentTask();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((currentTask == null || !isSearching()) && !bookLoaded) {
            showSearchingDialog();
            currentTask = new RandomSearchTask();
            currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, library, searchQueryText);
        }
    }

    @Override
    public void onDestroy() {
        cancelCurrentTask();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.todays_bread, menu);
    }

    public boolean isSearching() {
        return (currentTask != null && currentTask.getStatus() == AsyncTask.Status.RUNNING && !currentTask.isCancelled());
    }

    private class RandomSearchTask extends AsyncTask<Object, String, Object> implements Library.LibraryCallbacks {

        private Library library;
        private TextView queryWordText;

        @Override
        protected Object doInBackground(Object... params) {
            library = (Library) params[0];
            queryWordText = (TextView) params[1];
            bookData = (BookData) library.findRandomBook(this);
            if (isCancelled()) return null;
            if (bookData != null) {
                bookData.addBookDetails(library);
                if (isCancelled()) return null;
                bookData.addReadingDetails(library);
                if (isCancelled()) return null;
                publishProgress(queryWordText.getText().toString(), "\nBook found\nLoading...");
                if (isCancelled()) return null;
                setBookData(bookData);
                return library.getEbook(bookData);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            randomSearchWord = values[0].trim();
            queryWordText.setText(randomSearchWord + values[1]);
        }

        @Override
        protected void onPostExecute(Object ebook) {
            if (isCancelled()) return;
            loadNewBook(ebook);
        }

        @Override
        protected void onCancelled() {
            try {
                library.cancelRequest();
            } catch (Exception e) {
                Log.e("", "Unable to cancel request", e);
            }
        }

        @Override
        public void onSearchUpdate(Object info) {
            this.publishProgress(info.toString(), "");
        }
    }
}
