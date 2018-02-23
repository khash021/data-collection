package com.example.android.datacollection.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;

/**
 * Created by Khashayar on 2/17/2018.
 * This is the class which creates the database
 */

public class LocationDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = LocationDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "location.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link LocationDbHelper}.
     *
     * @param context of the app
     */
    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }//LocationDbHelper


    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(LOG_TAG, "onCreate() called");
        // Create a String that contains the SQL statement to create the locations table
        /**
         *  Remember the SQLite command:
            CREATE TABLE <table name> (<column_name> <column_datatype>, .....)
            We are doing the same thing except we make it a String constant so we can just pass in the
            String to the method (execSQL) instead of writing it everytime
         */
        String SQL_CREATE_DATA_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY, " +
                LocationEntry.COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_GARBAGE + " INTEGER NOT NULL DEFAULT 0, " +
                LocationEntry.COLUMN_LOCATION_CONTAINER + " INTEGER NOT NULL DEFAULT 0, " +
                LocationEntry.COLUMN_LOCATION_PAPER + " INTEGER NOT NULL DEFAULT 0, " +
                LocationEntry.COLUMN_LOCATION_ESTABLISHMENT + " INTEGER NOT NULL DEFAULT 0, " +
                LocationEntry.COLUMN_LOCATION_ESTABLISHMENT_COMMENT + " TEXT, " +
                LocationEntry.COLUMN_LOCATION_COMMENT + " TEXT, " +
                LocationEntry.COLUMN_LOCATION_DATE + " TEXT);";

        /**
         *    Execute the SQL statement
         *    Note; execSQL is not a static method and we run that on the SQLiteDatabase object (db)
         *    that was passed in as an input argument in the onCreate method
         */
        db.execSQL(SQL_CREATE_DATA_TABLE);
        Log.d(LOG_TAG, "Table Created");
    }//onCreate

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }//onUpgrade
}//main
