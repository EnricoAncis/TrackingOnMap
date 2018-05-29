package com.release.eancis.trackingonmap.data;

/**
 * Created by Enrico Ancis on 29/05/18.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.release.eancis.trackingonmap.data.TracksContract.*;

/**
 * It creates the Database
 * */
public class TracksDbHelper  extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "tracks.db";

    // If it changes the database schema, it's needed to increment the database version
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public TracksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold tracks data
        final String SQL_CREATE_WAITLIST_TABLE = "CREATE TABLE " + TracksEntry.TABLE_NAME + " (" +
                TracksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TracksEntry.COLUMN_START_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                TracksEntry.COLUMN_END_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                TracksEntry.COLUMN_PATH + " TEXT NOT NULL, " +
                TracksEntry.COLUMN_PATH_COLOR + " INTEGER NOT NULL" +
                "); ";

        db.execSQL(SQL_CREATE_WAITLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // HEre simply drop the table and create a new one. This means if it change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        db.execSQL("DROP TABLE IF EXISTS " + TracksEntry.TABLE_NAME);
        onCreate(db);
    }
}
