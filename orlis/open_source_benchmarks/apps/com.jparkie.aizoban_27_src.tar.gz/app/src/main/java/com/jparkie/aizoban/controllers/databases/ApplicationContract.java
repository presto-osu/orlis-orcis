package com.jparkie.aizoban.controllers.databases;

public class ApplicationContract {
    public static final String DATABASE_NAME = "AizobanApplication.db";
    public static final int DATABASE_VERSION = 1;

    private ApplicationContract() {
        throw new AssertionError();
    }

    public static final class Chapter {
        public static final String TABLE_NAME = "Chapter";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_SOURCE = "Source";
        public static final String COLUMN_URL = "Url";
        public static final String COLUMN_PARENT_URL = "ParentUrl";

        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_NEW = "New";
        public static final String COLUMN_DATE = "Date";

        public static final String COLUMN_NUMBER = "Number";

        private Chapter() {
            throw new AssertionError();
        }
    }

    public static final class DownloadChapter {
        public static final String TABLE_NAME = "DownloadChapter";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_SOURCE = "Source";
        public static final String COLUMN_URL = "Url";
        public static final String COLUMN_PARENT_URL = "ParentUrl";

        public static final String COLUMN_NAME = "Name";

        public static final String COLUMN_DIRECTORY = "Directory";

        public static final String COLUMN_CURRENT_PAGE = "CurrentPage";
        public static final String COLUMN_TOTAL_PAGES = "TotalPages";
        public static final String COLUMN_FLAG = "Flag";

        private DownloadChapter() {
            throw new AssertionError();
        }
    }

    public static final class DownloadManga {
        public static final String TABLE_NAME = "DownloadManga";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_SOURCE = "Source";
        public static final String COLUMN_URL = "Url";

        public static final String COLUMN_ARTIST = "Artist";
        public static final String COLUMN_AUTHOR = "Author";
        public static final String COLUMN_DESCRIPTION = "Description";
        public static final String COLUMN_GENRE = "Genre";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_COMPLETED = "Completed";
        public static final String COLUMN_THUMBNAIL_URL = "ThumbnailUrl";

        private DownloadManga() {
            throw new AssertionError();
        }
    }

    public static final class DownloadPage {
        public static final String TABLE_NAME = "DownloadPage";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_URL = "Url";
        public static final String COLUMN_PARENT_URL = "ParentUrl";

        public static final String COLUMN_NAME = "Name";

        public static final String COLUMN_DIRECTORY = "Directory";

        public static final String COLUMN_FLAG = "Flag";

        private DownloadPage() {
            throw new AssertionError();
        }
    }

    public static final class FavouriteManga {
        public static final String TABLE_NAME = "FavouriteManga";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_SOURCE = "Source";
        public static final String COLUMN_URL = "Url";

        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_THUMBNAIL_URL = "ThumbnailUrl";

        private FavouriteManga() {
            throw new AssertionError();
        }
    }

    public static final class RecentChapter {
        public static final String TABLE_NAME = "RecentChapter";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_SOURCE = "Source";
        public static final String COLUMN_URL = "Url";
        public static final String COLUMN_PARENT_URL = "ParentUrl";

        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_THUMBNAIL_URL = "ThumbnailUrl";

        public static final String COLUMN_DATE = "Date";
        public static final String COLUMN_PAGE = "PageNumber";

        public static final String COLUMN_OFFLINE = "Offline";

        private RecentChapter() {
            throw new AssertionError();
        }
    }
}
