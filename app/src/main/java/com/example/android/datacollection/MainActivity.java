package com.example.android.datacollection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.datacollection.Database.DataContract;
import com.example.android.datacollection.Database.DataDbHelper;

public class MainActivity extends AppCompatActivity  {

    //Variables
    private final String TAG = "Main Activity";
    private DataDbHelper mDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);


        //Buttons
        Button enterDataButton = findViewById(R.id.enter_data);


        //onClick listener for reset button
        enterDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This creates an Intent to open the DataEntry Class and then use that intent
                //to start DataEntry activity
                Intent intent = new Intent(MainActivity.this, DataEntry.class);
                startActivity(intent);
            }
        }); //onClickListener

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

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {DataContract.DataEntry._ID};

        Cursor cursor = db.query(
                DataContract.DataEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        TextView counterTextView  = (TextView) findViewById(R.id.data_counter);

        try {
            counterTextView.setText("Current number of data points: " + Integer.toString(cursor.getCount()));
        } finally {
            cursor.close();
        }
    }







} //Main Activity
