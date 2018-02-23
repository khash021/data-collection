package com.example.android.datacollection;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;

/**
 * Created by Khashayar on 2/23/2018.
 *
 * This is the class to show location data using a ListView and CursorAdapter
 */

public class LocationView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG ="LocationView";

    //Integer loader constant; you can set it up as any unique integer
    private final static int LOCATION_LOADER = 0;

    //Adapter for our ListView
    LocationCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.location_view);

        //find teh ListView and set it up
        ListView locationListView = findViewById(R.id.list_view);

        //Setup an adapter to create a list item for each row of location data in the Cursor.
        //There is no location data yet (until the loader finished)so pass in null for the Cursor
        mCursorAdapter = new LocationCursorAdapter(this, null);

        //Attach the cursoradapter to listview
        locationListView.setAdapter(mCursorAdapter);

        //Kick off the loader
        getLoaderManager().initLoader(LOCATION_LOADER, null, this);
    }//onCreate

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection for the columns we want
        String[] projection = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_GARBAGE,
                LocationEntry.COLUMN_LOCATION_PAPER,
                LocationEntry.COLUMN_LOCATION_CONTAINER
        };

        //This loader will execute the ContentProvider's query method on background thread
        return new CursorLoader(this,   //parent activity context
                LocationEntry.CONTENT_URI,           //provider content Uri to query
                projection,                     //The columns to return for each row
                null,                   //Selection criteria
                null,               //Selection criteria
                null                    //The sort order for returned rows
        );
    }//onCreateLoader

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update {@link LocationCursorAdapter} with new cursor containing updated location data
        mCursorAdapter.swapCursor(data);
    }//onLoadFinished

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }//onLoaderReset
}//LocationView Class
