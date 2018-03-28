package com.example.android.datacollection;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
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
 * This class is for entering data
 */

public class EnterLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Tag to be used for all our logs
    private final String TAG = "Data Entry";
    //Coordinator Layout (used for snacks)
    private CoordinatorLayout mCoordinatorLayout;
    //The TextView that displays the current location
    private GoogleApiClient mGoogleApiClient;
    //Date and time format and date instance
    final static DateFormat mDateFormat = new SimpleDateFormat("MM.dd.yyyy 'at' HH:mm:ss z");
    //Location data. They are initialized to 0.0 to check if there is no location acquired in the
    //helper methods throughout this Class
    private double mLat = 0.0;
    private double mLon = 0.0;
    //Checkboxes, and EditTexts
    CheckBox mGarbageCheckBox, mContainerCheckBox, mPaperCheckBox, mLocationCheckBox,
            mEstablishmentCheckBox, mCompostCheckBox;
    EditText mCommentText, mEstablishmentText;

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

        //creates variables for the checkboxes
        mGarbageCheckBox = findViewById(R.id.checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.checkbox_container);
        mPaperCheckBox = findViewById(R.id.checkbox_paper);
        mLocationCheckBox = findViewById(R.id.checkbox_location);
        mEstablishmentCheckBox = findViewById(R.id.checkbox_inside);
        mCommentText = findViewById(R.id.comment_text);
        mEstablishmentText = findViewById(R.id.inside_text);
        mCompostCheckBox = findViewById(R.id.checkbox_compost);


        //Buttons
        Button resetButton = findViewById(R.id.reset_button);
        Button submitButton = findViewById(R.id.submit_button);
        Button undoButton = findViewById(R.id.undo_button);
        Button mapButton = findViewById(R.id.map_button);

        /**
         * If the location is inside a establishment, and the user turns the checkbox off in edit
         * mode, they have to manually also delete the comment, since the app does not let them
         * save a location with establishment comment if the checkbox is not on, so the user need
         * to manually remove the comment. Here we set this event listener so when the user removes
         * the establishment, it automatically deletes the comment.
         *
         * here isChecked boolean object is the new state, and we want to act on the false, meaning
         * the user has turned it off
         */
        mEstablishmentCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mEstablishmentText.setText("");
                }
            }
        });//setOnCheckedChangeListener - establishment

        /**
         * If the user starts writing a comment in establishment, it automatically turns the check
         * box on, and vice versa.
         */
        mEstablishmentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /**
                 * Gets called when:
                 * within s, the count characters beginning at start are about to be replaced by new
                 * text with length after. It is an error to attempt to make changes to s
                 * from this callback.
                 */
            }//beforeTextChanged

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /**
                 * Notify that within s, the count characters beginning at start have just replaced
                 * old text that had length before. It is an error to attempt to make changes to s
                 * from this callback
                 */
                //Turn the checkbox on if they start writing
                if (s.length() > 0 && !mEstablishmentCheckBox.isChecked()) {
                    mEstablishmentCheckBox.setChecked(true);
                }
                //turn the checkbox off if they delete it
                if (s.length() < 1 && mEstablishmentCheckBox.isChecked()) {
                    mEstablishmentCheckBox.setChecked(false);
                }
            }//onTextChanged

            @Override
            public void afterTextChanged(Editable s) {
                /**
                 * Gets called when:
                 * somewhere within s, the text has been changed
                 */

            }//afterTextChanged
        });//mEstablishmentText text change listener

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
                if (mEstablishmentCheckBox.isChecked()) {
                    mEstablishmentCheckBox.setChecked(false);
                }
                if (mCompostCheckBox.isChecked()) {
                    mCompostCheckBox.setChecked(false);
                }
                if (mCommentText.getText().toString().trim().length() > 0) {
                    mCommentText.setText("");
                }
                if (mEstablishmentText.getText().toString().trim().length() > 0) {
                    mEstablishmentText.setText("");
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
                 */
                if (mLocationCheckBox.isChecked()) {
                    if ((mEstablishmentCheckBox.isChecked() &&
                            mEstablishmentText.getText().toString().trim().length() > 0) ||
                            (!mEstablishmentCheckBox.isChecked() &&
                                    mEstablishmentText.getText().toString().trim().length() < 1)) {

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
                        if (mLocationCheckBox.isChecked()) {
                            mLocationCheckBox.setChecked(false);
                        }
                        if (mCommentText.getText().toString().length() > 0) {
                            mCommentText.setText("");
                        }
                        if (mCommentText.getText().toString().trim().length() > 0) {
                            mCommentText.setText("");
                        }
                        if (mEstablishmentText.getText().toString().trim().length() > 0) {
                            mEstablishmentText.setText("");
                        }
                } else {
                        Toast.makeText(EnterLocationActivity.this, "If the establishment" +
                                " checkbox is ticked, there needs to be a comment; and vice versa",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EnterLocationActivity.this, "Location is not acquired yet, "
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

        //onClickListener for maps button
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMaps();
            }
        });//onClickListener - map

    } //OnCreate

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
        mLocationCheckBox = findViewById(R.id.checkbox_location);
        mEstablishmentCheckBox = findViewById(R.id.checkbox_inside);
        mCommentText = findViewById(R.id.comment_text);
        mEstablishmentText = findViewById(R.id.inside_text);

        String mComment = "";

        //Converting boolean from our checkboxes into integers for the database
        int mGarbage, mContainer, mPaper, mEstablishment;
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
        if (mEstablishmentCheckBox.isChecked()) {
            mEstablishment = 1;
        } else {
            mEstablishment = 0;
        }

        //Handling compost
        if (mCompostCheckBox.isChecked()){
            mComment = "compost";
        }

        //Get the text from comments edit text
        mComment += "\n" + mCommentText.getText().toString().trim();
        String mEstablishmentComment = mEstablishmentText.getText().toString().trim();

        //Current date and time using the format declared at the beginning
        final String mCurrentDateTime = mDateFormat.format(Calendar.getInstance().getTime());

        // Create a new map of values,
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_LATITUDE, mLat);
        values.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, mLon);
        values.put(LocationEntry.COLUMN_LOCATION_GARBAGE, mGarbage);
        values.put(LocationEntry.COLUMN_LOCATION_CONTAINER, mContainer);
        values.put(LocationEntry.COLUMN_LOCATION_PAPER, mPaper);
        values.put(LocationEntry.COLUMN_LOCATION_ESTABLISHMENT, mEstablishment);
        values.put(LocationEntry.COLUMN_LOCATION_COMMENT, mComment);
        values.put(LocationEntry.COLUMN_LOCATION_ESTABLISHMENT_COMMENT, mEstablishmentComment);
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
            //pop-up message
            mCoordinatorLayout = findViewById(R.id.coordinator_layout);
            Snackbar.make(mCoordinatorLayout,"Location added with ID: " + id,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }//insertLocation

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
        int result = getContentResolver().delete(uri, null , null);
        //If the result is zero, that means there was an error
        if (result == 0){
            Toast.makeText(this,
                    "Error with deleting the last entry", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Last entry has been deleted ", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }//undo

    //This method will send an intent and the phone will direct this Intent to the apps that can
    // handle maps. (The commented line will send the Intent explicitly to Google Maps.)
    private void goToMaps(){
        //We need to make sure that there is live location, otherwise maps will point to 0,0 in the
        //middle of nowhere. In this case we show a Toast message and exit the method.
        if (mLat == 0.0 && mLon == 0.0) {
            Toast.makeText(this, "Location is not aquired.", Toast.LENGTH_SHORT).show();
            return;
        }
        //Create the Uri first
        //By adding "?q= mLat, mLon" we put a pin in the location on the map
        Uri mapIntentUri = Uri.parse("geo:" + mLat + ", " + mLon + "?q=" + mLat + ", " + mLon );
        Intent intent =new Intent(Intent.ACTION_VIEW, mapIntentUri);
        // Make the Intent explicit by setting the Google Maps package, otherwise it will use the
        //any app that can handle maps.
//        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }//showOnGoogleMaps

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
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called");
        //LocationRequest object
        LocationRequest mLocationRequest;
        //Create a LocationRequest using create() method
        mLocationRequest = LocationRequest.create();
        //set the interval on the locationRequest object (times are in milli seconds).
        // This is how often it updates
        mLocationRequest.setInterval(1000);
        //set the priority
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d(TAG, "mLocationRequest: " + mLocationRequest);

        /**
         *  We could just simply put the line LocationServices.Fused.... here; but this way, we
         *  first make sure that we have permission, and then we can start requesting our location
         *  updates.
         */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.REQUEST_CODE);
        } else {
            //Start requesting location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    } //onConnected

    /**
     *     This is where we define what to be done when the location changes (based on the
     *     interval we set in onConnected. Here for example we update the mLat/mLon in the TextView
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

} //EnterLocationActivity Class
