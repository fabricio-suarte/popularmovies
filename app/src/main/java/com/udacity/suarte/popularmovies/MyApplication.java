package com.udacity.suarte.popularmovies;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.udacity.suarte.popularmovies.core.RepositoryManager;
import com.udacity.suarte.popularmovies.domain.MovieRepository;
import com.udacity.suarte.popularmovies.repository.MovieDataMapper;

/**
 * Provides initial application configuration. It must be set in the manifest.
 */
public class MyApplication extends Application {

    private static final String LOG_TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "Application 'onCreate' running...");

        Context context = this.getApplicationContext();

        //Configure the Repository manager!
        RepositoryManager.getInstance()
                .addRepositoryMap(MovieRepository.class, new MovieDataMapper(context));
    }
}
