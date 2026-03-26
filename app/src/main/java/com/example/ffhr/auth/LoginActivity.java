package com.example.ffhr.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;
import com.example.ffhr.ui.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button goButton, forgetPasswordText;
    TextView signupRedirectText;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        goButton = findViewById(R.id.goButton);
        forgetPasswordText = findViewById(R.id.forgetPasswordText);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        goButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                usernameInput.setError("Email required");
                usernameInput.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                usernameInput.setError("Enter a valid email");
                usernameInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password required");
                passwordInput.requestFocus();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Login failed";

                            Toast.makeText(LoginActivity.this,
                                    "Login Failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        forgetPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
