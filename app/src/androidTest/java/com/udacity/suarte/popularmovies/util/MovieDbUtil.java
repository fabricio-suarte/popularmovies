package com.udacity.suarte.popularmovies.util;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import com.udacity.suarte.popularmovies.data.Contract;
import com.udacity.suarte.popularmovies.data.DbHelper;

/**
 * Database util test class
 */
public class MovieDbUtil {

    private static MovieDbUtil instance;
    private Context context;
    private DbHelper helper;

    private MovieDbUtil() {

        //gets a reference to application context (android)
        this.context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        this.helper = new DbHelper(this.context);

    }

    public static MovieDbUtil getInstance() {
        if(instance == null)
            instance = new MovieDbUtil();

        return instance;
    }

    public void deleteDatabase() {
        context.deleteDatabase(DbHelper.DB_NAME);
    }

    public int getCount(String tableName) {

        String sql = String.format("Select count(*) from %s", tableName);

        Cursor cursor = this.helper.getReadableDatabase().rawQuery(sql, null);

        int count = 0;

        if(cursor.moveToNext())
            count = cursor.getInt(0);

        cursor.close();

        return count;
    }
}
