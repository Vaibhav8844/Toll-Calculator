package com.example.tollcalculator;

public class TollZone {
    private String name;
    private double latitude;
    private double longitude;
    private double cost;
    private int radius;

    public TollZone(String name, double latitude, double longitude, int radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cost = cost;
        this.radius = radius;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getCost() { return cost; }
    public int getRadius() { return radius; }
}
