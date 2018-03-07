package com.example.android.datacollection;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;

/**
 * Created by Khashayar on 2/23/2018.
 *
 * This is the class to show location data using a ListView and CursorAdapter
 *
 * Since this whole class is an Cursor Adapter, and we are sorting the data as well, we will not
 * use the globalLocationArrayList and stick to the database query.
 */

public class LocationViewActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG ="LocationViewActivity";

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

        locationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LocationViewActivity.this, "Id is: " + Long.toString(id),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //Setup item click listener for location data
        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create new Intent to go to {@link EditorActivity}
                Intent intent = new Intent(LocationViewActivity.this, LocationEditActivity.class);

                /**
                 * From the content URi that represents the specific location that was clicked, by
                 * appending the "id" (passed as input to this method) onto the
                 * {@link LocationEntry#Content_URI}.
                 * For example the URI would be "content://come.example.android.datacollection/locations/2"
                 * if the location with ID 2 was clicked on
                 */
                Uri currentPetUri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, id);

                //set the Uri on the data field of the intent
                intent.setData(currentPetUri);

                //Launch the {@link LocationEditActivity} to display data for the current location
                startActivity(intent);
            }
        });//onClickListener ListView
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

        /**
         * We can order the list using the sorOrder argument; null will use the default sort.
         * Formatted as SQL ORDER BY clause (excluding the ORDER BY itself.
         * for reference: ORDER BY <column_name> <ASC|DESC>
         *     Here I want it to be ordered in descending order based on the ID (newest first)
         */
        String sortOrder = LocationEntry._ID + " DESC";

        //This loader will execute the ContentProvider's query method on background thread
        return new CursorLoader(this,   //parent activity context
                LocationEntry.CONTENT_URI,           //provider content Uri to query
                projection,                     //The columns to return for each row
                null,                   //Selection criteria
                null,               //Selection criteria
                sortOrder                    //The sort order for returned rows
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

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart called");
        super.onStart();
    }
}//LocationViewActivity Class


