/*
 * SearchFragment.java is a part of DailybRead
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import tk.elevenk.dailybread.R;
import tk.elevenk.olapi.Library;
import tk.elevenk.olapi.search.SearchQuery;
import tk.elevenk.olapi.search.SearchResults;

/**
 * Fragment for executing a search
 *
 * Created by John Krause on 1/6/15.
 */
public class SearchFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText query, title, author, subject, person, place, publisher, isbn;
    private CheckBox hasFullText;
    private Spinner language, sortMethod;
    private Button search;
    private String selectedLanguage, selectedSortMethod;
    private boolean searching = false;
    private SearchTask currentTask;
    private Library library;

    public void setLibrary(Library lib) {
        library = lib;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        query = (EditText) view.findViewById(R.id.input_search_query);
        title = (EditText) view.findViewById(R.id.input_search_title);
        author = (EditText) view.findViewById(R.id.input_search_author);
        subject = (EditText) view.findViewById(R.id.input_search_subject);
        person = (EditText) view.findViewById(R.id.input_search_person);
        place = (EditText) view.findViewById(R.id.input_search_place);
        publisher = (EditText) view.findViewById(R.id.input_search_publisher);
        isbn = (EditText) view.findViewById(R.id.input_search_isbn);

        hasFullText = (CheckBox) view.findViewById(R.id.checkbox_search_show_ebooks);

        language = (Spinner) view.findViewById(R.id.spinner_search_language);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.search_languages, android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language.setAdapter(languageAdapter);
        language.setOnItemSelectedListener(this);

        sortMethod = (Spinner) view.findViewById(R.id.spinner_search_sort);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.search_sort_methods, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortMethod.setAdapter(sortAdapter);
        sortMethod.setOnItemSelectedListener(this);

        search = (Button) view.findViewById(R.id.button_search);
        search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!searching) {
            searching = true;
            SearchQuery searchQuery = new SearchQuery();
            searchQuery.query(query.getText().toString());
            searchQuery.title(title.getText().toString());
            searchQuery.author(author.getText().toString());
            searchQuery.subject(subject.getText().toString());
            searchQuery.person(person.getText().toString());
            searchQuery.place(place.getText().toString());
            searchQuery.publisher(publisher.getText().toString());
            searchQuery.isbn(isbn.getText().toString());
            searchQuery.hasFullText(hasFullText.isChecked());
            searchQuery.language(selectedLanguage);
            searchQuery.sort(selectedSortMethod);
            currentTask = new SearchTask();
            currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, library, searchQuery);
        } else {
            Toast.makeText(this.getActivity(), "Still searching...", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner_search_language) {
            this.selectedLanguage = getResources().getStringArray(R.array.search_language_codes)[position];
        } else if (parent.getId() == R.id.spinner_search_sort) {
            this.selectedSortMethod = getResources().getStringArray(R.array.search_sort_codes)[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showResults(SearchResults results) {
        SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_layout, searchResultsFragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        searchResultsFragment.populateSearchResults(library, results.getBooks());
    }

    private class SearchTask extends AsyncTask<Object, String, SearchResults> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(getActivity(), "Searching...", "Performing search, please wait.", true, false);
        }

        @Override
        protected SearchResults doInBackground(Object... params) {
            return ((Library) params[0]).search((SearchQuery) params[1]);
        }

        @Override
        protected void onPostExecute(SearchResults searchResults) {
            super.onPostExecute(searchResults);
            showResults(searchResults);
            progress.hide();
            searching = false;
        }
    }
}
