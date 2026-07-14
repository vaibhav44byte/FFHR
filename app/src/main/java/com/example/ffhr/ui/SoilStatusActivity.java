package com.example.ffhr.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SoilStatusActivity extends AppCompatActivity {

    private Spinner spCycle, spState, spDistrict;
    private Button btnLoadSoilStatus;
    private TextView tvSoilResult;

    private DatabaseReference soilBaselineRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_status);

        spCycle = findViewById(R.id.spCycle);
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        btnLoadSoilStatus = findViewById(R.id.btnLoadSoilStatus);
        tvSoilResult = findViewById(R.id.tvSoilResult);

        soilBaselineRef = FirebaseDatabase.getInstance("https://ffhr-3c167-default-rtdb.firebaseio.com/")
                .getReference("soil_health_baseline");

        setupCycleSpinner();

        spCycle.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected() {
                loadStates();
            }
        });

        spState.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected() {
                loadDistricts();
            }
        });

        btnLoadSoilStatus.setOnClickListener(v -> loadSoilStatus());
        loadStates();
    }

    private void setupCycleSpinner() {
        String[] cycles = {"2025-26", "2024-25", "2023-24"};
        spCycle.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cycles));
    }

    private void loadStates() {
        String cycle = getSelectedCycle();
        tvSoilResult.setText("Loading states for cycle: " + cycle);

        soilBaselineRef.child(cycle).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> states = new ArrayList<>();

                for (DataSnapshot stateSnap : snapshot.getChildren()) {
                    states.add(stateSnap.getKey());
                }

                if (states.isEmpty()) {
                    tvSoilResult.setText("No states found for cycle " + cycle);
                    return;
                }

                spState.setAdapter(new ArrayAdapter<>(SoilStatusActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, states));

                loadDistricts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SoilStatusActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts() {
        String cycle = getSelectedCycle();
        String state = getSelectedState();

        if (TextUtils.isEmpty(state)) return;

        soilBaselineRef.child(cycle).child(state)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> districts = new ArrayList<>();

                        for (DataSnapshot districtSnap : snapshot.getChildren()) {
                            districts.add(districtSnap.getKey());
                        }

                        spDistrict.setAdapter(new ArrayAdapter<>(SoilStatusActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, districts));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SoilStatusActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSoilStatus() {
        String cycle = getSelectedCycle();
        String state = getSelectedState();
        String district = getSelectedDistrict();

        if (TextUtils.isEmpty(state) || TextUtils.isEmpty(district)) {
            Toast.makeText(this, "Please select state and district", Toast.LENGTH_SHORT).show();
            return;
        }

        tvSoilResult.setText("Loading soil profile...");

        soilBaselineRef.child(cycle).child(state).child(district)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            tvSoilResult.setText("No soil profile found for selected district.");
                            return;
                        }

                        double nLow = getDouble(snapshot, "nLow");
                        double nMedium = getDouble(snapshot, "nMedium");
                        double nHigh = getDouble(snapshot, "nHigh");

                        double pLow = getDouble(snapshot, "pLow");
                        double pMedium = getDouble(snapshot, "pMedium");
                        double pHigh = getDouble(snapshot, "pHigh");

                        double kLow = getDouble(snapshot, "kLow");
                        double kMedium = getDouble(snapshot, "kMedium");
                        double kHigh = getDouble(snapshot, "kHigh");

                        double ocLow = getDouble(snapshot, "organicCarbonLow");
                        double ocMedium = getDouble(snapshot, "organicCarbonMedium");
                        double ocHigh = getDouble(snapshot, "organicCarbonHigh");

                        double phAcidic = getDouble(snapshot, "phAcidic");
                        double phNeutral = getDouble(snapshot, "phNeutral");
                        double phAlkaline = getDouble(snapshot, "phAlkaline");

                        double ecNonSaline = getDouble(snapshot, "ecNonSaline");
                        double ecSaline = getDouble(snapshot, "ecSaline");

                        double znSufficient = getDouble(snapshot, "znSufficient");
                        double znDeficient = getDouble(snapshot, "znDeficient");

                        double feSufficient = getDouble(snapshot, "feSufficient");
                        double feDeficient = getDouble(snapshot, "feDeficient");

                        double bSufficient = getDouble(snapshot, "bSufficient");
                        double bDeficient = getDouble(snapshot, "bDeficient");

                        String result = "Cycle: " + cycle + "\n"
                                + "State: " + state + "\n"
                                + "District: " + district + "\n\n"

                                + "Nitrogen: " + getLevelLabel(nLow, nMedium, nHigh)
                                + String.format(Locale.US, " (Low %.2f%% / Medium %.2f%% / High %.2f%%)\n", nLow, nMedium, nHigh)

                                + "Phosphorus: " + getLevelLabel(pLow, pMedium, pHigh)
                                + String.format(Locale.US, " (Low %.2f%% / Medium %.2f%% / High %.2f%%)\n", pLow, pMedium, pHigh)

                                + "Potassium: " + getLevelLabel(kLow, kMedium, kHigh)
                                + String.format(Locale.US, " (Low %.2f%% / Medium %.2f%% / High %.2f%%)\n\n", kLow, kMedium, kHigh)

                                + "Organic Carbon: " + getLevelLabel(ocLow, ocMedium, ocHigh) + "\n"
                                + "pH Trend: " + getPhLabel(phAcidic, phNeutral, phAlkaline) + "\n"
                                + "Salinity: " + (ecNonSaline >= ecSaline ? "Mostly Non-Saline" : "Salinity Risk") + "\n\n"

                                + "Zinc: " + (znDeficient > znSufficient ? "Deficiency Risk" : "Mostly Sufficient") + "\n"
                                + "Iron: " + (feDeficient > feSufficient ? "Deficiency Risk" : "Mostly Sufficient") + "\n"
                                + "Boron: " + (bDeficient > bSufficient ? "Deficiency Risk" : "Mostly Sufficient") + "\n\n"

                                + "Note: This is a district-level estimated soil profile, not a direct lab test.";

                        tvSoilResult.setText(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvSoilResult.setText("Error: " + error.getMessage());
                    }
                });
    }

    private double getDouble(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private String getLevelLabel(double low, double medium, double high) {
        if (low >= medium && low >= high) return "Likely Low";
        if (medium >= low && medium >= high) return "Likely Medium";
        return "Likely High";
    }

    private String getPhLabel(double acidic, double neutral, double alkaline) {
        if (acidic >= neutral && acidic >= alkaline) return "Mostly Acidic";
        if (neutral >= acidic && neutral >= alkaline) return "Mostly Neutral";
        return "Mostly Alkaline";
    }

    private String getSelectedCycle() {
        return spCycle.getSelectedItem() != null ? spCycle.getSelectedItem().toString() : "2025-26";
    }

    private String getSelectedState() {
        return spState.getSelectedItem() != null ? spState.getSelectedItem().toString() : "";
    }

    private String getSelectedDistrict() {
        return spDistrict.getSelectedItem() != null ? spDistrict.getSelectedItem().toString() : "";
    }

    public abstract static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        public abstract void onItemSelected();

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
            onItemSelected();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}