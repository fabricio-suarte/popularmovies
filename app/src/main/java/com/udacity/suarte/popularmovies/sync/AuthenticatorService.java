package com.udacity.suarte.popularmovies.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * Wraps an AbstractAccountAuthenticator object and stub out all
 * of its methods
 */
public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private AbstractAccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        authenticator = new Authenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call,
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}