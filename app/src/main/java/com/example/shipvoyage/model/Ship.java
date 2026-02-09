package com.example.shipvoyage.model;

import com.google.gson.annotations.SerializedName;

public class Ship {
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("capacity")
    public int capacity;
    
    @SerializedName("description")
    public String description;
    
    public Ship() {}
    public Ship(String id, String name, int capacity, String description) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
