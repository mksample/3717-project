package com.example.a3717project;

public class Favourite {
    private String title;
    private double latitude;
    private double longitude;

    public Favourite() {}

    public void setLatitude(Double latitude) {this.latitude = latitude;}
    public void setLongitude(Double longitude) {this.longitude = longitude;}
    public void setTitle(String title) {this.title = title;}
    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}
    public String getTitle() {return title;}
}
