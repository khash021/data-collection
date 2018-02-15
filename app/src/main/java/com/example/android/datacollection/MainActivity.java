package com.example.android.datacollection;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
    //ArrayList for holding the data
    ArrayList<Data> dataArray = new ArrayList<>();
    //Data object
    Data data = new Data();
    //Location data
    double lat, lon;
    //Date and time format and date instance
    final DateFormat dateFormat = new SimpleDateFormat("EEE, MMM.dd.yyyy 'at' HH:mm");
    final String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());


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
        final EditText commentText = findViewById(R.id.comment_text);

        //Buttons
        Button resetButton = findViewById(R.id.reset_button);
        Button submitButton = findViewById(R.id.submit_button);
        Button listButton = findViewById(R.id.list_button);
        Button sendButton = findViewById(R.id.send_button);

        //onClick listener for reset button
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


        //onClickListener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationCheckBox.isChecked()) {
                    data = new Data();
                    data.garbage = garbageCheckBox.isChecked();
                    data.container = containerCheckBox.isChecked();
                    data.container = containerCheckBox.isChecked();
                    data.lat = lat;
                    data.lon = lon;
                    data.message = commentText.getText().toString();
                    dataArray.add(data);
                } else {
                    Toast.makeText(MainActivity.this, "Location is not acquired yet, "
                            + "please wait for the location check box and try again",
                            Toast.LENGTH_SHORT).show();
                }

                //for now only a toast message
                Toast.makeText(MainActivity.this, "Added to the list",
                        Toast.LENGTH_SHORT).show();
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
                if (commentText.getText().toString().length() > 0) {
                    commentText.setText("");
                }
            }
        });

        //onClickListener for list button
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "There are " + dataArray.size() +
                        " data objects in the list", Toast.LENGTH_SHORT).show();
            }
        });

        //onClick Listener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This section emails the ArrayList using the message created by getDataList method of Data class
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: khash.021@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Data collection: " + currentDateTime);
                intent.putExtra(Intent.EXTRA_TEXT, Data.getDataList(dataArray));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
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

    @Override
    protected void onStart() {
        super.onStart();
        //Connect client
        mGoogleApiClient.connect();
    } //onStart

    @Override
    protected void onStop() {
        //This section emails the ArrayList using the message created by getDataList method of Data class
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto: khash.021@gmail.com"));
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Data collection: " + currentDateTime);
//        intent.putExtra(Intent.EXTRA_TEXT, Data.getDataList(dataArray));
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        }
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


} //Main Activity
