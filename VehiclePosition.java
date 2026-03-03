package com.sample.demo3.models;

public class VehiclePosition {
    private String vehicleId;
    private double latitude;
    private double longitude;
    private double speed;
    private String timestamp;
    private String status;

    // Constructeur par défaut
    public VehiclePosition() {
    }

    // Constructeur avec tous les paramètres
    public VehiclePosition(String vehicleId, String coachId, String driverId, double latitude, double longitude,
                           double speed, String timestamp, String status, String s) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters
    public String getVehicleId() {
        return vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}