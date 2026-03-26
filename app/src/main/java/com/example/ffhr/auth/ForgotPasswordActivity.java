package com.example.ffhr.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etPhone;
    private Button btnSendOtp;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etPhone = findViewById(R.id.etPhone);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnSendOtp.setOnClickListener(v -> validateAndSendResetLink());
    }

    private void validateAndSendResetLink() {
        String email = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etPhone.setError("Email is required");
            etPhone.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etPhone.setError("Enter a valid email");
            etPhone.requestFocus();
            return;
        }

        btnSendOtp.setEnabled(false);

        usersRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                btnSendOtp.setEnabled(true);
                Toast.makeText(
                        ForgotPasswordActivity.this,
                        "Failed to verify email. Please try again.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            boolean emailFound = false;

            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                UserHelperClass user = snapshot.getValue(UserHelperClass.class);
                if (user != null && user.getEmail() != null
                        && user.getEmail().equalsIgnoreCase(email)) {
                    emailFound = true;
                    break;
                }
            }

            if (!emailFound) {
                btnSendOtp.setEnabled(true);
                etPhone.setError("This email is not registered");
                etPhone.requestFocus();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(resetTask -> {
                        btnSendOtp.setEnabled(true);

                        if (resetTask.isSuccessful()) {
                            Intent intent = new Intent(
                                    ForgotPasswordActivity.this,
                                    ResetLinkSentActivity.class
                            );
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = resetTask.getException() != null
                                    ? resetTask.getException().getMessage()
                                    : "Failed to send reset link";

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}
