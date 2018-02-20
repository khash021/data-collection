package com.example.android.datacollection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.datacollection.Database.DataContract;
import com.example.android.datacollection.Database.DataDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity  {

    //Variables
    private final String TAG = "Main Activity";
    private DataDbHelper mDbHelper;
    //ArrayList for holding the data
    static ArrayList<Data> dataArray = new ArrayList<>();
    //Date and time format and date instance
    final DateFormat dateFormat = new SimpleDateFormat("EEE, MMM.dd.yyyy 'at' HH:mm");
    final String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);


        //Buttons
        Button enterDataButton = findViewById(R.id.enter_data);
        Button uploadArrayList  = findViewById(R.id.send_email);

        //onClick listener for enter data button
        enterDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This creates an Intent to open the DataEntry Class and then use that intent
                //to start DataEntry activity
                Intent intent = new Intent(MainActivity.this, DataEntry.class);
                startActivity(intent);
            }
        }); //onClickListener-enter data

        //onClickListener for uploadArrayList
        uploadArrayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This section emails the ArrayList using the message created by getDataList method of Data class
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: khash.021@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Data collection: " + currentDateTime);
                intent.putExtra(Intent.EXTRA_TEXT, Data.getDataList(dataArray));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }//if

            }//onClick
        });//onClickListener - uploadArrayList

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

        TextView counterTextView  = findViewById(R.id.data_counter);
        TextView arrayCounterTextView = findViewById(R.id.data_array_counter);

        try {
            counterTextView.setText("Current number of data-points in database: "
                    + Integer.toString(cursor.getCount()));
            arrayCounterTextView.setText("Current number of data-points in ArrayList: "
                    + dataArray.size());

        } finally {
            cursor.close();
        }
    }







} //Main Activity
