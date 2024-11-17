package edu.psu.sweng888.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This sets the content view to the splash activity layout
        setContentView(R.layout.activity_splash);

        // This delays the transition to the LoginActivity for 1 second
        new Handler().postDelayed(() -> {
            // This creates an intent to navigate to LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            // This starts the LoginActivity
            startActivity(intent);
            // This finishes the SplashActivity to prevent returning to it
            finish();
        }, 1000);
    }
}