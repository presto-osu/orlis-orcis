/*
 * Library.java is a part of DailybRead
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

import tk.elevenk.olapi.books.BookQuery;
import tk.elevenk.olapi.data.BookData;
import tk.elevenk.olapi.data.Credential;
import tk.elevenk.olapi.read.ReadQuery;
import tk.elevenk.olapi.search.SearchQuery;
import tk.elevenk.olapi.search.SearchResults;

import java.util.Map;

/**
 * Interface for the Library
 *
 * Created by John Krase on 12/26/14.
 */
public interface Library {
    /**
     * Executes a search using the given SearchQuery 
     * @param search The SearchQuery object that has the search parameters to be used
     * @return A list of results wrapped in the SearchResults class
     */
    public SearchResults search(SearchQuery search);

    /**
     * Gets the data from the OpenLibrary Book API and returns it
     * wrapped in a Book object 
     * @param bookQuery The query to use for the data retrieval
     * @return A BookData object with all of the data from the API
     */
    public BookData getBookDetails(BookQuery bookQuery);

    /**
     *
     * Gets the data from the OpenLibrary Read API and returns it
     * wrapped in a Book object
     * @param readQuery The query to use for the data retrieval
     * @return A BookData object with all of the data from the API
     */
    public BookData getReadingDetails(ReadQuery readQuery);

    /**
     * Gets the eBook file from the given BookData. Currently
     * only implemented to get the epub file 
     * @param bookData A Book to get the eBook from
     * @return The eBook, usually a File object that can be opened by a reader or saved
     */
    public Object getEbook(BookData bookData);

    /**
     * Searches the library database for a random book using
     * a random word generating service to execute searches.
     * The calling class must implement the callbacks interface
     * in order to get updates on the status of the search 
     * @param callbacks The interface that will receive the updates
     * @return A book object, usually a BookData class with the info
     *      about the book 
     */
    public Object findRandomBook(LibraryCallbacks callbacks);

    /**
     * Logs the user in to OpenLibrary
     * @param creds
     * @return
     */
    public boolean login(Credential creds);

    /**
     * Callback interface to receive updates on the random search 
     */
    public static interface LibraryCallbacks {
        public void onSearchUpdate(Object info);
    }

    /**
     * Cancels any network requests being made by the library  
     */
    public void cancelRequest();

    /**
     * Gets the small, medium and large cover urls for a book.
     * @param key The search key, either ISBN, OCLC, LCCN, OLID or ID
     * @param value The value associated with the key
     * @return A map of the cover urls with keys 'small', 'medium' and 'large'
     */
    public Map getCoverUrls(Object key, Object value);

    /**
     * Returns the base url of the library
     * @return The base url as a String
     */
    public String getBaseUrl();
}
