package com.example.android.datacollection;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Variables
    private static final int REQUEST_LOCATION = 0;
    //Tag to be used for all our logs
    private final String TAG = "Location Test";
    //The TextView that displays the current location
    private GoogleApiClient mGoogleApiClient;
    //LocationRequest object
    private LocationRequest mLocationRequest;
    private TextView locationOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);

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
        final CheckBox garbageCheckBox = findViewById(R.id.checkbox_garbage);
        final CheckBox containerCheckBox = findViewById(R.id.checkbox_container);
        final CheckBox paperCheckBox = findViewById(R.id.checkbox_paper);
        final CheckBox locationCheckBox = findViewById(R.id.checkbox_location);

        //reset button
        Button resetButton = findViewById(R.id.reset_button);

        //submit button
        Button submitButton = findViewById(R.id.submit_button);

        //location button
        Button locationButton = findViewById(R.id.location_button);

        //set on click listener for reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //clears all the checkboxes
                if (garbageCheckBox.isChecked()) {
                    garbageCheckBox.setChecked(false);
                }
                if (containerCheckBox.isChecked()) {
                    containerCheckBox.setChecked(false);
                }
                if (paperCheckBox.isChecked()) {
                    paperCheckBox.setChecked(false);
                }
                if (locationCheckBox.isChecked()) {
                    locationCheckBox.setChecked(false);
                }
            }
        });

        //set onClickListener for Location button
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //for now only a toast message
                Toast.makeText(MainActivity.this, "Not functional yet :)",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //set onClickListener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //for now only a toast message
                Toast.makeText(MainActivity.this, "Not functional yet :)",
                        Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStart() {
        super.onStart();
        //Connect client
        mGoogleApiClient.connect();
    } //onStart

    @Override
    protected void onStop() {
        //Disconnect the client
        mGoogleApiClient.disconnect();
        super.onStop();
    } //onStop

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

    //This is where we define what to be done when the location changes (nased on the interval we set
    //in onConnected. Here for example we update the lat/log in the TextView
    @Override
    public void onLocationChanged(Location location) {
        CheckBox locationCheckBox = findViewById(R.id.checkbox_location);
        Log.d(TAG, "onLocationChanged() called");
        Log.i(TAG, "Location: " + location.toString());
        locationCheckBox.setChecked(true);
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


} //Main Activity
