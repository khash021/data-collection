package com.example.android.datacollection.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Khashayar on 3/5/2018.
 *
 * This is the Items (markers) that are going to be used for creating Marker Clusters.
 *
 */

public class LocationItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public LocationItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public LocationItem(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() { return mSnippet; }
}//main
