package com.udacity.suarte.popularmovies;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.udacity.suarte.popularmovies.data.Contract;
import com.udacity.suarte.popularmovies.domain.DefaultMovieDataImporterFactory;
import com.udacity.suarte.popularmovies.domain.MovieDataImporter;
import com.udacity.suarte.popularmovies.domain.MovieRepository;
import com.udacity.suarte.popularmovies.util.MovieDbUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test Class for {@link com.udacity.suarte.popularmovies.domain.MovieDataImporter}
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MovieDataImporterTest {

    private static final String LOG_TAG = MovieDataImporterTest.class.getSimpleName();
    private MovieDataImporter importer;

    //This is executed before every method test...
    @Before
    public void before() {
        this.importer = DefaultMovieDataImporterFactory.getInstance().createJsonDataImporter();

        //Deletes the database, so tests can starts from scratch
        MovieDbUtil.getInstance().deleteDatabase();
    }

    /**
     * Tests the importing of Movies by the very first time (no previous movies imported)
     * successfully
     */
    @Test
    public void importMovies_VeryFirstTime_Ok() {

        int movieCount = 0;
        int videoCount = 0;
        int reviewCount = 0;

        try {
            //Import movies first page...
            this.importer.importMovies(1);

            //Import movies videos data
            this.importer.importCurrentMoviesDetails();

            movieCount = MovieDbUtil.getInstance().getCount(Contract.MovieTable.NAME);
            videoCount = MovieDbUtil.getInstance().getCount(Contract.VideoTable.NAME);
            reviewCount = MovieDbUtil.getInstance().getCount(Contract.ReviewTable.NAME);
        }
        catch (Exception ex){
            Log.e(LOG_TAG,
                    String.format("'importMovies_VeryFirstTime_Ok' test has failed due an exception: %s", ex.getMessage()),
                    ex);
        }

        //Asserts if movies have been imported
        assertThat(movieCount > 0 && videoCount > 0 && reviewCount > 0, is(true));
    }
}
