package com.example.android.datacollection.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.datacollection.Database.DataContract.DataEntry;

/**
 * Created by Khashayar on 2/17/2018.
 * This is the class which creates the database
 */

public class DataDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DataDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "location.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link DataDbHelper}.
     *
     * @param context of the app
     */
    public DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }//DataDbHelper


    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(LOG_TAG, "onCreate() called");
        // Create a String that contains the SQL statement to create the pets table
        /*
        Remember the SQLite command:
        CREATE TABLE <table name> (<column_name> <column_datatype>, .....)
        We are doing the same thing except we make it a String constant so we can just pass in the
        String to the method (execSQL) instead of writing it everytime
         */
        String SQL_CREATE_DATA_TABLE = "CREATE TABLE " + DataEntry.TABLE_NAME + " (" +
                DataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataEntry.COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                DataEntry.COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                DataEntry.COLUMN_LOCATION_GARBAGE + " INTEGER NOT NULL DEFAULT 0, " +
                DataEntry.COLUMN_LOCATION_CONTAINER + " INTEGER NOT NULL DEFAULT 0, " +
                DataEntry.COLUMN_LOCATION_PAPER + " INTEGER NOT NULL DEFAULT 0, " +
                DataEntry.COLUMN_LOCATION_COMMENT + " TEXT);";

        // Execute the SQL statement
        //Note; execSQL is not a static method and we run that on the SQLiteDatabase object (db)
        //that was passed in as an input argument in the onCreate method
        db.execSQL(SQL_CREATE_DATA_TABLE);
        Log.d(LOG_TAG, "Table Created");

    }//onCreate

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.

    }//onUpgrade
}//main
