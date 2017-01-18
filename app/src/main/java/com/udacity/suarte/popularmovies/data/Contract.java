package com.udacity.suarte.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A Contract data classes holder.
 */

public class Contract {

    //region constants

    /**
     * The content authority name
     */
    public static final String CONTENT_AUTHORITY = "com.udacity.suarte.popularmovies.provider";

    /**
     * A base content uri
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Format base string for content mime type
    private static final String CONTENT_MIME_TYPE_FORMAT = "%s/%s/%s";

    //endregion

    /**
     * Movie's Table contract
     */
    public static class MovieTable implements BaseColumns{

        //region constants

        /**
         * Represents the Content URI path for Movie data
         */
        public static final String URI_PATH = "movie";

        /**
         * A base URI for accessing content
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(URI_PATH).build();

        /**
         * The content MIME type string representation
         */
        public static final String CONTENT_MIME_TYPE =
                String.format(CONTENT_MIME_TYPE_FORMAT,
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        URI_PATH);

        /**
         * The content MIME type string representation for an Item
         */
        public static final String CONTENT_ITEM_MIME_TYPE =
                String.format(CONTENT_MIME_TYPE_FORMAT,
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        URI_PATH);

        //Table name and column names constants
        public static final String NAME = "Movie";
        public static final String COLUMN_EXTERNAL_ID = "external_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RELEASE = "release_date";
        public static final String COLUMN_POSTER = "poster_image_file";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_JSON_HASH = "json_hash";

        //endregion

        //region public methods

        /**
         * Returns an Movie Uri for the given movie Id
         * @param id an movie id
         * @return Uri
         */
        public static Uri buildUri(long id) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

            return uri;
        }

        //endregion
    }

    /**
     * Video's Table contract
     */
    public static class VideoTable implements BaseColumns {

        //region constants

        /**
         * Represents the Content URI path for Video data
         */
        public static final String URI_PATH = "video";

        /**
         * A base URI for accessing content
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(URI_PATH).build();

        /**
         * The content MIME type string representation
         */
        public static final String CONTENT_MIME_TYPE =
                String.format(CONTENT_MIME_TYPE_FORMAT,
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        URI_PATH);

        /**
         * The content MIME type string representation for an Item
         */
        public static final String CONTENT_ITEM_MIME_TYPE =
                String.format(CONTENT_MIME_TYPE_FORMAT,
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        URI_PATH);

        //Table name and column names constants
        public static final String NAME = "Video";
        public static final String COLUMN_MOVIE_ID = "movie_id"; //movie's foreign key
        public static final String COLUMN_EXTERNAL_ID = "external_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_TYPE = "type";

        //endregion

        //region public methods

        /**
         * Returns an Video Uri for the given video Id
         * @param id an video id
         * @return Uri
         */
        public static Uri buildUri(long id) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

            return uri;
        }

        //endregion

    }


    /**
     * Review's Table contract
     */
    public static class ReviewTable implements BaseColumns {

        //region constants

        /**
         * Represents the Content URI path for Video data
         */
        public static final String URI_PATH = "review";

        /**
         * A base URI for accessing content
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(URI_PATH).build();

        /**
         * The content MIME type string representation
         */
        public static final String CONTENT_MIME_TYPE =
                String.format(CONTENT_MIME_TYPE_FORMAT,
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        URI_PATH);

        /**
         * The content MIME type string representation for an Item
         */
        public static final String CONTENT_ITEM_MIME_TYPE =
                String.format(CONTENT_MIME_TYPE_FORMAT,
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        URI_PATH);

        //Table name and column names constants
        public static final String NAME = "Review";
        public static final String COLUMN_MOVIE_ID = "movie_id"; //movie's foreign key
        public static final String COLUMN_EXTERNAL_ID = "external_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";

        //endregion

        //region public methods

        /**
         * Returns an Review Uri for the given Review Id
         * @param id an Review id
         * @return Uri
         */
        public static Uri buildUri(long id) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

            return uri;
        }

        //endregion

    }
}
