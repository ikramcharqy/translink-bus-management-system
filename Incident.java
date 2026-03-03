package com.sample.demo3.models;

public class Incident {
    private String Id;
    private String type;
    private String description;
    private String date;
    private String status;
    private String assignedTo;
    private String driverId;
    private String location;

    public Incident(String s, String mechanical, String string, String date, String resolved) {
        // Constructeur vide nécessaire pour Firestore
    }

    public Incident(String Id, String type, String description, String date,
                    String status, String assignedTo, String driverId, String location) {
        this.Id = Id;
        this.type = type;
        this.description = description;
        this.date = date;
        this.status = status;
        this.assignedTo = assignedTo;
        this.driverId = driverId;
        this.location = location;
    }

    // Getters et setters
    public String getId() { return Id; }
    public void setId(String id) { this.Id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}