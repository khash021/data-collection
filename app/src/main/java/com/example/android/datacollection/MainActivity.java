package com.example.android.datacollection;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;
import com.example.android.datacollection.Database.DataDbHelper;

public class MainActivity extends AppCompatActivity  {

    //Variables
    //Tag for logging
    private final String TAG = "Main Activity";
    //Declaring the Database helper object
    private DataDbHelper mDbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);

        //TODO: Move the permission here from the LocationEntry


        //Enter data button
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This creates an Intent to open the LocationEntry Class and then use that intent
                //to start LocationEntry activity
                Intent intent = new Intent(MainActivity.this, DataEntry.class);
                startActivity(intent);
            }
        }); //onClickListener-enter data


        //Delete button
        Button deleteAllButton = findViewById(R.id.delete_all);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });//onClickListener - Delete All

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new DataDbHelper(this);



    } //OnCreate



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
        TextView arrayCounterTextView = findViewById(R.id.data_array_counter);

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
