/*
 * BookData.java is a part of DailybRead
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

package tk.elevenk.olapi.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tk.elevenk.olapi.Library;
import tk.elevenk.olapi.books.BookQuery;
import tk.elevenk.olapi.logging.Log;
import tk.elevenk.olapi.read.ReadQuery;

import java.util.*;

/**
 * Class that handles all of the data for books.
 *
 * Created by John Krause on 12/26/14.
 */
public class BookData {

    // ******************************************
    // KEYS FROM SEARCH
    // ******************************************

    private static final String EDITION_KEY = "edition_key", TITLE_SEARCH = "title", TITLE_SUGGEST = "title_suggest",
            AUTHOR = "author_name", SUBJECT = "subject", PUBLISH_DATE = "publish_date",
            NUM_EBOOKS = "ebook_count_i", COVER_ID = "cover_i", HAS_FULL_TEXT = "has_fulltext",
            OCLC = "oclc", ISBN = "isbn", TEXT_SEARCH = "text", AUTHOR_KEY = "author_key", PUBLISHER = "publisher",
            COVER_EDITION_KEY = "cover_edition_key", PUBLISH_YEARS = "publish_year",
            FIRST_PUBLISHED_YEAR = "first_publish_year", CONTRIBUTOR = "contributor",
            NUM_EDITIONS = "edition_count", PUBLIC_SCANNED_BOOK = "public_scan_b",
            IA_LOADED_ID = "ia_loaded_id", SEED = "seed", IA = "ia", IA_COLLECTION_S = "ia_collection_s",
            TYPE = "type", PUBLISH_PLACE = "publish_place", IA_BOX_ID = "ia_box_id", OVERDIRVE_S = "overdrive_s",
            ID_LIBRIS = "id_libris", KEY_SEARCH = "key", ID_GOODREADS = "id_goodreads",
            AUTHOR_ALTERNATIVE_NAME = "author_alternative_name", ID_OVERDRIVE = "id_overdrive", LANGUAGE = "language",
            LCCN = "lccn", LAST_MODIFIED_I = "last_modified_i", ID_LIBRARYTHING = "id_librarything",
            FIRST_SENTENCE = "first_sentence", PERSON = "person", PRINT_DISABLED_S = "printdisabled_s",
            ID_DNB = "id_dnb";

    // ******************************************
    // KEYS FROM BOOK API
    // ******************************************

    private static final String TITLE_BOOK = "title", PUBLISHERS = "publishers", PUBLISHERS_NAME = "name", NUM_PAGES = "number_of_pages",
            IDENTIFIERS = "identifiers", LINKS = "links", URL = "url", COVERS = "cover", SUBJECTS = "subjects",
            KEY_BOOK = "key", AUTHORS = "authors", BY_STATEMENT = "by_statement", EXCEPTS = "excerpts",
            PUBLISH_PLACES = "publish_places", NOTES = "notes", EBOOKS = "ebooks", WEIGHT = "weight",
            FORMATS = "formats", EPUB = "epub", PDF = "pdf", DJVU = "djvu", TEXT_BOOK = "text", OLID = "OLID:",
            TABLE_OF_CONTENTS = "table_of_contents", TOC_TITLE = "title", TOC_LABEL = "label", TOC_PAGENUM = "pagenum",
            TOC_LEVEL = "level", PAGINATION = "pagination", CLASSIFICATIONS = "classifications";

    // ******************************************
    // KEYS FROM READ API
    // ******************************************

    private static final String RECORDS = "records", BOOKS = "/books/", DATA = "data", ITEMS = "items",
            STATUS = "status", FULL_ACCESS = "full access", OL_EDITION_ID = "ol-edition-id", OILD_LOWERCASE = "olid:";

    private JSONObject bookDataFromSearch, bookDataFromApi, readingDataFromApi;

    private String epubUrl, olid, title, firstPublishedYear, url, byStatement, coverEditionKey, notes,
            pagination, publishDate, key;
    private List<String> publishers, subjects, authors, excerpts, publishPlaces, publishYears,
            oclcs, editionKeys, authorKeys, publishDates, isbns, contributors, authorAlternativeNames, languages,
            firstSentences, persons;
    private Integer numEditions, numPages, numEbooks, coverId;
    private Boolean fullText, publicScannedBook;
    private Map<String, String> covers;
    private Map<String, List> identifiers, classifications;
    private List<Map<String, String>> tableOfContents, links, subjectsMap, authorsMap;
    private Object coverImage;

    private BookData(JSONObject search, JSONObject api, JSONObject reading) {
        this.bookDataFromSearch = search;
        this.bookDataFromApi = api;
        this.readingDataFromApi = reading;
    }

    public static BookData bookFromSearch(JSONObject data) {
        return new BookData(data, new JSONObject(), new JSONObject());
    }

    public static BookData bookFromBookApi(JSONObject data) {
        return new BookData(new JSONObject(), data, new JSONObject());
    }

    public static BookData bookFromReadingApi(JSONObject data) {
        return new BookData(new JSONObject(), new JSONObject(), data);
    }

    public String getByStatement() {
        if (this.byStatement == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.byStatement = bookDataFromApi.getString(BY_STATEMENT);
                } catch (Exception e) {
                    Log.e("Unable to get by statement", e);
                }
            }
        }
        return byStatement;
    }

    public String getKey() {
        if (this.key == null) {
            this.key = "";
            if (this.bookDataFromApi != null) {
                try {
                    this.key = bookDataFromApi.getString(KEY_BOOK);
                } catch (Exception e) {
                    Log.e("Unable to get key", e);
                }
            }
            if (this.key.isEmpty() && this.bookDataFromSearch != null) {
                try {
                    this.key = bookDataFromSearch.getString("key").replaceAll("/.*/", "");
                } catch (Exception e) {
                    try {
                        this.key = this.getEditionKeys().get(0);
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        Log.e("Unable to get key", e2);
                    }
                }
            }
        }
        return key;
    }

    public String getPublishDate() {
        if (this.publishDate == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.publishDate = bookDataFromApi.getString(NOTES);
                } catch (Exception e) {
                    Log.e("Unable to get publish date", e);
                }
            }
        }
        return publishDate;
    }

    public Map<String, String> getCovers() {
        if (this.covers == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.covers = new HashMap<String, String>();
                    JSONObject object = bookDataFromApi.getJSONObject(COVERS);
                    Iterator<String> iterator = object.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        covers.put(key, object.getString(key));
                    }
                } catch (Exception e) {
                    Log.e("Unable to get covers", e);
                }
            }
        }
        return covers;
    }

    public String getNotes() {
        if (this.notes == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.notes = bookDataFromApi.getString(NOTES);
                } catch (Exception e) {
                    Log.e("Unable to get notes", e);
                }
            }
        }
        return notes;
    }

    public String getPagination() {
        if (this.pagination == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.url = bookDataFromApi.getString(PAGINATION);
                } catch (Exception e) {
                    Log.e("Unable to get pagination", e);
                }
            }
        }
        return pagination;
    }

    public String getUrl() {
        if (this.url == null) {
            this.url = "";
            if (this.bookDataFromApi != null) {
                try {
                    this.url = bookDataFromApi.getString(URL);
                } catch (Exception e) {
                    Log.e("Unable to get URL", e);
                }
            }
            if (this.url.isEmpty() && this.bookDataFromSearch != null) {
                try {
                    this.url = bookDataFromSearch.getString(KEY_SEARCH);
                } catch (Exception e) {
                    Log.e("Unable to get url", e);
                }
            }
        }
        return url;
    }

    public Map<String, List> getClassifications() {
        if (this.classifications == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.classifications = new HashMap<String, List>();
                    JSONObject object = bookDataFromApi.getJSONObject(CLASSIFICATIONS);
                    Iterator<String> iterator = object.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONArray array = object.getJSONArray(key);
                        List<String> list = new ArrayList<String>();
                        for (int i = 0; i < array.length(); i++) {
                            list.add(array.getString(i));
                        }
                        classifications.put(key, list);
                    }
                } catch (Exception e) {
                    Log.e("Unable to parse classifications", e);
                }
            }
        }
        return classifications;
    }

    public Map<String, List> getIdentifiers() {
        if (this.identifiers == null) {
            if (this.bookDataFromApi != null) {
                try {
                    this.identifiers = new HashMap<String, List>();
                    JSONObject object = bookDataFromApi.getJSONObject(IDENTIFIERS);
                    Iterator<String> iterator = object.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONArray array = object.getJSONArray(key);
                        List<String> list = new ArrayList<String>();
                        for (int i = 0; i < array.length(); i++) {
                            list.add(array.getString(i));
                        }
                        identifiers.put(key, list);
                    }
                } catch (Exception e) {
                    Log.e("Unable to parse identifiers", e);
                }
            }
        }
        return identifiers;
    }

    public List<Map<String, String>> getLinks() {
        if (this.links == null) {
            if (this.bookDataFromApi != null) {
                try {
                    links = convertJSONArrayOfJSONObjectsToListOfMaps(bookDataFromApi.getJSONArray(LINKS));
                } catch (Exception e) {
                    Log.e("Unable to parse links", e);
                }
            }
        }
        return links;
    }

    public List<Map<String, String>> getAuthorsMap() {
        if (this.authorsMap == null) {
            if (this.bookDataFromApi != null) {
                try {
                    authorsMap = convertJSONArrayOfJSONObjectsToListOfMaps(bookDataFromApi.getJSONArray(AUTHORS));
                } catch (Exception e) {
                    Log.e("Unable to parse subjects", e);
                }
            }
        }
        return authorsMap;
    }

    public List<Map<String, String>> getSubjectsMap() {
        if (this.subjectsMap == null) {
            if (this.bookDataFromApi != null) {
                try {
                    subjectsMap = convertJSONArrayOfJSONObjectsToListOfMaps(bookDataFromApi.getJSONArray(SUBJECTS));
                } catch (Exception e) {
                    Log.e("Unable to parse subjects", e);
                }
            }
        }
        return subjectsMap;
    }

    public List<Map<String, String>> getTableOfContents() {
        if (this.tableOfContents == null) {
            if (this.bookDataFromApi != null) {
                try {
                    tableOfContents = convertJSONArrayOfJSONObjectsToListOfMaps(bookDataFromApi.getJSONArray(TABLE_OF_CONTENTS));
                } catch (Exception e) {
                    Log.e("Unable to parse table of contents", e);
                }
            }
        }
        return tableOfContents;
    }

    public List<String> getPersons() {
        if (this.persons == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.persons = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(PUBLISH_PLACE));
                } catch (JSONException e) {
                    Log.e("Unable to get publish places", e);
                }
            }
        }
        return persons;
    }

    public List<String> getFirstSentences() {
        if (this.firstSentences == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.firstSentences = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(FIRST_SENTENCE));
                } catch (JSONException e) {
                    Log.e("Unable to get publish places", e);
                }
            }
        }
        return firstSentences;
    }

    public List<String> getLanguages() {
        if (this.languages == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.languages = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(LANGUAGE));
                } catch (JSONException e) {
                    Log.e("Unable to get publish places", e);
                }
            }
        }
        return languages;
    }

    public List<String> getAuthorAlternativeNames() {
        if (this.authorAlternativeNames == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.authorAlternativeNames = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(AUTHOR_ALTERNATIVE_NAME));
                } catch (JSONException e) {
                    Log.e("Unable to get author alternative names", e);
                }
            }
        }
        return authorAlternativeNames;
    }

    public List<String> getPublishPlaces() {
        if (this.publishPlaces == null) {
            this.publishPlaces = new ArrayList<String>();
            if (this.bookDataFromSearch != null) {
                try {
                    this.publishPlaces = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(PUBLISH_PLACE));
                } catch (JSONException e) {
                    Log.e("Unable to get publish places", e);
                }
            }
            if (publishPlaces.size() == 0 && this.bookDataFromApi != null) {
                try {
                    List<Map<String, String>> mapList = convertJSONArrayOfJSONObjectsToListOfMaps(bookDataFromApi.getJSONArray(PUBLISH_PLACES));
                    for (Map<String, String> map : mapList) {
                        publishPlaces.add(map.values().iterator().next());
                    }
                } catch (Exception e) {
                    Log.e("Unable to parse publish places", e);
                }
            }
        }
        return publishPlaces;
    }

    public String getTitle() {
        if (this.title == null) {
            this.title = "";
            if (this.bookDataFromApi != null) {
                try {
                    this.title = bookDataFromApi.getString(TITLE_BOOK);
                } catch (JSONException e) {
                    Log.e("Unable to get title", e);
                }
            }
            if (this.title.isEmpty() && this.bookDataFromSearch != null) {
                try {
                    this.title = bookDataFromSearch.getString(TITLE_SEARCH);
                } catch (JSONException e) {
                    Log.e("Unable to get title", e);
                }
            }
        }
        return title;
    }

    public List<String> getAuthorNames() {
        if (this.authors == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.authors = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(AUTHOR));
                } catch (JSONException e) {
                    Log.e("Unable to get author name", e);
                }
            }
        }
        return authors;
    }

    public List<String> getSubjects() {
        if (this.subjects == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.subjects = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(SUBJECT));
                } catch (JSONException e) {
                    Log.e("Unable to get subject", e);
                }
            }
        }
        return subjects;
    }

    public List<String> getPublishDates() {
        if (this.publishDates == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.publishDates = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(PUBLISH_DATE));
                } catch (JSONException e) {
                    Log.e("Unable to get publish date", e);
                }
            }
        }
        return publishDates;
    }

    public List<String> getOclcs() {
        if (this.oclcs == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.oclcs = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(OCLC));
                } catch (JSONException e) {
                    Log.e("Unable to get oclc", e);
                }
            }
        }
        return oclcs;
    }

    public List<String> getPublishers() {
        if (this.publishers == null) {
            this.publishers = new ArrayList<String>();
            if (this.bookDataFromSearch != null) {
                try {
                    this.publishers = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(PUBLISHER));
                } catch (JSONException e) {
                    Log.e("Unable to get publisher", e);
                }
            }
            if (this.publishers.size() == 0 && this.bookDataFromApi != null) {
                try {
                    JSONArray array = bookDataFromApi.getJSONArray(PUBLISHERS);
                    for (int i = 0; i < array.length(); i++) {
                        this.publishers.add(array.getJSONObject(i).getString(PUBLISHERS_NAME));
                    }
                } catch (Exception e) {
                    Log.e("Unable to get publishers", e);
                }
            }
        }
        return publishers;
    }

    public List<String> getPublishYears() {
        if (this.publishYears == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.publishYears = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(PUBLISH_YEARS));
                } catch (JSONException e) {
                    Log.e("Unable to get publish year", e);
                }
            }
        }
        return publishYears;
    }

    public String getFirstPublishYear() {
        if (this.firstPublishedYear == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.firstPublishedYear = bookDataFromSearch.getString(FIRST_PUBLISHED_YEAR);
                } catch (JSONException e) {
                    Log.e("Unable to get first published year", e);
                }
            }
        }
        return firstPublishedYear;
    }

    public int getNumEditions() {
        if (this.numEditions == null) {
            numEditions = 0;
            if (this.bookDataFromSearch != null) {
                try {
                    this.numEditions = bookDataFromSearch.getInt(NUM_EDITIONS);
                } catch (JSONException e) {
                    Log.e("Unable to get num editions", e);
                }
            }
        }
        return numEditions;
    }

    public int getNumPages() {
        if (this.numPages == null) {
            numPages = 0;
            if (this.bookDataFromSearch != null) {
                try {
                    this.numPages = bookDataFromSearch.getInt(NUM_PAGES);
                } catch (JSONException e) {
                    Log.e("Unable to get num pages", e);
                }
            }
        }
        return numPages;
    }

    public int getNumEbooks() {
        if (this.numEbooks == null) {
            numEbooks = 0;
            if (this.bookDataFromSearch != null) {
                try {
                    this.numEbooks = bookDataFromSearch.getInt(NUM_EBOOKS);
                } catch (JSONException e) {
                    Log.e("Unable to get num ebooks", e);
                }
            }
        }
        return numEbooks;
    }

    public List<String> getAuthorKeys() {
        if (this.authorKeys == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.authorKeys = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(AUTHOR_KEY));
                } catch (JSONException e) {
                    Log.e("Unable to get author key", e);
                }
            }
        }
        return authorKeys;
    }

    public int getCoverId() {
        if (this.coverId == null) {
            coverId = 0;
            if (this.bookDataFromSearch != null) {
                try {
                    this.coverId = bookDataFromSearch.getInt(COVER_ID);
                } catch (JSONException e) {
                    Log.e("Unable to get cover id", e);
                }
            }
        }
        return coverId;
    }

    public String getCoverEditionKey() {
        if (this.coverEditionKey == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.coverEditionKey = bookDataFromSearch.getString(COVER_EDITION_KEY);
                } catch (JSONException e) {
                    Log.e("Unable to get cover edition key", e);
                }
            }
        }
        return coverEditionKey;
    }

    public boolean hasFullText() {
        if (fullText == null) {
            fullText = false;
            if (this.bookDataFromSearch != null) {
                try {
                    this.fullText = bookDataFromSearch.getBoolean(HAS_FULL_TEXT);
                } catch (JSONException e) {
                    Log.e("Unable to get fullText", e);
                }
            }
        }
        return fullText;
    }

    public List<String> getEditionKeys() {
        if (this.editionKeys == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.editionKeys = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(EDITION_KEY));
                } catch (JSONException e) {
                    Log.e("Unable to get edition keys", e);
                }
            }
        }
        return editionKeys;
    }

    public String getOlid() {
        if (this.olid == null) {
            if (this.bookDataFromApi != null) {
                try {
                    olid = this.bookDataFromApi.keys().next().replaceAll("OLID:?", "");
                } catch (Exception e) {
                    try {
                        olid = this.getIdentifiers().get("openlibrary").get(0).toString();
                    } catch (Exception e2) {
                        try {
                            String key = this.getKey();
                            if (key.contains("OL")) {
                                olid = key;
                            }
                        } catch (Exception e3) {
                            e2.addSuppressed(e);
                            e3.addSuppressed(e2);
                            Log.e("Unable to get olid", e3);
                        }
                    }
                }
            }
        }
        return olid;
    }

    public boolean isPublicScannedBook() {
        if (this.publicScannedBook == null) {
            publicScannedBook = false;
            if (this.bookDataFromSearch != null) {
                try {
                    publicScannedBook = this.bookDataFromSearch.getBoolean(PUBLIC_SCANNED_BOOK);
                } catch (Exception e) {
                    Log.e("Unable to get public scanned book", e);
                }
            }
        }
        return publicScannedBook;
    }

    public List<String> getIsbns() {
        if (this.isbns == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.isbns = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(ISBN));
                } catch (JSONException e) {
                    Log.e("Unable to get ISBNs", e);
                }
            }
        }
        return isbns;
    }

    public List<String> getContributors() {
        if (this.contributors == null) {
            if (this.bookDataFromSearch != null) {
                try {
                    this.contributors = convertJSONArrayToStringList(bookDataFromSearch.getJSONArray(CONTRIBUTOR));
                } catch (JSONException e) {
                    Log.e("Unable to get contributors", e);
                }
            }
        }
        return contributors;
    }

    public void addBookDetails(Library lib) {
        this.addBookDetails(lib, 0);
    }

    public void addBookDetails(Library lib, int edition) {
        if (bookDataFromApi.length() <= 0) {
            BookQuery query = new BookQuery();
            try {
                query.olid(getEditionKeys().get(edition));
            } catch (Exception e) {
                try {
                    query.olid(getKey().replaceAll("/.*/", ""));
                } catch (Exception e2) {
                    try {
                        query.olid(getOlid());
                    } catch (Exception e3) {
                        e2.addSuppressed(e);
                        e3.addSuppressed(e2);
                        Log.e("Unable to add book details", e3);
                    }
                }
            }
            if (query.size() > 0) {
                this.bookDataFromApi = lib.getBookDetails(query).getBookData();
            }
        }
    }

    public void addReadingDetails(Library lib) {
        this.addReadingDetails(lib, 0);
    }

    public void addReadingDetails(Library lib, int edition) {
        if (readingDataFromApi.length() <= 0) {
            ReadQuery query = new ReadQuery();
            try {
                query.olid(getEditionKeys().get(edition));
            } catch (NullPointerException e) {
                try {
                    query.olid(getOlid());
                } catch (Exception e2) {
                    Log.e("Unable to get olid for reading api", e2);
                }
            }
            if (query.size() > 0) {
                this.readingDataFromApi = lib.getReadingDetails(query).getReadingData();
            }
        }
    }

    public JSONObject getSearchData() {
        return bookDataFromSearch;
    }

    public JSONObject getBookData() {
        return bookDataFromApi;
    }

    public JSONObject getReadingData() {
        return readingDataFromApi;
    }

    public boolean hasEpubUrl() {
        boolean value = false;
        getEpubUrl();
        if (!(this.epubUrl == null) && !(this.epubUrl.isEmpty())) {
            value = true;
        }
        return value;
    }

    public Object getEpubUrl() {
        Object data;
        if (this.epubUrl == null) {
            data = this.getEbookUrl(EPUB);
            if (data instanceof String) {
                this.epubUrl = data.toString();
            }
        } else {
            data = this.epubUrl;
        }
        return data;
    }

    public Object getPdfUrl() {
        return this.getEbookUrl(PDF);
    }

    public Object getDjvuUrl() {
        return this.getEbookUrl(DJVU);
    }

    public Object getTextUrl() {
        return this.getEbookUrl(TEXT_BOOK);
    }

    private Object getEbookUrl(String format) {
        String data = "";
        Object returnedData = data;
        ArrayList<String> alternatives = new ArrayList<String>();
        if (this.bookDataFromApi.length() > 0) {
            try {
                data = bookDataFromApi.getJSONObject(bookDataFromApi.keys().next()).getJSONArray(EBOOKS).getJSONObject(0)
                        .getJSONObject(FORMATS).getJSONObject(format).getString(URL);
            } catch (JSONException e) {
                Log.e("Unable to get ebook data", e);
            }
        }
        if (data.isEmpty() && this.readingDataFromApi.length() > 0) {
            try {
                data = readingDataFromApi.getJSONObject(readingDataFromApi.keys().next()).getJSONObject(RECORDS)
                        .getJSONObject(BOOKS + getOlid()).getJSONObject(DATA).getJSONArray(EBOOKS).getJSONObject(0)
                        .getJSONObject(FORMATS).getJSONObject(format).getString(URL);
            } catch (JSONException e) {
                Log.e("Unable to get ebook data", e);
            }
        }
        // no epub url found on this edition. return a list of other editions to try
        if (data.isEmpty() && this.readingDataFromApi.length() > 0) {

            try {
                JSONArray items = readingDataFromApi.getJSONObject(readingDataFromApi.keys().next())
                        .getJSONArray(ITEMS);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject object = (JSONObject) items.get(i);
                    if (object.get(STATUS).equals(FULL_ACCESS)) {
                        alternatives.add(object.getString(OL_EDITION_ID));
                    }
                }
                if (alternatives.size() > 0) {
                    returnedData = alternatives;
                }
            } catch (JSONException e) {
                //TODO
                Log.e("", e);
            }
        } else {
            returnedData = data;
        }
        return returnedData;
    }

    private List<String> convertJSONArrayToStringList(JSONArray array) {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    private List<Map<String, String>> convertJSONArrayOfJSONObjectsToListOfMaps(JSONArray array) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 0; i < array.length(); i++) {
            Map<String, String> map = new HashMap<String, String>();
            JSONObject object = array.getJSONObject(i);
            Iterator<String> iterator = object.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                map.put(key, object.getString(key));
            }
            list.add(map);
        }
        return list;
    }

    public Object getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Object coverImage) {
        this.coverImage = coverImage;
    }
}
