package com.sample.demo3.models;

public class Agency {
    private String agencyId;
    private String name;
    private String location;
    private String manager;
    private String phone;

    public Agency(String agencyId, String name, String location, String manager, String phone) {
        this.agencyId = agencyId;
        this.name = name;
        this.location = location;
        this.manager = manager;
        this.phone = phone;
    }

    // Getters
    public String getAgencyId() { return agencyId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getManager() { return manager; }
    public String getPhone() { return phone; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setManager(String manager) { this.manager = manager; }
    public void setPhone(String phone) { this.phone = phone; }
}