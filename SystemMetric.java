package com.sample.demo3.models;

public class SystemMetric {
    private String metricId;
    private String type;
    private double value;
    private String unit;
    private String timestamp;
    private String source;

    // Constructeur par défaut
    public SystemMetric() {
    }

    // Constructeur avec les paramètres
    public SystemMetric(String metricId, String type, double value, String unit,
                        String timestamp, String source) {
        this.metricId = metricId;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
        this.source = source;
    }

    // Getters
    public String getMetricId() {
        return metricId;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    // Setters
    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSource(String source) {
        this.source = source;
    }
}