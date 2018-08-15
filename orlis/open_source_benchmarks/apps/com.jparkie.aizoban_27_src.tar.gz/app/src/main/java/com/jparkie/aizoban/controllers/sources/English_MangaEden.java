package com.jparkie.aizoban.controllers.sources;

import android.database.sqlite.SQLiteDatabase;

import com.jparkie.aizoban.controllers.caches.CacheProvider;
import com.jparkie.aizoban.controllers.databases.ApplicationContract;
import com.jparkie.aizoban.controllers.databases.ApplicationSQLiteOpenHelper;
import com.jparkie.aizoban.controllers.databases.LibraryContract;
import com.jparkie.aizoban.controllers.databases.LibrarySQLiteOpenHelper;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.controllers.networks.MangaService;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class English_MangaEden implements Source {
    public static final String NAME = "MangaEden (EN)";
    public static final String BASE_URL = "www.mangaeden.com";

    private static final String INITIAL_UPDATE_URL = "http://www.mangaeden.com/ajax/news/1/0";

    @Override
    public Observable<String> getName() {
        return Observable.just(NAME);
    }

    @Override
    public Observable<String> getBaseUrl() {
        return Observable.just(BASE_URL);
    }

    @Override
    public Observable<String> getInitialUpdateUrl() {
        return Observable.just(INITIAL_UPDATE_URL);
    }

    @Override
    public Observable<List<String>> getGenres() {
        List<String> genres = new ArrayList<String>();

        return Observable.just(genres);
    }

    @Override
    public Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(final UpdatePageMarker newUpdate) {
        return MangaService.getPermanentInstance()
                .getResponse(newUpdate.getNextPageUrl())
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return MangaService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<UpdatePageMarker>>() {
                    @Override
                    public Observable<UpdatePageMarker> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToLatestUpdates(newUpdate, unparsedHtml));
                    }
                });
    }

    private UpdatePageMarker parseHtmlToLatestUpdates(UpdatePageMarker newUpdate, String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Manga> updatedMangaList = scrapeUpdateMangasFromParsedDocument(parsedDocument);
        updateLibraryInDatabase(updatedMangaList);

        String nextPageUrl = findNextUrlFromUpdatePageMarker(newUpdate);
        int lastMangaPostion = updatedMangaList.size();

        return new UpdatePageMarker(nextPageUrl, lastMangaPostion);
    }

    private List<Manga> scrapeUpdateMangasFromParsedDocument(Document parsedDocument) {
        List<Manga> updatedMangaList = new ArrayList<Manga>();

        Elements updatedHtmlBlocks = parsedDocument.select("body > li");
        for (Element currentHtmlBlock : updatedHtmlBlocks) {
            Manga currentlyUpdatedManga = constructMangaFromHtmlBlock(currentHtmlBlock);

            updatedMangaList.add(currentlyUpdatedManga);
        }

        return updatedMangaList;
    }

    private Manga constructMangaFromHtmlBlock(Element htmlBlock) {
        Manga mangaFromHtmlBlock = DefaultFactory.Manga.constructDefault();
        mangaFromHtmlBlock.setSource(NAME);

        Element urlElement = htmlBlock.select("div.newsManga").first();
        Element nameElement = htmlBlock.select("div.manga_tooltop_header > a").first();
        Element updateElement = htmlBlock.select("div.chapterDate").first();

        if (urlElement != null) {
            String fieldUrl = "https://www.mangaeden.com/api/manga/" + urlElement.id().substring(0, 24) + "/";
            mangaFromHtmlBlock.setUrl(fieldUrl);
        }
        if (nameElement != null) {
            String fieldName = nameElement.text();
            mangaFromHtmlBlock.setName(fieldName);
        }
        if (updateElement != null) {
            long fieldUpdate = parseUpdateFromElement(updateElement);
            mangaFromHtmlBlock.setUpdated(fieldUpdate);
        }

        int updateCount = htmlBlock.select("div.chapterDate").size();
        mangaFromHtmlBlock.setUpdateCount(updateCount);

        return mangaFromHtmlBlock;
    }

    private long parseUpdateFromElement(Element updateElement) {
        String updatedDateAsString = updateElement.text();

        if (updatedDateAsString.contains("Today")) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            try {
                Date withoutDay = new SimpleDateFormat("h:mm a", Locale.ENGLISH).parse(updatedDateAsString.replace("Today", ""));
                return today.getTimeInMillis() + withoutDay.getTime();
            } catch (ParseException e) {
                return today.getTimeInMillis();
            }
        } else if (updatedDateAsString.contains("Yesterday")) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);

            try {
                Date withoutDay = new SimpleDateFormat("h:mm a", Locale.ENGLISH).parse(updatedDateAsString.replace("Yesterday", ""));
                return yesterday.getTimeInMillis() + withoutDay.getTime();
            } catch (ParseException e) {
                return yesterday.getTimeInMillis();
            }
        } else {
            try {
                Date specificDate = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).parse(updatedDateAsString);

                return specificDate.getTime();
            } catch (ParseException e) {
                // Do Nothing.
            }
        }

        return DefaultFactory.Manga.DEFAULT_UPDATED;
    }

    private void updateLibraryInDatabase(List<Manga> mangaList) {
        LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();

        SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            for (Manga currentManga : mangaList) {
                StringBuilder selection = new StringBuilder();
                List<String> selectionArgs = new ArrayList<String>();

                selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
                selectionArgs.add(NAME);
                selection.append(" AND ").append(LibraryContract.Manga.COLUMN_URL + " = ?");
                selectionArgs.add(currentManga.getUrl());

                Manga existingManga = cupboard().withDatabase(sqLiteDatabase).query(Manga.class)
                        .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                        .limit(1)
                        .get();

                if (existingManga != null) {
                    existingManga.setUpdated(currentManga.getUpdated());
                    existingManga.setUpdateCount(currentManga.getUpdateCount());

                    cupboard().withDatabase(sqLiteDatabase).put(existingManga);
                }
            }

            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    private String findNextUrlFromUpdatePageMarker(UpdatePageMarker newUpdate) {
        String requestUrl = newUpdate.getNextPageUrl();

        if (!requestUrl.equals(DefaultFactory.UpdatePageMarker.DEFAULT_NEXT_PAGE_URL)) {
            String currentPageNumber = requestUrl.substring(0, requestUrl.lastIndexOf("/0")).replaceAll("[^\\d]", "");

            int newPageNumber = Integer.parseInt(currentPageNumber) + 1;
            return "http://www.mangaeden.com/ajax/news/" + newPageNumber + "/0";
        }

        return DefaultFactory.UpdatePageMarker.DEFAULT_NEXT_PAGE_URL;
    }

    @Override
    public Observable<Manga> pullMangaFromNetwork(final RequestWrapper request) {
        return MangaService.getPermanentInstance()
                .getResponse(request.getUrl())
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return MangaService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<Manga>>() {
                    @Override
                    public Observable<Manga> call(final String unparsedJson) {
                        return Observable.create(new Observable.OnSubscribe<Manga>() {
                            @Override
                            public void call(Subscriber<? super Manga> subscriber) {
                                try {
                                    subscriber.onNext(parseJsonToManga(request, unparsedJson));
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    private Manga parseJsonToManga(RequestWrapper request, String unparsedJson) throws JSONException {
        JSONObject parsedJsonObject = new JSONObject(unparsedJson);

        String fieldGenre = "";
        JSONArray genreArrayNodes = parsedJsonObject.getJSONArray("categories");
        for (int index = 0; index < genreArrayNodes.length(); index++) {
            if (index != genreArrayNodes.length() - 1) {
                fieldGenre += genreArrayNodes.getString(index) + ", ";
            } else {
                fieldGenre += genreArrayNodes.getString(index);
            }
        }

        LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();
        SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
        selectionArgs.add(NAME);
        selection.append(" AND ").append(LibraryContract.Manga.COLUMN_URL + " = ?");
        selectionArgs.add(request.getUrl());

        Manga newManga = cupboard().withDatabase(sqLiteDatabase).query(Manga.class)
                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                .limit(1)
                .get();

        newManga.setArtist(parsedJsonObject.getString("artist"));
        newManga.setAuthor(parsedJsonObject.getString("author"));
        newManga.setDescription(parsedJsonObject.getString("description").trim());
        newManga.setGenre(fieldGenre);
        newManga.setCompleted(parsedJsonObject.getInt("status") == 2);
        newManga.setThumbnailUrl("https://cdn.mangaeden.com/mangasimg/" + parsedJsonObject.getString("image"));

        newManga.setInitialized(true);

        cupboard().withDatabase(sqLiteDatabase).put(newManga);

        return newManga;
    }

    @Override
    public Observable<List<Chapter>> pullChaptersFromNetwork(final RequestWrapper request) {
        return MangaService.getPermanentInstance()
                .getResponse(request.getUrl())
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return MangaService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<List<Chapter>>>() {
                    @Override
                    public Observable<List<Chapter>> call(final String unparsedJson) {
                        return Observable.create(new Observable.OnSubscribe<List<Chapter>>() {
                            @Override
                            public void call(Subscriber<? super List<Chapter>> subscriber) {
                                try {
                                    subscriber.onNext(parseJsonToChapters(request, unparsedJson));
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    private List<Chapter> parseJsonToChapters(RequestWrapper request, String unparsedJson) throws JSONException{
        JSONObject parsedJsonObject = new JSONObject(unparsedJson);

        List<Chapter> chapterList = scrapeChaptersFromParsedJson(parsedJsonObject);
        chapterList = setSourceForChapterList(chapterList);
        chapterList = setParentUrlForChapterList(chapterList, request.getUrl());
        chapterList = setNumberForChapterList(chapterList);

        saveChaptersToDatabase(chapterList, request.getUrl());

        return chapterList;
    }

    private List<Chapter> scrapeChaptersFromParsedJson(JSONObject parsedJsonObject) throws JSONException {
        List<Chapter> chapterList = new ArrayList<Chapter>();

        String mangaName = parsedJsonObject.getString("title");
        JSONArray chapterArrayNodes = parsedJsonObject.getJSONArray("chapters");
        for (int index = 0; index < chapterArrayNodes.length(); index++) {
            JSONArray currentChapterArray = chapterArrayNodes.getJSONArray(index);

            Chapter currentChapter = constructChapterFromJSONArray(currentChapterArray, mangaName);

            chapterList.add(currentChapter);
        }

        return chapterList;
    }

    private Chapter constructChapterFromJSONArray(JSONArray chapterNode, String mangaName) throws JSONException {
        Chapter newChapter = DefaultFactory.Chapter.constructDefault();

        newChapter.setUrl("https://www.mangaeden.com/api/chapter/" + chapterNode.getString(3) + "/");
        newChapter.setName(mangaName + " " + chapterNode.getDouble(0));
        newChapter.setDate(chapterNode.getLong(1) * 1000);

        return newChapter;
    }

    private List<Chapter> setSourceForChapterList(List<Chapter> chapterList) {
        for (Chapter currentChapter : chapterList) {
            currentChapter.setSource(NAME);
        }

        return chapterList;
    }

    private List<Chapter> setParentUrlForChapterList(List<Chapter> chapterList, String parentUrl) {
        for (Chapter currentChapter : chapterList) {
            currentChapter.setParentUrl(parentUrl);
        }

        return chapterList;
    }

    private List<Chapter> setNumberForChapterList(List<Chapter> chapterList) {
        Collections.reverse(chapterList);
        for (int index = 0; index < chapterList.size(); index++) {
            chapterList.get(index).setNumber(index + 1);
        }

        return chapterList;
    }

    private void saveChaptersToDatabase(List<Chapter> chapterList, String parentUrl) {
        ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper = ApplicationSQLiteOpenHelper.getInstance();
        SQLiteDatabase sqLiteDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        selection.append(ApplicationContract.Chapter.COLUMN_SOURCE + " = ?");
        selectionArgs.add(NAME);
        selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_PARENT_URL + " = ?");
        selectionArgs.add(parentUrl);

        sqLiteDatabase.beginTransaction();
        try {
            cupboard().withDatabase(sqLiteDatabase).delete(Chapter.class, selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]));

            for (Chapter currentChapter : chapterList) {
                cupboard().withDatabase(sqLiteDatabase).put(currentChapter);
            }

            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public Observable<String> pullImageUrlsFromNetwork(final RequestWrapper request) {
        return MangaService.getPermanentInstance()
                .getResponse(request.getUrl())
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return MangaService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(final String unparsedJson) {
                        return Observable.create(new Observable.OnSubscribe<List<String>>() {
                            @Override
                            public void call(Subscriber<? super List<String>> subscriber) {
                                try {
                                    subscriber.onNext(parseJsonToImageUrls(unparsedJson));
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .doOnNext(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> imageUrls) {
                        CacheProvider.getInstance().putImageUrlsToDiskCache(request.getUrl(), imageUrls);
                    }
                })
                .flatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> imageUrls) {
                        return Observable.from(imageUrls.toArray(new String[imageUrls.size()]));
                    }
                });
    }

    private List<String> parseJsonToImageUrls(String unparsedJson) throws JSONException{
        JSONObject parsedJson = new JSONObject(unparsedJson);

        List<String> imageUrlList = new ArrayList<String>();

        JSONArray imageArrayNodes = parsedJson.getJSONArray("images");
        for (int index = 0; index < imageArrayNodes.length(); index++) {
            JSONArray currentImageNode = imageArrayNodes.getJSONArray(index);

            imageUrlList.add("https://cdn.mangaeden.com/mangasimg/" + currentImageNode.getString(1));
        }
        Collections.reverse(imageUrlList);

        return imageUrlList;
    }

    @Override
    public Observable<String> recursivelyConstructDatabase(final String url) {
        return MangaService.getPermanentInstance()
                .getResponse(url)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return MangaService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(final String unparsedJson) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                try {
                                    subscriber.onNext(parseEnglish_MangaHere(unparsedJson));
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    private static String INITIAL_DATABASE_URL = "https://www.mangaeden.com/api/list/0/";

    private String parseEnglish_MangaHere(String unparsedJson) throws JSONException {
        JSONObject parsedJson = new JSONObject(unparsedJson);

        List<Manga> mangaList = new ArrayList<Manga>();
        JSONArray mangaArrayNodes = parsedJson.getJSONArray("manga");
        for (int index = 0; index < mangaArrayNodes.length(); index++) {
            JSONObject currentMangaNode = mangaArrayNodes.getJSONObject(index);

            Manga newManga = new Manga();
            newManga.setSource(NAME);
            newManga.setUrl("https://www.mangaeden.com/api/manga/" + currentMangaNode.getString("i") + "/");
            newManga.setName(currentMangaNode.getString("t"));
            newManga.setThumbnailUrl("https://cdn.mangaeden.com/mangasimg/" + currentMangaNode.getString("im"));
            newManga.setCompleted(currentMangaNode.getInt("s") == 2);
            newManga.setRank(currentMangaNode.getInt("h")); // Hits.

            mangaList.add(newManga);
        }

        Collections.sort(mangaList, new Comparator<Manga>() {
            @Override
            public int compare(Manga lhs, Manga rhs) {
                if (lhs.getRank() < rhs.getRank()) {
                    return 1;
                } else if (lhs.getRank() == rhs.getRank()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        for (int index = 0; index < mangaList.size(); index++) {
            mangaList.get(index).setRank(index + 1);
        }

        LibrarySQLiteOpenHelper librarySQLiteOpenHelper = LibrarySQLiteOpenHelper.getInstance();

        SQLiteDatabase sqLiteDatabase = librarySQLiteOpenHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            for (Manga currentManga : mangaList) {
                cupboard().withDatabase(sqLiteDatabase).put(currentManga);
            }

            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }

        return null;
    }
}
