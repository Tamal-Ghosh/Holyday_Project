package com.example.shipvoyage.dao;

import android.content.Context;

import com.example.shipvoyage.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * UserDAO - Supabase PostgreSQL Operations
 * CRUD operations for User table
 */
public class UserDAO extends BaseSupabaseDAO {
    public UserDAO(Context context) {
        super(context, "users");
    }

    /**
     * Add new user
     */
    public CompletableFuture<Boolean> addUser(User user) {
        return insert(user);
    }

    /**
     * Get user by ID
     */
    public CompletableFuture<User> getUser(String id) {
        return getById(id, User.class);
    }

    /**
     * Get all users
     */
    public CompletableFuture<List<User>> getAllUsers() {
        return getAll(User.class);
    }

    /**
     * Get user by email
     */
    public CompletableFuture<List<User>> getUserByEmail(String email) {
        return query("email=eq." + email, User.class);
    }

    /**
     * Get user by username
     */
    public CompletableFuture<List<User>> getUserByUsername(String username) {
        return query("username=eq." + username, User.class);
    }

    /**
     * Get users by role
     */
    public CompletableFuture<List<User>> getUsersByRole(String role) {
        return query("role=eq." + role, User.class);
    }

    /**
     * Update user
     */
    public CompletableFuture<Boolean> updateUser(User user) {
        return updateById(user.getId(), user);
    }

    /**
     * Update user fields
     */
    public CompletableFuture<Boolean> updateUser(String id, Map<String, Object> updates) {
        return updateById(id, updates);
    }

    /**
     * Get user by phone number
     */
    public CompletableFuture<List<User>> getUserByPhone(String phone) {
        return query("phone=eq." + phone, User.class);
    }

    /**
     * Update user password
     */
    public CompletableFuture<Boolean> updatePassword(String id, String password) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("password", password);
        return updateById(id, updates);
    }

    /**
     * Activate user account
     */
    public CompletableFuture<Boolean> activateUser(String id) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("isActive", true);
        return updateById(id, updates);
    }

    /**
     * Deactivate user account
     */
    public CompletableFuture<Boolean> deactivateUser(String id) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("isActive", false);
        return updateById(id, updates);
    }

    /**
     * Delete user by ID
     */
    public CompletableFuture<Boolean> deleteUser(String id) {
        return deleteById(id);
    }
}
