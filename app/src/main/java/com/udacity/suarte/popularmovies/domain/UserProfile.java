package com.udacity.suarte.popularmovies.domain;

/**
 * Created by suarte on 12/5/16.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;

import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.core.ArgumentHelper;
import com.udacity.suarte.popularmovies.util.AndroidHelper;

/**
 * This class is in charge of handling some business logic regarding app user's profile.
 */
public class UserProfile {

    //region members

    private Context context;

    //endregion

    //region constructors

    public UserProfile(Context context) {
        ArgumentHelper.validateNull(context, "context");

        this.context = context;
    }

    //endregion

    //region public methods

    /**
     * Returns true if it is the very first time the user is running the application.
     *
     * @return boolean
     */
    public boolean isFirstRun() {

        boolean firstRunByPreferences = this.isFirstRunByPreferences();
        boolean firstRunByAccount = this.isFirstRunByAccount();

        return (firstRunByAccount || firstRunByPreferences);
    }

    /**
     * Set that the first run has taken place.
     */
    public void setFirstRun() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        if (preferences == null)
            return;

        String key = this.context.getString(R.string.user_profile_first_run_key);
        preferences
                .edit()
                .putBoolean(key, false)
                .apply();
    }

    /**
     * Returns the current sort order criteria value from SharedPreferences.
     * If it does not exist, returns ""
     *
     * @return String
     */
    public String getCurrentSortOrderCriteriaValue() {

        //Get the current sort order from shared preferences
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(this.context);

        String key = this.context.getString(R.string.pref_sort_criteria_key);
        String value = sharedPreferences.getString(key, "");

        return value;
    }

    /**
     * Returns true if the option "show only favorite" is checked.
     * @return boolean
     */
    public boolean isOnlyFavoritesChecked() {
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(this.context);

        String key = this.context.getString(R.string.pref_show_only_favorites_key);
        boolean value = sharedPreferences.getBoolean(key, false);

        return value;
    }

    /**
     * Returns the max number of pages to be imported from Movies API
     * @return int
     */
    public int getCurrentMaxNumberOfPages() {

        //Get the current sort order from shared preferences
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(this.context);

        String key = this.context.getString(R.string.pref_max_number_of_pages_key);
        String defaultValue = this.context.getString(R.string.pref_max_number_of_pages_default_value);
        String value = sharedPreferences.getString(key, defaultValue);

        int intValue = Integer.parseInt(value);

        return intValue;
    }

    //endregion

    //region private aux methods

    private boolean isFirstRunByPreferences() {

        boolean firstRun;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        //If there is no default preferences, let's assume that it is the first time the application
        //runs.
        if (preferences == null)
            firstRun = true;
        else {

            String key = this.context.getString(R.string.user_profile_first_run_key);

            //Try to get the setting. If it does not exist, it is the first time the application
            //is running. That's why the default is true.
            firstRun = preferences.getBoolean(key, true);
        }

        return firstRun;
    }

    private boolean isFirstRunByAccount() {

        //Create an Account object
        Account account = AndroidHelper.createApplicationAccount(this.context);

        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // If the password doesn't exist, the account doesn't exist.
        // Let's consider "first run" too, if the account does not exist. This way, it is possible
        // to address both issues: account creation (possible harmed by Android backup apps feature,
        // which backups app's shared preferences and could avoid account and syncing setting after
        // an uninstall / install operation) and app's data deleting.
        boolean firstRun = accountManager.getPassword(account) == null;

        return firstRun;
    }

    //endregion

}
