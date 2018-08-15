package com.luorrak.ouroboros.util;

import android.provider.BaseColumns;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class DbContract {

    public static final class BoardEntry implements BaseColumns{
        public static final String TABLE_NAME = "boards";

        //name of the board submitted by user
        public static final String COLUMN_BOARDS = "board_name";

        public static final String BOARD_ORDER = "board_order";
    }

    public static final class CatalogEntry implements BaseColumns{
        public static final String TABLE_NAME = "catalog";

        //foreign key from table boardEntry
        public static final String COLUMN_BOARD_NAME = "board_name";

        /*
        Post Number CATALOG
        type: String
         */
        public static final String COLUMN_CATALOG_NO = "no";
        /*
        original file name
        type: String
         */
        public static final String COLUMN_CATALOG_FILENAME = "filename";
        /*
        Renamed filename
        type: String
         */
        public static final String COLUMN_CATALOG_TIM = "tim";
        /*
        filename extension
        type: String
         */
        public static final String COLUMN_CATALOG_EXT = "ext";
        /*
        Thread Subtitle
        type: String
         */
        public static final String COLUMN_CATALOG_SUB = "sub";
        /*
        comment text
        type: String
         */
        public static final String COLUMN_CATALOG_COM = "com";
        /*
        number of replies
        type: Integer
         */
        public static final String COLUMN_CATALOG_REPLIES = "replies";
        /*
        number of images
        type: Integer
         */
        public static final String COLUMN_CATALOG_IMAGES = "images";
        /*
        Is thread stickied
        type: Integer
         */
        public static final String COLUMN_CATALOG_STICKY = "sticky";
        /*
        Is thread locked
        type: Integer
         */
        public static final String COLUMN_CATALOG_LOCKED = "locked";

        public static final String COLUMN_CATALOG_EMBED = "embed";

    }

    public static final class ThreadEntry implements BaseColumns{

        public static final String TABLE_NAME = "thread";

        /*
        foreign key from table boards
        Type: String
         */
        public static final String COLUMN_BOARD_NAME = "board_name";
        /*
        OP post people are replying to
        Type: String
         */
        public static final String COLUMN_THREAD_RESTO = "resto";
        /*
        post number
        Type: String
         */
        public static final String COLUMN_THREAD_NO = "no";
        /*
        original filename
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_FILENAME = "filename";

        public static final String COLUMN_THREAD_IMAGE_HEIGHT = "image_01_height";

        public static final String COLUMN_THREAD_IMAGE_WIDTH = "image_01_width";
        /*
        Post Subtitle
        WARNING: should only appear on OP else return null
        Type: String
         */
        public static final String COLUMN_THREAD_SUB = "sub";
        /*
        post comment
        WARNING: will be filled with html special characters
        Type: String
         */
        public static final String COLUMN_THREAD_COM = "com";
        /*
        post email
        WARNING: Can have any type of text
        Type: String
        */
        public static final String COLUMN_THREAD_EMAIL = "email";
        /*
        Name of poster
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_NAME = "name";
        /*
        trip of poster
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_TRIP = "trip";
        /*
        What time the post was submitted
        Type: String
         */
        public static final String COLUMN_THREAD_TIME = "time";
        /*
        last time the post was modified
        Type: String
         */
        public static final String COLUMN_THREAD_LAST_MODIFIED = "last_modified";
        /*
        poster id
        WARNING: may be null
        Type: String
         */
        public static final String COLUMN_THREAD_ID = "id";

        public static final String COLUMN_THREAD_EMBED = "embed";

        /*
        Serialized Arraylist of Media Items
        WARNING: may be null
        Type: BLOB
        */
        public static final String COLUMN_THREAD_MEDIA_FILES = "media_files";

        public static final String COLUMN_POSITION = "position";
    }

    public static final class ReplyCheck implements BaseColumns{

        public static final String TABLE_NAME = "reply_check_cache";

        /*
        foreign key from table boards
        Type: String
         */
        public static final String COLUMN_BOARD_NAME = "board_name";
        /*
        OP post people are replying to
        Type: String
         */
        public static final String COLUMN_THREAD_RESTO = "resto";
        /*
        post number
        Type: String
         */
        public static final String COLUMN_THREAD_NO = "no";
        /*
        original filename
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_FILENAME = "filename";

        public static final String COLUMN_THREAD_IMAGE_HEIGHT = "image_01_height";

        public static final String COLUMN_THREAD_IMAGE_WIDTH = "image_01_width";
        /*
        Post Subtitle
        WARNING: should only appear on OP else return null
        Type: String
         */
        public static final String COLUMN_THREAD_SUB = "sub";
        /*
        post comment
        WARNING: will be filled with html special characters
        Type: String
         */
        public static final String COLUMN_THREAD_COM = "com";
        /*
        post email
        WARNING: Can have any type of text
        Type: String
        */
        public static final String COLUMN_THREAD_EMAIL = "email";
        /*
        Name of poster
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_NAME = "name";
        /*
        trip of poster
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_TRIP = "trip";
        /*
        What time the post was submitted
        Type: String
         */
        public static final String COLUMN_THREAD_TIME = "time";
        /*
        last time the post was modified
        Type: String
         */
        public static final String COLUMN_THREAD_LAST_MODIFIED = "last_modified";
        /*
        poster id
        WARNING: may be null
        Type: String
         */
        public static final String COLUMN_THREAD_ID = "id";

        public static final String COLUMN_THREAD_EMBED = "embed";

        /*
        Serialized Arraylist of Media Items
        WARNING: may be null
        Type: BLOB
        */
        public static final String COLUMN_THREAD_MEDIA_FILES = "media_files";

        public static final String COLUMN_REPLY_CHECK_POSITION = "position";
    }

    public static final class UserPosts implements BaseColumns{
        public static final String TABLE_NAME = "userposts";

        //name of the board
        public static final String COLUMN_BOARDS = "board_name";

        public static final String COLUMN_NO = "user_post_no";

        public static final String COLUMN_RESTO = "user_post_resto";

        public static final String COLUMN_SUBJECT = "user_post_subject";

        public static final String COLUMN_COMMENT = "user_post_comment";

        public static final String COLUMN_NUMBER_OF_REPLIES = "user_post_number_of_replies";

        public static final String COLUMN_NEW_REPLY_FLAG = "new_reply_flag";

        public static final String COLUMN_ERROR_COUNT = "error_count";

        public static final String COLUMN_POSITION = "position";
    }

    public static final class WatchlistEntry implements BaseColumns{
        public static final String TABLE_NAME = "watchlist";

        /*
        Thread Title, either subject text or '/board/threadno
        Type: String
        */
        public static final String COLUMN_TITLE = "watchlist_title";

        /*
        Board name
        Type: String
        */
        public static final String COLUMN_BOARD = "watchlist_board";

        /*
        Thread Number. Resto
        Type: String
        */
        public static final String COLUMN_NO = "watchlist_no";

        /*
        Serialized media objects
        Type: Blob
        */
        public static final String COLUMN_MEDIA_FILES = "watchlist_serialized_media";

        /*
        Order of object
        Type: int
        */
        public static final String WATCHLIST_ORDER = "watchlist_order";
    }

    public static final class ThreadReplyCountTracker implements BaseColumns {
        public static final String TABLE_NAME = "thread_reply_count_tracker";

        public static final String BOARD_NAME = "board_name";

        public static final String RESTO = "resto";

        public static final String REPLY_COUNT = "reply_count";
    }
}
