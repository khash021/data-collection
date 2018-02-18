package com.example.android.datacollection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {

    //Variables
    private final String TAG = "Main Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);

        TextView dataCounter = findViewById(R.id.data_counter);

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



    } //OnCreate


    private void displayDatabaseInfo(){

    }//displayDatabaseInfo


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





} //Main Activity
