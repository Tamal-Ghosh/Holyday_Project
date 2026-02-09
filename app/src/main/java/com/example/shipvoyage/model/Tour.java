package com.example.shipvoyage.model;
import com.google.gson.annotations.SerializedName;

public class Tour {
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("from")
    public String from;
    
    @SerializedName("to")
    public String to;
    
    @SerializedName("description")
    public String description;
    
    public Tour() {}
    
    public Tour(String id, String name, String from, String to, String description) {
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
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
    
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
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
