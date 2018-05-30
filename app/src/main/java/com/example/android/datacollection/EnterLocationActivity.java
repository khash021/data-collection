package com.example.android.datacollection;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by Khashayar on 2/17/2018.
 * This class is for entering data
 */

public class EnterLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener {

    //Tag to be used for all our logs
    private final String TAG = "Data Entry";
    //The TextView that displays the current location
    private GoogleApiClient mGoogleApiClient;
    //Date and time format and date instance
    final static DateFormat mDateFormat = new SimpleDateFormat("MM.dd.yyyy 'at' HH:mm:ss z");
    //Location data. They are initialized to 0.0 to check if there is no location acquired in the
    //helper methods throughout this Class
    private Location mLocation;
    private LatLng mLatLng;
    //Checkboxes, and EditTexts
    CheckBox mGarbageCheckBox, mContainerCheckBox, mPaperCheckBox, mCompostCheckBox;
    EditText mCommentText;
    GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.location_entry);

        //Building a GoogleApiClient on
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //This tells it we want to use the LocationServices API
                .addApi(LocationServices.API)
                //Means we want the ConnectionCallbacks on the GoogleApiClient client to come to "this" class
                .addConnectionCallbacks(this)
                //Same as above, send the callbacks to this activity. this could be replaced by any other activity
                .addOnConnectionFailedListener(this)
                //Finally build the ApiClient
                .build();

        //Add map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //creates variables for the checkboxes
        mGarbageCheckBox = findViewById(R.id.checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.checkbox_container);
        mPaperCheckBox = findViewById(R.id.checkbox_paper);
        mCommentText = findViewById(R.id.comment_text);
        mCompostCheckBox = findViewById(R.id.checkbox_compost);


        //Buttons
        Button resetButton = findViewById(R.id.reset_button);
        Button submitButton = findViewById(R.id.submit_button);
        Button undoButton = findViewById(R.id.undo_button);


        //onClick listener for reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //clears all the checkboxes
                if (mGarbageCheckBox.isChecked()) {
                    mGarbageCheckBox.setChecked(false);
                }
                if (mContainerCheckBox.isChecked()) {
                    mContainerCheckBox.setChecked(false);
                }
                if (mPaperCheckBox.isChecked()) {
                    mPaperCheckBox.setChecked(false);
                }
                if (mCompostCheckBox.isChecked()) {
                    mCompostCheckBox.setChecked(false);
                }
                if (mCommentText.getText().toString().trim().length() > 0) {
                    mCommentText.setText("");
                }
            }
        });//Reset - onClickListener

        //onClickListener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * First we will check to see if the location is acquired, if not, show a Toast
                 * telling the user to wait for the location.
                 * We also need to check to see if the establishment checkbox is true, if so
                 * we require details. if the establishment text is empty, we show a toast message
                 * telling the user they need to enter comments regarding the establishment
                 * We also need to make sure at least one of the checkboxes is checked
                 */
                if (!mGarbageCheckBox.isChecked() && !mContainerCheckBox.isChecked() &&
                        !mPaperCheckBox.isChecked() && !mCompostCheckBox.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Please select at least on detail",
                            Toast.LENGTH_SHORT).show();
                } else {
                    insertLocation();
                    //Reset all the checkboxes and Edit Texts
                    if (mGarbageCheckBox.isChecked()) {
                        mGarbageCheckBox.setChecked(false);
                    }
                    if (mContainerCheckBox.isChecked()) {
                        mContainerCheckBox.setChecked(false);
                    }
                    if (mPaperCheckBox.isChecked()) {
                        mPaperCheckBox.setChecked(false);
                    }
                    if (mCompostCheckBox.isChecked()) {
                        mCompostCheckBox.setChecked(false);
                    }
                    if (mCommentText.getText().toString().length() > 0) {
                        mCommentText.setText("");
                    }
                    if (mCommentText.getText().toString().trim().length() > 0) {
                        mCommentText.setText("");
                    }
                }
            }
        });//onClickListener - submit

        //Undo onClickListener
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();

            }
        });//onClickListener - undo
    } //OnCreate

    @Override
    protected void onStart() {
        Log.d(TAG, "Started");
        super.onStart();
        //Connect client
        mGoogleApiClient.connect();
    } //onStart

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        //Disconnect the client
        mGoogleApiClient.disconnect();
        super.onStop();
    } //onStop

    @Override
    protected void onPause() {
        Log.d(TAG, "Paused");
        //Disconnect the client on pause
        mGoogleApiClient.disconnect();
        super.onPause();
    } //onPause

    @Override
    protected void onResume() {
        Log.d(TAG, "Resumed");
        //Re-connect the client on resume
        mGoogleApiClient.connect();
        super.onResume();
    } //onResume

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraIdleListener(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
    }//onMapReady


    @Override
    public void onCameraIdle() {

        mLatLng = mMap.getCameraPosition().target;

    }//onCameraIdle

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called");

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 16.0f));

    } //onConnected


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    } //onConnectionSuspended

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed");
    } //onConnectionFailed

    /**
     * Get user input from editor and save new location into database.
     * The boolean values are converted to integer to be stored in the databse. Follows the
     * convention: True = 1 ; False = 0
     */
    private void insertLocation() {

        //Setting up the Checkboxes variables
        mGarbageCheckBox = findViewById(R.id.checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.checkbox_container);
        mPaperCheckBox = findViewById(R.id.checkbox_paper);
        mCompostCheckBox = findViewById(R.id.checkbox_compost);
        mCommentText = findViewById(R.id.comment_text);

        String mComment = "";

        //Converting boolean from our checkboxes into integers for the database
        int mGarbage, mContainer, mPaper;
        if (mGarbageCheckBox.isChecked()) {
            mGarbage = 1;
        } else {
            mGarbage = 0;
        }
        if (mContainerCheckBox.isChecked()) {
            mContainer = 1;
        } else {
            mContainer = 0;
        }
        if (mPaperCheckBox.isChecked()) {
            mPaper = 1;
        } else {
            mPaper = 0;
        }

        //Handling compost
        if (mCompostCheckBox.isChecked()) {
            mComment = "compost";
        }

        //Get the text from comments edit text
        mComment += "\n" + mCommentText.getText().toString().trim();

        //Current date and time using the format declared at the beginning
        final String mCurrentDateTime = mDateFormat.format(Calendar.getInstance().getTime());

        double lat = mLatLng.latitude;
        double lng = mLatLng.longitude;

        // Create a new map of values,
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_LATITUDE, lat);
        values.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, lng);
        values.put(LocationEntry.COLUMN_LOCATION_GARBAGE, mGarbage);
        values.put(LocationEntry.COLUMN_LOCATION_CONTAINER, mContainer);
        values.put(LocationEntry.COLUMN_LOCATION_PAPER, mPaper);
        values.put(LocationEntry.COLUMN_LOCATION_COMMENT, mComment);
        values.put(LocationEntry.COLUMN_LOCATION_DATE, mCurrentDateTime);

        // Insert a new location into the provider, returning the content URI for the new location.
        Uri newUri = getContentResolver().insert(LocationEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "Error with saving location",
                    Toast.LENGTH_SHORT).show();
        } else {
            //since the insert method return the Uri of the row created, we can extract the ID of
            //the new row using the parseID method with our newUri as an input. This method gets the
            //last segment of the Uri, which is our new ID in this case and we store it in an object
            // And add it to the confirmation method.
            String id = String.valueOf(ContentUris.parseId(newUri));
            // Otherwise, the insertion was successful and we can display a snackbar
//            Toast.makeText(this, "Added ID: " + id, Toast.LENGTH_SHORT).show();
            CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator_layout);
            Snackbar.make(coordinatorLayout, "Adde with ID: " + id, Snackbar.LENGTH_SHORT).show();
        }
    }//insertLocation

    //This deletes the last input
    public void undo() {
        String[] projection = {
                LocationEntry._ID
        };

        //Query the database with ID as projection. We will use this to find out the ID
        //of the last item in the database to be used for deleting
        Cursor cursor = getContentResolver().query(
                LocationEntry.CONTENT_URI,     //The content Uri
                projection,               //The columns to return for each row
                null,            //Selection criteria
                null,         //Selection criteria
                null            //The sort order for returned rows
        );

        /**
         * Here we need to check and see if there is at least one item in the database;
         * otherwise if we try to delete the last row which doesnt exist, the app crashes.
         */
        if (cursor.getCount() <= 0) {
            Toast.makeText(this, "Database is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        //get the index of the ID column
        int columnID = cursor.getColumnIndex(LocationEntry._ID);
        //Move the cursor to the last row
        cursor.moveToLast();
        //get the ID of the last item
        int lastRow = cursor.getInt(columnID);
        //convert to double to be Appended in Uri
        long id = lastRow;

        //Creating a correct URI pointing to the last row
        Uri uri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, id);

        //delete will return an integer with the number of rows deleted
        int result = getContentResolver().delete(uri, null, null);
        //If the result is zero, that means there was an error
        if (result == 0) {
            Toast.makeText(this,
                    "Error with deleting the last entry", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Last entry has been deleted ", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }//undo

} //EnterLocationActivity Class
