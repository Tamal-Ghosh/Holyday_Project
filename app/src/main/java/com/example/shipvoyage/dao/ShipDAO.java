package com.example.shipvoyage.dao;

import android.content.Context;

import com.example.shipvoyage.model.Ship;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ShipDAO - Supabase PostgreSQL Operations
 * CRUD operations for Ship table
 */
public class ShipDAO extends BaseSupabaseDAO {
    public ShipDAO(Context context) {
        super(context, "ships");
    }

    /**
     * Add new ship
     */
    public CompletableFuture<Boolean> addShip(Ship ship) {
        return insert(ship);
    }

    /**
     * Get ship by ID
     */
    public CompletableFuture<Ship> getShip(String shipId) {
        return getById(shipId, Ship.class);
    }

    /**
     * Get all ships
     */
    public CompletableFuture<List<Ship>> getAllShips() {
        return getAll(Ship.class);
    }

    /**
     * Update ship (full object)
     */
    public CompletableFuture<Boolean> updateShip(String shipId, Ship ship) {
        return updateById(shipId, ship);
    }

    /**
     * Update ship fields (partial)
     */
    public CompletableFuture<Boolean> updateShipPartial(String shipId, Map<String, Object> updates) {
        return updateById(shipId, updates);
    }

    /**
     * Get ships by minimum capacity
     */
    public CompletableFuture<List<Ship>> getShipsByCapacity(int minCapacity) {
        return query("capacity=gte." + minCapacity, Ship.class);
    }

    /**
     * Get ships by status
     */
    public CompletableFuture<List<Ship>> getShipsByStatus(String status) {
        return query("status=eq." + status, Ship.class);
    }

    /**
     * Activate ship
     */
    public CompletableFuture<Boolean> activateShip(String shipId) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "active");
        return updateById(shipId, updates);
    }

    /**
     * Deactivate ship
     */
    public CompletableFuture<Boolean> deactivateShip(String shipId) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "inactive");
        return updateById(shipId, updates);
    }

    /**
     * Delete ship by ID
     */
    public CompletableFuture<Boolean> deleteShip(String shipId) {
        return deleteById(shipId);
    }
}
