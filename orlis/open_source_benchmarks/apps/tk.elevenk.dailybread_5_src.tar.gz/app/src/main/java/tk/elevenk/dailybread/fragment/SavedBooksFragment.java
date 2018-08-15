/*
 * SavedBooksFragment.java is a part of DailybRead
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

import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import tk.elevenk.dailybread.R;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Fragment for displaying the list of saved books
 *
 * Created by John Krause on 1/1/15.
 */
public class SavedBooksFragment extends BooksListFragment {

    private File[] books;

    @Override
    public void onResume() {
        super.onResume();
        populateSavedBooks();
    }

    private void populateSavedBooks() {
        File booksDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.book_storage_dir));
        if (booksDir.exists() && booksDir.isDirectory()) {
            books = booksDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".epub");
                }
            });
            String[] bookNames = new String[books.length];
            for (int i = 0; i < books.length; i++) {
                bookNames[i] = books[i].getName();
            }
            this.setListAdapter(new ArrayAdapter<>(this.getActivity(), R.layout.adapter_saved_books_list, bookNames));
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        File book = books[position];
        FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.main_content_layout, reader);
        transaction.addToBackStack(null);
        transaction.commit();
        getFragmentManager().executePendingTransactions();
        reader.loadNewBook(book);
    }
}
