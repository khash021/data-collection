package com.example.android.datacollection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Khashayar on 3/1/2018.
 *
 * This is the class that shows the data on Google Maps (in the app)
 */

public class MapsView extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG =this.getClass().getSimpleName();
    //Google Maps object
    GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_view_activity);

        /**
         * The fragment is what actually contains the Google Maps and displays it.
         * Here we get the SupportMapFragment (from the layout file) and request notification
         * when the map is ready to be used. (onMapReady callback will get called when the map is
         * ready
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }//onCreate

    /**
     * By implementing the OnMapReadyCallback interface and using the onMapReady(GoogleMap) callback
     * method, we get a handle to the GoogleMap object.
     * The callback is triggered when the map is ready to be used.
     * The GoogleMap object is the internal representation of the map itself
     * To set the view options for a map, you modify its GoogleMap object.
     * Then call getMapAsync() on the fragment to register the callback (above in onCreate)
     */
    @Override
    public void onMapReady(GoogleMap map) {
        Log.v(TAG, "onMapReady callback triggered");

    }//onMapReady
}//MapsView class
