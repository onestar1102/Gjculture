package com.example.gjculture.dto;

public class CulturePlaceDto {
    private String placeName;
    private double distance; // 미터 단위
    private double latitude;
    private double longitude;
    private String category;
    private String address;

    public CulturePlaceDto() {}

    public CulturePlaceDto(String placeName, double distance, double latitude,
                           double longitude, String category, String address) {
        this.placeName = placeName;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.address = address;
    }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
