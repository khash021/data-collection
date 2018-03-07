package com.example.android.datacollection;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
import com.example.android.datacollection.model.MyLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by Khashayar on 3/1/2018.
 *
 * This is the class that shows the data on Google Maps (in the app)
 */

public class LocationMapsViewActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private String TAG =this.getClass().getSimpleName();

    /**
     * This class is for customizing Info Windows
     *
     */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // Viewgroup containing 3 ImageViews with ids "garbage, container, and paper"
        private final View mContent;

        CustomInfoWindowAdapter() {
            mContent = getLayoutInflater().inflate(R.layout.custom_info_content, null);
        }//CustomInfoWindowAdapter

        /**
         * These two methods are part of the InfoWindowAdapter interface that we have to implement
         * It first calls getInfoWindow, and if this turns nill, then it goes to getInfoContent;
         * if that one also returns null, then the default behavior will happen; which is the default
         * Info Window.
         *
         * @param marker is the marker that was clicked on
         * @return view
         */
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }//getInfoWindow

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContent);
            return mContent;
        }//getInfoContents

        /**
         * This is the helper method for making the custom Info Window.
         *
         * In the layout file (custom_info_content), all images are set, but their visibility is
         * set to GONE. Here we turn them back on if the marker has those attributes
         *
         * @param marker is the marker that was clicked on, passed using the callbacks above
         *
         */
        private void render(Marker marker, View view) {
            //get the data from marker
            int garbageImage, containerImage, paperImage;
            boolean garbage, container, paper;
            Object tag = marker.getTag();
            boolean[] markerTag = (boolean[]) tag;
            garbage = markerTag[0];
            container = markerTag[1];
            paper = markerTag[2];

            //Make the images visible based on the properties of this location
            if (garbage) {
                ((ImageView) view.findViewById(R.id.garbage_imageView)).
                        setVisibility(View.VISIBLE);
            }
            if (container)
                ((ImageView) view.findViewById(R.id.container_imageView)).
                        setVisibility(View.VISIBLE);
            if (paper) {
                ((ImageView) view.findViewById(R.id.paper_imageView)).
                        setVisibility(View.VISIBLE);
            }

            //Add comment
            String comment = marker.getSnippet().trim();
            if (comment.length() > 1)
                ((TextView) view.findViewById(R.id.comment_textView)).setText(comment);
        }//render
    }//CustomInfoWindowAdapter


    //Google Maps object
    GoogleMap mMap;

    CheckBox mGarbageCheckbox, mContainerCheckbox, mPaperCheckbox;

    //ArrayList of all the markers
    ArrayList<Marker> mMarkerArrayList = new ArrayList<>();

    //ArrayList to be used for locations
    private ArrayList<MyLocation> myLocationArrayList = new ArrayList<>();

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

        //initialize the activity with Garbage check box set, showing all the markers
        mGarbageCheckbox = findViewById(R.id.garbage_checkbox);
        mGarbageCheckbox.setChecked(true);
        mGarbageCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isChecked is the new state
                boolean result = garbageFilter(isChecked);
                if (!result) {
                    Toast.makeText(getApplicationContext(), "Map is not ready",
                            Toast.LENGTH_SHORT).show();
                    //since nothing was done reverse the check box to its original format
                    mGarbageCheckbox.setChecked(!isChecked);
                }
            }
        });//setOnCheckedChangeListener

        //Get the ArrayList
        myLocationArrayList = MainActivity.getGlobalLocationArrayList();
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

        //restricting users panning to Vancouver dt area. First input is the SW corner, and the
        // second NE corner of the restricted pan area
        LatLngBounds limit = new LatLngBounds(new LatLng( 49.268642, -123.148639),
                new LatLng( 49.300045, -123.095893));
        //Add limit to our map
        mMap.setLatLngBoundsForCameraTarget(limit);

        //Enable my location layer
        mMap.setMyLocationEnabled(true);

        //Since the pan is limited, we should also limit min zoom, other wise they can zoom all the
        //way out and the bounds would be useless
        mMap.setMinZoomPreference(13.0f);

        //Use the helper method to add markers to the map retrieved from the database
        populateMap();

        //Set the initial camera to the center of downtown (where it says Vancouver)
        LatLng initialLocation =  new LatLng( 49.282733f, -123.120732f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 13.0f));

        // Setting the our custom info window, passing out helper method
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        //Set onClickListener
        mMap.setOnMarkerClickListener(this);

        //Log the number of markers
        Log.v(TAG, "Total number of markers added to the map: " + mMarkerArrayList.size());
    }//onMapReady

    /**
     * This is the helper method that gets all the location data from the database, and creates
     * a marker object for each location (i.e. each row), and places them in an ArrayList
     * @return ArrayList<Marker>
     */
    //TODO: we need add the feature to automatically update the ArrayList as the database updates
    private void populateMap(){
        /**
         * First we need to query the database and get all the data (mCursor object).
         */


        /**
         * This part uses the loop to go through each MyLocation object in the ArrayList, extract
         * all the data, and set the markers
         */
        //TODO: set the marker colors, based on what is available at that spot

        //create a marker object
        Marker mMaker;
        //This goes through the ArrayList for every MyLocation object and sets up the marker
        for (MyLocation myLocation: myLocationArrayList) {
            //get the boolean values
            boolean[] booleanValues = {myLocation.garbage, myLocation.container, myLocation.paper,
                myLocation.establishment};
            //Make a new MarkerOptions object to add the data
            mMaker = mMap.addMarker(new MarkerOptions()
                    .position(myLocation.latLng)
                    .title("Location: " + myLocation._id));
            //Add our int array as an object to our marker
            mMaker.setTag(booleanValues);
            //Add the comment if not null
            if (myLocation.comment != null) {
                mMaker.setSnippet(myLocation.comment);
            }//if
            //Add the marker to the ArrayList
            mMarkerArrayList.add(mMaker);
        }//for
    }//populateMap

    /**
     * Helper method for checking if the map is ready, used by other methods before performing
     * their tasks
     * @return tru if the map is ready, false otherwise
     */
    private boolean checkMapReady() {
        if (mMap == null) {
            Toast.makeText(this, "Map not ready", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }//checkMapReady

    /**
     *   Helper method for garbage checkbox
     *   return true if the map was ready and the operation was successful
     *   false if the map was not ready and nothing happened so that the chekbox can be turned into
     *   original state.
     */
    private boolean garbageFilter(Boolean b){
        //check to see if map is ready
        if (!checkMapReady()) {
            return false;
        }//if Map Not ready
        if (b) {
            //this is the case that the check box is turned on. Make markers with garbgae tag visible
            for (Marker marker: mMarkerArrayList) {
                Object tagObject = marker.getTag();
                boolean[] tagArray = (boolean[]) tagObject;
                if (tagArray[1]) {
                    marker.setVisible(true);
                }
            }//for
        } else {
            //This is for when they turned it off, make all the ones with garbage tag invisible
            for (Marker marker: mMarkerArrayList) {
                Object tagObject = marker.getTag();
                boolean[] tagArray = (boolean[]) tagObject;
                if (tagArray[1]) {
                    marker.setVisible(false);
                }
            }//for
        }
        return true;
    }//garbageFilter

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }//onMarkerClick


}//LocationMapsViewActivity class


