package com.sample.demo3.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    private final StringProperty username;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty role;
    private final StringProperty phone;
    private final StringProperty status;
    private final StringProperty createdAt;

    public User(String username, String fullName, String email,
                String role, String phone, String status, String createdAt) {
        this.username = new SimpleStringProperty(username);
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.phone = new SimpleStringProperty(phone);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleStringProperty(createdAt);
    }

    // Getters
    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getCreatedAt() { return createdAt.get(); }
    public StringProperty createdAtProperty() { return createdAt; }

    // Setters
    public void setUsername(String username) { this.username.set(username); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setRole(String role) { this.role.set(role); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setStatus(String status) { this.status.set(status); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
}