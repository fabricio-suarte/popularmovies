package com.udacity.suarte.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * The Sync Service to bond the Sync Adapter
 */
public class SyncService extends Service {

    private static SyncAdapter syncAdapter;
    private static Object syncLock = new Object();

    @Override
    public void onCreate()
    {
        synchronized(syncLock) {

            //Creates the adapter not allowing parallel sync
            if(syncAdapter == null)
                syncAdapter = new SyncAdapter( this.getApplicationContext(), true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return (syncAdapter != null) ? syncAdapter.getSyncAdapterBinder() : null;
    }
}
