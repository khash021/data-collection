package com.example.android.datacollection.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Khashayar on 3/6/2018.
 *
 * MyLocation is similiar to a row of our database table. The object of this class will have all
 * the attributes of the Location data (including Lat, Lng, boolean variables, comments, etc).
 *
 * This is used to create an object for each location and store it in an ArrayList for app
 * operations, so we only have to query the database once on startup.
 *
 */

public class MyLocation {

    //Variables
    public LatLng latLng;
    public int _id;
    public String comment, establishmentComment, timeDate;
    public boolean garbage, container, paper, establishment;

    //Defauly constructor
    public MyLocation(){}

    //Constructor with input arguments
    public MyLocation (int id, double lat, double lng, int intGarbage, int intContainer, int intPaper,
                int intEstablishment, String inputTimeDate ) {
        latLng = new LatLng(lat, lng);
        _id = id;
        if (intGarbage == 1) {
            garbage = true;
        } else if (intGarbage == 0) {
            garbage = false;
        }
        if (intContainer == 1) {
            container = true;
        } else if (intContainer == 0) {
            container = false;
        }
        if (intPaper == 1) {
            paper = true;
        } else if (intPaper == 0) {
            paper = false;
        }
        if (intEstablishment == 1) {
            establishment = true;
        } else if (intEstablishment == 0) {
            establishment = false;
        }
        timeDate = inputTimeDate;
        //Since comment, and establishment comment could be null, we will check them when we are
        //making the MyLocation object and add them manually if they are not null
    }
}//MyLocation class
