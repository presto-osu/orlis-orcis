/*
 * MainReaderFragment.java is a part of DailybRead
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

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import tk.elevenk.dailybread.R;
import tk.elevenk.olapi.data.BookData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Main reader fragment that handles displaying documents of different formats
 *
 * Created by John Krause on 12/26/14.
 */
public class MainReaderFragment extends Fragment implements BookReaderFragment.OnPageLoadedListener{

    protected BookReaderFragment readerFragment;
    protected TextView pageNum;
    protected Object currentBook;
    protected LinearLayout progressLayout;
    protected TextView searchQueryText, currentQueryText, findingBreadText, loadingBookText;
    protected boolean bookLoaded = false;
    protected boolean fragmentPaused = false;
    protected boolean currentBookSaved;
    protected Object bookToLoad;
    protected BookData bookData;
    protected Button previous, next;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_book_reader, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previous = (Button) view.findViewById(R.id.previous_button);
        next = (Button) view.findViewById(R.id.next_button);
        progressLayout = (LinearLayout) view.findViewById(R.id.reader_progress_layout);
        loadingBookText = (TextView) view.findViewById(R.id.book_loading_text);
        searchQueryText = (TextView) view.findViewById(R.id.progress_search_query);
        currentQueryText = (TextView) view.findViewById(R.id.current_search_query_text);
        findingBreadText = (TextView) view.findViewById(R.id.finding_bread_text);
        progressLayout.setVisibility(View.GONE);
        pageNum = (TextView) view.findViewById(R.id.page_number);
        pageNum.setText("");

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (readerFragment != null) {
                    if(readerFragment.previous()){
                        next.setEnabled(false);
                        previous.setEnabled(false);
                    }
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (readerFragment != null && currentBook != null) {
                    if(readerFragment.next()){
                        next.setEnabled(false);
                        previous.setEnabled(false);
                    }
                }
            }
        });
    }

    protected void showLoadingBookSpinner() {
        previous.setEnabled(false);
        next.setEnabled(false);
        progressLayout.setVisibility(View.VISIBLE);
        loadingBookText.setVisibility(View.VISIBLE);
        searchQueryText.setVisibility(View.GONE);
        currentQueryText.setVisibility(View.GONE);
        findingBreadText.setVisibility(View.GONE);
        getChildFragmentManager().popBackStack();
    }

    protected void showSearchingDialog() {
        previous.setEnabled(false);
        next.setEnabled(false);
        progressLayout.setVisibility(View.VISIBLE);
        loadingBookText.setVisibility(View.GONE);
        searchQueryText.setText(getString(R.string.text_empty_query));
        searchQueryText.setVisibility(View.VISIBLE);
        currentQueryText.setVisibility(View.VISIBLE);
        findingBreadText.setVisibility(View.VISIBLE);
        getChildFragmentManager().popBackStack();
    }

    public boolean loadNewBook(Object book) {
        bookToLoad = book;
        bookLoaded = false;
        loadBook(bookToLoad);
        currentBookSaved = false;
        return bookLoaded;
    }

    protected boolean reloadBook() {
        if (!bookLoaded && bookToLoad != null) {
            loadBook(bookToLoad);
        }
        return bookLoaded;
    }

    private void loadBook(Object book) {
        if (!fragmentPaused) {
            showLoadingBookSpinner();
            if (book instanceof File) {
                if (((File) book).getName().endsWith("epub")) {
                    EpubReader reader = new EpubReader();
                    try {
                        loadEpub(reader.readEpub(new FileInputStream((File) book)));
                    } catch (Exception e) {
                        //TODO logging
                        e.printStackTrace();
                    }
                }
            } else if (book instanceof Book) {
                loadEpub((Book) book);
            }
            progressLayout.setVisibility(View.GONE);
        }
    }

    private void loadEpub(Book book) {
        this.currentBook = book;
        try {
            FragmentManager fragmentManager = this.getChildFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            EpubReaderFragment epubReaderFragment = new EpubReaderFragment();
            epubReaderFragment.setListener(this);
            transaction.replace(R.id.book_content, epubReaderFragment).addToBackStack(null);
            transaction.commit();
            fragmentManager.executePendingTransactions();
            epubReaderFragment.load(currentBook);
            readerFragment = epubReaderFragment;
            bookLoaded = true;
        } catch (Exception e) {
            //TODO logging
            e.printStackTrace();
        }
    }

    public boolean saveCurrentBook() {
        if (!currentBookSaved) {
            try {
                File externalDir = Environment.getExternalStorageDirectory();
                File booksDir = new File(externalDir, getString(R.string.book_storage_dir));
                booksDir.mkdir();
                if (currentBook instanceof Book) {
                    File bookFile = new File(booksDir, ((Book) currentBook).getTitle().trim().replaceAll(" ", "_").replaceAll("\\W", "") + ".epub");
                    EpubWriter epubWriter = new EpubWriter();
                    try {
                        epubWriter.write((Book) currentBook, new FileOutputStream(bookFile));
                        currentBookSaved = true;
                    } catch (Exception e) {
                        // TODO logging
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                //TODO logging
                e.printStackTrace();
            }
        }
        return currentBookSaved;
    }

    public BookData getBookData() {
        return bookData;
    }

    protected void setBookData(BookData data) {
        this.bookData = data;
    }

    public Object getCurrentBook() {
        return currentBook;
    }

    @Override
    public void onResume() {
        fragmentPaused = false;
        reloadBook();
        super.onResume();
    }

    @Override
    public void onPause() {
        fragmentPaused = true;
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.reader, menu);
    }

    @Override
    public void onPageLoaded(int pageNum) {
        this.pageNum.setText(String.valueOf(pageNum+1));
        next.setEnabled(true);
        previous.setEnabled(true);
    }
}
