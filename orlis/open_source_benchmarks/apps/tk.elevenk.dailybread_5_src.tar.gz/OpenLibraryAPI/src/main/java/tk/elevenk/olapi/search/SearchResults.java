/*
 * SearchResults.java is a part of DailybRead
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

package tk.elevenk.olapi.search;

import org.json.JSONException;
import org.json.JSONObject;
import tk.elevenk.olapi.data.BookList;

/**
 * Search Results data wrapper
 *
 * Created by John Krause on 12/26/14.
 */
public class SearchResults {

    // ****************************************
    // KEYS
    // ****************************************

    public static final String STARTING_BOOK_NUM = "start",
            NUMBER_OF_RESULTS = "num_found",
            BOOKS = "docs";

    private JSONObject results;

    public SearchResults(JSONObject json) {
        this.results = json;
    }

    public BookList getBooks() {
        BookList list;
        try {
            list = new BookList(results.getJSONArray(BOOKS));
        } catch (JSONException e) {
            list = new BookList();
        }
        return list;
    }

    public int getStartingBookNum() {
        try {
            return results.getInt(STARTING_BOOK_NUM);
        } catch (JSONException e) {
            return -1;
        }
    }

    public int getNumberOfResults() {
        try {
            return results.getInt(NUMBER_OF_RESULTS);
        } catch (JSONException e) {
            return -1;
        }
    }

    public JSONObject getUnderlyingJSON() {
        return results;
    }

}
