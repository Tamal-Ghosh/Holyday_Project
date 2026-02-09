package com.example.shipvoyage.model;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;
public class Room {
    @SerializedName("id")
    public String id;
    
    @SerializedName("ship_id")
    public String shipId;
    
    @SerializedName("name")
    public String roomNumber;
    
    @SerializedName("type")
    public String type;
    
    @SerializedName("price")
    public double price;
    
    @SerializedName("is_available")
    public boolean availability;
    public Room() {}
    public Room(String id, String shipId, String roomNumber, String type, double price, boolean availability) {
        this.id = id;
        this.shipId = shipId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.availability = availability;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getShipId() {
        return shipId;
    }
    public void setShipId(String shipId) {
        this.shipId = shipId;
    }
    public String getRoomNumber() {
        return roomNumber;
    }
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public boolean isAvailability() {
        return availability;
    }
    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("ship_id", shipId);
        map.put("name", roomNumber);
        map.put("type", type);
        map.put("price", price);
        map.put("is_available", availability);
        return map;
    }
}
