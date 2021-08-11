package com.example.reflorestar.classes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Project {
    public String name;
    public String description;
    public String status;
    public String availability;
    public int size;
    public String username_owner;

    public Project() {
    }

    public Project(String name, String description, String username_owner) {
        this.name = name;
        this.description = description;
        this.status = "Planning";
        this.availability = "Free";
        this.size = 0;
        this.username_owner = username_owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUsername_owner() {
        return username_owner;
    }

    public void setUsername_owner(String username_owner) {
        this.username_owner = username_owner;
    }
}
