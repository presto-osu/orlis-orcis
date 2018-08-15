package com.jparkie.aizoban.controllers.databases;

public class LibraryContract {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "AizobanLibrary.db";

    private LibraryContract() {
        throw new AssertionError();
    }

    public static final class Manga {
        public static final String TABLE_NAME = "Manga";

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

        public static final String COLUMN_RANK = "Rank";
        public static final String COLUMN_UPDATED = "Updated";
        public static final String COLUMN_UPDATE_COUNT = "UpdateCount";

        public static final String COLUMN_INITIALIZED = "Initialized";

        private Manga() {
            throw new AssertionError();
        }
    }
}
