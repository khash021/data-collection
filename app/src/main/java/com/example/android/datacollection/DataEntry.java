package com.example.android.datacollection;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
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

import com.example.android.datacollection.Database.DataContract;
import com.example.android.datacollection.Database.DataDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Khashayar on 2/17/2018.
 * This class is for entering data
 */

public class DataEntry extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Variables
    //Tag to be used for all our logs
    private final String TAG = "Data Entry";

    private static final int REQUEST_LOCATION = 0;
    //The TextView that displays the current location
    private GoogleApiClient mGoogleApiClient;
    //LocationRequest object
    private LocationRequest mLocationRequest;
    private TextView locationOutput;
    //ArrayList for holding the data
    ArrayList<Data> dataArray = new ArrayList<>();
    //Data object
    Data data = new Data();
    //Location data
    double lat, lon;
    //Date and time format and date instance
    final DateFormat dateFormat = new SimpleDateFormat("EEE, MMM.dd.yyyy 'at' HH:mm");
    final String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());


    CheckBox mGarbageCheckBox, mContainerCheckBox, mPaperCheckBox, mLocationCheckBox;
    EditText mCommentText;


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

        //Text box to show location
        locationOutput = findViewById(R.id.txt_location);

        //creates variables for the checkboxes
        mGarbageCheckBox = findViewById(R.id.checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.checkbox_container);
        mPaperCheckBox = findViewById(R.id.checkbox_paper);
        mLocationCheckBox = findViewById(R.id.checkbox_location);
        mCommentText = findViewById(R.id.comment_text);

        //Buttons
        Button resetButton = findViewById(R.id.reset_button);
        Button submitButton = findViewById(R.id.submit_button);


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
        });



        //onClickListener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationCheckBox.isChecked()) {
//                    data = new Data();
//                    data.garbage = garbageCheckBox.isChecked();
//                    data.container = containerCheckBox.isChecked();
//                    data.container = containerCheckBox.isChecked();
//                    data.lat = lat;
//                    data.lon = lon;
//                    data.message = commentText.getText().toString();
//                    dataArray.add(data);
                    insertData();
                } else {
                    Toast.makeText(DataEntry.this, "Location is not acquired yet, "
                                    + "please wait for the location check box and try again",
                            Toast.LENGTH_SHORT).show();
                }

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
        });


    } //OnCreate

    //This is the popup notification when you first run the app and you give permission for this app
    //to use the phone's location (Android's default pop-up.
    // Request Permissions
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                Log.i(TAG, "Permission was denied or request was cancelled");
            }
        }
    } //onRequestPermissionResult

    /**
     * Temporary helper method to display information in the onscreen TextView about the number of
     * Data points in our database
     */
    private void displayDatabaseInfo() {


        // Create database helper
        DataDbHelper mDbHelper = new DataDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //this is what we are going to pass into the Query method. This String is similiar
        //to the statement after SELECT, we tell it which columns we want, here we want everything
        String[] projection = {DataContract.DataEntry._ID};

        //Now we need to create a Cursor object (Cursor contains row and columns from the database
        //based on the input arguments we have given it
        Cursor cursor = db.query(
                DataContract.DataEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        TextView counterTextView = findViewById(R.id.data_counter);

        try {
            counterTextView.setText("Current number of data: " + cursor.getCount());

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        } //finally


    }//displayDatabaseInfo

    /**
     * Get user input from editor and save new pet into database.
     */
    private void insertData() {

        //Setting up the Checkboxes variables
        mGarbageCheckBox = findViewById(R.id.checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.checkbox_container);
        mPaperCheckBox = findViewById(R.id.checkbox_paper);
        mLocationCheckBox = findViewById(R.id.checkbox_location);
        mCommentText = findViewById(R.id.comment_text);

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

        // Create database helper
        DataDbHelper mDbHelper = new DataDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        //and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(DataContract.DataEntry.COLUMN_LOCATION_LATITUDE, lat);
        values.put(DataContract.DataEntry.COLUMN_LOCATION_LONGITUDE, lon);
        values.put(DataContract.DataEntry.COLUMN_LOCATION_GARBAGE, mGarbage);
        values.put(DataContract.DataEntry.COLUMN_LOCATION_CONTAINER, mContainer);
        values.put(DataContract.DataEntry.COLUMN_LOCATION_PAPER, mPaper);
        values.put(DataContract.DataEntry.COLUMN_LOCATION_COMMENT, mComment);

        // we add the new pet to the database by insert method and this return a double which is the
        //id of that row.
        long newRowId = db.insert(DataContract.DataEntry.TABLE_NAME, null, values);

        //make a toast message showing the id of the added
        if (newRowId == -1) {
            //insert return -1 if there was an error and this means the pet was NOT added
            Toast.makeText(this, "Error with saving location.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location saved with id: " + newRowId, Toast.LENGTH_SHORT )
                    .show();
        }

        displayDatabaseInfo();

    }//insertData

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

//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        //We could just simply put the line LocationServices.Fused.... here; but this way we first
        //make sure that we have permission
        // Here we check to see if we have permission and then we can start requesting our location updates
        // Add support for runtime permission check
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            //Start requesting location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    } //onConnected

    //This is where we define what to be done when the location changes (based on the interval we set
    //in onConnected. Here for example we update the lat/log in the TextView
    @Override
    public void onLocationChanged(Location location) {
        CheckBox locationCheckBox = findViewById(R.id.checkbox_location);
        Log.d(TAG, "onLocationChanged() called");
        Log.i(TAG, "Location: " + location.toString());
        locationCheckBox.setChecked(true);
        lat = location.getLatitude();
        lon = location.getLongitude();
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu options from the res/menu/menu_editor.xml file.
//        // This adds menu items to the app bar.
//        getMenuInflater().inflate(R.menu.menu_data_entry, menu);
//        return true;
//    } //onCreateOptionsMenu


} //Main Activity
