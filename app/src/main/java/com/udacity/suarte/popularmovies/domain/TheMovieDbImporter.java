package com.udacity.suarte.popularmovies.domain;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.udacity.suarte.popularmovies.BuildConfig;
import com.udacity.suarte.popularmovies.core.RepositoryManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link MovieDataImporter} implementation for themoviedb.org service
 */
public class TheMovieDbImporter implements MovieDataImporter {

    private static final String LOG_TAG = TheMovieDbImporter.class.getSimpleName();

    private static final String API_NAME = "themoviedb.org";
    private static final String API_ROOT_PATH = "https://api.themoviedb.org/3/movie";
    //                                             "http://image.tmdb.org/t/p/w342";
    private static final String IMAGES_ROOT_PATH = "http://image.tmdb.org/t/p/w185";
    private static final String PATH_MOST_POPULAR = "popular";
    private static final String PATH_TOP_RATED = "top_rated";

    private static final String PATH_MOVIE_VIDEOS_FORMAT = "%s/videos";
    private static final String PATH_MOVIE_REVIEWS_FORMAT = "%s/reviews";

    private static final String API_KEY_PARAMETER = "api_key";
    private static final String PAGE_PARAMETER = "page";
    private static final int DEFAULT_PAGE = 1;

    //Tells to 'getJsonFromApiService' method to not append page parameter
    private static final int NO_PAGING = -1;

    //themoviedb.org json object names constants mapping
    private static final String MOVIES_LIST = "results";
    private static final String MOVIE_VIDEOS = "results";
    private static final String MOVIE_REVIEWS = "results";

    //The amount in milliseconds for pausing between details importing
    private static final long DEFAULT_PAUSE_BETWEEN_DETAILS_IMPORTING = 50;

    //region MovieDataImporter interface implementation

    @Override
    public String getApiName() {
        return API_NAME;
    }

    @Override
    public String getApiRootPath() {
        return API_ROOT_PATH;
    }

    @Override
    public String getImagesRootPath() { return IMAGES_ROOT_PATH; }

    @Override
    public void importMovies(int page) {

        if(page <=0 )
            page = DEFAULT_PAGE;

        String mostPopularContent = this.getJsonFromApiService(PATH_MOST_POPULAR, page);
        if( ! TextUtils.isEmpty(mostPopularContent))
            this.importMovieJsonContent(mostPopularContent);

        String topRatedContent = this.getJsonFromApiService(PATH_TOP_RATED, page);
        if( ! TextUtils.isEmpty(topRatedContent))
            this.importMovieJsonContent(topRatedContent);
    }

    @Override
    public void importCurrentMoviesDetails() {

        MovieRepository repository = RepositoryManager
                .getInstance()
                .getRepository(MovieRepository.class);

        //Get a Map containing movie's external id as the key and the internal id as value.
        Map<String, Long> externalIds = repository.getExternalIdIdMap();

        if(externalIds == null || externalIds.keySet().size() == 0)
            return;

        String movieDetailPath = null;
        String reviewDetailPath = null;
        String json = null;
        long movieId;
        for(String externalId : externalIds.keySet()) {

            //Local movie id
            movieId = externalIds.get( externalId);

            //First, let's build the path for videos
            movieDetailPath = String.format(PATH_MOVIE_VIDEOS_FORMAT, externalId);
            json = this.getJsonFromApiService(movieDetailPath, NO_PAGING);

            if(json != null) {
                this.importMovieVideosJsonContent(movieId, json );
            }

            //Now, reviews
            reviewDetailPath = String.format(PATH_MOVIE_REVIEWS_FORMAT, externalId);
            json = this.getJsonFromApiService(reviewDetailPath, NO_PAGING);

            if(json != null) {
                this.importMovieReviewsJsonContent(movieId, json );
            }

            //Just for trying not flood MovieDb API with many requests per second...
            this.pause(DEFAULT_PAUSE_BETWEEN_DETAILS_IMPORTING);
        }
    }

    //endregion

    //region Private auxiliary methods

    private void pause(long millis) {

        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException ex)
        {
        }
    }

    private void importMovieVideosJsonContent(long movieId, String videosJsonContent) {
        if(videosJsonContent == null)
            return;

        try {

            JSONObject jsonObj = new JSONObject(videosJsonContent);
            JSONArray videoArray = jsonObj.getJSONArray(MOVIE_REVIEWS);

            JSONObject videoJsonObj;

            List<JSONObject> videosForImporting = new ArrayList<>(videoArray.length());

            for(int i = 0; i < videoArray.length(); i++) {

                try {
                    videoJsonObj = videoArray.getJSONObject(i);
                    videosForImporting.add(videoJsonObj);
                }
                catch (Exception ex) {
                    Log.e(LOG_TAG, "Error parsing a 'Video' from Json for Movie Id '" + String.valueOf(movieId) + "'!", ex);
                }
            }

            MovieRepository repository = RepositoryManager.getInstance()
                    .getRepository(MovieRepository.class);

            repository.persistVideos(movieId, videosForImporting);

        }
        catch (JSONException ex) {
            Log.e(LOG_TAG, "Something went wrong during videos importing for movie id '" + String.valueOf(movieId) + "'!", ex);
        }
    }

    private void importMovieReviewsJsonContent(long movieId, String reviewsJsonContent) {
        if(reviewsJsonContent == null)
            return;

        try {

            JSONObject jsonObj = new JSONObject(reviewsJsonContent);
            JSONArray reviewArray = jsonObj.getJSONArray(MOVIE_REVIEWS);

            JSONObject reviewJsonObj;

            List<JSONObject> reviewsForImporting = new ArrayList<>(reviewArray.length());

            for(int i = 0; i < reviewArray.length(); i++) {

                try {
                    reviewJsonObj = reviewArray.getJSONObject(i);
                    reviewsForImporting.add(reviewJsonObj);
                }
                catch (Exception ex) {
                    Log.e(LOG_TAG, "Error parsing a 'Review' from Json for Movie Id '" + String.valueOf(movieId) + "'!", ex);
                }
            }

            MovieRepository repository = RepositoryManager.getInstance()
                    .getRepository(MovieRepository.class);

            repository.persistReviews(movieId, reviewsForImporting);

        }
        catch (JSONException ex) {
            Log.e(LOG_TAG, "Something went wrong during reviews importing for movie id '" + String.valueOf(movieId) + "'!", ex);
        }
    }

    private void importMovieJsonContent(String content) {
        if(content == null)
            return; //nothing to do

        try {
            JSONObject movieDbJson = new JSONObject(content);
            JSONArray movieArray = movieDbJson.getJSONArray(MOVIES_LIST);

            JSONObject movieJson;

            //Create this list of movies that should be imported / inserted and updated.
            List<JSONObject> moviesForInsert = new ArrayList<>();
            List<JSONObject> moviesForUpdate = new ArrayList<>();

            //Let's instantiate the DataMapper for Movie and get a set of Movies's external ids
            //This is going to be useful to test if a Movie is already imported or not
            MovieRepository repository = RepositoryManager
                    .getInstance()
                    .getRepository(MovieRepository.class);

            Map<String, String> externalIds = repository.getExternalIdJsonHashMap();

            String jsonHashAux;
            String movieExternalId;

            for (int i = 0; i < movieArray.length(); i++) {
                movieJson = movieArray.getJSONObject(i);

                movieExternalId = movieJson.getString("id");

                //Verifies if this movie has been imported before
                if ( ! externalIds.containsKey(movieExternalId) ) {

                   //Add this instance for insert / import
                    moviesForInsert.add(movieJson);
                }
                else {

                    //This movie has been imported before. Let's check if its data has changed.
                    jsonHashAux = repository.getMD5Hash(movieJson);

                    if(jsonHashAux != null && !jsonHashAux.equals( externalIds.get(movieExternalId)) ) {
                        //Data has changed.

                        //Add this instance for update
                        moviesForUpdate.add(movieJson);
                    }
                }
            }

            //Persists all the new movies...
            repository.bulkInsert(moviesForInsert);

            //Persists all the updates...
            repository.updateMany(moviesForUpdate);
        }
        catch (JSONException ex) {
            String message = String.format("A Json error has occurred during the import: %s", ex.getMessage());
            Log.e(LOG_TAG, message, ex);
            ex.printStackTrace();
        }
        catch (Exception ex) {
            String message = String.format("An unexpected error has occurred during the import: %s", ex.getMessage());
            Log.e(LOG_TAG, message, ex);
            ex.printStackTrace();
        }
    }

    private String getJsonFromApiService(String path, int page) {

        //These ones MUST be out of the try / catch so that they can be closed
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        //The returned Json
        String jsonContent = null;

        try {

            //Take a look in the "app/build.gradle" and "gradle.properties" files
            // in order to figure out how BuildConfig.MOVIE_DB_ORG_API_KEY works
            Uri.Builder builder = Uri.parse(API_ROOT_PATH)
                    .buildUpon()
                    .appendEncodedPath( path );

            //Appending the api key parameter
            Uri builtUri;

            if(page != NO_PAGING) {
                builtUri = builder
                        .appendQueryParameter(API_KEY_PARAMETER, BuildConfig.MOVIE_DB_ORG_API_KEY)
                        .appendQueryParameter(PAGE_PARAMETER, String.valueOf(page))
                        .build();
            }
            else {
                builtUri = builder
                        .appendQueryParameter(API_KEY_PARAMETER, BuildConfig.MOVIE_DB_ORG_API_KEY)
                        .build();
            }

            URL url = new URL(builtUri.toString());

            // Create the request and open the connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            if (inputStream == null)
                return null; //Nothing to do here

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON,  adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            jsonContent = buffer.toString();
        }
        catch (IOException ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
        }
        finally {
            try {
                if (reader != null)
                    reader.close();
            }
            catch (IOException ex) { }

            if(connection != null)
                connection.disconnect();
        }

        return  jsonContent;
    }

    //endregion
}