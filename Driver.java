package com.sample.demo3.models;

public class Driver {
    private String driverId;
    private String name;
    private String license;
    private String assignedLine;
    private String phone;
    private String email;
    private String status;
    private Double salary;
    private String assignedCoachId;
    private String userId;

    public Driver(String driverId, String name, String license, String assignedLine,
                  String phone, String email, String status, Double salary,
                  String assignedCoachId, String userId) {
        this.driverId = driverId;
        this.name = name;
        this.license = license;
        this.assignedLine = assignedLine;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.salary = salary;
        this.assignedCoachId = assignedCoachId;
        this.userId = userId;
    }

    // Getters et Setters
    public String getDriverId() { return driverId; }
    public String getName() { return name; }
    public String getLicense() { return license; }
    public String getAssignedLine() { return assignedLine; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public Double getSalary() { return salary; }
    public String getAssignedCoachId() { return assignedCoachId; }
    public String getUserId() { return userId; }

    public void setName(String name) { this.name = name; }
    public void setLicense(String license) { this.license = license; }
    public void setAssignedLine(String assignedLine) { this.assignedLine = assignedLine; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(String status) { this.status = status; }
    public void setSalary(Double salary) { this.salary = salary; }
    public void setAssignedCoachId(String assignedCoachId) { this.assignedCoachId = assignedCoachId; }
}