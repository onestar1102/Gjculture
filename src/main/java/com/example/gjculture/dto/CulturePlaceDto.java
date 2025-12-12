package com.example.gjculture.dto;

public class CulturePlaceDto {
    public String placeName;
    public double distance; // 미터 단위
    public double latitude;
    public double longitude;
    public String category;
    public String address;
    public String locplc;

    public CulturePlaceDto() {}

    public CulturePlaceDto(String placeName, double distance, double latitude,
                           double longitude, String category, String address, String locplc) {
        this.placeName = placeName;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.address = address;
        this.locplc = locplc;
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
