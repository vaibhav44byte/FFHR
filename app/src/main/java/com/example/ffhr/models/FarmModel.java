package com.example.ffhr.models;

import java.util.List;

public class FarmModel {

    public String farmId;
    public String userId;
    public String farmName;
    public String cropType;
    public String soilQuality;
    public String irrigationType;
    public String village;
    public String fullAddress;

    public double estimatedAreaAcres;
    public double boundaryAreaAcres;
    public double boundaryAreaSqMeters;
    public double centerLatitude;
    public double centerLongitude;

    public List<BoundaryPointModel> boundaryPoints;

    public long createdAt;
    public long updatedAt;

    public FarmModel() {
    }

    public FarmModel(String farmId, String userId, String farmName, String cropType,
                     String soilQuality, String irrigationType, String village,
                     String fullAddress, double estimatedAreaAcres,
                     double boundaryAreaAcres, double boundaryAreaSqMeters,
                     double centerLatitude, double centerLongitude,
                     List<BoundaryPointModel> boundaryPoints,
                     long createdAt, long updatedAt) {
        this.farmId = farmId;
        this.userId = userId;
        this.farmName = farmName;
        this.cropType = cropType;
        this.soilQuality = soilQuality;
        this.irrigationType = irrigationType;
        this.village = village;
        this.fullAddress = fullAddress;
        this.estimatedAreaAcres = estimatedAreaAcres;
        this.boundaryAreaAcres = boundaryAreaAcres;
        this.boundaryAreaSqMeters = boundaryAreaSqMeters;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.boundaryPoints = boundaryPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
