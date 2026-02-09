package com.example.shipvoyage.dao;

import android.content.Context;

import com.example.shipvoyage.model.TourInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TourInstanceDAO - Supabase PostgreSQL Operations
 * CRUD operations for TourInstance table
 */
public class TourInstanceDAO extends BaseSupabaseDAO {
    public TourInstanceDAO(Context context) {
        super(context, "tour_instances");
    }

    /**
     * Add new tour instance
     */
    public CompletableFuture<Boolean> addTourInstance(TourInstance tourInstance) {
        return insert(tourInstance);
    }

    /**
     * Get tour instance by ID
     */
    public CompletableFuture<TourInstance> getTourInstance(String id) {
        return getById(id, TourInstance.class);
    }

    /**
     * Get all tour instances
     */
    public CompletableFuture<List<TourInstance>> getAllTourInstances() {
        return getAll(TourInstance.class);
    }

    /**
     * Get tour instances by tour ID
     */
    public CompletableFuture<List<TourInstance>> getTourInstancesByTour(String tourId) {
        return query("tour_id=eq." + tourId, TourInstance.class);
    }

    /**
     * Get tour instances by ship ID
     */
    public CompletableFuture<List<TourInstance>> getTourInstancesByShip(String shipId) {
        return query("ship_id=eq." + shipId, TourInstance.class);
    }

    /**
     * Get tour instances by status
     */
    public CompletableFuture<List<TourInstance>> getTourInstancesByStatus(String status) {
        return query("status=eq." + status, TourInstance.class);
    }

    /**
     * Get tour instances by date range
     */
    public CompletableFuture<List<TourInstance>> getTourInstancesByDateRange(String startDate, String endDate) {
        return query("departure_date=gte." + startDate + "&departure_date=lte." + endDate, TourInstance.class);
    }

    /**
     * Get available tour instances (status=available)
     */
    public CompletableFuture<List<TourInstance>> getAvailableInstances() {
        return query("status=eq.available", TourInstance.class);
    }

    /**
     * Update tour instance (full object)
     */
    public CompletableFuture<Boolean> updateTourInstance(String id, TourInstance tourInstance) {
        return updateById(id, tourInstance);
    }

    /**
     * Update tour instance fields (partial)
     */
    public CompletableFuture<Boolean> updateTourInstancePartial(String id, Map<String, Object> updates) {
        return updateById(id, updates);
    }

    /**
     * Update availability
     */
    public CompletableFuture<Boolean> updateAvailability(String id, int availability) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("availability", availability);
        return updateById(id, updates);
    }

    /**
     * Delete tour instance by ID
     */
    public CompletableFuture<Boolean> deleteTourInstance(String id) {
        return deleteById(id);
    }
}
