package com.example.ffhr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ffhr.R;
import com.example.ffhr.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    TextView tvGreeting;
    ImageView btnLogout;
    CardView cardHealthReport, cardScanLeaf, cardFertilizer, cardAddFarm, cardSoilStatus, cardSettings;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Initialize Main Views
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);

        cardHealthReport = findViewById(R.id.cardHealthReport);
        cardScanLeaf = findViewById(R.id.cardScanLeaf);
        cardFertilizer = findViewById(R.id.cardFertilizer);
        cardAddFarm = findViewById(R.id.cardAddFarm);
        cardSoilStatus = findViewById(R.id.cardSoilStatus);
        cardSettings = findViewById(R.id.cardSettings);

        // 2. LOAD YOUR WEATHER FRAGMENT
        // This runs the code inside your 'WeatherFragment.java' file
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_weather, new WeatherFragment())
                    .commit();
        }

        // 3. Firebase Name Logic
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            // Use the URL from your screenshot
            String dbUrl = "https://ffhr-3c167-default-rtdb.firebaseio.com/";
            mDatabase = FirebaseDatabase.getInstance(dbUrl).getReference("users").child(uid);

            mDatabase.child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        String name = snapshot.getValue(String.class);
                        tvGreeting.setText("WELCOME " + name.toUpperCase());
                    } else {
                        tvGreeting.setText("WELCOME FARMER");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        // 4. Logout Logic
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        setupGridListeners();
    }

    private void setupGridListeners() {
        cardHealthReport.setOnClickListener(v -> Toast.makeText(this, "Health Report", Toast.LENGTH_SHORT).show());

        // REPLACE THIS LINE:
        cardScanLeaf.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LeafScanActivity.class);
            startActivity(intent);
        });

        cardFertilizer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FertilizerCalculatorActivity.class);
            startActivity(intent);
        });

        cardAddFarm.setOnClickListener(v -> Toast.makeText(this, "Add Farm", Toast.LENGTH_SHORT).show());
        cardSoilStatus.setOnClickListener(v -> Toast.makeText(this, "Soil Status", Toast.LENGTH_SHORT).show());
        cardSettings.setOnClickListener(v -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show());
    }
}