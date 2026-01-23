package com.example.ict602app; // <-- Ni saya dah tukar jadi com.example

import com.google.gson.annotations.SerializedName;

public class LocationLog {

    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("created_at")
    private String createdAt;

    // Constructor kosong (Penting untuk Gson)
    public LocationLog() {
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
    public String getCreatedAt() { return createdAt; }
}