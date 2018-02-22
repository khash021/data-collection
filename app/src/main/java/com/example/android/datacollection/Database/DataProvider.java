package com.example.android.datacollection.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Khashayar on 2/20/2018.
 */

/**
 * {@link ContentProvider} for Data Collection app.
 */
public class DataProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = DataProvider.class.getSimpleName();

    //Database helper object
    DataDbHelper mDbHelper;

    /** URI matcher code for the content URI for the locations table */
    private static final int LOCATIONS = 100;

    /** URI matcher code for the content URI for a single location in the locations table */
    private static final int LOCATION_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_LOCATIONS, LOCATIONS);
        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_LOCATIONS + "/#", LOCATION_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.

        //We used getContext() here, because we cannot use 'this' can only be used when the Class
        //extends from Context (such as Application, Activity, Service and IntentServices
        mDbHelper = new DataDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //Access database using the mDbHelper variable that we initialized in the onCreate, and get
        //the SQL object from the DbHelper
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        //Use the Uri matcher to help us determine what kind of input Uri was passed into us.
        //We will call match method on sUriMatcher (assigned in the beginning) and pass in the
        //uri that was given to us as an input argument of query. This will return an integer code
        int match = sUriMatcher.match(uri);

        //This is where we decide which path to go down based on the integer code of the Uri
        switch (match) {
            case LOCATIONS:
                //For the PETS code, query the pets table directly with the given input arguments.
                //This is because this code means the entire table and we do not need to decode the
                //specific row like we did in the PET_ID case below where we inserted the desired
                //row to the selection, and selection arg.
                cursor = database.query(DataContract.DataEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case LOCATION_ID:

                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = DataContract.DataEntry._ID + "=?";
                //ContentUris.parseID method converts the last path segment to a long (i.e. we get
                //the number after / corresponding to the row of the table
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(DataContract.DataEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                //This will make the app crash in case we have entered a wrong uri
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }//switch
        return cursor;
    }//query

    /**
     * Insert new data into the provider with the given ContentValues.
     */

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATIONS:
                return insertLocation(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }//switch
    }//insert

    /**
     * Insert a location point into the database with the given content values. Return the new
     * content URI for that specific row in the database.
     */
    private Uri insertLocation(Uri uri, ContentValues values){
        //Usually we would perform sanity check here, in order to prevent wrong data to be added
        //to our database. However, since the location data comes directly from LocationServices,
        //and the rest are booleans from checkboxes; there is no need for sanity check. Moreover,
        //comment could be anything String, so we do not perfomr any sanity check here.


        //Access database using the mDbHelper variable that we initialized in the onCreate, and get
        //the SQL object from the DbHelper
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //We store the number that was returned from our insert method (i.e. column ID)
        long newRowId = database.insert(DataContract.DataEntry.TABLE_NAME,null, values);

        if (newRowId == -1){
            //insert return -1 if there was an error and this means the pet was NOT added
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } //if

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowId);

    } //insertLocation

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     *
     */
    public int update( Uri uri, ContentValues values,  String selection,  String[] selectionArgs) {
        /**
         * We are not going to update data at this point. I feel like if there was an error in the input,
         * it would be much wiser to delete the last data, and input it again. The error is choosing the
         * wrong location from the list without any visual representation would create more errors.
         * As a result, we are leaving this blank for now
         */
        return 0;
    }//update


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        switch (match) {
            case LOCATIONS:
                return database.delete(DataContract.DataEntry.TABLE_NAME, selection, selectionArgs);
            case LOCATION_ID:
                selection = DataContract.DataEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(DataContract.DataEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }//switch
    }//delete

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATIONS:
                return DataContract.DataEntry.CONTENT_LIST_TYPE;
            case LOCATION_ID:
                return DataContract.DataEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }//getType

}//main
