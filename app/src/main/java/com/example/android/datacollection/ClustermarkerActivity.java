package com.example.android.datacollection;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioGroup;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
import com.example.android.datacollection.model.LocationItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

/**
 * Created by Khashayar on 3/5/2018.
 */

public class ClustermarkerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = this.getClass().getSimpleName();

    private GoogleMap mMap;

    //Constants for type of the map
    private final int MAPTYPE_CLUSTER = 1;
    private final int MAPTYPE_HEAT = 2;
    private int mMapType = MAPTYPE_CLUSTER;

    //ArrayList of all the locations
    ArrayList<LatLng> mLocationArrayList = new ArrayList<>();

    //Cursor object and IDs
    Cursor mCursor;
    int mIdColumnIndex, mLatColumnIndex, mLngColumnIndex;

    //Used for heatmaps
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    // Declare a variable for the cluster manager. (Note the type argument <LocationItem>, which declares
    // the ClusterManager to be of type LocationItem The class LocationItem)
    private ClusterManager<LocationItem> mClusterManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cluster_view);

        /**
         * The fragment is what actually contains the Google Maps and displays it.
         * Here we get the SupportMapFragment (from the layout file) and request notification
         * when the map is ready to be used. (onMapReady callback will get called when the map is
         * ready
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Query the database
        getLocation();

        //Radio Buttons
        RadioGroup optionsRadioGroup = findViewById(R.id.options_radiogroup);
        optionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                //checkedId is the ID of the new selection
                switch (checkedId){
                    case R.id.cluster_radiobutton:
                        mMapType = MAPTYPE_CLUSTER;
                        break;
                    case R.id.heat_radiobutton:
                        mMapType = MAPTYPE_HEAT;
                        break;
                }//switch
                addLocation();
            }
        });

    }//onCreate


    @Override
    public void onMapReady(GoogleMap map) {
        Log.v(TAG, "onMapReady callback triggered");
        mMap = map;

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

        addLocation();
    }//onMapReady

    private void getLocation() {
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
        mCursor = getContentResolver().query(
                LocationEntry.CONTENT_URI,     //The content Uri
                projection,               //The columns to return for each row
                null,            //Selection criteria
                null,         //Selection criteria
                null            //The sort order for returned rows
        );

        //get the column ID for each column
        mIdColumnIndex = mCursor.getColumnIndex(LocationEntry._ID);
        mLatColumnIndex = mCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE);
        mLngColumnIndex = mCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE);
    }//getLocation

    private void addLocation() {

        switch (mMapType) {
            case MAPTYPE_CLUSTER:
                if (mOverlay != null) {mOverlay.remove();}
                /**
                 * Initialize the ClusterManager (Context context, Google Maps maps); passing in this for
                 * this activity and out GoogleMaps object
                 */
                mClusterManager = new ClusterManager<LocationItem>(this, mMap);
                mMap.setOnCameraIdleListener(mClusterManager);
                mCursor.moveToFirst();
                while (mCursor.moveToNext()) {
                    mClusterManager.addItem(new LocationItem(
                            mCursor.getDouble(mLatColumnIndex), mCursor.getDouble(mLngColumnIndex),
                            "ID: " + mCursor.getString(mIdColumnIndex),
                            "Garbage"));
                }//while
                break;
            case MAPTYPE_HEAT:
                if (mClusterManager != null) {mMap.clear();}
                // Create a heat map tile provider, passing it the latlngs (using the helper method to
                //create an ArrayList of LatLng
                mProvider = new HeatmapTileProvider.Builder()
                        .data(makeArrayList())
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }//switch
    }//addLocation

    private ArrayList<LatLng> makeArrayList(){
        mCursor.moveToFirst();
        while (mCursor.moveToNext()) {
            LatLng latLng = new LatLng(mCursor.getDouble(mLatColumnIndex),
                    mCursor.getDouble(mLngColumnIndex));
            mLocationArrayList.add(latLng);
        }//while
        return mLocationArrayList;
    }//makeArrayList
}//main class
