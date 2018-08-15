package com.jparkie.aizoban.controllers.factories;

import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.SearchUtils;

import java.util.ArrayList;

public class DefaultFactory {
    private DefaultFactory() {
        throw new AssertionError();
    }

    public static final class Chapter {
        public static final String DEFAULT_SOURCE = "No Source";
        public static final String DEFAULT_URL = "No Url";
        public static final String DEFAULT_PARENT_URL = "No Parent Url";

        public static final String DEFAULT_NAME = "No Name";
        public static final boolean DEFAULT_NEW = false;
        public static final long DEFAULT_DATE = 0;

        public static final int DEFAULT_NUMBER = 0;

        private Chapter() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.Chapter constructDefault() {
            com.jparkie.aizoban.models.Chapter newInstance = new com.jparkie.aizoban.models.Chapter();

            newInstance.setSource(DEFAULT_SOURCE);
            newInstance.setUrl(DEFAULT_URL);
            newInstance.setParentUrl(DEFAULT_PARENT_URL);

            newInstance.setName(DEFAULT_NAME);
            newInstance.setNew(DEFAULT_NEW);
            newInstance.setDate(DEFAULT_DATE);

            newInstance.setNumber(DEFAULT_NUMBER);

            return newInstance;
        }
    }

    public static final class DownloadChapter {
        public static final String DEFAULT_SOURCE = "No Source";
        public static final String DEFAULT_URL = "No Url";
        public static final String DEFAULT_PARENT_URL = "No Parent Url";

        public static final String DEFAULT_NAME = "No Name";

        public static final String DEFAULT_DIRECTORY = "No Directory";

        public static final int DEFAULT_CURRENT_PAGE = 0;
        public static final int DEFAULT_TOTAL_PAGES = 0;
        public static final int DEFAULT_FLAG = DownloadUtils.FLAG_FAILED;

        private DownloadChapter() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.downloads.DownloadChapter constructDefault() {
            com.jparkie.aizoban.models.downloads.DownloadChapter newInstance = new com.jparkie.aizoban.models.downloads.DownloadChapter();

            newInstance.setSource(DEFAULT_SOURCE);
            newInstance.setUrl(DEFAULT_URL);
            newInstance.setParentUrl(DEFAULT_PARENT_URL);

            newInstance.setName(DEFAULT_NAME);

            newInstance.setDirectory(DEFAULT_DIRECTORY);

            newInstance.setCurrentPage(DEFAULT_CURRENT_PAGE);
            newInstance.setTotalPages(DEFAULT_TOTAL_PAGES);
            newInstance.setFlag(DEFAULT_FLAG);

            return newInstance;
        }
    }

    public static final class DownloadManga {
        public static final String DEFAULT_SOURCE = "No Source";
        public static final String DEFAULT_URL = "No Url";

        public static final String DEFAULT_ARTIST = "No Artist";
        public static final String DEFAULT_AUTHOR = "No Author";
        public static final String DEFAULT_DESCRIPTION = "No Description";
        public static final String DEFAULT_GENRE = "No Genre";
        public static final String DEFAULT_NAME = "No Name";
        public static final boolean DEFAULT_COMPLETED = false;
        public static final String DEFAULT_THUMBNAIL_URL = "No Thumbnail Url";

        private DownloadManga() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.downloads.DownloadManga constructDefault() {
            com.jparkie.aizoban.models.downloads.DownloadManga newInstance = new com.jparkie.aizoban.models.downloads.DownloadManga();

            newInstance.setSource(DEFAULT_SOURCE);
            newInstance.setUrl(DEFAULT_URL);

            newInstance.setArtist(DEFAULT_ARTIST);
            newInstance.setAuthor(DEFAULT_AUTHOR);
            newInstance.setDescription(DEFAULT_DESCRIPTION);
            newInstance.setGenre(DEFAULT_GENRE);
            newInstance.setName(DEFAULT_NAME);
            newInstance.setCompleted(DEFAULT_COMPLETED);
            newInstance.setThumbnailUrl(DEFAULT_THUMBNAIL_URL);

            return newInstance;
        }
    }

    public static final class DownloadPage {
        public static final String DEFAULT_URL = "No Url";
        public static final String DEFAULT_PARENT_URL = "No Parent Url";

        public static final String DEFAULT_DIRECTORY = "No Directory";

        public static final String DEFAULT_NAME = "No Name";

        public static final int DEFAULT_FLAG = DownloadUtils.FLAG_FAILED;

        private DownloadPage() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.downloads.DownloadPage constructDefault() {
            com.jparkie.aizoban.models.downloads.DownloadPage newInstance = new com.jparkie.aizoban.models.downloads.DownloadPage();

            newInstance.setUrl(DEFAULT_URL);
            newInstance.setParentUrl(DEFAULT_PARENT_URL);

            newInstance.setDirectory(DEFAULT_DIRECTORY);

            newInstance.setName(DEFAULT_NAME);

            newInstance.setFlag(DEFAULT_FLAG);

            return newInstance;
        }
    }

    public static final class FavouriteManga {
        public static final String DEFAULT_SOURCE = "No Source";
        public static final String DEFAULT_URL = "No Url";

        public static final String DEFAULT_NAME = "No Name";
        public static final String DEFAULT_THUMBNAIL_URL = "No Thumbnail Url";

        private FavouriteManga() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.databases.FavouriteManga constructDefault() {
            com.jparkie.aizoban.models.databases.FavouriteManga newInstance = new com.jparkie.aizoban.models.databases.FavouriteManga();

            newInstance.setSource(DEFAULT_SOURCE);
            newInstance.setUrl(DEFAULT_URL);

            newInstance.setName(DEFAULT_NAME);
            newInstance.setThumbnailUrl(DEFAULT_THUMBNAIL_URL);

            return newInstance;
        }
    }

    public static final class Manga {
        public static final String DEFAULT_SOURCE = "No Source";
        public static final String DEFAULT_URL = "No Url";

        public static final String DEFAULT_ARTIST = "No Artist";
        public static final String DEFAULT_AUTHOR = "No Author";
        public static final String DEFAULT_DESCRIPTION = "No Description";
        public static final String DEFAULT_GENRE = "No Genre";
        public static final String DEFAULT_NAME = "No Name";
        public static final boolean DEFAULT_COMPLETED = false;
        public static final String DEFAULT_THUMBNAIL_URL = "No Thumbnail Url";

        public static final int DEFAULT_RANK = 0;
        public static final long DEFAULT_UPDATED = 0;
        public static final int DEFAULT_UPDATE_COUNT = 0;

        public static final boolean DEFAULT_INITIALIZED = false;

        private Manga() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.Manga constructDefault() {
            com.jparkie.aizoban.models.Manga newInstance = new com.jparkie.aizoban.models.Manga();

            newInstance.setSource(DEFAULT_SOURCE);
            newInstance.setUrl(DEFAULT_URL);

            newInstance.setArtist(DEFAULT_ARTIST);
            newInstance.setAuthor(DEFAULT_AUTHOR);
            newInstance.setDescription(DEFAULT_DESCRIPTION);
            newInstance.setGenre(DEFAULT_GENRE);
            newInstance.setName(DEFAULT_NAME);
            newInstance.setCompleted(DEFAULT_COMPLETED);
            newInstance.setThumbnailUrl(DEFAULT_THUMBNAIL_URL);

            newInstance.setRank(DEFAULT_RANK);
            newInstance.setUpdated(DEFAULT_UPDATED);
            newInstance.setUpdateCount(DEFAULT_UPDATE_COUNT);

            newInstance.setInitialized(DEFAULT_INITIALIZED);

            return newInstance;
        }
    }

    public static final class RecentChapter {
        public static final String DEFAULT_SOURCE = "No Source";
        public static final String DEFAULT_URL = "No Url";
        public static final String DEFAULT_PARENT_URL = "No Parent Url";

        public static final String DEFAULT_NAME = "No Name";
        public static final String DEFAULT_THUMBNAIL_URL = "No Thumbnail Url";

        public static final long DEFAULT_DATE = 0;
        public static final int DEFAULT_PAGE_NUMBER = 0;

        public static final boolean DEFAULT_OFFLINE = false;

        private RecentChapter() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.models.databases.RecentChapter constructDefault() {
            com.jparkie.aizoban.models.databases.RecentChapter newInstance = new com.jparkie.aizoban.models.databases.RecentChapter();

            newInstance.setSource(DEFAULT_SOURCE);
            newInstance.setUrl(DEFAULT_URL);
            newInstance.setParentUrl(DEFAULT_PARENT_URL);

            newInstance.setName(DEFAULT_NAME);
            newInstance.setThumbnailUrl(DEFAULT_THUMBNAIL_URL);

            newInstance.setDate(DEFAULT_DATE);
            newInstance.setPageNumber(DEFAULT_PAGE_NUMBER);

            newInstance.setOffline(DEFAULT_OFFLINE);

            return newInstance;
        }
    }

    public static final class SearchCatalogueWrapper {
        public static final String DEFAULT_NAME = null;
        public static final String DEFAULT_STATUS = SearchUtils.STATUS_ALL;
        public static final String DEFAULT_ORDER_BY = SearchUtils.ORDER_BY_RANK;
        public static final int DEFAULT_OFFSET = 0;

        private SearchCatalogueWrapper() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper constructDefault() {
            com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper newInstance = new com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper();

            newInstance.setNameArgs(DEFAULT_NAME);
            newInstance.setStatusArgs(DEFAULT_STATUS);
            newInstance.setOrderByArgs(DEFAULT_ORDER_BY);
            newInstance.setGenresArgs(new ArrayList<String>());
            newInstance.setOffsetArgs(DEFAULT_OFFSET);

            return newInstance;
        }
    }

    public static final class UpdatePageMarker {
        public static final String DEFAULT_NEXT_PAGE_URL = "No Next Page Url";
        public static final int DEFAULT_LAST_MANGA_POSITION = 0;

        private UpdatePageMarker() {
            throw new AssertionError();
        }

        public static com.jparkie.aizoban.controllers.sources.UpdatePageMarker constructDefault() {
            return new com.jparkie.aizoban.controllers.sources.UpdatePageMarker(AizobanManager.getInitialUpdateUrlFromPreferenceSource().toBlocking().single(), DEFAULT_LAST_MANGA_POSITION);
        }
    }
}
