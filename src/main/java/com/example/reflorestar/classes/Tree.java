package com.example.reflorestar.classes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Tree {
    public float latitude;
    public float longitude;
    public float height;
    public String type;

    public Tree() {
    }

    public Tree(float latitude, float longitude, float height, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.type = type;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
