package com.sample.demo3.models;

public class Coach {
    private String id;
    private String model;
    private String capacity;
    private String licensePlate;
    private String status;

    public Coach(String id, String model, String capacity, String licensePlate, String status) {
        this.id = id;
        this.model = model;
        this.capacity = capacity;
        this.licensePlate = licensePlate;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getModel() { return model; }
    public String getCapacity() { return capacity; }
    public String getLicensePlate() { return licensePlate; }
    public String getStatus() { return status; }

    // Setters
    public void setModel(String model) { this.model = model; }
    public void setCapacity(String capacity) { this.capacity = capacity; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setStatus(String status) { this.status = status; }
}