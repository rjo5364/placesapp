package edu.psu.sweng888.placesapp;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

//  class for binding place information to a RecyclerView
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private final ArrayList<PlaceInfo> placesList; // List of places to display
    private final LatLng userLocation; // User's current location for distance calculation

    // Const initializes the list of places and user location
    public PlaceAdapter(ArrayList<PlaceInfo> placesList, LatLng userLocation) {
        this.placesList = placesList;
        this.userLocation = userLocation;
    }

    // Inflates the layout for individual place items
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        return new ViewHolder(view);
    }

    // Binds data for a single place to the corresponding item view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceInfo place = placesList.get(position);
        holder.name.setText(place.getName()); // Sets the place name
        holder.address.setText(place.getAddress()); // Sets the place address

        // Calculates the distance between user and place
        float[] results = new float[1];
        Location.distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                place.getLatLng().latitude,
                place.getLatLng().longitude,
                results
        );
        float distanceInMeters = results[0];
        float distanceInMiles = distanceInMeters / 1609.34f;

        holder.distance.setText(String.format("Distance: %.2f miles", distanceInMiles)); // Displays the distance
    }

    // Returns the total number of places in the list
    @Override
    public int getItemCount() {
        return placesList.size();
    }

    // ViewHolder class to hold and manage individual item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, distance; // TextViews for place details

        // Initializes views for place details
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            distance = itemView.findViewById(R.id.distance);
        }
    }
}