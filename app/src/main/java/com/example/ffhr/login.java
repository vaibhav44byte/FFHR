package com.example.ffhr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button goButton;
    TextView signupRedirectText;

    FirebaseAuth auth; // FirebaseAuth reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Link XML
        usernameInput = findViewById(R.id.usernameInput);  // this is EMAIL
        passwordInput = findViewById(R.id.passwordInput);
        goButton = findViewById(R.id.goButton);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        // LOGIN BUTTON
        goButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validation
            if (email.isEmpty()) {
                usernameInput.setError("Email required");
                return;
            }
            if (password.isEmpty()) {
                passwordInput.setError("Password required");
                return;
            }

            // Firebase Login
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Toast.makeText(login.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(login.this, HomeActivity.class));

// For now, after login just stay on login page or redirect to a new page we create later

                            finish();

                        } else {
                            Toast.makeText(login.this,
                                    "Login Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        });

        // Redirect to Signup
        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(login.this, SignupActivity.class));
        });
    }
}
