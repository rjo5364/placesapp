package edu.psu.sweng888.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView welcomeText;
    private String userName; // Stores the username
    private String userPreference; // Stores the user preference
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This initializes Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // This sets up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This configures the DrawerLayout and toggle
        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // This sets up the navigation view, welcome text, and floating button
        NavigationView navigationView = findViewById(R.id.navigationView);
        welcomeText = findViewById(R.id.welcomeText);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);

        // This retrieves user details from Firestore
        fetchUserDetails();

        // This handles navigation drawer item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_map) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("USER_NAME", userName);
                intent.putExtra("USER_PREFERENCE", userPreference);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // This handles the floating action button click
        fabSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_PREFERENCE", userPreference);
            startActivity(intent);
        });
    }

    private void fetchUserDetails() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    userName = document.getString("name"); // Retrieves username
                    userPreference = document.getString("preference"); // Retrieves user preference

                    welcomeText.setText(userName != null && !userName.isEmpty()
                            ? "Welcome, " + userName + "!"
                            : "Welcome!");
                    userPreference = userPreference != null && !userPreference.isEmpty()
                            ? userPreference
                            : "park"; // Default fallback
                } else {
                    welcomeText.setText("Welcome!");
                    userPreference = "park"; // Default fallback
                }
            });
        } else {
            welcomeText.setText("Welcome!");
            userPreference = "park"; // Default fallback
            Toast.makeText(this, "No logged-in user.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // This ensures the drawer toggle functions properly
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}