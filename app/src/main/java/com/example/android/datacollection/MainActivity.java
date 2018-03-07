package com.example.android.datacollection;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;

public class MainActivity extends AppCompatActivity  {

    //Variables
    //Tag for logging
    private final String TAG = "Main Activity";
    //Request location constant for location permission
    static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);

        setTitle("Main Menu");

        /**
         *  This is what gets the permission for the location on start up.
         *  Notice: that this activity is not using the location, but we check this here
         *          so we won't need to deal with the permission in the Location activity
         *
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);

            // REQUEST_CODE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } //permission

        //This checks to see if there is permission to access files
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }

        //Enter data button (this takes us to the LocationEnter activity
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This creates an Intent to open the LocationEnter Class and then use that intent
                //to start LocationEnter activity
                Intent intent = new Intent(MainActivity.this, LocationEnter.class);
                startActivity(intent);
            }
        }); //onClickListener-enter data

        //Delete-all button
        Button deleteAllButton = findViewById(R.id.delete_all);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We pass it to the helper method which will show a dialog and confirm the deletion
                //Disabled for now!!!
//                confirmDelete();
                Toast.makeText(MainActivity.this, "Disabled", Toast.LENGTH_SHORT).show();

            }
        });//onClickListener - Delete All

        //Show Database button
        Button showDatabaseButton = findViewById(R.id.show_database);
        showDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open the LocationView activity
                Intent intent = new Intent(MainActivity.this, LocationView.class);
                startActivity(intent);
            }
        });//onClickListener - show database

        //Google Maps button
        Button viewDataMaps = findViewById(R.id.google_maps_button);
        viewDataMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this, MapsView.class);
                startActivity(intent);
            }
        });// Goole Maps button


        //Cluster button
        Button clustermarkerButton = findViewById(R.id.cluster_button);
        clustermarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ClustermarkerActivity.class);
                startActivity(i);
            }
        });//Cluster

        /**
         * This section figures out what is the source of data (mobile, Wifi, etc)
         * This is going to be used later together with Day/Night to set the night mode (only if
         * outside during night, so if connected to Wifi means they are probably inside and no need
         * for Night mode)
         *
         * NOTE: you need to add user-permission (ACCESS_NETWORK_STATE) to manifest
         * Right now, only displays what is the source of data in a textview.
         */
        TextView networkTextView = findViewById(R.id.network_textview);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mActiveNetwork = cm.getActiveNetworkInfo();
        //this returns a human-readable name
        String activeNetworkString = mActiveNetwork.getTypeName();
        networkTextView.append(activeNetworkString);

    } //OnCreate

    /**
     * Helper method to deal with the confirmation of the action delete all.
     *
     * It first creates a simple AlerDialog. Then it launches the Dialog warning the user
     * that this action will not be reversible and ask wither to confirm or cancel
     */
    //TODO: Create another class for making all these dialogs so we can just refer to it once.
    private void confirmDelete() {
        /**
         * We first make an object of AlertDialog.Builder class (builder). This is used to make the
         * dialog. We set the title, and action of each button with this object.
         * Then we will create an object of AlertDialog class (dialogConfirmation) which will
         * actually show the dialog. We do this using create() method on the build object.
         * Then we show the dialog by calling the show() method on the dialog object.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CAUTION").setMessage("This will delete the entire databse, " +
                "and is irreversible.\nWould you like to continue?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //By passing in 1 as the whereClause, it will delete all rows and return the number
                //of rows deleted.
                String whereClause = "1";
                int result = getContentResolver().delete(LocationEntry.CONTENT_URI,
                        whereClause, null);
                //We should be getting an integer with the number of deleted rows since we have
                // passed in 1 as where clause
                Toast.makeText(MainActivity.this,
                        "All rows in database have been deleted" +
                                "\nnumber of deleted rows: " + result,
                        Toast.LENGTH_SHORT).show();
                displayDatabaseInfo();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialogConfirmation = builder.create();
        dialogConfirmation.show();
    }//confirmDelete

    @Override
    protected void onStart() {
        Log.d(TAG, "Started");
        super.onStart();
        displayDatabaseInfo();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Paused");
        //Disconnect the client on pause
        super.onPause();
    } //onPause

    @Override
    protected void onResume() {
        Log.d(TAG, "Resumed");
        //Re-connect the client on resume
        displayDatabaseInfo();
        super.onResume();
    } //onResume

    private void displayDatabaseInfo(){

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

} //Main Activity
