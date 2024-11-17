package edu.psu.sweng888.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // This handles Firebase Authentication
    private FirebaseFirestore db; // This interacts with Firestore
    private EditText emailInput, passwordInput; // Stores email and password input fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // This initializes Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // This finds and assigns views for email and password inputs
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // This assigns the login button and signup text view
        Button loginButton = findViewById(R.id.loginButton);
        TextView signUpText = findViewById(R.id.signUpText);

        // This sets up the login button click event
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                // This displays a message if any fields are empty
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // This handles Firebase authentication for login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                // This fetches the user's data from Firestore
                                db.collection("users").document(uid).get()
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful() && userTask.getResult() != null) {
                                                DocumentSnapshot document = userTask.getResult();
                                                String userName = document.getString("name");

                                                // This starts MainActivity with the username
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.putExtra("USER_NAME", userName);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // This shows an error if user data fetch fails
                                                Toast.makeText(LoginActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // This shows an error if login fails
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // This handles the signup text click to navigate to SignUpActivity
        signUpText.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }
}