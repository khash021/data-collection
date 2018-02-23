package com.example.android.datacollection;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.datacollection.Database.DataContract.LocationEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Khashayar on 2/17/2018.
 * This class is for entering the location data.
 */

public class DataEntry extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     *  Variables
     */

    //Tag to be used for all our logs
    private final String TAG = "Data Entry";
    //The TextView that displays the current location
    private GoogleApiClient mGoogleApiClient;
    //LocationRequest object
    private LocationRequest mLocationRequest;
    //Date and time (to be used for entering and modifying location data
    final DateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy 'at' HH:mm:ss z");
    final String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());
    //Location data
    private double mLat, mLon;
    //Checkboxes, buttons, and EditText. Since they are used by different methods
    CheckBox mGarbageCheckBox = findViewById(R.id.checkbox_garbage);
    CheckBox mContainerCheckBox = findViewById(R.id.checkbox_container);
    CheckBox mPaperCheckBox = findViewById(R.id.checkbox_paper);
    CheckBox mLocationCheckBox = findViewById(R.id.checkbox_location);
    EditText mCommentText = findViewById(R.id.comment_text);

    Button resetButton = findViewById(R.id.reset_button);
    Button submitButton = findViewById(R.id.submit_button);
    Button undoButton = findViewById(R.id.undo_button);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.data_entry);

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
                if (mLocationCheckBox.isChecked()) {
                    mLocationCheckBox.setChecked(false);
                }
                if (mCommentText.getText().toString().length() > 0) {
                    mCommentText.setText("");
                }
            }
        });//Reset - onClickListener

        //onClickListener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationCheckBox.isChecked()) {
                    insertData();
                    if (mGarbageCheckBox.isChecked()) {
                        mGarbageCheckBox.setChecked(false);
                    }
                    if (mContainerCheckBox.isChecked()) {
                        mContainerCheckBox.setChecked(false);
                    }
                    if (mPaperCheckBox.isChecked()) {
                        mPaperCheckBox.setChecked(false);
                    }
                    if (mLocationCheckBox.isChecked()) {
                        mLocationCheckBox.setChecked(false);
                    }
                    if (mCommentText.getText().toString().length() > 0) {
                        mCommentText.setText("");
                    }
                } else {
                    Toast.makeText(DataEntry.this, "Location is not acquired yet, "
                                    + "please wait for the location check box and try again",
                            Toast.LENGTH_SHORT).show();
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

    /**
     * Temporary helper method to display information in the onscreen TextView about the number of
     * Data points in our database
     */
    private void displayDatabaseInfo() {

        //this is what we are going to pass into the Query method. This String is similar
        //to the statement after SELECT, we tell it which columns we want, here we want everything
        String[] projection = {
                LocationEntry._ID,
        };

        Cursor cursor = getContentResolver().query(
                LocationEntry.CONTENT_URI,     //The content Uri
                projection,               //The columns to return for each row
                null,            //Selection criteria
                null,         //Selection criteria
                null            //The sort order for returned rows
        );

        TextView counterTextView = findViewById(R.id.data_counter);

        try {
            counterTextView.setText("Current number of data-points in database: " +
                    cursor.getCount());

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        } //finally
    }//displayDatabaseInfo

    /**
     * Get user input from editor and save new pet into database.
     * Here we convert our booleans into integer to be saved in the database
     *          True = 1  and   False = 0
     */
    private void insertData() {

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

        //Get the text from comments edit text
        String mComment = mCommentText.getText().toString().trim();

        // Create a new map of values, where column names are the keys
        //and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_LATITUDE, mLat);
        values.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, mLon);
        values.put(LocationEntry.COLUMN_LOCATION_GARBAGE, mGarbage);
        values.put(LocationEntry.COLUMN_LOCATION_CONTAINER, mContainer);
        values.put(LocationEntry.COLUMN_LOCATION_PAPER, mPaper);
        values.put(LocationEntry.COLUMN_LOCATION_COMMENT, mComment);

        // Insert a new pet into the provider, returning the content URI for the new pet.
        Uri newUri = getContentResolver().insert(LocationEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "Error with saving location",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, "Location saved",
                    Toast.LENGTH_SHORT).show();
        }
        displayDatabaseInfo();

    }//insertData

    //This deletes the last input
    public void undo(){
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

        //Here we need to check and see if there is at least one item in the database; otherwise
        //if we try to delete the last row which doesn't exist, the app crashes.
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
        int result = getContentResolver().delete(uri, null , null);
        //If the result is zero, that means there was an error
        if (result == 0){
            Toast.makeText(this,
                    "Error with deleting the last entry", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Last entry has been deleted ", Toast.LENGTH_SHORT).show();
            displayDatabaseInfo();
        }

    }//undo

    @Override
    protected void onStart() {
        Log.d(TAG, "Started");
        super.onStart();
        //Connect client
        mGoogleApiClient.connect();
        displayDatabaseInfo();
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
        displayDatabaseInfo();
        super.onResume();
    } //onResume

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called");

        //Create a LocationRequest using create() method
        mLocationRequest = LocationRequest.create();
        //set the interval on the locationRequest object (times are in milli seconds). This is how often it updates
        mLocationRequest.setInterval(1000);
        //set the priority
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d(TAG, "mLocationRequest: " + mLocationRequest);
        /**
         * We could just simply put the line LocationServices.Fused.... here; but this way we first
         * make sure that we have permission.
         * Here we check to see if we have permission and then we can start requesting our location
         * updates.
         * Add support for runtime permission check
         */

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.REQUEST_LOCATION);
        } else {
            //Start requesting location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    } //onConnected

    /**
     * This is where we define what to be done when the location changes (based on the interval we set
     in onConnected. Here for example we update the mLat/log in the TextView
     */
    @Override
    public void onLocationChanged(Location location) {
        CheckBox locationCheckBox = findViewById(R.id.checkbox_location);
        Log.d(TAG, "onLocationChanged() called");
        Log.i(TAG, "Location: " + location.toString());
        locationCheckBox.setChecked(true);
        mLat = location.getLatitude();
        mLon = location.getLongitude();

        //Text box to show location
        TextView locationOutput = findViewById(R.id.txt_location);
        locationOutput.setText("Latitude: "+Double.toString(location.getLatitude())+
                "\nLongitude: "+Double.toString(location.getLongitude()));
    } //onLocationChanged

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "GoogleApiClient connection suspended");
    } //onConnectionSuspended

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed");
    } //onConnectionFailed

} //DataEntry Class
