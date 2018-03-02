package com.example.android.datacollection;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by Khashayar on 3/1/2018.
 *
 * This is the class that shows the data on Google Maps (in the app)
 */

public class MapsView extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private String TAG =this.getClass().getSimpleName();
    //Google Maps object
    GoogleMap mMap;

    CheckBox mGarbageCheckbox, mContainerCheckbox, mPaperCheckbox;

    ArrayList<Marker> mMarkerArrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_view_activity);

        /**
         * The fragment is what actually contains the Google Maps and displays it.
         * Here we get the SupportMapFragment (from the layout file) and request notification
         * when the map is ready to be used. (onMapReady callback will get called when the map is
         * ready
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }//onCreate

    /**
     * By implementing the OnMapReadyCallback interface and using the onMapReady(GoogleMap) callback
     * method, we get a handle to the GoogleMap object.
     * The callback is triggered when the map is ready to be used.
     * The GoogleMap object is the internal representation of the map itself
     * To set the view options for a map, you modify its GoogleMap object.
     * Then call getMapAsync() on the fragment to register the callback (above in onCreate)
     */

    //TODO: Need to add the class to wait for the data and markers to be added before inflating the map

    @Override
    public void onMapReady(GoogleMap map) {
        Log.v(TAG, "onMapReady callback triggered");
        mMap = map;

        //TODO: set limits for the map and also open the map centered at downtown with zoom 13-15

        //Use the helper method to add markers to the map retrieved from the database
        populateMarker();

        //Log the number of markers
        Log.v(TAG, "Total number of markers added to the map: " + mMarkerArrayList.size());
    }//onMapReady

    /**
     * This is the helper method that gets all the location data from the database, and creates
     * a marker object for each location (i.e. each row), and places them in an ArrayList
     * @return ArrayList<Marker>
     */
    //TODO: we need add the feature to automatically update the ArrayList as the database updates
    private void populateMarker(){
        /**
         * First we need to query the database and get all the data (cursor object).
         */
        //Define a projection for the columns we want
        //For now, I am not getting the establishment data since I have not collected any
        String[] projection = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_LATITUDE,
                LocationEntry.COLUMN_LOCATION_LONGITUDE,
                LocationEntry.COLUMN_LOCATION_GARBAGE,
                LocationEntry.COLUMN_LOCATION_PAPER,
                LocationEntry.COLUMN_LOCATION_CONTAINER,
                LocationEntry.COLUMN_LOCATION_COMMENT
        };

        //Query the database using our projection. The data is then passed back is a cursor Object
        Cursor cursor = getContentResolver().query(
                LocationEntry.CONTENT_URI,     //The content Uri
                projection,               //The columns to return for each row
                null,            //Selection criteria
                null,         //Selection criteria
                null            //The sort order for returned rows
        );

        //get the column ID for each column
        int idColumnIndex = cursor.getColumnIndex(LocationEntry._ID);
        int latColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE);
        int lngColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE);
        int garbageColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_GARBAGE);
        int containerColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_CONTAINER);
        int paperColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_PAPER);
        int commentColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_COMMENT);


        /**
         * This part uses the loop to go through each row, extract all the data, and
         */
        //TODO: set the marker colors, based on what is available at that spot
        //create a marker object
        Marker mMaker;
        while (cursor.moveToNext()) {
            //get the data from cursor
            int id = cursor.getInt(idColumnIndex);
            LatLng latLng = new LatLng(cursor.getDouble(latColumnIndex),
                    cursor.getDouble(lngColumnIndex));
            int garbage = cursor.getInt(garbageColumnIndex);
            int container = cursor.getInt(containerColumnIndex);
            int paper = cursor.getInt(paperColumnIndex);
            String comment = cursor.getString(commentColumnIndex);
            int[] booleanValues = {id, garbage, container, paper};

            //Make a new MarkerOptions object to add the data
            mMaker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .snippet(comment));
            //Add our int array as an object to our marker
            mMaker.setTag(booleanValues);

            //This section decides what to be shown in the title (garbage, container, or paper based
            //on what is available at that spot.
            String title = "";
            if (garbage == 1) {
                title += "Garbage  ";
            }
            if (container == 1) {
                title += "Container  ";
            }
            if (paper == 1) {
                title += "Paper";
            }
            mMaker.setTitle(title);

            mMarkerArrayList.add(mMaker);
        }//while
    }//populateMarker

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }//onMarkerClick
}//MapsView class
