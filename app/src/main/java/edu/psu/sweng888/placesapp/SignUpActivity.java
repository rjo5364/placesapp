package edu.psu.sweng888.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // Manages Firebase Authentication
    private FirebaseFirestore db; // Handles Firestore database operations
    private EditText nameInput, emailInput, passwordInput; // Input fields for user data
    private Spinner preferenceSpinner; // Dropdown for selecting user preference
    private TextView loginText; // Navigates back to the login screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Sets up Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Links UI elements
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        preferenceSpinner = findViewById(R.id.preferenceSpinner);
        Button signUpButton = findViewById(R.id.signUpButton);
        loginText = findViewById(R.id.loginText);

        // Populates the spinner with predefined preferences
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.preferences, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        preferenceSpinner.setAdapter(adapter);

        // Registers a new user when the sign-up button is clicked
        signUpButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String preference = preferenceSpinner.getSelectedItem().toString();

            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                registerUser(name, email, password, preference);
            } else {
                Toast.makeText(SignUpActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Redirects to the login screen when the login text is clicked
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Handles user registration and saving user data to Firestore
    private void registerUser(String name, String email, String password, String preference) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("preference", preference);
                            user.put("email", email);

                            db.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "Sign-Up Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error retrieving user UID", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign-Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}