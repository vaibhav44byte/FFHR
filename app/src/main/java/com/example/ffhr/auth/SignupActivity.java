package com.example.ffhr.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    // UI Fields
    private TextInputLayout fullnameLayout, usernameLayout, emailLayout, phoneLayout, passwordLayout;
    private Button signupButton;
    private TextView loginRedirectText;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");

        // Link UI
        fullnameLayout = findViewById(R.id.fullname);
        usernameLayout = findViewById(R.id.username);
        emailLayout = findViewById(R.id.email);
        phoneLayout = findViewById(R.id.phone);
        passwordLayout = findViewById(R.id.password);
        signupButton = findViewById(R.id.signupButton);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // On Signup Button Click
        signupButton.setOnClickListener(v -> registerUser());

        // Go to Login
        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    // ============================
    // REGISTER USER METHOD
    // ============================
    private void registerUser() {
        String name = getText(fullnameLayout);
        String username = getText(usernameLayout);
        String email = getText(emailLayout);
        String phone = getText(phoneLayout);
        String password = getText(passwordLayout);

        // VALIDATION
        if (!validateName(name)) return;
        if (!validateUsername(username)) return;
        if (!validateEmail(email)) return;
        if (!validatePhone(phone)) return;
        if (!validatePassword(password)) return;

        // Firebase AUTH Create Account
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // User UID from Firebase Auth
                String uid = auth.getCurrentUser().getUid();

                // Create user helper object
                UserHelperClass user = new UserHelperClass(name, username, email, phone, password);

                // Save user data under UID
                reference.child(uid).setValue(user);

                Toast.makeText(SignupActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();

            } else {
                Toast.makeText(SignupActivity.this, "Signup Failed: " +
                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ============================
    // VALIDATION FUNCTIONS
    // ============================

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            fullnameLayout.setError("Full Name is required");
            return false;
        } else if (name.length() < 3) {
            fullnameLayout.setError("Name must be at least 3 letters");
            return false;
        } else if (!name.matches("[a-zA-Z ]+")) {
            fullnameLayout.setError("Only letters allowed");
            return false;
        }
        fullnameLayout.setError(null);
        return true;
    }

    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            usernameLayout.setError("Username is required");
            return false;
        } else if (username.length() < 4) {
            usernameLayout.setError("Must be at least 4 characters");
            return false;
        } else if (!username.matches("[a-zA-Z0-9]+")) {
            usernameLayout.setError("Letters + numbers only");
            return false;
        }
        usernameLayout.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            return false;
        }
        emailLayout.setError(null);
        return true;
    }

    private boolean validatePhone(String phoneNo) {
        if (phoneNo.isEmpty()) {
            phoneLayout.setError("Phone number required");
            return false;
        } else if (phoneNo.length() != 10) {
            phoneLayout.setError("Enter 10-digit phone");
            return false;
        } else if (!phoneNo.matches("[0-9]+")) {
            phoneLayout.setError("Digits only");
            return false;
        }
        phoneLayout.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Minimum 6 characters");
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }

    // Safely get text from input fields
    private String getText(TextInputLayout layout) {
        TextInputEditText editText = (TextInputEditText) layout.getEditText();
        return (editText != null) ? editText.getText().toString().trim() : "";
    }
}
