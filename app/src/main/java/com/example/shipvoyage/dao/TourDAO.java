package com.example.shipvoyage.dao;

import android.content.Context;

import com.example.shipvoyage.model.Tour;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TourDAO - Supabase PostgreSQL Operations
 * CRUD operations for Tour table
 */
public class TourDAO extends BaseSupabaseDAO {
    public TourDAO(Context context) {
        super(context, "tours");
    }

    /**
     * Add new tour
     */
    public CompletableFuture<Boolean> addTour(Tour tour) {
        return insert(tour);
    }

    /**
     * Get tour by ID
     */
    public CompletableFuture<Tour> getTour(String tourId) {
        return getById(tourId, Tour.class);
    }

    /**
     * Get all tours
     */
    public CompletableFuture<List<Tour>> getAllTours() {
        return getAll(Tour.class);
    }

    /**
     * Update tour (full object)
     */
    public CompletableFuture<Boolean> updateTour(String tourId, Tour tour) {
        return updateById(tourId, tour);
    }

    /**
     * Update tour fields (partial)
     */
    public CompletableFuture<Boolean> updateTourPartial(String tourId, Map<String, Object> updates) {
        return updateById(tourId, updates);
    }

    /**
     * Get tours by status
     */
    public CompletableFuture<List<Tour>> getToursByStatus(String status) {
        return query("status=eq." + status, Tour.class);
    }

    /**
     * Get tours by duration range
     */
    public CompletableFuture<List<Tour>> getToursByDuration(int minDays, int maxDays) {
        return query("duration=gte." + minDays + "&duration=lte." + maxDays, Tour.class);
    }

    /**
     * Activate tour
     */
    public CompletableFuture<Boolean> activateTour(String tourId) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "active");
        return updateById(tourId, updates);
    }

    /**
     * Deactivate tour
     */
    public CompletableFuture<Boolean> deactivateTour(String tourId) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "inactive");
        return updateById(tourId, updates);
    }

    /**
     * Delete tour by ID
     */
    public CompletableFuture<Boolean> deleteTour(String tourId) {
        return deleteById(tourId);
    }
}
