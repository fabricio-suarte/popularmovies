package com.udacity.suarte.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.domain.DefaultMovieDataImporterFactory;
import com.udacity.suarte.popularmovies.domain.MovieDataImporter;
import com.udacity.suarte.popularmovies.domain.MovieDataImporterFactory;
import com.udacity.suarte.popularmovies.domain.UserProfile;
import com.udacity.suarte.popularmovies.util.AndroidHelper;

/**
 * An {@link AbstractThreadedSyncAdapter} implementation for syncing Movie's data between server API
 * and the app.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getCanonicalName();

    // Interval at which to sync with the movie API, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private Context context;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        synchronized (this) {

//            //Just for debugging...
//            try {
//                Thread.sleep(10000);
//            }
//            catch (Exception ex) {}

            //First, test if there is connection to the internet
            Object service = this.context.getSystemService(context.CONNECTIVITY_SERVICE);

            if (service != null) {
                ConnectivityManager manager = (ConnectivityManager) service;

                NetworkInfo info = manager.getActiveNetworkInfo();

                if (info != null && info.isConnected()) {

                    //Now we know that we are connected. Let's try to sync data
                    MovieDataImporterFactory factory = DefaultMovieDataImporterFactory.getInstance();
                    MovieDataImporter importer = factory.createJsonDataImporter();

                    //region Movies
                    Log.i(TAG, "Starting importing of movies...");

                    UserProfile profile = new UserProfile(this.getContext());
                    int pages = profile.getCurrentMaxNumberOfPages();

                    for(int i = 1; i <= pages; i++){

                        Log.i(TAG, "Importing page " + String.valueOf(i));

                        importer.importMovies(i);
                    }

                    Log.i(TAG, "Movies imported successfully");

                    //endregion

                    //region Movies's details

                    Log.i(TAG, "Starting importing of movies's details (videos and reviews...");

                    importer.importCurrentMoviesDetails();

                    Log.i(TAG, "Movies's details imported successfully");

                    //endregion
                }
            }
        }
    }

    /**
     * Takes care of the necessary in order to get things done
     * @param context Application context
     */
    public static void initializeSyncAdapter(Context context) {

        Log.d(TAG, "initializeSyncAdapter running...");

        Account account = AndroidHelper.createApplicationAccount(context);

        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // If the password doesn't exist, the account doesn't exist
        if ( accountManager.getPassword(account) == null ) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                Log.e(TAG, "It was not possible to add the sync account!");
                return;
            }

            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(account, context);
        }

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context, account);

    }

    //region private auxiliary methods


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, newAccount);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.app_content_authority),
                true
        );
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, Account account) {

        String authority = context.getString(R.string.app_content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle())
                    .build();

            ContentResolver.requestSync(request);

        } else {

            ContentResolver.addPeriodicSync(account, authority, new Bundle(), SYNC_INTERVAL);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    private static void syncImmediately(Context context, Account account) {

        Bundle bundle = new Bundle();

        //Schedule this request at the front of the sync request queue
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        //The same as setting both SYNC_EXTRAS_IGNORE_SETTINGS and SYNC_EXTRAS_INITIALIZE
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(account,
                context.getString(R.string.app_content_authority),
                bundle
        );
    }

    //endregion
}
