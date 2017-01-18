package com.udacity.suarte.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.udacity.suarte.popularmovies.domain.UserProfile;
import com.udacity.suarte.popularmovies.fragment.MovieDetailFragment;
import com.udacity.suarte.popularmovies.fragment.MoviesFragment;
import com.udacity.suarte.popularmovies.fragment.ReviewFullContentFragment;
import com.udacity.suarte.popularmovies.sync.SyncAdapter;

public class MainActivity extends AppCompatActivity
                          implements MoviesFragment.CallBackListener,
                                     MovieDetailFragment.CallBackListener,
                                     SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final String MOVIE_DETAIL_FRAGMENT_TAG = "movieDetailFragmentTAG";

    //region members

    private boolean notifySortOrderChanged;
    private boolean twoPane;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(this.findViewById(R.id.fragment_right_container) != null) {

            //this is a large screen device (probably a tablet)
            this.setupForTwoPane(savedInstanceState);
        }
        else {

            this.setupForSinglePane(savedInstanceState);
        }

        //Checks if it is the very first time the application is running
        //If application data was removed, it will be considered "first run" as well.
        UserProfile profile = new UserProfile(this);
        if(profile.isFirstRun()) {

            SyncAdapter.initializeSyncAdapter(this.getApplicationContext());
            profile.setFirstRun();
        }

        //Registers this activity as listener for shared preferences changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(this.notifySortOrderChanged) {
            this.notifyCurrentSortCriteria();
            this.notifySortOrderChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(TAG,"OnCreateOptionsMenu running...");

        this.getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "onOptionsItemSelected running...");

        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_settings:

                Intent showSettings = new Intent(this, SettingsActivity.class);
                this.startActivity(showSettings);

                return true;
        }

        return false;
    }

    //region 'SharedPreferences.OnSharedPreferenceChangeListener' implementation

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(sharedPreferences == null || key == null)
            return;

        //Checks if the preference that was changed is the one I'm interested.
        String sortCriteriaKey = this.getString(R.string.pref_sort_criteria_key);
        String showOnlyFavoritesKey = this.getString(R.string.pref_show_only_favorites_key);

        boolean callBackMoviesFragment = false;

        if(key.equals(sortCriteriaKey)) {

            //Set this flag so that the toast can be showed
            this.notifySortOrderChanged = true;
            callBackMoviesFragment = true;
        }
        else if(key.equals(showOnlyFavoritesKey)) {

            callBackMoviesFragment = true;
        }

        if(callBackMoviesFragment) {

            //Tells the movies fragment that some loading criteria setting has changed
            String fragTAG = this.getString(R.string.fragment_movies_tag);
            MoviesFragment fragment
                    = (MoviesFragment) this.getSupportFragmentManager()
                    .findFragmentByTag(fragTAG);

            if(fragment != null)
                fragment.onLoadingCriteriaChanged();
        }
    }

    //endregion


    //region MoviesFragment.CallBackListener implementation

    @Override
    public void onMovieSelected(long movieId) {

        if( !this.twoPane) {

            Intent movieDetailIntent = new Intent(this, DetailsActivity.class);
            movieDetailIntent.putExtra(DetailsActivity.EXTRA_MOVIE_ID, movieId);

            this.startActivity(movieDetailIntent);
        }
        else {

            Fragment frag = MovieDetailFragment.newInstance(movieId);

            //It is a two pane layout. We should have already an fragment loaded.
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_right_container, frag, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }

    //endregion

    //region MovieDetailFragment.CallBackListener implementation

    @Override
    public void onReviewFullContent(String fullContent) {

        ReviewFullContentFragment
                .createAndReplaceOn(fullContent, this, R.id.fragment_right_container);
    }

    //endregion

    //region private aux methods

    private void setupForTwoPane(Bundle savedInstanceState) {

        this.twoPane = true;

        if(savedInstanceState == null) {

            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_right_container,
                            new MovieDetailFragment(), MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }


    private void setupForSinglePane(Bundle savedInstanceState) {

        this.twoPane = false;
    }

    private void notifyCurrentSortCriteria() {

        UserProfile profile = new UserProfile(this);
        String currentSortCriteriaValue = profile.getCurrentSortOrderCriteriaValue();

        String mostPopularOrderValue
                = this.getString(R.string.pref_sort_criteria_most_popular_value);

        String highestRatedValue
                = this.getString(R.string.pref_sort_criteria_highest_rated_value);

        String messageFormat = this.getString(R.string.movies_listed_by_message);
        String message;

        if(currentSortCriteriaValue.equals(mostPopularOrderValue)) {
            message = String.format(messageFormat,
                    this.getString(R.string.pref_sort_criteria_most_popular_option)
            );
        }
        else if(currentSortCriteriaValue.equals(highestRatedValue)) {
            message = String.format(messageFormat,
                    this.getString(R.string.pref_sort_criteria_highest_rated_option));
        }
        else {
            message = String.format(messageFormat, "unknown");
        }

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    //endregion
}
