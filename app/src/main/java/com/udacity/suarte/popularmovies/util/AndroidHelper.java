package com.udacity.suarte.popularmovies.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.udacity.suarte.popularmovies.core.ArgumentHelper;

/**
 * An Helper for dealing with some Android objects
 */

public final class AndroidHelper {

    /**
     * Creates an Account object for this application
     * @param context The android application context
     * @return {@link Account}
     */
    public static Account createApplicationAccount(Context context) {

        ArgumentHelper.validateNull(context, "context");

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(com.udacity.suarte.popularmovies.R.string.app_name),
                context.getString(com.udacity.suarte.popularmovies.R.string.app_sync_account_type)
        );

        return newAccount;
    }
}
