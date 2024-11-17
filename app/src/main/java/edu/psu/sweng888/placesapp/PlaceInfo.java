package edu.psu.sweng888.placesapp;

import com.google.android.gms.maps.model.LatLng;

// Represents a place with a name, address, and geographic location
public class PlaceInfo {
    private String name; // Stores the name place
    private String address; // Stores the addres of  place
    private LatLng latLng; // Stores the geo coord of  place

    // Cons to set the name, address, and location of the place
    public PlaceInfo(String name, String address, LatLng latLng) {
        this.name = name;
        this.address = address;
        this.latLng = latLng;
    }

    // Returns the name of the place
    public String getName() {
        return name;
    }

    // Returns the address of the place
    public String getAddress() {
        return address;
    }

    // Returns the geo coordinates of the place
    public LatLng getLatLng() {
        return latLng;
    }
}