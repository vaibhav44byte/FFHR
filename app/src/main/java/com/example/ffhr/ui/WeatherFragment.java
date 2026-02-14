package com.example.ffhr.ui; // IMPORTANT: Change to your package name

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ffhr.R;
import com.example.ffhr.network.WeatherApi;
import com.example.ffhr.models.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {

    // Your actual API Key is inserted here
    private static final String API_KEY = "195958cf88c37bb39e4318484bf8f224";
    private static final String BASE_URL = "https://api.openweathermap.org/";

    private TextView tvLocation, tvTemp, tvHumidity, tvWind;
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        // 1. Link the Java code to your XML layout
        tvLocation = view.findViewById(R.id.tvLocation);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWind = view.findViewById(R.id.tvWind);

        // 2. Setup the location tool
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 3. Start the process
        getLocationAndFetchWeather();

        return view;
    }

    private void getLocationAndFetchWeather() {
        // Check if we have permission to use GPS
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If not, ask the user for permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        // Get the last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                // We found the location! Now fetch the weather.
                fetchWeather(location.getLatitude(), location.getLongitude());
            } else {
                tvLocation.setText("GPS signal lost");
                Toast.makeText(getContext(), "Make sure GPS is ON", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchWeather(double lat, double lon) {
        // Build the Retrofit network tool
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);

        // Send the request using your API Key and Metric units (Celsius)
        Call<WeatherResponse> call = api.getCurrentWeather(lat, lon, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();

                    // Update the UI with real data
                    tvLocation.setText(data.name); // City name
                    tvTemp.setText(Math.round(data.main.temp) + "°C");
                    tvHumidity.setText("Humidity: " + data.main.humidity + "%");
                    tvWind.setText("Wind: " + data.wind.speed + " m/s");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvLocation.setText("Network Error");
            }
        });
    }

    // Handle what happens after the user clicks "Allow" on the permission dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                tvLocation.setText("Permission Denied");
            }
        }
    }
}