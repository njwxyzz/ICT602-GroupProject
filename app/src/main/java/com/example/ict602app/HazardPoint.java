package com.example.ict602app;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HazardPoint {
    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("lat")
    @Expose
    public Double lat;

    @SerializedName("lng")
    @Expose
    public Double lng;
}