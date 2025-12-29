package com.example.ffhr;
import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    public Main main;

    @SerializedName("wind")
    public Wind wind;

    @SerializedName("name")
    public String name;

    // Inner class for temperature and humidity
    public static class Main {
        @SerializedName("temp")
        public float temp;

        @SerializedName("humidity")
        public int humidity;
    }

    // Inner class for wind speed
    public static class Wind {
        @SerializedName("speed")
        public float speed;
    }
}
