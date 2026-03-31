package com.example.ffhr.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ffhr.R;
import com.example.ffhr.models.WeatherResponse;
import com.example.ffhr.network.WeatherApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {

    private static final String API_KEY = "195958cf88c37bb39e4318484bf8f224";
    private static final String BASE_URL = "https://api.openweathermap.org/";

    private TextView tvLocation;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvCondition;
    private TextView tvRainRisk;
    private ImageView ivWeatherIcon;

    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        tvLocation = view.findViewById(R.id.tvLocation);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWind = view.findViewById(R.id.tvWind);
        tvCondition = view.findViewById(R.id.tvCondition);
        tvRainRisk = view.findViewById(R.id.tvRainRisk);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLocationAndFetchWeather();

        return view;
    }

    private void getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                fetchWeather(location.getLatitude(), location.getLongitude());
            } else {
                tvLocation.setText("GPS signal lost");
                tvCondition.setText("Turn on location services");
                Toast.makeText(getContext(), "Make sure GPS is ON", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchWeather(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);
        Call<WeatherResponse> call = api.getCurrentWeather(lat, lon, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();

                    String condition = "Weather insight ready";
                    if (data.weather != null && data.weather.length > 0 && data.weather[0] != null) {
                        condition = capitalizeWords(data.weather[0].description);
                        updateWeatherIcon(data.weather[0].main);
                    } else {
                        updateWeatherIcon("Clear");
                    }

                    tvLocation.setText(data.name != null ? data.name : "Current location");
                    tvTemp.setText(String.format(Locale.US, "%d°C", Math.round(data.main.temp)));
                    tvHumidity.setText(data.main.humidity + "%");
                    tvWind.setText(String.format(Locale.US, "%.1f m/s", data.wind.speed));
                    tvCondition.setText(condition);
                    tvRainRisk.setText(getRainRisk(data.main.humidity, data.weather != null && data.weather.length > 0 ? data.weather[0].main : ""));
                } else {
                    tvLocation.setText("Weather unavailable");
                    tvCondition.setText("Unable to load latest conditions");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvLocation.setText("Network Error");
                tvCondition.setText("Check internet and try again");
            }
        });
    }

    private String getRainRisk(int humidity, String mainCondition) {
        String condition = mainCondition == null ? "" : mainCondition.toLowerCase(Locale.US);

        if (condition.contains("rain") || condition.contains("storm") || humidity >= 85) {
            return "High";
        }

        if (condition.contains("cloud") || humidity >= 65) {
            return "Medium";
        }

        return "Low";
    }

    private void updateWeatherIcon(String mainCondition) {
        String condition = mainCondition == null ? "" : mainCondition.toLowerCase(Locale.US);

        if (condition.contains("rain") || condition.contains("drizzle")) {
            ivWeatherIcon.setImageResource(android.R.drawable.ic_menu_upload);
        } else if (condition.contains("cloud")) {
            ivWeatherIcon.setImageResource(android.R.drawable.ic_menu_gallery);
        } else if (condition.contains("storm") || condition.contains("thunder")) {
            ivWeatherIcon.setImageResource(android.R.drawable.ic_dialog_alert);
        } else {
            ivWeatherIcon.setImageResource(android.R.drawable.ic_menu_compass);
        }
    }

    private String capitalizeWords(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Weather insight ready";
        }

        String[] parts = value.toLowerCase(Locale.US).split(" ");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }

        return builder.toString().trim();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                tvLocation.setText("Permission Denied");
                tvCondition.setText("Location permission is needed");
            }
        }
    }
}
