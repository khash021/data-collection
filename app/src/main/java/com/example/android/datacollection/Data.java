package com.example.android.datacollection;

/**
 * Created by Khashayar on 2/13/2018.
 */

import java.util.ArrayList;

/**
 * This class is responsible for out Data object which will include all of the information
 * we collect
 */

public class Data {

    double lat = 0.0;
    double lon = 0.0;
    boolean garbage = false;
    boolean container = false;
    boolean paper = false;

    //default constructor
    Data(){}


    //Create a message with all the current Data objects to be sent out
    public static String getDataList (ArrayList<Data> dataArray) {
        String list = "";
        Data data = new Data();
        int g, c, p;
        for (int i=0; i<dataArray.size(); i++ ) {
            data = dataArray.get(i);
            //sets the value of g (garbage to 1 if the boolean value is true, and 0 if false
            g = (data.garbage) ? 1:0;
            c = (data.container) ? 1:0;
            p = (data.paper) ? 1:0;
            list += "\n" + Integer.toString(i) + " ; " + data.lat + " : " + data.lon + " ; " +
                    Integer.toString(g) + " : " + Integer.toString(c) + " : " +
                    Integer.toString(p) + " .";
        }
        return list;
    }
}
