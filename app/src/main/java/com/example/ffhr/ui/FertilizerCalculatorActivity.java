package com.example.ffhr.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FertilizerCalculatorActivity extends AppCompatActivity {

    private Spinner spCrop;
    private Spinner spStage;
    private Spinner spSoilQuality;
    private Spinner spAreaUnit;
    private EditText etArea;
    private EditText etNitrogen;
    private EditText etPhosphorus;
    private EditText etPotassium;
    private Button btnLoadPreset;
    private Button btnCalculate;
    private TextView tvPresetHint;
    private TextView tvResult;

    private final Map<String, double[]> cropPresets = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_calculator);

        bindViews();
        setupPresetData();
        setupSpinners();
        setupListeners();
        loadPresetValues();
    }

    private void bindViews() {
        spCrop = findViewById(R.id.spCrop);
        spStage = findViewById(R.id.spStage);
        spSoilQuality = findViewById(R.id.spSoilQuality);
        spAreaUnit = findViewById(R.id.spAreaUnit);

        etArea = findViewById(R.id.etArea);
        etNitrogen = findViewById(R.id.etNitrogen);
        etPhosphorus = findViewById(R.id.etPhosphorus);
        etPotassium = findViewById(R.id.etPotassium);

        btnLoadPreset = findViewById(R.id.btnLoadPreset);
        btnCalculate = findViewById(R.id.btnCalculate);

        tvPresetHint = findViewById(R.id.tvPresetHint);
        tvResult = findViewById(R.id.tvResult);
    }

    private void setupPresetData() {
        cropPresets.put("Wheat_Basal", new double[]{50, 25, 20});
        cropPresets.put("Wheat_Top Dress", new double[]{30, 0, 0});

        cropPresets.put("Rice_Basal", new double[]{40, 20, 20});
        cropPresets.put("Rice_Top Dress", new double[]{35, 0, 0});

        cropPresets.put("Maize_Basal", new double[]{60, 30, 20});
        cropPresets.put("Maize_Top Dress", new double[]{40, 0, 0});

        cropPresets.put("Cotton_Basal", new double[]{40, 20, 20});
        cropPresets.put("Cotton_Top Dress", new double[]{30, 0, 10});

        cropPresets.put("Tomato_Basal", new double[]{50, 25, 25});
        cropPresets.put("Tomato_Flowering", new double[]{25, 10, 20});

        cropPresets.put("Potato_Basal", new double[]{55, 30, 25});
        cropPresets.put("Potato_Tuber Development", new double[]{20, 0, 20});

        cropPresets.put("Sugarcane_Basal", new double[]{70, 30, 25});
        cropPresets.put("Sugarcane_Top Dress", new double[]{50, 0, 0});
    }

    private void setupSpinners() {
        String[] crops = {"Wheat", "Rice", "Maize", "Cotton", "Tomato", "Potato", "Sugarcane"};
        String[] stages = {"Basal", "Top Dress", "Flowering", "Tuber Development"};
        String[] soilQualities = {"Low", "Medium", "High"};
        String[] units = {"Acre", "Hectare"};

        spCrop.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops));
        spStage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stages));
        spSoilQuality.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, soilQualities));
        spAreaUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));
    }

    private void setupListeners() {
        btnLoadPreset.setOnClickListener(v -> loadPresetValues());
        btnCalculate.setOnClickListener(v -> calculateFertilizer());
    }

    private void loadPresetValues() {
        String key = spCrop.getSelectedItem().toString() + "_" + spStage.getSelectedItem().toString();
        double[] values = cropPresets.get(key);

        if (values == null) {
            values = new double[]{40, 20, 20};
        }

        String soilQuality = spSoilQuality.getSelectedItem() != null
                ? spSoilQuality.getSelectedItem().toString()
                : "Medium";

        double factor = getSoilFactor(soilQuality);

        double adjustedN = values[0] * factor;
        double adjustedP = values[1] * factor;
        double adjustedK = values[2] * factor;

        etNitrogen.setText(formatNumber(adjustedN));
        etPhosphorus.setText(formatNumber(adjustedP));
        etPotassium.setText(formatNumber(adjustedK));

        tvPresetHint.setText(
                "Dose adjusted for " + soilQuality.toLowerCase(Locale.US)
                        + " soil quality. You can still edit N, P, K manually."
        );
    }

    private double getSoilFactor(String soilQuality) {
        if ("Low".equalsIgnoreCase(soilQuality)) {
            return 1.0;
        }
        if ("Medium".equalsIgnoreCase(soilQuality)) {
            return 0.8;
        }
        return 0.6;
    }

    private void calculateFertilizer() {
        String areaText = etArea.getText().toString().trim();
        String nText = etNitrogen.getText().toString().trim();
        String pText = etPhosphorus.getText().toString().trim();
        String kText = etPotassium.getText().toString().trim();

        if (TextUtils.isEmpty(areaText) || TextUtils.isEmpty(nText)
                || TextUtils.isEmpty(pText) || TextUtils.isEmpty(kText)) {
            Toast.makeText(this, "Please fill area and NPK values", Toast.LENGTH_SHORT).show();
            return;
        }

        double area;
        double nDose;
        double pDose;
        double kDose;

        try {
            area = Double.parseDouble(areaText);
            nDose = Double.parseDouble(nText);
            pDose = Double.parseDouble(pText);
            kDose = Double.parseDouble(kText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (area <= 0) {
            Toast.makeText(this, "Area must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        double areaInAcre = spAreaUnit.getSelectedItem().toString().equals("Hectare")
                ? area * 2.471
                : area;

        double totalN = nDose * areaInAcre;
        double totalP = pDose * areaInAcre;
        double totalK = kDose * areaInAcre;

        double dapKg = totalP / 0.46;
        double nitrogenFromDap = dapKg * 0.18;
        double remainingN = Math.max(totalN - nitrogenFromDap, 0);
        double ureaKg = remainingN / 0.46;
        double mopKg = totalK / 0.60;

        String crop = spCrop.getSelectedItem().toString();
        String stage = spStage.getSelectedItem().toString();
        String soilQuality = spSoilQuality.getSelectedItem().toString();
        String areaUnit = spAreaUnit.getSelectedItem().toString();

        String result = "Crop: " + crop + "\n"
                + "Stage: " + stage + "\n"
                + "Soil quality: " + soilQuality + "\n"
                + String.format(Locale.US, "Area: %.2f %s (%.2f acre)\n\n", area, areaUnit, areaInAcre)

                + "Minimum nutrient requirement\n"
                + String.format(Locale.US, "Nitrogen (N): %.2f kg\n", totalN)
                + String.format(Locale.US, "Phosphorus (P): %.2f kg\n", totalP)
                + String.format(Locale.US, "Potassium (K): %.2f kg\n\n", totalK)

                + "Recommended fertilizer mix\n"
                + String.format(Locale.US, "DAP: %.2f kg\n", dapKg)
                + String.format(Locale.US, "Urea: %.2f kg\n", ureaKg)
                + String.format(Locale.US, "MOP: %.2f kg\n\n", mopKg)

                + "Suggested split\n"
                + String.format(Locale.US, "First application: DAP %.2f kg + Urea %.2f kg + MOP %.2f kg\n",
                dapKg, ureaKg * 0.5, mopKg * 0.5)
                + String.format(Locale.US, "Second application: Urea %.2f kg + MOP %.2f kg after 10-15 days\n\n",
                ureaKg * 0.5, mopKg * 0.5)

                + "Farmer note\n"
                + "High soil quality gets the lowest safe dose, medium gets moderate dose, and low gets full recommended dose.";

        tvResult.setText(result);
    }

    private String formatNumber(double value) {
        return String.format(Locale.US, "%.0f", value);
    }
}
