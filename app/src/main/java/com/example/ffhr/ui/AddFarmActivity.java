package com.example.ffhr.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ffhr.R;
import com.example.ffhr.models.FarmModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class AddFarmActivity extends AppCompatActivity {

    private EditText etFarmName, etVillage, etArea;
    private Spinner spCropType, spSoilQuality, spIrrigationType;
    private TextView tvLatitude, tvLongitude, tvLocationStatus;
    private Button btnCaptureLocation, btnSaveFarm;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth auth;
    private DatabaseReference farmsRef;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm);

        bindViews();
        setupSpinners();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        auth = FirebaseAuth.getInstance();
        farmsRef = FirebaseDatabase.getInstance("https://ffhr-3c167-default-rtdb.firebaseio.com/")
                .getReference("farms");

        btnCaptureLocation.setOnClickListener(v -> captureLocation());
        btnSaveFarm.setOnClickListener(v -> saveFarm());
    }

    private void bindViews() {
        etFarmName = findViewById(R.id.etFarmName);
        etVillage = findViewById(R.id.etVillage);
        etArea = findViewById(R.id.etArea);

        spCropType = findViewById(R.id.spCropType);
        spSoilQuality = findViewById(R.id.spSoilQuality);
        spIrrigationType = findViewById(R.id.spIrrigationType);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        btnCaptureLocation = findViewById(R.id.btnCaptureLocation);
        btnSaveFarm = findViewById(R.id.btnSaveFarm);
    }

    private void setupSpinners() {
        String[] crops = {"Wheat", "Rice", "Maize", "Cotton", "Tomato", "Potato", "Sugarcane"};
        String[] soilLevels = {"Low", "Medium", "High"};
        String[] irrigationTypes = {"Rainfed", "Drip", "Canal", "Borewell"};

        spCropType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops));
        spSoilQuality.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, soilLevels));
        spIrrigationType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, irrigationTypes));
    }

    private void captureLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        tvLocationStatus.setText("Fetching live farm location...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                tvLatitude.setText(String.format(Locale.US, "%.6f", currentLatitude));
                tvLongitude.setText(String.format(Locale.US, "%.6f", currentLongitude));
                tvLocationStatus.setText("Location captured successfully.");
            } else {
                tvLocationStatus.setText("Unable to fetch location. Turn on GPS and try again.");
            }
        });
    }

    private void saveFarm() {
        String farmName = etFarmName.getText().toString().trim();
        String village = etVillage.getText().toString().trim();
        String areaText = etArea.getText().toString().trim();

        if (TextUtils.isEmpty(farmName)) {
            etFarmName.setError("Farm name is required");
            etFarmName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(village)) {
            etVillage.setError("Village / locality is required");
            etVillage.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(areaText)) {
            etArea.setError("Estimated area is required");
            etArea.requestFocus();
            return;
        }

        if (currentLatitude == 0.0 && currentLongitude == 0.0) {
            Toast.makeText(this, "Please capture the farm location first", Toast.LENGTH_SHORT).show();
            return;
        }

        double area;
        try {
            area = Double.parseDouble(areaText);
        } catch (NumberFormatException e) {
            etArea.setError("Enter a valid area");
            etArea.requestFocus();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String farmId = farmsRef.push().getKey();
        if (farmId == null) {
            Toast.makeText(this, "Unable to create farm record", Toast.LENGTH_SHORT).show();
            return;
        }

        FarmModel farm = new FarmModel(
                farmId,
                currentUser.getUid(),
                farmName,
                spCropType.getSelectedItem().toString(),
                spSoilQuality.getSelectedItem().toString(),
                spIrrigationType.getSelectedItem().toString(),
                village,
                area,
                currentLatitude,
                currentLongitude
        );

        farmsRef.child(currentUser.getUid()).child(farmId).setValue(farm)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Farm added successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void clearForm() {
        etFarmName.setText("");
        etVillage.setText("");
        etArea.setText("");
        tvLatitude.setText("--");
        tvLongitude.setText("--");
        tvLocationStatus.setText("Capture your farm's live location");
        currentLatitude = 0.0;
        currentLongitude = 0.0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureLocation();
        } else if (requestCode == 101) {
            tvLocationStatus.setText("Location permission denied");
        }
    }
}
