package com.example.android.datacollection;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

/**
 * Created by Khashayar on 3/2/2018.
 *
 * This class shows the location data in the form of heatmap
 *
 * I have added the steps to be done at the end of this class
 *
 * NOTE: don't forget to add compile 'com.google.maps.android:android-maps-utils:0.3+' to your
 * gradle to get the libraries
 */

public class HeatmapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = this.getClass().getSimpleName();

    private GoogleMap mMap;

    //ArrayList of all the locations
    ArrayList<LatLng> mLocationArrayList = new ArrayList<>();

    //Cursor object containing the data
    private Cursor mCursor;

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

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

    @Override
    public void onMapReady(GoogleMap map) {
        Log.v(TAG, "onMapReady called");
        mMap = map;

        // Create a heat map tile provider, passing it the latlngs (using the helper method to
        //create an ArrayList of LatLng
        mProvider = new HeatmapTileProvider.Builder()
                .data(makeArrayList())
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        //restricting users panning to Vancouver dt area. First input is the SW corner, and the
        // second NE corner of the restricted pan area
        LatLngBounds limit = new LatLngBounds(new LatLng( 49.268642, -123.148639),
                new LatLng( 49.300045, -123.095893));
        //Add limit to our map
        mMap.setLatLngBoundsForCameraTarget(limit);

        //Since the pan is limited, we should also limit min zoom, other wise they can zoom all the
        //way out and the bounds would be useless
        mMap.setMinZoomPreference(13.0f);

        //Set the initial camera to the center of downtown (where it says Vancouver)
        LatLng initialLocation =  new LatLng( 49.282733f, -123.120732f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 13.0f));

        Log.v(TAG, "onMapReady finished");
    }//onMapReady

    /**
     * Helper method that get all the data from databse and creates an ArrayList<LatLng>
     * @return ArrayList
     */
    private ArrayList<LatLng> makeArrayList(){
        /**
         * First we need to query the database and get all the data (mCursor object).
         */
        //Define a projection for the columns we want
        //For now, I am not getting the establishment data since I have not collected any
        String[] projection = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_LATITUDE,
                LocationEntry.COLUMN_LOCATION_LONGITUDE
        };

        //Query the database using our projection. The data is then passed back is a mCursor Object
        Cursor mCursor = getContentResolver().query(
                LocationEntry.CONTENT_URI,     //The content Uri
                projection,               //The columns to return for each row
                null,            //Selection criteria
                null,         //Selection criteria
                null            //The sort order for returned rows
        );

        //get the column ID for each column
        int idColumnIndex = mCursor.getColumnIndex(LocationEntry._ID);
        int latColumnIndex = mCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE);
        int lngColumnIndex = mCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE);

        while (mCursor.moveToNext()) {
            LatLng latLng = new LatLng(mCursor.getDouble(latColumnIndex),
                    mCursor.getDouble(lngColumnIndex));
            mLocationArrayList.add(latLng);

        }//while
        return mLocationArrayList;
    }//populateArray

}//main class

/**
 * Making a heatmap:
 *
 *      To add a heatmap to your map, you will need a dataset consisting of the coordinates for
 *      each location of interest
 *      First create a HeatmapTileProvider, passing it the collection of LatLng objects. Then
 *      create a new TileOverlay, passing it the heatmap tile provider, and add the tile overlay to
 *      the map.
 *
 *      HeatmapTileProvider accepts a collection of LatLng objects (or WeightedLatLng objects, as
 *      described below). It creates the tile images for various zoom levels, based on the radius,
 *      gradient and opacity options supplied. You can change the default values for these options.
 *
 *      STEPS:
 *      1) Use HeatmapTileProvider.Builder(), passing it a collection of LatLng objects, to add a
 *          new HeatmapTileProvider.
 *      2)Create a new TileOverlayOptions object with the relevant options, including the
 *          HeatmapTileProvider.
 *      3)Call GoogleMap.addTileOverlay() to add the overlay to the map.
 */

