package com.udacity.suarte.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.udacity.suarte.popularmovies.core.ArgumentHelper;

/**
 * The default ContentProvider for this Popular Movies application
 */

public class Provider extends ContentProvider {

    //region constants

    private static final String TAG = Provider.class.getCanonicalName();

    //Uri mapping codes
    private static final int MOVIE_MULTIPLE_ROWS = 1;
    private static final int MOVIE_SINGLE_ROW = 2;
    private static final int VIDEO_MULTIPLE_ROWS = 3;
    private static final int VIDEO_SINGLE_ROW = 4;
    private static final int REVIEW_MULTIPLE_ROWS = 5;
    private static final int REVIEW_SINGLE_ROW = 6;

    //Helper constants
    private static final String SINGLE_MOVIE_SELECTION
            = String.format("%s = ?", Contract.MovieTable._ID);

    private static final String SINGLE_VIDEO_SELECTION
            = String.format("%s = ?", Contract.VideoTable._ID);

    private static final String SINGLE_REVIEW_SELECTION
            = String.format("%s = ?", Contract.ReviewTable._ID);

    //endregion

    //region members

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteOpenHelper dbHelper;

    private SQLiteQueryBuilder movieQueryBuilder;
    private SQLiteQueryBuilder videoQueryBuilder;
    private SQLiteQueryBuilder reviewQueryBuilder;

    //endregion

    //region static context initialization

    static {
        //Let's set URIs mappings! :)

        //Movie matches
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.MovieTable.URI_PATH,
                MOVIE_MULTIPLE_ROWS);

        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                String.format("%s/#", Contract.MovieTable.URI_PATH),
                MOVIE_SINGLE_ROW);

        //Video matches
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.VideoTable.URI_PATH,
                VIDEO_MULTIPLE_ROWS);

        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                String.format("%s/#", Contract.VideoTable.URI_PATH),
                VIDEO_SINGLE_ROW);

        //Review matches
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.ReviewTable.URI_PATH,
                REVIEW_MULTIPLE_ROWS);

        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                String.format("%s/#", Contract.ReviewTable.URI_PATH),
                REVIEW_SINGLE_ROW);

    }

    //endregion

    //region ContentProvider implementation

    @Override
    public boolean onCreate() {

        //Let's set some member objects
        this.dbHelper = new DbHelper(this.getContext());

        this.movieQueryBuilder = new SQLiteQueryBuilder();
        this.movieQueryBuilder.setTables(Contract.MovieTable.NAME);

        this.videoQueryBuilder = new SQLiteQueryBuilder();
        this.videoQueryBuilder.setTables(Contract.VideoTable.NAME);

        this.reviewQueryBuilder = new SQLiteQueryBuilder();
        this.reviewQueryBuilder.setTables(Contract.ReviewTable.NAME);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sortOrder) {

        ArgumentHelper.validateNull(uri, "uri");

        Cursor cursor = null;

        int uriCode = uriMatcher.match(uri);
        switch (uriCode) {

            case MOVIE_MULTIPLE_ROWS:

                if(TextUtils.isEmpty(sortOrder))
                    sortOrder = String.format("%s ASC", Contract.MovieTable._ID);

                cursor = this.getMovies(projection, selection, args, sortOrder);

                break;

            case MOVIE_SINGLE_ROW:

                String movieId =  uri.getLastPathSegment();
                cursor = this.getSingleMovie(projection, movieId);

                break;

            case VIDEO_MULTIPLE_ROWS:

                if(TextUtils.isEmpty(sortOrder))
                    sortOrder = String.format("%s ASC", Contract.MovieTable._ID);

                cursor = this.getVideos(projection, selection, args, sortOrder);
                break;

            case VIDEO_SINGLE_ROW:

                String videoId =  uri.getLastPathSegment();
                cursor = this.getSingleVideo(projection, videoId);

                break;

            case REVIEW_MULTIPLE_ROWS:

                if(TextUtils.isEmpty(sortOrder))
                    sortOrder = String.format("%s ASC", Contract.ReviewTable._ID);

                cursor = this.getReviews(projection, selection, args, sortOrder);
                break;

            case REVIEW_SINGLE_ROW:

                String reviewId =  uri.getLastPathSegment();
                cursor = this.getSingleReview(projection, reviewId);

                break;

            default:

                this.throwUnknownUriException(uri);
        }

        //This is very important! Otherwise, your cursor will not be able to be aware of
        //data changing.
        cursor.setNotificationUri(this.getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        ArgumentHelper.validateNull(uri, "uri");

        String type = "";

        int uriCode = uriMatcher.match(uri);
        switch (uriCode) {

            case MOVIE_MULTIPLE_ROWS:
                type = Contract.MovieTable.CONTENT_MIME_TYPE;
                break;

            case MOVIE_SINGLE_ROW:
                type = Contract.MovieTable.CONTENT_ITEM_MIME_TYPE;
                break;

            case VIDEO_MULTIPLE_ROWS:
                type = Contract.VideoTable.CONTENT_MIME_TYPE;
                break;

            case VIDEO_SINGLE_ROW:
                type = Contract.VideoTable.CONTENT_ITEM_MIME_TYPE;
                break;

            case REVIEW_MULTIPLE_ROWS:
                type = Contract.ReviewTable.CONTENT_MIME_TYPE;
                break;

            case REVIEW_SINGLE_ROW:
                type = Contract.ReviewTable.CONTENT_ITEM_MIME_TYPE;
                break;

            default:

                this.throwUnknownUriException(uri);
        }

        return type;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        ArgumentHelper.validateNull(uri, "uri");
        ArgumentHelper.validateNull(contentValues, "contentValues");

        Uri insertedUri = null;
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        int uriCode = uriMatcher.match(uri);
        long id;

        switch (uriCode) {
            case MOVIE_MULTIPLE_ROWS:

                id = db.insert(Contract.MovieTable.NAME, null, contentValues);
                if( id > 0) {
                    insertedUri = Contract.MovieTable.buildUri(id);
                }
                else {
                    throw new SQLException("Error when trying to insert into " + uri);
                }

                break;

            case VIDEO_MULTIPLE_ROWS:

                id = db.insert(Contract.VideoTable.NAME, null, contentValues);
                if(id > 0) {
                    insertedUri = Contract.VideoTable.buildUri(id);
                }
                else {
                    throw new SQLException("Error when trying to insert into " + uri);
                }

                break;

            case REVIEW_MULTIPLE_ROWS:

                id = db.insert(Contract.ReviewTable.NAME, null, contentValues);
                if(id > 0) {
                    insertedUri = Contract.ReviewTable.buildUri(id);
                }
                else {
                    throw new SQLException("Error when trying to insert into " + uri);
                }

                break;

            default:

                this.throwUnknownUriException(uri);
        }

        //Notify the insert to the ContentResolver
        this.getContext().getContentResolver().notifyChange(uri, null);

        return insertedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {

        ArgumentHelper.validateNull(uri, "uri");

        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int deletedRows = 0;

        int uriCode = uriMatcher.match(uri);
        switch (uriCode) {
            case MOVIE_MULTIPLE_ROWS:

                deletedRows = db.delete(Contract.MovieTable.NAME, selection, args);
                break;

            case MOVIE_SINGLE_ROW:

                String movieId = uri.getLastPathSegment();
                deletedRows = db.delete(Contract.MovieTable.NAME,
                        SINGLE_MOVIE_SELECTION,
                        new String[] { movieId });
                break;

            case VIDEO_MULTIPLE_ROWS:

                deletedRows = db.delete(Contract.VideoTable.NAME, selection, args);
                break;

            case REVIEW_MULTIPLE_ROWS:

                deletedRows = db.delete(Contract.ReviewTable.NAME, selection, args);
                break;

            default:

                this.throwUnknownUriException(uri);
        }

        //Notify the change to the ContentResolver
        if(deletedRows > 0)
            this.getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] args) {

        ArgumentHelper.validateNull(uri, "uri");

        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int updatedRows = 0;

        int uriCode = uriMatcher.match(uri);
        switch (uriCode) {
            case MOVIE_MULTIPLE_ROWS:

                updatedRows = db.update(Contract.MovieTable.NAME,
                        contentValues,
                        selection,
                        args);

                break;

            case MOVIE_SINGLE_ROW:

                String movieId = uri.getLastPathSegment();
                updatedRows = db.update(Contract.MovieTable.NAME,
                        contentValues,
                        SINGLE_MOVIE_SELECTION,
                        new String[] { movieId });
                break;

            default:

                this.throwUnknownUriException(uri);
        }

        //Notify the change to the ContentResolver
        if(updatedRows > 0)
            this.getContext().getContentResolver().notifyChange(uri, null);

        return updatedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        ArgumentHelper.validateNull(uri, "uri");
        ArgumentHelper.validateNull(values, "values");

        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int insertedRows = 0;

        long id;
        int uriCode = uriMatcher.match(uri);

        String tableName = "";

        switch (uriCode) {
            case MOVIE_MULTIPLE_ROWS:

                tableName = Contract.MovieTable.NAME;
                break;

            case VIDEO_MULTIPLE_ROWS:
                tableName = Contract.VideoTable.NAME;
                break;

            case REVIEW_MULTIPLE_ROWS:
                tableName = Contract.ReviewTable.NAME;
                break;

            default:

                this.throwUnknownUriException(uri);
        }

        db.beginTransaction();

        try {

            for (ContentValues value : values) {
                id = db.insert(tableName,
                        null,
                        value);

                if (id > 0)
                    insertedRows++;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if(insertedRows > 0) {
            this.getContext().getContentResolver().notifyChange(uri, null);

            Log.d(TAG, "The following uri has been notified after a bulk insert: " + uri.toString());
        }

        return insertedRows;
    }

    //endregion

    //region private aux methods

    private void throwUnknownUriException(Uri uri) {
        throw new UnsupportedOperationException("Unknown uri:" + uri);
    }

    //Returns a cursor for all movies entries
    private Cursor getMovies(String[] projection, String selection, String[] args, String sortOrder) {

        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = this.movieQueryBuilder.query(db,
                projection,
                selection, //selection
                args, //parameters
                null, //grouping
                null, //having
                sortOrder);

        return cursor;
    }

    //Returns a cursor for a single movie entry
    private Cursor getSingleMovie(String[] projection, String id) {

        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = this.movieQueryBuilder.query(db,
                projection,
                SINGLE_MOVIE_SELECTION, //selection
                new String[] { id }, //parameters
                null, //grouping
                null, //having
                null); //sort order

        return cursor;
    }

    private Cursor getVideos(String[] projection, String selection, String[] args, String sortOrder) {

        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = this.videoQueryBuilder.query(db,
                projection,
                selection,
                args, //parameters
                null, //grouping
                null, //having
                sortOrder);

        return cursor;
    }

    //Returns a cursor for a single video entry
    private Cursor getSingleVideo(String[] projection, String id) {

        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = this.videoQueryBuilder.query(db,
                projection,
                SINGLE_VIDEO_SELECTION, //selection
                new String[] { id }, //parameters
                null, //grouping
                null, //having
                null); //sort order

        return cursor;
    }

    private Cursor getReviews(String[] projection, String selection, String[] args, String sortOrder) {

        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = this.reviewQueryBuilder.query(db,
                projection,
                selection,
                args, //parameters
                null, //grouping
                null, //having
                sortOrder);

        return cursor;
    }

    //Returns a cursor for a single Review entry
    private Cursor getSingleReview(String[] projection, String id) {

        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = this.reviewQueryBuilder.query(db,
                projection,
                SINGLE_REVIEW_SELECTION, //selection
                new String[] { id }, //parameters
                null, //grouping
                null, //having
                null); //sort order

        return cursor;
    }

    //endregion
}
