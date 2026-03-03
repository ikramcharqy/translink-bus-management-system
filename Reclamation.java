package com.sample.demo3.models;

public class Reclamation {
    private String id;
    private String date;
    private String customer;
    private String type;
    private String description;
    private String status;

    public Reclamation(String id, String date, String customer, String type, String description, String status) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.type = type;
        this.description = description;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getCustomer() { return customer; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}