package com.example.android.datacollection;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;

import java.util.Calendar;

/**
 * Created by Khashayar on 2/23/2018.
 */

public class LocationEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Identifier for the location data loader
    private static final int EXISTING_LOCATION_LOADER = 0;

    private final String TAG = this.getClass().getSimpleName();

    //Checkboxes, and EditTexts
    CheckBox mGarbageCheckBox, mContainerCheckBox, mPaperCheckBox, mEstablishmentCheckBox;
    EditText mCommentText, mEstablishmentText;
    TextView mLocationId;
    Uri mCurrentLocationUri;
    Button mMapsButton, mSaveButton, mDeleteButton;
    double mLat, mLon;

    //This is for tracking unsaved changes
    private boolean mDataChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mDataChanged boolean to true.
     *
     * Then we will use this as the input of our setOnTouchListener on the views that we want to
     * trigger this in the onCreate
     *
     * Having a OnTouchListener object is very useful if we want to do something for all of the
     * touch events such as setting mDataChanged to true regardless of what that view that triggered
     * is. This could have also been accomplished by adding the line 'mDataChanged = true;' to all
     * of our individual views, but is bad coding. Everytime we will copy and paste something, we
     * should think about generalizing that using helper methods, etc.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDataChanged = true;
            return false;
        }
    };//OnTouchListener

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_edit);

        mGarbageCheckBox = findViewById(R.id.edit_checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.edit_checkbox_container);
        mPaperCheckBox = findViewById(R.id.edit_checkbox_paper);
        mEstablishmentCheckBox = findViewById(R.id.edit_checkbox_inside);
        mCommentText = findViewById(R.id.edit_comment_text);
        mEstablishmentText = findViewById(R.id.edit_inside_text);
        mLocationId = findViewById(R.id.edit_location_ID);

        //Get the Intent (the one used to start this activity)
        Intent intent = getIntent();
        mCurrentLocationUri = intent.getData();

        // Initialize a loader to read the location data from the database
        getLoaderManager().initLoader(EXISTING_LOCATION_LOADER, null, this);

        //Here we set the onTouchListener on the items we want to trigger the dialog confirming user
        //wants to exit the activity
        mCommentText.setOnTouchListener(mTouchListener);
        mEstablishmentText.setOnTouchListener(mTouchListener);
        mGarbageCheckBox.setOnTouchListener(mTouchListener);
        mContainerCheckBox.setOnTouchListener(mTouchListener);
        mPaperCheckBox.setOnTouchListener(mTouchListener);
        mEstablishmentCheckBox.setOnTouchListener(mTouchListener);

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

        //maps Button
        mMapsButton = findViewById(R.id.edit_map_button);
        mMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMaps();
            }
        });//maps Button

        //save button
        mSaveButton = findViewById(R.id.edit_submit_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if ((mEstablishmentCheckBox.isChecked() &&
                            mEstablishmentText.getText().toString().trim().length() > 0) ||
                            (!mEstablishmentCheckBox.isChecked() &&
                                    mEstablishmentText.getText().toString().trim().length() < 1)) {
                        boolean result = saveLocation();
                        if (result) {
                            Toast.makeText(LocationEditActivity.this,
                                    "Location updated successfully",Toast.LENGTH_SHORT).show();
                            finish();
                        } else  {
                            Toast.makeText(LocationEditActivity.this, "Error with updating",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LocationEditActivity.this, "If the establishment" +
                                " checkbox is ticked, there needs to be a comment; and vice versa",
                                Toast.LENGTH_SHORT).show();
                    }
            }
        });//save button

        //Delete button
        mDeleteButton = findViewById(R.id.edit_delete_button);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = getContentResolver().delete(mCurrentLocationUri, null , null);
                //Check to see if the delete was successful
                if (result == 1) {
                    Toast.makeText(LocationEditActivity.this,
                            "Location deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(LocationEditActivity.this,
                            "Error with deleting the last entry", Toast.LENGTH_SHORT).show();
                }
            }
        });//Delete button
    }//onCreate

    private void goToMaps(){
        //Create the Uri first
        //By adding "?q= mLat, mLon" we put a pin in the location on the map
        Uri mapIntentUri = Uri.parse("geo:" + mLat + ", " + mLon + "?q=" + mLat + ", " + mLon );
        Intent intent =new Intent(Intent.ACTION_VIEW, mapIntentUri);
        startActivity(intent);
    }//showOnGoogleMaps

    /**
     * This helper method updates the location data.
     * Update is only possible for checkboxes and comments
     * @return true if the update was successful, false otherwise.
     */

    private boolean saveLocation(){
        mGarbageCheckBox = findViewById(R.id.edit_checkbox_garbage);
        mContainerCheckBox = findViewById(R.id.edit_checkbox_container);
        mPaperCheckBox = findViewById(R.id.edit_checkbox_paper);
        mEstablishmentCheckBox = findViewById(R.id.edit_checkbox_inside);
        mCommentText = findViewById(R.id.edit_comment_text);
        mEstablishmentText = findViewById(R.id.edit_inside_text);
        mLocationId = findViewById(R.id.edit_location_ID);

        //converting booleans
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

        //Get the text from comments edit text
        String mComment = mCommentText.getText().toString().trim();
        mComment += "\nUpdated on " + LocationEnterActivity.mDateFormat.format(Calendar.getInstance().getTime());
        String mEstablishmentComment = mEstablishmentText.getText().toString().trim();

        // Create a new map of values,
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_GARBAGE, mGarbage);
        values.put(LocationEntry.COLUMN_LOCATION_CONTAINER, mContainer);
        values.put(LocationEntry.COLUMN_LOCATION_PAPER, mPaper);
        values.put(LocationEntry.COLUMN_LOCATION_ESTABLISHMENT, mEstablishment);
        values.put(LocationEntry.COLUMN_LOCATION_COMMENT, mComment);
        values.put(LocationEntry.COLUMN_LOCATION_ESTABLISHMENT_COMMENT, mEstablishmentComment);

        int result = getContentResolver().update(
                mCurrentLocationUri,             //Uri
                values,                     //Values to be updated
                null,                //null will update all rows
                null           //no where so we dont need selectionArgs
        );

        if (result == 0) {
            //return false if the update was not successful
            return false;
        } else {
            //return true if the update was successful
            return true;
        }

    }//saveLocation

    /**
     *
     * @param id id of the location we want to edit
     * @param args
     * @return Loader<Cursor>
     *
     * This loader will execute the ContentProvider's query method on background thread. We pass in
     * the Uri of the location that was associated with the Intent that started this activity.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection for the columns we want
        String[] projection = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_LATITUDE,
                LocationEntry.COLUMN_LOCATION_LONGITUDE,
                LocationEntry.COLUMN_LOCATION_GARBAGE,
                LocationEntry.COLUMN_LOCATION_PAPER,
                LocationEntry.COLUMN_LOCATION_CONTAINER,
                LocationEntry.COLUMN_LOCATION_COMMENT,
                LocationEntry.COLUMN_LOCATION_ESTABLISHMENT,
                LocationEntry.COLUMN_LOCATION_ESTABLISHMENT_COMMENT
        };

        return new CursorLoader(this,   //parent activity context
                mCurrentLocationUri,           //Current Uri
                projection,                     //The columns to return for each row
                null,                   //Selection criteria
                null,               //Selection criteria
                null                    //The sort order for returned rows
        );
    }//onCreateLoader

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }//if

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of location attributes and set them to the current view
            // (all in one line)
            String Id = data.getString(data.getColumnIndex(LocationEntry._ID));
            Id = "Location ID: " + Id;
            mLat = data.getDouble(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE));
            mLon = data.getDouble(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE));
            int garbage = data.getInt(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_GARBAGE));
            int container = data.getInt(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_CONTAINER));
            int paper = data.getInt(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_PAPER));
            int establishment = data.getInt(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_ESTABLISHMENT));
            String comment = data.getString(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_COMMENT));
            String establishmentComment = data.getString(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_ESTABLISHMENT_COMMENT));

            //update the view on the screen
            mLocationId.setText(Id);
            mCommentText.setText(comment);
            mEstablishmentText.setText(establishmentComment);
            //Check box default is false, so we only update them if they are true (1) in the db
            if (garbage == 1) {
                mGarbageCheckBox.setChecked(true);
            }
            if (container == 1) {
                mContainerCheckBox.setChecked(true);
            }
            if (paper == 1) {
                mPaperCheckBox.setChecked(true);
            }
            if (establishment == 1) {
                mEstablishmentCheckBox.setChecked(true);
            }
        }//main if
    }//onLoadFinished

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is invalidated show a toast message and exit.
        Log.v(TAG, "Something went wrong with the Loader.");
        //exit activity
        finish();
    }//onLoaderReset

    /**
     * For handling the discard changes dialog, we use a helper method to show the dialog if there
     * were any unsaved changes.
     * onBackPressed gets initiated if the user press the Phone's back button.
     * onOptionsItemSelected gets initiated if the user uses the app's back button.
     *
     */

    private void discardDialog(){
        //This only gets executed if there are unsaved data.
        //Make a dialog.
        //Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard Changes?");
        // Add the buttons
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //make the Dialog object
        final AlertDialog dialog = builder.create();
        dialog.show();
    }//discardDialog

    //This gets triggered when the phone's back button is pressed
    @Override
    public void onBackPressed() {
        //We check to see if the location has changed
        if (!mDataChanged) {
            super.onBackPressed();
            return;
        }//if
        discardDialog();
    }//onBackPressed

    //This gets triggered when the app back arrow is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mDataChanged) {
            return super.onOptionsItemSelected(item);
        }//if
        discardDialog();
        return true;
    }//onOptionsItemSelected
}//LocationEditActivity class
