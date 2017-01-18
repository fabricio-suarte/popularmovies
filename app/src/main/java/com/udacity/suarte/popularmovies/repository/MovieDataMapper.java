package com.udacity.suarte.popularmovies.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Base64;
import android.util.Log;

import com.udacity.suarte.popularmovies.core.ArgumentHelper;
import com.udacity.suarte.popularmovies.data.Contract;
import com.udacity.suarte.popularmovies.domain.MovieRepository;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a kind of Facade for persisting and returning data for Movies
 */
public class MovieDataMapper implements MovieRepository {

    //region constants

    private static final String LOG_TAG = MovieDataMapper.class.getSimpleName();

    //themoviedb.org json object names constants mapping for Movies
    private static final String MOVIE_POSTER = "poster_path";
    private static final String MOVIE_SYNOPSIS = "overview";
    private static final String MOVIE_RELEASE = "release_date";
    private static final String MOVIE_EXTERNAL_ID = "id";
    private static final String MOVIE_TITLE = "original_title";
    private static final String MOVE_USER_RATING = "vote_average";
    private static final String MOVIE_POPULARITY = "popularity";

    //themoviedb.org json object names constants mapping for Videos
    private static final String VIDEO_EXTERNAL_ID = "id";
    private static final String VIDEO_KEY = "key";
    private static final String VIDEO_NAME = "name";
    private static final String VIDEO_SITE = "site";
    private static final String VIDEO_TYPE = "type";

    //themoviedb.org json object names constants mapping for Reviews
    private static final String REVIEW_EXTERNAL_ID = "id";
    private static final String REVIEW_AUTHOR = "author";
    private static final String REVIEW_CONTENT = "content";

    //The date format used for "release_date"
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    //Possible sort orders clauses
    private static final String MOST_POPULAR_SORT_ORDER
            = String.format("%s DESC", Contract.MovieTable.COLUMN_POPULARITY);

    private static final String HIGHEST_RATED_SORT_ORDER
            = String.format("%s DESC", Contract.MovieTable.COLUMN_USER_RATING);

    //Favorite flags
    private static int NOT_FAVORITE_FLAG = 0;
    private static int FAVORITE_FLAG = 1;

    //endregion

    //region members

    private Context currentContext;

    //endregion

    //region constructors

    public MovieDataMapper(Context context) {
        this.currentContext = context;
    }

    //endregion

    //region MovieRepository interface implementation


    //region cursor wrapper

    // These are cursor wrapper implementations. This way, I can still avoid other application parts
    // getting coupling with the data package other than the repository implementation package...

    @Override
    public MovieCursorWrapper createMovieCursorWrapper(final Cursor data) {

        ArgumentHelper.validateNull(data, "data");

        MovieCursorWrapper wrapper = new MovieCursorWrapper() {
            @Override
            public long getId() {

                int i = data.getColumnIndex(Contract.MovieTable._ID);
                return data.getLong(i);
            }

            @Override
            public String getPoster() {

                int i = data.getColumnIndex(Contract.MovieTable.COLUMN_POSTER);
                return data.getString(i);
            }

            @Override
            public String getTitle() {
                int i = data.getColumnIndex(Contract.MovieTable.COLUMN_TITLE);
                return data.getString(i);
            }

            @Override
            public String getSynopsis() {
                int i = data.getColumnIndex(Contract.MovieTable.COLUMN_SYNOPSIS);
                return data.getString(i);
            }

            @Override
            public float getUserRating() {
                int i = data.getColumnIndex(Contract.MovieTable.COLUMN_USER_RATING);
                return data.getFloat(i);
            }

            @Override
            public long getReleaseDate() {
                int i = data.getColumnIndex(Contract.MovieTable.COLUMN_RELEASE);
                return data.getLong(i);
            }

            @Override
            public int getFavorite() {
                int i = data.getColumnIndex(Contract.MovieTable.COLUMN_FAVORITE);
                return data.getInt(i);
            }
        };

        return wrapper;
    }

    @Override
    public VideoCursorWrapper createVideoCursorWrapper(final Cursor data) {
        ArgumentHelper.validateNull(data, "data");

        VideoCursorWrapper wrapper = new VideoCursorWrapper() {

            @Override
            public long getId() {
                int i = data.getColumnIndex(Contract.VideoTable._ID);
                return data.getLong(i);
            }

            @Override
            public String getName() {
                int i = data.getColumnIndex(Contract.VideoTable.COLUMN_NAME);
                return data.getString(i);
            }

            @Override
            public String getType() {
                int i = data.getColumnIndex(Contract.VideoTable.COLUMN_TYPE);
                return data.getString(i);
            }

            @Override
            public String getSite() {
                int i = data.getColumnIndex(Contract.VideoTable.COLUMN_SITE);
                return data.getString(i);
            }

            @Override
            public String getKey() {
                int i = data.getColumnIndex(Contract.VideoTable.COLUMN_KEY);
                return data.getString(i);
            }
        };

        return wrapper;
    }

    @Override
    public ReviewCursorWrapper createReviewCursorWrapper(final Cursor data) {
        ArgumentHelper.validateNull(data, "data");

        ReviewCursorWrapper wrapper = new ReviewCursorWrapper() {

            @Override
            public long getId() {
                int i = data.getColumnIndex(Contract.ReviewTable._ID);
                return data.getLong(i);
            }

            @Override
            public String getReviewContent() {
                int i = data.getColumnIndex(Contract.ReviewTable.COLUMN_CONTENT);
                return data.getString(i);
            }

            @Override
            public String getAuthor() {
                int i = data.getColumnIndex(Contract.ReviewTable.COLUMN_AUTHOR);
                return data.getString(i);
            }
        };

        return wrapper;
    }

    //endregion

    @Override
    public void bulkInsert(List<JSONObject> movies) {

        if(movies == null || movies.size() == 0)
            return;

        //Let's translated JSONObjects to ContentValues
        List<ContentValues> translatedObjects = new ArrayList<>();
        ContentValues translated = null;

        for(JSONObject obj : movies) {
            translated = this.getMovieContentValues(obj, true);

            if(translated.size() > 0)
                translatedObjects.add( translated);
        }

        //Now it is time to send it to the provider...
        if(translatedObjects.size() > 0) {
            ContentValues[] values = new ContentValues[translatedObjects.size()];
            values = translatedObjects.toArray(values);

            this.currentContext.getContentResolver()
                    .bulkInsert(Contract.MovieTable.CONTENT_URI, values);
        }
    }

    @Override
    public void updateMany(List<JSONObject> movies) {

        if(movies == null || movies.size() == 0)
            return;

        ContentValues values = null;
        for(JSONObject obj: movies) {
            values = this.getMovieContentValues(obj, false);

            if(values.size() > 0) {

                Uri uri = Contract
                        .MovieTable
                        .buildUri(values.getAsLong(Contract.MovieTable._ID));

                this.currentContext.getContentResolver()
                    .update(uri, values, null, null);
            }
        }
    }

    @Override
    public Map<String, String> getExternalIdJsonHashMap() {

        Cursor cursor = null;
        Map<String, String> myMap = new HashMap<>();
        try {
            cursor = this.currentContext.getContentResolver()
                    .query(Contract.MovieTable.CONTENT_URI,
                            new String[] { Contract.MovieTable.COLUMN_EXTERNAL_ID,
                                           Contract.MovieTable.COLUMN_JSON_HASH},
                    null,
                    null,
                    null);

            while (cursor.moveToNext()) {
                myMap.put( cursor.getString(0), cursor.getString(1));
            }
        }
        finally {
            if(cursor != null)
                cursor.close();
        }

        return myMap;
    }

    @Override
    public Map<String, Long> getExternalIdIdMap() {

        Cursor cursor = null;
        Map<String, Long> myMap = new HashMap<>();
        try {
            cursor = this.currentContext.getContentResolver()
                    .query(Contract.MovieTable.CONTENT_URI,
                            new String[] { Contract.MovieTable.COLUMN_EXTERNAL_ID,
                                    Contract.MovieTable._ID},
                            null,
                            null,
                            null);

            while (cursor.moveToNext()) {
                myMap.put( cursor.getString(0), cursor.getLong(1));
            }
        }
        finally {
            if(cursor != null)
                cursor.close();
        }

        return myMap;
    }

    @Override
    public String getMD5Hash(JSONObject jsonObject) {

        if(jsonObject == null)
            return null;

        String hashed = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();

            byte[] bytes = digest.digest(jsonObject.toString().getBytes("UTF-8"));
            hashed = Base64.encodeToString(bytes, 0);

        }
        catch (Exception ex) {
            String message = String.format("Error generating hash from json: %s", ex.getMessage());
            Log.e(LOG_TAG, message);
        }

        return  hashed;
    }

    @Override
    public CursorLoader createCursorLoaderForAvailableMovies(SortOrder order, boolean onlyFavorites) {

        //Determining the projection...
        String[] projection = new String[] {
                        Contract.MovieTable._ID,
                        Contract.MovieTable.COLUMN_POSTER
                };

        //Determining the selection and args...
        String selection = null;
        String[] args = null;

        if(onlyFavorites) {
            selection = String.format("%s = ?", Contract.MovieTable.COLUMN_FAVORITE);
            args = new String[] { String.valueOf(FAVORITE_FLAG) };
        }

        //Determining the sort order...
        String sortOrder = null;

        switch (order) {
            case MOST_POPULAR:
                sortOrder = MOST_POPULAR_SORT_ORDER;
                break;

            case HIGHEST_RATED:
                sortOrder = HIGHEST_RATED_SORT_ORDER;
                break;
        }

        //Finally... create de loader
        CursorLoader loader
                = new CursorLoader(this.currentContext,
                            Contract.MovieTable.CONTENT_URI,
                            projection,
                            selection,
                            args,
                            sortOrder
                );

        return loader;
    }

    @Override
    public CursorLoader createCursorLoaderForMovieDetail(long movieId) {

        //Projection..
        String[] projection = new String[] {
                Contract.MovieTable._ID,
                Contract.MovieTable.COLUMN_TITLE,
                Contract.MovieTable.COLUMN_POSTER,
                Contract.MovieTable.COLUMN_SYNOPSIS,
                Contract.MovieTable.COLUMN_USER_RATING,
                Contract.MovieTable.COLUMN_RELEASE,
                Contract.MovieTable.COLUMN_FAVORITE
        };

        //Movie uri
        Uri uri = Contract.MovieTable.buildUri(movieId);

        CursorLoader loader = new CursorLoader(this.currentContext,
                uri,
                projection,
                null,
                null,
                null);

        return loader;

    }

    @Override
    public CursorLoader createCursorLoaderForMovieVideos(long movieId) {

        //Determining the projection...
        String[] projection = new String[] {
                Contract.VideoTable._ID,
                Contract.VideoTable.COLUMN_KEY,
                Contract.VideoTable.COLUMN_NAME,
                Contract.VideoTable.COLUMN_TYPE,
                Contract.VideoTable.COLUMN_SITE
        };

        //Selection
        String selection = String.format("%s = ?", Contract.VideoTable.COLUMN_MOVIE_ID);

        //Selection args
        String[] args = new String[] { String.valueOf(movieId) };

        CursorLoader loader
                = new CursorLoader(this.currentContext,
                Contract.VideoTable.CONTENT_URI,
                projection,
                selection,
                args,
                null
        );

        return loader;
    }

    @Override
    public CursorLoader createCursorLoaderForMovieReviews(long movieId) {

        //Determining the projection...
        String[] projection = new String[] {
                Contract.ReviewTable._ID,
                Contract.ReviewTable.COLUMN_CONTENT,
                Contract.ReviewTable.COLUMN_AUTHOR
        };

        //Selection
        String selection = String.format("%s = ?", Contract.VideoTable.COLUMN_MOVIE_ID);

        //Selection args
        String[] args = new String[] { String.valueOf(movieId) };

        CursorLoader loader
                = new CursorLoader(this.currentContext,
                Contract.ReviewTable.CONTENT_URI,
                projection,
                selection,
                args,
                null
        );

        return loader;
    }

    @Override
    public void persistVideos(long movieId, List<JSONObject> videos) {

        if(videos == null)
            return;

        Uri uri = Contract.VideoTable.CONTENT_URI;

        //First, lets "clean" all possible existing registries
        this.currentContext
                .getContentResolver()
                .delete(uri,
                        String.format("%s = ?", Contract.VideoTable.COLUMN_MOVIE_ID),
                        new String[] { String.valueOf(movieId)}
                );

        //Start inserting...

        //Let's translated JSONObjects to ContentValues
        List<ContentValues> translatedObjects = new ArrayList<>();
        ContentValues translated = null;

        for(JSONObject obj : videos) {
            translated = this.getVideoContentValues(movieId, obj);

            if(translated.size() > 0)
                translatedObjects.add(translated);
        }

        //Now it is time to send it to the provider...
        if(translatedObjects.size() > 0) {
            ContentValues[] values = new ContentValues[translatedObjects.size()];
            values = translatedObjects.toArray(values);

            this.currentContext.getContentResolver()
                    .bulkInsert(Contract.VideoTable.CONTENT_URI, values);
        }
    }

    @Override
    public void persistReviews(long movieId, List<JSONObject> reviews) {

        if(reviews == null)
            return;

        Uri uri = Contract.ReviewTable.CONTENT_URI;

        //First, lets "clean" all possible existing registries
        this.currentContext
                .getContentResolver()
                .delete(uri,
                        String.format("%s = ?", Contract.ReviewTable.COLUMN_MOVIE_ID),
                        new String[] { String.valueOf(movieId)}
                );

        //Start inserting...

        //Let's translated JSONObjects to ContentValues
        List<ContentValues> translatedObjects = new ArrayList<>();
        ContentValues translated = null;

        for(JSONObject obj : reviews) {
            translated = this.getReviewContentValues(movieId, obj);

            if(translated.size() > 0)
                translatedObjects.add(translated);
        }

        //Now it is time to send it to the provider...
        if(translatedObjects.size() > 0) {
            ContentValues[] values = new ContentValues[translatedObjects.size()];
            values = translatedObjects.toArray(values);

            this.currentContext.getContentResolver()
                    .bulkInsert(Contract.ReviewTable.CONTENT_URI, values);
        }
    }

    @Override
    public void toggleMovieFavorite(long movieId) {

        if(movieId <= 0)
            return;

        Uri movieUri = Contract.MovieTable.buildUri(movieId);

        Cursor cursor = this.currentContext.getContentResolver()
                            .query(movieUri,
                                    new String[] { Contract.MovieTable.COLUMN_FAVORITE},
                                    null,
                                    null,
                                    null);

        if(cursor != null && cursor.moveToFirst()) {

            int favoriteValue = cursor.getInt(0);

            //toggles the current value
            favoriteValue = favoriteValue ^ 1;

            //Updates the new value
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.MovieTable.COLUMN_FAVORITE, favoriteValue);

            this.currentContext.getContentResolver()
                    .update(movieUri,
                            contentValues,
                            null,
                            null);
        }
    }

    //endregion

    //region private aux methods

    //Translate the Json object to ContentValues object
    private ContentValues getMovieContentValues(JSONObject jsonObject, boolean forInsert) {

        ContentValues myMovie = new ContentValues();

        try {
            myMovie.put(Contract.MovieTable.COLUMN_EXTERNAL_ID, jsonObject.getString(MOVIE_EXTERNAL_ID));
            myMovie.put(Contract.MovieTable.COLUMN_TITLE, jsonObject.getString(MOVIE_TITLE));
            myMovie.put(Contract.MovieTable.COLUMN_SYNOPSIS, jsonObject.getString(MOVIE_SYNOPSIS));
            myMovie.put(Contract.MovieTable.COLUMN_RELEASE,
                    DATE_FORMAT.parse(jsonObject.getString(MOVIE_RELEASE)).getTime());
            myMovie.put(Contract.MovieTable.COLUMN_POSTER, jsonObject.getString(MOVIE_POSTER));
            myMovie.put(Contract.MovieTable.COLUMN_USER_RATING, (float) jsonObject.getDouble(MOVE_USER_RATING));
            myMovie.put(Contract.MovieTable.COLUMN_POPULARITY, (float) jsonObject.getDouble(MOVIE_POPULARITY));

            if(forInsert) {

                //By default, imported movies are not favorites
                myMovie.put(Contract.MovieTable.COLUMN_FAVORITE, NOT_FAVORITE_FLAG);
            }

            String jsonHash = this.getMD5Hash(jsonObject);
            myMovie.put(Contract.MovieTable.COLUMN_JSON_HASH, jsonHash);
        }
        catch (Exception ex) {
            String message = String.format("Error translating a 'Movie' JSONObject: %s", ex.getMessage());
            Log.e(LOG_TAG, message, ex);
        }

        return  myMovie;
    }

    private ContentValues getVideoContentValues(long movieId, JSONObject jsonObject) {

        ContentValues myVideo = new ContentValues();

        try {
            myVideo.put(Contract.VideoTable.COLUMN_EXTERNAL_ID, jsonObject.getString(VIDEO_EXTERNAL_ID));
            myVideo.put(Contract.VideoTable.COLUMN_KEY, jsonObject.getString(VIDEO_KEY));
            myVideo.put(Contract.VideoTable.COLUMN_MOVIE_ID, movieId);
            myVideo.put(Contract.VideoTable.COLUMN_NAME, jsonObject.getString(VIDEO_NAME));
            myVideo.put(Contract.VideoTable.COLUMN_SITE, jsonObject.getString(VIDEO_SITE));
            myVideo.put(Contract.VideoTable.COLUMN_TYPE, jsonObject.getString(VIDEO_TYPE));
        }
        catch (Exception ex) {
            String message = String.format("Error translating a 'Video' JSONObject: %s", ex.getMessage());
            Log.e(LOG_TAG, message, ex);
        }

        return  myVideo;
    }

    private ContentValues getReviewContentValues(long movieId, JSONObject jsonObject) {

        ContentValues myReview = new ContentValues();

        try {
            myReview.put(Contract.ReviewTable.COLUMN_EXTERNAL_ID, jsonObject.getString(REVIEW_EXTERNAL_ID));
            myReview.put(Contract.ReviewTable.COLUMN_MOVIE_ID, movieId);
            myReview.put(Contract.ReviewTable.COLUMN_AUTHOR, jsonObject.getString(REVIEW_AUTHOR));
            myReview.put(Contract.ReviewTable.COLUMN_CONTENT, jsonObject.getString(REVIEW_CONTENT));
        }
        catch (Exception ex) {
            String message = String.format("Error translating a 'Review' JSONObject: %s", ex.getMessage());
            Log.e(LOG_TAG, message, ex);
        }

        return  myReview;
    }

    //endregion
}
