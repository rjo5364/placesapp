package edu.psu.sweng888.placesapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private ArrayList<PlaceInfo> placesList = new ArrayList<>();
    private String userPreference = "park"; // Default fallback
    private boolean locationPermissionGranted = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private String userName; // To store the username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firebase for user authentication and data storage
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the toolbar and its title for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Map Activity");

        // Configure the navigation drawer and synchronize its toggle with the toolbar
        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle navigation item selection, including navigating back to home
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateBackToHome(); // Navigate to the home activity
            } else if (id == R.id.nav_map) {
                Toast.makeText(this, "Already on Map Activity", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers(); // Close the drawer after an item is selected
            return true;
        });

        // Set up the RecyclerView for displaying nearby places
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize location services for retrieving the user's current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load and display the map using the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Set up callback for when the map is ready
        } else {
            Log.e(TAG, "Error initializing map fragment");
        }

        // Retrieve user preferences and username from Firestore
        fetchUserDetails();

        // Set up the Search Button to perform searches based on user preferences
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            if (locationPermissionGranted) {
                performSearch(userPreference); // Perform a search if location permission is granted
            } else {
                Toast.makeText(this, "Location permission not granted!", Toast.LENGTH_SHORT).show();
                checkLocationPermission(); // Request location permission if not already granted
            }
        });

        // Ensure location permissions are checked at activity startup
        checkLocationPermission();
    }

    private void fetchUserDetails() {
        // Fetches user details (preference and username) from Firestore based on the logged-in user's UID
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();

                    // Retrieve user preference or set to default if null/empty
                    userPreference = document.getString("preference");
                    if (userPreference == null || userPreference.isEmpty()) {
                        userPreference = "park";
                    }

                    // Retrieve username or set to default if null/empty
                    userName = document.getString("username");
                    if (userName == null || userName.isEmpty()) {
                        userName = "User";
                    }

                    Log.d(TAG, "Fetched user preference: " + userPreference + ", Username: " + userName);

                    // Automatically perform a search after details are fetched if permissions are granted
                    if (locationPermissionGranted) {
                        performSearch(userPreference);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch user details from Firestore");
                    Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "No logged-in user");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBackToHome() {
        // Navigates back to the MainActivity and passes the username as an extra
        Intent intent = new Intent(MapActivity.this, MainActivity.class);
        intent.putExtra("USER_NAME", userName);
        startActivity(intent);
        finish();
    }

    private void checkLocationPermission() {
        // Checks if location permissions are granted; requests them if not
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handles the result of the location permission request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot fetch location or nearby places.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performSearch(String preference) {
        // Fetches the user's current location and performs a search based on the provided preference
        if (!locationPermissionGranted) {
            Toast.makeText(this, "Location permissions are not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "GoogleMap object is null");
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));

                // Fetch nearby places based on user location and preference
                fetchNearbyPlaces(userLocation, preference);
            } else {
                Toast.makeText(MapActivity.this, "Unable to fetch location. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to get last location");
            }
        });
    }

    private void fetchNearbyPlaces(LatLng userLocation, String preference) {
        // Makes a request to the Google Places API to fetch nearby places based on user location and preference
        String apiKey = getString(R.string.MAPS_API_KEY);
        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing. Cannot fetch nearby places.");
            return;
        }

        String nearbySearchUrl = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=5000&type=%s&key=%s",
                userLocation.latitude,
                userLocation.longitude,
                preference.toLowerCase(),
                apiKey
        );

        Log.d(TAG, "Sending Nearby Search request: " + nearbySearchUrl);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                nearbySearchUrl,
                null,
                response -> {
                    Log.d(TAG, "API Response: " + response.toString());

                    try {
                        JSONArray results = response.getJSONArray("results");
                        placesList.clear();

                        // Parse each result and add to the places list
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            String name = place.getString("name");
                            String address = place.optString("vicinity", "No address available");
                            JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                            LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                            Log.d(TAG, "Place found: " + name + ", Address: " + address);

                            placesList.add(new PlaceInfo(name, address, latLng));
                        }

                        updateRecyclerView(userLocation);
                        addMarkersToMap();

                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse Nearby Search response", e);
                    }
                },
                error -> Log.e(TAG, "Failed to fetch nearby places", error)
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void updateRecyclerView(LatLng userLocation) {
        // Updates the RecyclerView to display the list of nearby places
        PlaceAdapter adapter = new PlaceAdapter(placesList, userLocation);
        recyclerView.setAdapter(adapter);
    }

    private void addMarkersToMap() {
        // Adds markers for each place in the places list to the map
        for (PlaceInfo place : placesList) {
            mMap.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName()));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Sets up the map when it is ready and enables location if permission is granted
        mMap = googleMap;
        if (locationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

}