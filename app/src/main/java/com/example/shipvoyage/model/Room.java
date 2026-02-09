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
    
    @SerializedName("is_available")
    public boolean availability;
    public Room() {}
    public Room(String id, String shipId, String roomNumber, String type, boolean availability) {
        this.id = id;
        this.shipId = shipId;
        this.roomNumber = roomNumber;
        this.type = type;
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
    
    // Dummy method for backward compatibility with BookingRoomAdapter
    public double getPrice() {
        return 0;
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
        map.put("is_available", availability);
        return map;
    }
}
