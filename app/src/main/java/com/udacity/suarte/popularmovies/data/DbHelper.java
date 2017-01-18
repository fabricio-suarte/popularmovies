package com.udacity.suarte.popularmovies.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite Open Helper implementation
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DbHelper.class.getCanonicalName();
    public static final String DB_NAME = "PopularMovieDB";
    private static final int DB_VERSION = 2;

    public DbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        this.runDDL(db, DDL.CREATE_MOVIE_TABLE);
        this.runDDL(db, DDL.CREATE_VIDEO_TABLE);
        this.runDDL(db, DDL.CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //For version 2: videos and reviews were added.
        if(oldVersion == 1 && newVersion == 2) {

            this.runDDL(db, DDL.CREATE_VIDEO_TABLE);
            this.runDDL(db, DDL.CREATE_REVIEW_TABLE);
            this.runDDL(db, DDL.ALTER_TABLE_MOVIE_ADD_FAVORITE);
        }
    }

    private void runDDL(SQLiteDatabase db, String DDL) {
        try {

            db.execSQL(DDL);
        }
        catch (SQLException ex) {
            String msg = String.format("An error has occurred while trying to run the given script:" + DDL);
            Log.e(LOG_TAG, msg, ex);
        }
    }
}
