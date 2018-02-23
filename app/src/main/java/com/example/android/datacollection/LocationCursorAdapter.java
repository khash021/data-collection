package com.example.android.datacollection;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.datacollection.Database.LocationContract.LocationEntry;

/**
 * Created by Khashayar on 2/23/2018.
 *
 * {@link LocationCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of location data as its data source. This adapter knows
 * how to create list items for each row of location data in the {@link Cursor}.
 */

public class LocationCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link LocationCursorAdapter}.
     *
     * @param context       The context
     * @param cursor        The cursor from which to get the data.
     */
    public LocationCursorAdapter (Context context, Cursor cursor) {
        super(context, cursor);
    }//LocationCursorAdapter

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }//newView

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView idTextView = view.findViewById(R.id.location_id);
        TextView summaryTextView = view.findViewById(R.id.location_summary);

        /**
         *   Find the columns of pet attributes that we're interested in our Cursor is already set
         *   at a specific row (managed by the listView depending on where we are so we don't need
         *   to worry about row index). However, in order to get the data from a specific column
         *   (here getting the ID, time, and other checkbox info), first we need to find the index
         *   associated with that column name. This is exactly what cursor.getColumnIndex does.
         *   It returns an integer which is the index of the Column name that was passed in as a
         *   String argument.
         */
        int idColumnIndex = cursor.getColumnIndex(LocationEntry._ID);
        int garbageColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_GARBAGE);
        int paperColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_PAPER);
        int containerColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_CONTAINER);

        /**
         *  Read the location attributes from the Cursor for the current location. Now that we have
         *  the column index, we can get the value associated with that column index by
         *  cursor.getString method and we pass in our index. It is possible to put
         *  cursor.getColumnIndex(LocationEntry._ID) as the input argument of the getString method;
         *  I left it this way to understand better.
         *  We will also handle comments/establishment with onTouch popup message to conserve space
         *  in the listview
         */
        //TODO: put everything in one line. get the index and string at the same time.
        String locationID = cursor.getString(idColumnIndex);
        int locationGarbage = cursor.getInt(garbageColumnIndex);
        int locationPaper = cursor.getInt(paperColumnIndex);
        int locationContainer = cursor.getInt(containerColumnIndex);

        // Populate fields with extracted properties
        idTextView.setText(locationID);
        //We check for the boolean values here and only show them if they are true
        String garbage, container, paper;
        if (locationGarbage == 1) {
            garbage = "Garbage";
        } else {
            garbage = "";
        }
        if (locationPaper == 1) {
            paper = "Paper";
        } else {
            paper = "";
        }
        if (locationContainer == 1) {
            container = "Container";
        } else {
            container = "";
        }

        //Create the summary and set the text
        String summary = garbage + "  " + container + "  " + paper;
        summaryTextView.setText(summary);
    }//bindView
}//LocationCursorAdapter Class
