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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class Spanish_MangaHere implements Source {
    public static final String NAME = "MangaHere (ES)";
    public static final String BASE_URL = "es.mangahere.co";

    private static final String INITIAL_UPDATE_URL = "http://www.mangahere.co/latest/";

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
        List<String> genres = new ArrayList<String>(29);

        genres.add("Acción");
        genres.add("Aventura");
        genres.add("Comedia");
        genres.add("Doujinshi");
        genres.add("Drama");
        genres.add("Ecchi");
        genres.add("Fantasía");
        genres.add("Gender Bender");
        genres.add("Harem");
        genres.add("Histórico");
        genres.add("Horror");
        genres.add("Josei");
        genres.add("Artes Marciales");
        genres.add("Mature");
        genres.add("Mecha");
        genres.add("Misterio");
        genres.add("One Shot");
        genres.add("Psicológico");
        genres.add("Romance");
        genres.add("Escolar");
        genres.add("Ciencia Ficción");
        genres.add("Seinen");
        genres.add("Shojo");
        genres.add("Shojo Ai");
        genres.add("Shonen");
        genres.add("Shonen Ai");
        genres.add("Vida Cotidiana");
        genres.add("Deportes");
        genres.add("Sobrenatural");
        genres.add("Tragedia");

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
                        return Observable.just(parseHtmlToLatestUpdates(unparsedHtml));
                    }
                });
    }

    private UpdatePageMarker parseHtmlToLatestUpdates(String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Manga> updatedMangaList = scrapeUpdateMangasFromParsedDocument(parsedDocument);
        updateLibraryInDatabase(updatedMangaList);

        String nextPageUrl = findNextUrlFromParsedDocument(parsedDocument);
        int lastMangaPostion = updatedMangaList.size();

        return new UpdatePageMarker(nextPageUrl, lastMangaPostion);
    }

    private List<Manga> scrapeUpdateMangasFromParsedDocument(Document parsedDocument) {
        List<Manga> updatedMangaList = new ArrayList<Manga>();

        Elements updatedHtmlBlocks = parsedDocument.select("div.manga_updates dl");
        for (Element currentHtmlBlock : updatedHtmlBlocks) {
            Manga currentlyUpdatedManga = constructMangaFromHtmlBlock(currentHtmlBlock);

            updatedMangaList.add(currentlyUpdatedManga);
        }

        return updatedMangaList;
    }

    private Manga constructMangaFromHtmlBlock(Element htmlBlock) {
        Manga mangaFromHtmlBlock = DefaultFactory.Manga.constructDefault();
        mangaFromHtmlBlock.setSource(NAME);

        Element urlElement = htmlBlock.select("a.manga_info").first();
        Element nameElement = htmlBlock.select("a.manga_info").first();
        Element updateElement = htmlBlock.select("span.time").first();

        if (urlElement != null) {
            String fieldUrl = urlElement.attr("href");
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

        int updateCount = htmlBlock.select("dd a").size();
        mangaFromHtmlBlock.setUpdateCount(updateCount);

        return mangaFromHtmlBlock;
    }

    private long parseUpdateFromElement(Element updateElement) {
        String updatedDateAsString = updateElement.text();

        if (updatedDateAsString.contains("Today") || updatedDateAsString.contains("Hoy")) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            try {
                Date withoutDay = new SimpleDateFormat("MMM d, yyyy h:mma", Locale.ENGLISH).parse(updatedDateAsString.replace("Today", ""));
                return today.getTimeInMillis() + withoutDay.getTime();
            } catch (ParseException e) {
                return today.getTimeInMillis();
            }
        } else if (updatedDateAsString.contains("Yesterday") || updatedDateAsString.contains("Ayer")) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);

            try {
                Date withoutDay = new SimpleDateFormat("MMM d, yyyy h:mma", Locale.ENGLISH).parse(updatedDateAsString.replace("Yesterday", ""));
                return yesterday.getTimeInMillis() + withoutDay.getTime();
            } catch (ParseException e) {
                return yesterday.getTimeInMillis();
            }
        } else {
            try {
                Date specificDate = new SimpleDateFormat("MMM d, yyyy h:mma", Locale.ENGLISH).parse(updatedDateAsString);

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

    private String findNextUrlFromParsedDocument(Document parsedDocument) {
        Element nextUrlElement = parsedDocument.select("a.next").first();

        if (nextUrlElement != null) {
            return "http://es.mangahere.co" + nextUrlElement.attr("href");
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
                    public Observable<Manga> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToManga(request, unparsedHtml));
                    }
                });
    }

    private Manga parseHtmlToManga(RequestWrapper request, String unparsedHtml) {
        int beginIndex = unparsedHtml.indexOf("<ul class=\"detail_topText\">");
        int endIndex = unparsedHtml.indexOf("</ul>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        Elements detailElements = parsedDocument.select("ul.detail_topText li");

        Element artistElement = parsedDocument.select("a[href*=/author/]").first();
        Element authorElement = parsedDocument.select("a[href*=/author/]").first();
        Element descriptionElement = detailElements.select("#show").first();
        Element genreElement = detailElements.get(3);
        Element statusElement = detailElements.get(6);
        Element thumbnailUrlElement = parsedDocument.select("img.img").first();

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

        if (artistElement != null) {
            String fieldArtist = artistElement.text();
            newManga.setArtist(fieldArtist);
        }
        if (authorElement != null) {
            String fieldAuthor = authorElement.text();
            newManga.setAuthor(fieldAuthor);
        }
        if (descriptionElement != null) {
            String fieldDescription = descriptionElement.text().substring(0, descriptionElement.text().length() - "Show less".length());
            newManga.setDescription(fieldDescription);
        }
        if (genreElement != null) {
            String fieldGenre = genreElement.text().substring("Género(s):".length());
            newManga.setGenre(fieldGenre);
        }
        if (statusElement != null) {
            boolean fieldCompleted = statusElement.text().contains("Terminado");
            newManga.setCompleted(fieldCompleted);
        }
        if (thumbnailUrlElement != null) {
            String fieldThumbnailUrl = thumbnailUrlElement.attr("src");
            newManga.setThumbnailUrl(fieldThumbnailUrl);
        }

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
                    public Observable<List<Chapter>> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToChapters(request, unparsedHtml));
                    }
                });
    }

    private List<Chapter> parseHtmlToChapters(RequestWrapper request, String unparsedHtml) {
        int beginIndex = unparsedHtml.indexOf("<ul>");
        int endIndex = unparsedHtml.indexOf("</ul>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        List<Chapter> chapterList = scrapeChaptersFromParsedDocument(parsedDocument);
        chapterList = setSourceForChapterList(chapterList);
        chapterList = setParentUrlForChapterList(chapterList, request.getUrl());
        chapterList = setNumberForChapterList(chapterList);

        saveChaptersToDatabase(chapterList, request.getUrl());

        return chapterList;
    }

    private List<Chapter> scrapeChaptersFromParsedDocument(Document parsedDocument) {
        List<Chapter> chapterList = new ArrayList<Chapter>();

        Elements chapterElements = parsedDocument.getElementsByTag("li");
        for (Element chapterElement : chapterElements) {
            Chapter currentChapter = constructChapterFromHtmlBlock(chapterElement);

            chapterList.add(currentChapter);
        }

        return chapterList;
    }

    private Chapter constructChapterFromHtmlBlock(Element chapterElement) {
        Chapter newChapter = DefaultFactory.Chapter.constructDefault();

        Element urlElement = chapterElement.select("a").first();
        Element nameElement = chapterElement.select("a").first();
        Element dateElement = chapterElement.select("span.right").first();

        if (urlElement != null) {
            String fieldUrl = "http://es.mangahere.co" + urlElement.attr("href");
            newChapter.setUrl(fieldUrl);
        }
        if (nameElement != null) {
            String fieldName = nameElement.text();
            newChapter.setName(fieldName);
        }
        if (dateElement != null) {
            long fieldDate = parseDateFromElement(dateElement);
            newChapter.setDate(fieldDate);
        }

        boolean fieldNew = chapterElement.html().contains("<i class=\"new\">");
        newChapter.setNew(fieldNew);

        return newChapter;
    }

    private long parseDateFromElement(Element dateElement) {
        String dateAsString = dateElement.text();

        if (dateAsString.contains("Today") || dateAsString.contains("Hoy")) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            try {
                Date withoutDay = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).parse(dateAsString.replace("Today", ""));
                return today.getTimeInMillis() + withoutDay.getTime();
            } catch (ParseException e) {
                return today.getTimeInMillis();
            }
        } else if (dateAsString.contains("Yesterday") || dateAsString.contains("Ayer")) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);

            try {
                Date withoutDay = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).parse(dateAsString.replace("Yesterday", ""));
                return yesterday.getTimeInMillis() + withoutDay.getTime();
            } catch (ParseException e) {
                return yesterday.getTimeInMillis();
            }
        } else {
            try {
                Date date = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).parse(dateAsString);

                return date.getTime();
            } catch (ParseException e) {
                // Do Nothing.
            }
        }

        return DefaultFactory.Chapter.DEFAULT_DATE;
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
        final List<String> temporaryCachedImageUrls = new ArrayList<String>();

        final MangaService currentService = MangaService.getTemporaryInstance();

        return currentService
                .getResponse(request.getUrl())
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return MangaService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToPageUrls(unparsedHtml));
                    }
                })
                .flatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> pageUrls) {
                        return Observable.from(pageUrls.toArray(new String[pageUrls.size()]));
                    }
                })
                .buffer(5)
                .concatMap(new Func1<List<String>, Observable<? extends List<String>>>() {
                    @Override
                    public Observable<? extends List<String>> call(List<String> batchedPageUrls) {
                        List<Observable<String>> imageUrlObservables = new ArrayList<Observable<String>>();
                        for (String pageUrl : batchedPageUrls) {
                            Observable<String> temporaryObservable = currentService
                                    .getResponse(pageUrl)
                                    .flatMap(new Func1<Response, Observable<String>>() {
                                        @Override
                                        public Observable<String> call(Response response) {
                                            return MangaService.mapResponseToString(response);
                                        }
                                    })
                                    .flatMap(new Func1<String, Observable<String>>() {
                                        @Override
                                        public Observable<String> call(String unparsedHtml) {
                                            return Observable.just(parseHtmlToImageUrl(unparsedHtml));
                                        }
                                    })
                                    .subscribeOn(Schedulers.io());

                            imageUrlObservables.add(temporaryObservable);
                        }

                        return Observable.zip(imageUrlObservables, new FuncN<List<String>>() {
                            @Override
                            public List<String> call(Object... args) {
                                List<String> imageUrls = new ArrayList<String>();
                                for (Object uncastImageUrl : args) {
                                    imageUrls.add(String.valueOf(uncastImageUrl));
                                }

                                return imageUrls;
                            }
                        });
                    }
                })
                .concatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> batchedImageUrls) {
                        return Observable.from(batchedImageUrls.toArray(new String[batchedImageUrls.size()]));
                    }
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String imageUrl) {
                        temporaryCachedImageUrls.add(imageUrl);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        CacheProvider.getInstance().putImageUrlsToDiskCache(request.getUrl(), temporaryCachedImageUrls);
                    }
                })
                .onBackpressureBuffer();
    }

    private List<String> parseHtmlToPageUrls(String unparsedHtml) {
        int beginIndex = unparsedHtml.indexOf("<div class=\"go_page clearfix\">");
        int endIndex = unparsedHtml.indexOf("</div>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        List<String> pageUrlList = new ArrayList<String>();

        Elements pageUrlElements = parsedDocument.select("select.wid60").first().getElementsByTag("option");
        for (Element pageUrlElement : pageUrlElements) {
            pageUrlList.add("http://es.mangahere.co" + pageUrlElement.attr("value"));
        }

        return pageUrlList;
    }

    private String parseHtmlToImageUrl(String unparsedHtml) {
        int beginIndex = unparsedHtml.indexOf("<section class=\"read_img\" id=\"viewer\">");
        int endIndex = unparsedHtml.indexOf("</section>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        Element imageElement = parsedDocument.getElementById("image");

        return imageElement.attr("src");
    }

    private static String INITIAL_DATABASE_URL = "http://es.mangahere.co/directory/?views.za";

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
                    public Observable<String> call(String unparsedHtml) {
                        return Observable.just(parseSpanish_MangaHere(unparsedHtml));
                    }
                });
    }

    private static AtomicInteger mCounter = new AtomicInteger(0);

    private String parseSpanish_MangaHere(String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Manga> mangaList = new ArrayList<Manga>();
        Elements mangaElements = parsedDocument.select("div.directory_list > ul > li");
        for (Element mangaElement : mangaElements) {
            Manga newManga = new Manga();

            Element temporaryElementOne = mangaElement.select("div.manga_text").select("div.title").select("a").first();
            Element temporaryElementTwo = mangaElement.select("img[src*=thumb_cover.jpg]").first();
            Element temporaryElementThree = mangaElement.select("p").get(1);

            String fieldSource = Spanish_MangaHere.NAME;
            newManga.setSource(fieldSource);

            String fieldUrl = temporaryElementOne.attr("href");
            newManga.setUrl(fieldUrl);

            String fieldName = temporaryElementOne.text();
            newManga.setName(fieldName);

            String fieldThumbnailUrl = temporaryElementTwo.attr("src");
            newManga.setThumbnailUrl(fieldThumbnailUrl);

            String fieldGenres = temporaryElementThree.text();
            newManga.setGenre(fieldGenres);

            boolean fieldIsCompleted = mangaElement.html().contains("<em class=\"tag_completed\">");
            newManga.setCompleted(fieldIsCompleted);

            int fieldRank = mCounter.incrementAndGet();
            newManga.setRank(fieldRank);

            mangaList.add(newManga);
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

        Element nextUrlElement = parsedDocument.select("a.next").first();
        if (nextUrlElement != null) {
            return "http://es.mangahere.co" + nextUrlElement.attr("href");
        }

        return null;
    }
}
