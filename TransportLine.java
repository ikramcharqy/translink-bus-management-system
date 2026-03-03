package com.sample.demo3.models;

public class TransportLine {
    private String lineId;
    private String route;
    private String schedule;
    private String price;
    private String stops;

    public TransportLine(String lineId, String route, String schedule, String price, String stops) {
        this.lineId = lineId;
        this.route = route;
        this.schedule = schedule;
        this.price = price;
        this.stops = stops;
    }

    // Getters
    public String getLineId() { return lineId; }
    public String getRoute() { return route; }
    public String getSchedule() { return schedule; }
    public String getPrice() { return price; }
    public String getStops() { return stops; }

    // Setters
    public void setRoute(String route) { this.route = route; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public void setPrice(String price) { this.price = price; }
    public void setStops(String stops) { this.stops = stops; }
}