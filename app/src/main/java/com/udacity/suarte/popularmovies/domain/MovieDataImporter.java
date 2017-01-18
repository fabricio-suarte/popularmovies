package com.udacity.suarte.popularmovies.domain;

/**
 * Represents a base abstraction for a Movie data importer
 */
public interface MovieDataImporter {

    /**
     * Get the Movie database API name
     * @return A string for the name
     */
    String getApiName();

    /**
     * Get the API root path
     * @return A string for the API url root path
     */
    String getApiRootPath();

    /**
     * Get the images root path
     * @return A string for the images url root path
     */
    String getImagesRootPath();

    /**
     * Import movies data from the API Service and persists it in the local database
     * @param page The number of the desired page (paging starts at 1)
     */
    void importMovies(int page);

    /**
     * Import movies details like clips, reviews, etc. for already persisted movies.
     */
    void importCurrentMoviesDetails();

}
