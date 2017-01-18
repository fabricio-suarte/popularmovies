package com.udacity.suarte.popularmovies;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.udacity.suarte.popularmovies.fragment.MovieDetailFragment;
import com.udacity.suarte.popularmovies.fragment.ReviewFullContentFragment;

public class DetailsActivity extends AppCompatActivity
                             implements MovieDetailFragment.CallBackListener{

    //region constants

    public static final String EXTRA_MOVIE_ID = "movieID";

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if(savedInstanceState == null) {

            //Let's get the given Uri and pass it to the fragment
            long movieId = this.getIntent().getLongExtra(EXTRA_MOVIE_ID, 0);

            if(movieId <= 0)
                throw new RuntimeException("A 'Movie id' greater than '0' was supposed to be set for this activity!");

            Fragment frag = MovieDetailFragment.newInstance(movieId);

            //Add the movie detail fragment
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_details, frag)
                    .commit();
        }
    }

    //region MovieDetailFragment.CallBackListener implementation

    @Override
    public void onReviewFullContent(String fullContent) {

        ReviewFullContentFragment
                .createAndReplaceOn(fullContent, this, R.id.activity_details);
    }

    //endregion
}
