package com.example.ffhr.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;

public class ResetLinkSentActivity extends AppCompatActivity {

    private Button btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_link_sent);

        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ResetLinkSentActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
