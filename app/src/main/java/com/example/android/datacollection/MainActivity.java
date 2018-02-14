package com.example.android.datacollection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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


} //Main Activity
