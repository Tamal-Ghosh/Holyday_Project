package com.example.shipvoyage.dao;

import android.content.Context;

import com.example.shipvoyage.model.Room;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RoomDAO - Supabase PostgreSQL Operations
 * CRUD operations for Room table
 */
public class RoomDAO extends BaseSupabaseDAO {
    public RoomDAO(Context context) {
        super(context, "rooms");
    }

    /**
     * Add new room
     */
    public CompletableFuture<Boolean> addRoom(Room room) {
        return insert(room);
    }

    /**
     * Get room by ID
     */
    public CompletableFuture<Room> getRoom(String id) {
        return getById(id, Room.class);
    }

    /**
     * Get all rooms
     */
    public CompletableFuture<List<Room>> getAllRooms() {
        return getAll(Room.class);
    }

    /**
     * Get rooms by ship ID
     */
    public CompletableFuture<List<Room>> getRoomsByShip(String shipId) {
        return query("ship_id=eq." + shipId, Room.class);
    }

    /**
     * Get rooms by type
     */
    public CompletableFuture<List<Room>> getRoomsByType(String type) {
        return query("type=eq." + type, Room.class);
    }

    /**
     * Get available rooms by ship ID
     */
    public CompletableFuture<List<Room>> getAvailableRooms(String shipId) {
        return query("ship_id=eq." + shipId + "&is_available=eq.true", Room.class);
    }

    /**
     * Get rooms by price range
     */
    public CompletableFuture<List<Room>> getRoomsByPriceRange(double minPrice, double maxPrice) {
        return query("price=gte." + minPrice + "&price=lte." + maxPrice, Room.class);
    }

    /**
     * Update room (full object)
     */
    public CompletableFuture<Boolean> updateRoom(String id, Room room) {
        return updateById(id, room);
    }

    /**
     * Update room fields (partial)
     */
    public CompletableFuture<Boolean> updateRoomPartial(String id, Map<String, Object> updates) {
        return updateById(id, updates);
    }

    /**
     * Update room availability
     */
    public CompletableFuture<Boolean> updateAvailability(String id, boolean isAvailable) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("is_available", isAvailable);
        return updateById(id, updates);
    }

    /**
     * Delete room by ID
     */
    public CompletableFuture<Boolean> deleteRoom(String id) {
        return deleteById(id);
    }
}