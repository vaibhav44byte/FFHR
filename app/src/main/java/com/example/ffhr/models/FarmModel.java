package com.example.ffhr.models;

public class FarmModel {

    public String farmId;
    public String userId;
    public String farmName;
    public String cropType;
    public String soilQuality;
    public String irrigationType;
    public String village;
    public double area;
    public double latitude;
    public double longitude;

    public FarmModel() {
    }

    public FarmModel(String farmId, String userId, String farmName, String cropType,
                     String soilQuality, String irrigationType, String village,
                     double area, double latitude, double longitude) {
        this.farmId = farmId;
        this.userId = userId;
        this.farmName = farmName;
        this.cropType = cropType;
        this.soilQuality = soilQuality;
        this.irrigationType = irrigationType;
        this.village = village;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
