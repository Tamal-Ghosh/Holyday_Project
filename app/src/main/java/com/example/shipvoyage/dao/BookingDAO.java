package com.example.shipvoyage.dao;

import android.content.Context;
import com.example.shipvoyage.model.Booking;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookingDAO extends BaseSupabaseDAO {
    public BookingDAO(Context context) {
        super(context, "bookings");
    }

    /**
     * Add new booking
     */
    public CompletableFuture<Boolean> addBooking(Booking booking) {
        return insert(booking);
    }

    /**
     * Update booking
     */
    public CompletableFuture<Boolean> updateBooking(String id, Booking booking) {
        return updateById(id, booking);
    }

    /**
     * Get booking by ID
     */
    public CompletableFuture<Booking> getBooking(String id) {
        return getById(id, Booking.class);
    }

    /**
     * Get all bookings
     */
    public CompletableFuture<List<Booking>> getAllBookings() {
        return getAll(Booking.class);
    }

    /**
     * Get bookings by tour instance ID
     */
    public CompletableFuture<List<Booking>> getBookingsByTourInstance(String tourInstanceId) {
        return query("tour_instance_id=eq." + tourInstanceId, Booking.class);
    }

    /**
     * Get bookings by room ID
     */
    public CompletableFuture<List<Booking>> getBookingsByRoom(String roomId) {
        return query("room_id=eq." + roomId, Booking.class);
    }

    /**
     * Get bookings by status
     */
    public CompletableFuture<List<Booking>> getBookingsByStatus(String status) {
        return query("status=eq." + status, Booking.class);
    }

    /**
     * Delete booking
     */
    public CompletableFuture<Boolean> deleteBooking(String id) {
        return deleteById(id);
    }
}
