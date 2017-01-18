package com.udacity.suarte.popularmovies.domain;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Movie's repository contract.
 */

public interface MovieRepository {

    /**
     * Possible queries sort order
     */
    enum SortOrder {
        MOST_POPULAR,
        HIGHEST_RATED
    }

    /**
     * An Wrapper for a movie cursor data
     */
    interface MovieCursorWrapper {
        long getId();
        String getPoster();
        String getTitle();
        String getSynopsis();
        float getUserRating();
        long getReleaseDate();
        int getFavorite();
    }

    /**
     * An Wrapper for a video cursor data
     */
    interface VideoCursorWrapper {
        long getId();
        String getName();
        String getType();
        String getSite();
        String getKey();
    }

    /**
     * An Wrapper for a review cursor data
     */
    interface ReviewCursorWrapper {
        long getId();
        String getReviewContent();
        String getAuthor();
    }

    /**
     * Creates a movie cursor wrapper for the given cursor
     * @param data the cursor containing movie data
     * @return MovieCursorWrapper
     */
    MovieCursorWrapper createMovieCursorWrapper(Cursor data);

    /**
     * Creates a video cursor wrapper for the given cursor
     * @param data the cursor containing video data
     * @return VideoCursorWrapper
     */
    VideoCursorWrapper createVideoCursorWrapper(Cursor data);

    /**
     * Creates a review cursor wrapper for the given cursor
     * @param data the cursor containing review data
     * @return ReviewCursorWrapper
     */
    ReviewCursorWrapper createReviewCursorWrapper(Cursor data);

    /**
     * Insert the given Movie JSONObject list
     * @param movies
     */
     void bulkInsert(List<JSONObject> movies);

    /**
     * Updates, one by one, the list of Movie objects
     * @param movies
     */
    void updateMany(List<JSONObject> movies);

    /**
     * Gets an Map of all Movies's external ids as the keys and the Json Hashes as values
     * @return An instance that implements Map
     */
    Map<String, String> getExternalIdJsonHashMap();

    /**
     * Gets an Map of all Movie's external ids as the keys and the internal ids as values
     * @return
     */
    Map<String, Long> getExternalIdIdMap();

    /**
     * Gets an hashed String representation for the given JSONObject
     * @param jsonObject An JSONObject instance
     * @return A hashed string representation
     */
    String getMD5Hash(JSONObject jsonObject);

    /**
     * Creates an instance of {@link CursorLoader} for available Movies listing.
     * Returns only the _ID and Poster columns.
     * @param order The sort order
     * @param onlyFavorites true for loading only movies tagged as "favorite"
     * @return CursorLoader
     */
    CursorLoader createCursorLoaderForAvailableMovies(SortOrder order, boolean onlyFavorites);

    /**
     * Returns an instance of {@link CursorLoader} containing movie data.
     * @param id A Movie Id
     * @return CursorLoader
     */
    CursorLoader createCursorLoaderForMovieDetail(long id);

    /**
     * Returns an instance of {@link CursorLoader} containing video data for the given movie id.
     * @param id A Movie Id
     * @return CursorLoader
     */
    CursorLoader createCursorLoaderForMovieVideos(long id);

    /**
     * Returns an instance of {@link CursorLoader} containing review data for the given movie id.
     * @param id A Movie Id
     * @return CursorLoader
     */
    CursorLoader createCursorLoaderForMovieReviews(long id);

    /**
     * Persist the videos information for a movie
     * @param movieId a persisted Movie's id
     * @param videos the video data list
     */
    void persistVideos(long movieId, List<JSONObject> videos);

    /**
     * Persist the reviews information for a movie
     * @param movieId a persisted Movie's id
     * @param reviews the video data list
     */
    void persistReviews(long movieId, List<JSONObject> reviews);

    /**
     * Toggle the "favorite" flag in the repository for a given movie id.
     * @param movieId The movie id
     */
    void toggleMovieFavorite(long movieId);

}
