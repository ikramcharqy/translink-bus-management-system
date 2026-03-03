package com.sample.demo3.models;

public class UserActivity {
    private String userId;
    private String activityType;
    private String description;
    private String timestamp;
    private String ipAddress;
    private String deviceInfo;
    private String details;
    private String location;


    // Constructeur par défaut
    public UserActivity() {
    }

    // Constructeur avec tous les paramètres
    public UserActivity(String userId, String activityType, String description,
                        String timestamp, String ipAddress, String deviceInfo, String sessionId, String details, String location) {
        this.userId = userId;
        this.activityType = activityType;
        this.description = description;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.details=details;
        this.location=location;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}