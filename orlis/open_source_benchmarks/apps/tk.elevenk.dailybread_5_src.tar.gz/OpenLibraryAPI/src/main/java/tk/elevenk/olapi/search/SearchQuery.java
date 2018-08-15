/*
 * SearchQuery.java is a part of DailybRead
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

import java.util.HashMap;

/**
 * Query object used when calling the Search API
 *
 * Created by John Krause on 12/26/14.
 */
public class SearchQuery extends HashMap<String, String> {

    // ****************************************
    // KEYS
    // ****************************************

    public static final String QUERY = "q",
            HAS_FULL_TEXT = "has_fulltext",
            TITLE = "title",
            AUTHOR = "author",
            ISBN = "isbn",
            SUBJECT = "subject",
            PLACE = "place",
            PERSON = "person",
            PUBLISHER = "publisher",
            PAGE = "page",
            LANGUAGE = "language",
            SORT = "sort";

    public String query(String query) {
        if (query != null && query.length() > 0)
            return this.put(QUERY, query.trim());
        return null;
    }

    public String hasFullText() {
        return this.hasFullText("true");
    }

    public String hasFullText(boolean flag) {
        return this.hasFullText(String.valueOf(flag));
    }

    private String hasFullText(String bool) {
        return this.put(HAS_FULL_TEXT, bool);
    }

    public String title(String title) {
        if (title != null && title.length() > 0)
            return this.put(TITLE, title.trim());
        return null;
    }

    public String author(String author) {
        if (author != null && author.length() > 0)
            return this.put(AUTHOR, author.trim());
        return null;
    }

    public String isbn(String isbn) {
        if (isbn != null && isbn.length() > 0)
            return this.put(ISBN, isbn.trim());
        return null;
    }

    public String subject(String subject) {
        if (subject != null && subject.length() > 0)
            return this.put(SUBJECT, subject.trim());
        return null;
    }

    public String place(String place) {
        if (place != null && place.length() > 0)
            return this.put(PLACE, place.trim());
        return null;
    }

    public String person(String person) {
        if (person != null && person.length() > 0)
            return this.put(PERSON, person.trim());
        return null;
    }

    public String publisher(String publisher) {
        if (publisher != null && publisher.length() > 0)
            return this.put(PUBLISHER, publisher.trim());
        return null;
    }

    public String page(int page) {
        if (page > 0)
            return this.page(String.valueOf(page).trim());
        return null;
    }

    private String page(String page) {
        if (page != null && page.length() > 0)
            return this.put(PAGE, page.trim());
        return null;
    }

    public String language(String lang) {
        if (lang != null && lang.length() > 0)
            return this.put(LANGUAGE, lang);
        return null;
    }

    public String sort(String method) {
        if (method != null && method.length() > 0)
            return this.put(SORT, method.trim());
        return null;
    }
}
