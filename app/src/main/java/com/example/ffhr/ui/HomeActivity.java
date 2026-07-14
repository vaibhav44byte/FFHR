package com.example.ffhr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
    ImageView btnProfile;
    CardView cardHealthReport, cardScanLeaf, cardFertilizer, cardAddFarm, cardSoilStatus, cardSettings;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvGreeting = findViewById(R.id.tvGreeting);
        btnProfile = findViewById(R.id.btn_profile);

        cardHealthReport = findViewById(R.id.cardHealthReport);
        cardScanLeaf = findViewById(R.id.cardScanLeaf);
        cardFertilizer = findViewById(R.id.cardFertilizer);
        cardAddFarm = findViewById(R.id.cardAddFarm);
        cardSoilStatus = findViewById(R.id.cardSoilStatus);
        cardSettings = findViewById(R.id.cardSettings);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_weather, new WeatherFragment())
                    .commit();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
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
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        btnProfile.setOnClickListener(v -> showProfileMenu());
        setupGridListeners();
    }

    private void showProfileMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnProfile);
        popupMenu.getMenuInflater().inflate(R.menu.menu_profile, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(this::handleProfileMenuClick);
        popupMenu.show();
    }

    private boolean handleProfileMenuClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_my_profile) {
            Toast.makeText(this, "My Profile", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_more_options) {
            Toast.makeText(this, "More Options", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_logout) {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return false;
    }

    private void setupGridListeners() {
        cardHealthReport.setOnClickListener(v ->
                Toast.makeText(this, "Health Report", Toast.LENGTH_SHORT).show());

        cardScanLeaf.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LeafScanActivity.class);
            startActivity(intent);
        });

        cardFertilizer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FertilizerCalculatorActivity.class);
            startActivity(intent);
        });

        cardAddFarm.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFarmActivity.class);
            startActivity(intent);
        });


        cardSoilStatus.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SoilStatusActivity.class);
            startActivity(intent);
        });

        cardSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show());
    }
}
