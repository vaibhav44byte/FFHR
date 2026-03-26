package com.example.ffhr.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ffhr.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FertilizerCalculatorActivity extends AppCompatActivity {

    private Spinner spCrop, spCategory, spProduct, spAreaUnit;
    private EditText etArea, etDoseValue, etTankSize, etSprayVolumePerAcre;
    private RadioGroup rgMode;
    private RadioButton rbBroadcast, rbSpray;
    private Button btnCalculate;
    private TextView tvResult, tvDoseHint;

    private final Map<String, String[]> productMap = new HashMap<>();
    private final Map<String, Double> defaultDoseMap = new HashMap<>(); // per acre (kg or ml)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_calculator);

        bindViews();
        setupData();
        setupListeners();
        updateProductsForSelectedCategory();
        updateModeUI();
    }

    private void bindViews() {
        spCrop = findViewById(R.id.spCrop);
        spCategory = findViewById(R.id.spCategory);
        spProduct = findViewById(R.id.spProduct);
        spAreaUnit = findViewById(R.id.spAreaUnit);

        etArea = findViewById(R.id.etArea);
        etDoseValue = findViewById(R.id.etDoseValue);
        etTankSize = findViewById(R.id.etTankSize);
        etSprayVolumePerAcre = findViewById(R.id.etSprayVolumePerAcre);

        rgMode = findViewById(R.id.rgMode);
        rbBroadcast = findViewById(R.id.rbBroadcast);
        rbSpray = findViewById(R.id.rbSpray);

        btnCalculate = findViewById(R.id.btnCalculate);
        tvResult = findViewById(R.id.tvResult);
        tvDoseHint = findViewById(R.id.tvDoseHint);
    }

    private void setupData() {
        String[] crops = {"Wheat", "Rice", "Maize", "Cotton", "Tomato", "Potato", "Sugarcane"};
        String[] categories = {"Fertilizer", "Pesticide/Fungicide"};
        String[] areaUnits = {"Acre", "Hectare"};

        productMap.put("Fertilizer", new String[]{"Urea", "DAP", "MOP", "NPK 19:19:19"});
        productMap.put("Pesticide/Fungicide", new String[]{"Neem Oil", "Mancozeb", "Imidacloprid", "Chlorpyrifos"});

        // Baseline suggestion values (editable by farmer)
        defaultDoseMap.put("Urea", 45.0);
        defaultDoseMap.put("DAP", 50.0);
        defaultDoseMap.put("MOP", 20.0);
        defaultDoseMap.put("NPK 19:19:19", 25.0);
        defaultDoseMap.put("Neem Oil", 500.0);
        defaultDoseMap.put("Mancozeb", 600.0);
        defaultDoseMap.put("Imidacloprid", 100.0);
        defaultDoseMap.put("Chlorpyrifos", 400.0);

        spCrop.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops));
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        spAreaUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, areaUnits));
    }

    private void setupListeners() {
        spCategory.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                updateProductsForSelectedCategory();
            }
        });

        spProduct.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                applyDefaultDoseHint();
            }
        });

        rgMode.setOnCheckedChangeListener((group, checkedId) -> updateModeUI());

        btnCalculate.setOnClickListener(v -> calculate());
    }

    private void updateProductsForSelectedCategory() {
        String category = spCategory.getSelectedItem().toString();
        String[] products = productMap.get(category);
        if (products == null) products = new String[0];

        spProduct.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, products));
        applyDefaultDoseHint();
    }

    private void applyDefaultDoseHint() {
        if (spProduct.getSelectedItem() == null) return;
        String product = spProduct.getSelectedItem().toString();
        Double suggested = defaultDoseMap.get(product);

        if (suggested == null) suggested = 0.0;

        if (rbBroadcast.isChecked()) {
            tvDoseHint.setText(String.format(Locale.US, "Suggested dose: %.0f kg or ml per acre (editable)", suggested));
            etDoseValue.setHint("Enter dose per acre (kg/ml)");
        } else {
            tvDoseHint.setText("Suggested spray concentration: start with label recommendation");
            etDoseValue.setHint("Enter concentration (ml per liter)");
        }
    }

    private void updateModeUI() {
        boolean sprayMode = rbSpray.isChecked();
        etTankSize.setVisibility(sprayMode ? View.VISIBLE : View.GONE);
        etSprayVolumePerAcre.setVisibility(sprayMode ? View.VISIBLE : View.GONE);
        applyDefaultDoseHint();
    }

    private void calculate() {
        String areaText = etArea.getText().toString().trim();
        String doseText = etDoseValue.getText().toString().trim();

        if (TextUtils.isEmpty(areaText) || TextUtils.isEmpty(doseText)) {
            Toast.makeText(this, "Enter area and dose values", Toast.LENGTH_SHORT).show();
            return;
        }

        double area;
        double doseValue;
        try {
            area = Double.parseDouble(areaText);
            doseValue = Double.parseDouble(doseText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric input", Toast.LENGTH_SHORT).show();
            return;
        }

        if (area <= 0 || doseValue <= 0) {
            Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        String crop = spCrop.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();
        String product = spProduct.getSelectedItem().toString();
        String areaUnit = spAreaUnit.getSelectedItem().toString();

        double areaInAcre = areaUnit.equals("Hectare") ? area * 2.471 : area;

        if (rbBroadcast.isChecked()) {
            double totalProduct = areaInAcre * doseValue;
            String unit = category.equals("Fertilizer") ? "kg" : "ml";

            String result = "Crop: " + crop + "\n"
                    + "Category: " + category + "\n"
                    + "Product: " + product + "\n"
                    + String.format(Locale.US, "Area: %.2f %s (%.2f acre)\n", area, areaUnit, areaInAcre)
                    + String.format(Locale.US, "Dose used: %.2f %s/acre\n", doseValue, unit)
                    + String.format(Locale.US, "Total required: %.2f %s\n", totalProduct, unit)
                    + String.format(Locale.US, "Split plan: %.2f %s now + %.2f %s after 10-14 days",
                    totalProduct * 0.6, unit, totalProduct * 0.4, unit);

            tvResult.setText(result);
            return;
        }

        String tankText = etTankSize.getText().toString().trim();
        String sprayVolText = etSprayVolumePerAcre.getText().toString().trim();

        if (TextUtils.isEmpty(tankText) || TextUtils.isEmpty(sprayVolText)) {
            Toast.makeText(this, "Enter tank size and spray volume per acre", Toast.LENGTH_SHORT).show();
            return;
        }

        double tankSize;
        double sprayVolumePerAcre;
        try {
            tankSize = Double.parseDouble(tankText);
            sprayVolumePerAcre = Double.parseDouble(sprayVolText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid spray values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tankSize <= 0 || sprayVolumePerAcre <= 0) {
            Toast.makeText(this, "Spray values must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalWater = areaInAcre * sprayVolumePerAcre;
        double tanksNeeded = Math.ceil(totalWater / tankSize);
        double productPerTank = doseValue * tankSize; // ml/L * L = ml
        double totalProduct = doseValue * totalWater;

        String result = "Crop: " + crop + "\n"
                + "Category: " + category + "\n"
                + "Product: " + product + "\n"
                + String.format(Locale.US, "Area: %.2f %s (%.2f acre)\n", area, areaUnit, areaInAcre)
                + String.format(Locale.US, "Concentration: %.2f ml/L\n", doseValue)
                + String.format(Locale.US, "Tank size: %.2f L\n", tankSize)
                + String.format(Locale.US, "Total water required: %.2f L\n", totalWater)
                + String.format(Locale.US, "Tanks required: %.0f\n", tanksNeeded)
                + String.format(Locale.US, "Product per tank: %.2f ml\n", productPerTank)
                + String.format(Locale.US, "Total product required: %.2f ml\n", totalProduct)
                + "Safety: Spray in low wind, wear mask/gloves, avoid mixing unknown chemicals.";

        tvResult.setText(result);
    }

    // Tiny helper to avoid boilerplate listener class
    public abstract static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        public abstract void onSelected();

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            onSelected();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
