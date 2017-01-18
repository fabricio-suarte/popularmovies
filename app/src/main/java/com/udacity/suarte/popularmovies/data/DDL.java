package com.udacity.suarte.popularmovies.data;

/**
 * Database schema DDL statements
 */
class DDL {
    static final String CREATE_MOVIE_TABLE =
            String.format("CREATE TABLE %s(", Contract.MovieTable.NAME) +
                    String.format("%s INTEGER PRIMARY KEY, ", Contract.MovieTable._ID) +
                    String.format("%s TEXT NOT NULL, ", Contract.MovieTable.COLUMN_EXTERNAL_ID) +
                    String.format("%s TEXT NOT NULL, ", Contract.MovieTable.COLUMN_TITLE) +
                    String.format("%s TEXT NOT NULL, ", Contract.MovieTable.COLUMN_SYNOPSIS) +
                    String.format("%s NUMERIC NOT NULL, ", Contract.MovieTable.COLUMN_RELEASE) +
                    String.format("%s TEXT NOT NULL, ", Contract.MovieTable.COLUMN_POSTER) +
                    String.format("%s REAL NOT NULL, ", Contract.MovieTable.COLUMN_USER_RATING) +
                    String.format("%s REAL NOT NULL, ", Contract.MovieTable.COLUMN_POPULARITY) +
                    String.format("%s INTEGER,", Contract.MovieTable.COLUMN_FAVORITE) +
                    String.format("%s TEXT)", Contract.MovieTable.COLUMN_JSON_HASH);


    static final String CREATE_VIDEO_TABLE =
            String.format("CREATE TABLE %s(",  Contract.VideoTable.NAME) +
                    String.format("%s INTEGER PRIMARY KEY, ", Contract.VideoTable._ID) +
                    String.format("%s INTEGER NOT NULL, ", Contract.VideoTable.COLUMN_MOVIE_ID) +
                    String.format("%s TEXT NOT NULL, ", Contract.VideoTable.COLUMN_EXTERNAL_ID) +
                    String.format("%s TEXT NOT NULL, ", Contract.VideoTable.COLUMN_KEY) +
                    String.format("%s TEXT, ", Contract.VideoTable.COLUMN_NAME) +
                    String.format("%s TEXT, ", Contract.VideoTable.COLUMN_TYPE) +
                    String.format("%s TEXT, ", Contract.VideoTable.COLUMN_SITE) +
                    String.format("FOREIGN KEY(%s) REFERENCES %s(_ID) )",
                                                    Contract.VideoTable.COLUMN_MOVIE_ID,
                                                    Contract.MovieTable.NAME);

    static final String CREATE_REVIEW_TABLE =
            String.format("CREATE TABLE %s(",  Contract.ReviewTable.NAME) +
                    String.format("%s INTEGER PRIMARY KEY, ", Contract.ReviewTable._ID) +
                    String.format("%s INTEGER NOT NULL, ", Contract.ReviewTable.COLUMN_MOVIE_ID) +
                    String.format("%s TEXT NOT NULL, ", Contract.ReviewTable.COLUMN_EXTERNAL_ID) +
                    String.format("%s TEXT NOT NULL, ", Contract.ReviewTable.COLUMN_CONTENT) +
                    String.format("%s TEXT, ", Contract.ReviewTable.COLUMN_AUTHOR) +
                    String.format("FOREIGN KEY(%s) REFERENCES %s(_ID) )",
                            Contract.ReviewTable.COLUMN_MOVIE_ID,
                            Contract.MovieTable.NAME);

    static final String ALTER_TABLE_MOVIE_ADD_FAVORITE =
            String.format("ALTER TABLE %s ADD COLUMN %s INTEGER",
                    Contract.MovieTable.NAME, Contract.MovieTable.COLUMN_FAVORITE);
}
