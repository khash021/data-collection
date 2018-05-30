package com.example.android.datacollection;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datacollection.model.MyLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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

//TODO: There might be something wrong with the setting of images in the info window, because the methods to turn off and on seem to work fine but in the NE corner there is some disprepency

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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

            //set the title (for development phase)
            ((TextView) view.findViewById(R.id.title_textView)).setText(marker.getTitle());

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
                //Make the view visible
                ((TextView) view.findViewById(R.id.comment_textView)).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.comment_textView)).setText(comment);
        }//render
    }//CustomInfoWindowAdapter


    //Google Maps object
    GoogleMap mMap;

    private CheckBox mGarbageCheckbox, mContainerCheckbox, mPaperCheckbox;

    //ArrayList of all the markers
    private ArrayList<Marker> mMarkerArrayList = new ArrayList<>();

    //ArrayList to be used for locations
    private ArrayList<MyLocation> myLocationArrayList = new ArrayList<>();

    //Google Api Client
    protected GoogleApiClient mGoogleApiClient;

    //location object and corresponding LatLng object
    private Location mMyLocation;
    private LatLng mMyLatLng;

    //Vancouver dt bounds
    private LatLngBounds VANCOUVER_DT_BOUND = new LatLngBounds(new LatLng( 49.268642, -123.148639),
            new LatLng( 49.300045, -123.095893));
    //Vancouver bound
    private LatLngBounds VANCOUVER_BOUND = new LatLngBounds(
            new LatLng(49.033396, -123.302132),
            new LatLng(49.363144, -122.452284));

    //Vancouver bound
    private LatLngBounds SQUAMISH_VANCOUVER_BOUND = new LatLngBounds(
            new LatLng( 49.038178, -123.378854),
            new LatLng( 49.886601, -122.440345));

    //UBC Bound
    private LatLngBounds UBC_BOUND = new LatLngBounds(new LatLng(  49.239488, -123.270513),
            new LatLng(  49.281679, -123.20184));
    //Our default downtoan start location
    LatLng DOWNTOWN_CENTER =  new LatLng( 49.282733, -123.120732);
    //Our default UBC start location
    LatLng UBC_CENTER =  new LatLng(  49.262233, -123.249875);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_view_activity);

        //build Google Api Client
        buildGoogleApiClient();

        /**
         * The fragment is what actually contains the Google Maps and displays it.
         * Here we get the SupportMapFragment (from the layout file) and request notification
         * when the map is ready to be used. (onMapReady callback will get called when the map is
         * ready
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize the activity with all check boxes set, showing all the markers
        mGarbageCheckbox = findViewById(R.id.garbage_checkbox);
        mGarbageCheckbox.setChecked(true);
        mContainerCheckbox = findViewById(R.id.container_checkbox);
        mContainerCheckbox.setChecked(true);
        mPaperCheckbox = findViewById(R.id.paper_checkbox);
        mPaperCheckbox.setChecked(true);

        //Change listener for Garbage checkbox
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
                }//if
            }
        });//Garbage Checkbox listener

        //Change listener fot Container checkbox
        mContainerCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean result = containerFilter(isChecked);
                if (!result) {
                    Toast.makeText(getApplicationContext(), "Map is not ready",
                            Toast.LENGTH_SHORT).show();
                    //since nothing was done reverse the check box to its original format
                    mContainerCheckbox.setChecked(!isChecked);
                }//if
            }
        });//Container Checkbox listener

        //Change listener fot Paper checkbox
        mPaperCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean result = paperFilter(isChecked);
                if (!result) {
                    Toast.makeText(getApplicationContext(), "Map is not ready",
                            Toast.LENGTH_SHORT).show();
                    //since nothing was done reverse the check box to its original format
                    mContainerCheckbox.setChecked(!isChecked);
                }//if
            }
        });//Paper Checkbox listener

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
        mMap.setLatLngBoundsForCameraTarget(SQUAMISH_VANCOUVER_BOUND);

        //Enable my location layer
        mMap.setMyLocationEnabled(true);

        //Since the pan is limited, we should also VANCOUVER_DT_BOUND min zoom, other wise they can zoom all the
        //way out and the bounds would be useless
        mMap.setMinZoomPreference(8.0f);

        //Use the helper method to add markers to the map retrieved from the database
        populateMap();

        /**
         * If mMyLocation is not null (we have our location), it checks to see if it falls within
         * our VANCOUVER_DT_BOUND bounds (i.e. downtown Vancouver). If it is, it initializes map at that
         * location, otherwise use the center of downtown
         */


        //Initialize the map in center of downtown/UBC
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UBC_CENTER, 15.0f));

        // Setting the our custom info window, passing out helper method
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        //Set onClickListeners
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //check to see if the user's location is within bounds
                if (VANCOUVER_BOUND.contains(mMyLatLng)) {
                    //do nothing default behavior will move the camera to user's lcoation
                    return false;
                } else {
                    //Show a toast and return true, meaning the default zooming of camera is not
                    //executed since the user is outside the limit
                    Toast.makeText(getApplicationContext(),
                            "You are currently outside of supported area", Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            }
        });//OnMyLocationButtonClickListener

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

        //This means the new state is true (checked)
        //For this option, we need to see if the location also contains container and/or paper. In
        //that case, we should check the state of their corresponding checkboxes and leave the
        //marker visible in that case or else it will create bugs (like the one on Granville)
        if (b) {
            //this is the case that the check box is turned on. Make markers with garbgae tag visible
            for (Marker marker: mMarkerArrayList) {

                Object tagObject = marker.getTag();
                boolean[] tagArray = (boolean[]) tagObject;
                boolean garbage =tagArray[0];
                if (garbage) {
                    marker.setVisible(true);
                }
            }//for
        } else {
            //This is for when they turned it off, make all the ones with garbage tag invisible
            //Figure out if any other check box is on
            if (!mContainerCheckbox.isChecked() && !mPaperCheckbox.isChecked()) {
                for (Marker marker: mMarkerArrayList) {
                    Object tagObject = marker.getTag();
                    boolean[] tagArray = (boolean[]) tagObject;
                    boolean garbage = tagArray[0];
                    if (garbage) {
                        marker.setVisible(false);
                    }//if
                }//for
            } else {
                for (Marker marker: mMarkerArrayList) {
                    Object tagObject = marker.getTag();
                    boolean[] tagArray = (boolean[]) tagObject;
                    boolean garbage = tagArray[0];
                    boolean container = tagArray[1];
                    boolean paper = tagArray[2];
                    //Here we need to figure out the other attributes
                    if (garbage && !((container && mContainerCheckbox.isChecked()) || (paper && mPaperCheckbox.isChecked())) ) {
                        marker.setVisible(false);
                    }//if
                }//for
            }
        }
        return true;
    }//garbageFilter

    /**
     *   Helper method for container checkbox
     *   return true if the map was ready and the operation was successful
     *   false if the map was not ready and nothing happened so that the chekbox can be turned into
     *   original state.
     */
    private boolean containerFilter(boolean b){
        //check to see if map is ready
        if (!checkMapReady()) {
            return false;
        }//if Map Not ready

        //This means the new state is true (checked)
        if (b) {
            //this is the case that the check box is turned on. Make markers with container tag visible
            for (Marker marker: mMarkerArrayList) {
                Object tagObject = marker.getTag();
                boolean[] tagArray = (boolean[]) tagObject;
                boolean container = tagArray[1];
                if (container) {
                    marker.setVisible(true);
                }
            }//for
        } else {
            //This is for when they turned it off, make all the ones with garbage tag invisible
            //Figure out if any other check box is on
            if (!mGarbageCheckbox.isChecked() && !mPaperCheckbox.isChecked()) {
                for (Marker marker : mMarkerArrayList) {
                    Object tagObject = marker.getTag();
                    boolean[] tagArray = (boolean[]) tagObject;
                    boolean container = tagArray[1];
                    if (container) {
                        marker.setVisible(false);
                    }//if
                }//for
            } else {
                for (Marker marker : mMarkerArrayList) {
                    Object tagObject = marker.getTag();
                    boolean[] tagArray = (boolean[]) tagObject;
                    boolean garbage = tagArray[0];
                    boolean container = tagArray[1];
                    boolean paper = tagArray[2];
                    //Here we need to figure out the other attributes
                    if (container && !((garbage && mGarbageCheckbox.isChecked()) || (paper && mPaperCheckbox.isChecked()))) {
                        marker.setVisible(false);
                    }//if
                }//for
            }
        }
        return true;
    }//containerFilter

    /**
     *   Helper method for paper checkbox
     *   return true if the map was ready and the operation was successful
     *   false if the map was not ready and nothing happened so that the chekbox can be turned into
     *   original state.
     */
    private boolean paperFilter(boolean b){
        //check to see if map is ready
        if (!checkMapReady()) {
            return false;
        }//if Map Not ready

        //This means the new state is true (checked)
        if (b) {
            //this is the case that the check box is turned on. Make markers with container tag visible
            for (Marker marker: mMarkerArrayList) {
                Object tagObject = marker.getTag();
                boolean[] tagArray = (boolean[]) tagObject;
                boolean paper = tagArray[2];
                if (paper) {
                    marker.setVisible(true);
                }
            }//for
        } else {
            //This is for when they turned it off, make all the ones with garbage tag invisible
            //Figure out if any other check box is on
            if (!mGarbageCheckbox.isChecked() && !mContainerCheckbox.isChecked()) {
                for (Marker marker : mMarkerArrayList) {
                    Object tagObject = marker.getTag();
                    boolean[] tagArray = (boolean[]) tagObject;
                    boolean paper = tagArray[2];
                    if (paper) {
                        marker.setVisible(false);
                    }//if
                }//for
            } else {
                for (Marker marker : mMarkerArrayList) {
                    Object tagObject = marker.getTag();
                    boolean[] tagArray = (boolean[]) tagObject;
                    boolean garbage = tagArray[0];
                    boolean container = tagArray[1];
                    boolean paper = tagArray[2];
                    //Here we need to figure out the other attributes
                    if (paper && !((garbage && mGarbageCheckbox.isChecked()) || (container && mContainerCheckbox.isChecked()))) {
                        marker.setVisible(false);
                    }//if
                }//for
            }
        }
        return true;
    }//paperFilter

    /**
     * Helper method for initializing the Google Api Client.
     * This does not request location updates, and only gets our current location once to be used
     * for initializing the map
     */
    protected synchronized void buildGoogleApiClient() {
        Log.v(TAG, "buildGoogleApiClient called");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }//buildGoogleApiClient

    /**
     * Adding this is crucial for obtaining the location. This is when the Google Api client gets
     * connewcted. Without it, the Google Api Client gets built, but not connected
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }//onStart

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }//onStop

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }//onMarkerClick

    /**
     * This gets called when the Google Api Client is connected and the location is then acquired.
     * Since this gets connected slightly after the onMapReady gets called; we need to check the
     * user's location here, and if they are within the bounds, update the camera here; if not just
     * show a Toast message
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "Google Api Client onConnected");
        //Check to see if there is location available and assign it to the mMyLocation object
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mMyLocation = mLastLocation;
            //Make a LatLng object from myLocation data
            mMyLatLng = new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude());
            //Check to see if the user's location is within bounds
            if (VANCOUVER_BOUND.contains(mMyLatLng)) {
                //Move the camera to user's location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLatLng, 16.0f));
            } else {
                //If the user is outside the bounds, show a Toast message
                Toast.makeText(this, "You are currently outside of supported area",
                        Toast.LENGTH_SHORT).show();
            }//if
        }//if
    }//onConnected

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended, trying to reconnect");
        mGoogleApiClient.connect();
    }//onConnectionSuspended

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());
    }//onConnectionFailed

}//MapViewActivity class


