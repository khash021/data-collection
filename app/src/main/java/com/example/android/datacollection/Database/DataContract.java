package com.example.android.datacollection.Database;

import android.provider.BaseColumns;

/**
 * Created by Khashayar on 2/17/2018.
 *  * This is the class that contains the schema of the table and all the constants for out DB
 */

public class DataContract {

    //Default empty constructor
    private DataContract(){}

    // Inner class that defines the table contents
    public static class DataEntry implements BaseColumns {

        /*
                Note that all of these Strings are the object data type of those names;
                and not the actual data type of the database in the corresponding column.
         */

        public static final String TABLE_NAME = "locations";
        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;
        /**
         * Latitude of the datapoint
         *
         * Type: REAL
         */
        public final static String COLUMN_LOCATION_LATITUDE ="latitude";

        /**
         * Longitude of datapoint
         *
         * Type: REAL
         */
        public final static String COLUMN_LOCATION_LONGITUDE = "longitude";

        /**
         * Garbage
         *
         * The only possible values are {@link #GARBAGE_TRUE}, {@link #GARBAGE_FALSE},
         *
         * Type: INTEGER
         */
        public final static String COLUMN_LOCATION_GARBAGE = "garbage";
        /**
         * Container
         *
         * The only possible values are {@link #CONTAINER_TRUE}, {@link #CONTAINER_FALSE},
         *
         * Type: INTEGER
         */
        public final static String COLUMN_LOCATION_CONTAINER = "container";
        /**
         * Paper
         *
         * The only possible values are {@link #PAPER_TRUE}, {@link #PAPER_FALSE},
         *
         * Type: INTEGER
         */
        public final static String COLUMN_LOCATION_PAPER = "paper";

        /**
         * Comments
         *
         * Type: TEXT
         */
        public final static String COLUMN_LOCATION_COMMENT = "comments";

        /**
         * Possible values for the garbage, container, paper.
         */
        public static final int GARBAGE_TRUE = 1;
        public static final int GARBAGE_FALSE = 0;
        public static final int CONTAINER_TRUE = 1;
        public static final int CONTAINER_FALSE = 0;
        public static final int PAPER_TRUE = 1;
        public static final int PAPER_FALSE = 0;


    }//DataEntry

}//main
