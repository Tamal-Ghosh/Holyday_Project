package com.example.shipvoyage.dao;

import android.content.Context;
import android.util.Log;

import com.example.shipvoyage.util.SupabaseClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Base DAO class for Supabase PostgreSQL operations
 * Handles common CRUD operations via REST API (PostgREST)
 * All operations are async using CompletableFuture
 */
public abstract class BaseSupabaseDAO {
    private static final String TAG = "BaseSupabaseDAO";
    protected final OkHttpClient httpClient;
    protected final Gson gson;
    protected final String baseUrl;
    protected final String tableName;

    public BaseSupabaseDAO(Context context, String tableName) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        this.httpClient = client.getHttpClient();
        this.gson = client.getGson();
        this.baseUrl = client.getBaseUrl();
        this.tableName = tableName;
    }

    /**
     * Get single row by ID
     */
    protected <T> CompletableFuture<T> getById(String id, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = baseUrl + "/" + tableName + "?id=eq." + id;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        JsonArray arr = gson.fromJson(jsonResponse, JsonArray.class);
                        if (arr != null && arr.size() > 0) {
                            return gson.fromJson(arr.get(0), clazz);
                        }
                    } else {
                        Log.e(TAG, "Error response: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage(), e);
            }
            return null;
        }, Executors.newCachedThreadPool());
    }

    /**
     * Get all rows from table
     */
    public <T> CompletableFuture<List<T>> getAll(Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> results = new ArrayList<>();
            try {
                String url = baseUrl + "/" + tableName;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        JsonArray arr = gson.fromJson(jsonResponse, JsonArray.class);
                        if (arr != null) {
                            for (int i = 0; i < arr.size(); i++) {
                                results.add(gson.fromJson(arr.get(i), clazz));
                            }
                        }
                    } else {
                        Log.e(TAG, "Error response: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage(), e);
            }
            return results;
        }, Executors.newCachedThreadPool());
    }

    /**
     * Insert new row
     */
    public CompletableFuture<Boolean> insert(Object data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = gson.toJson(data);
                String url = baseUrl + "/" + tableName;

                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    if (!success) {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        Log.e(TAG, "Insert failed - Code: " + response.code() + ", Message: " + response.message() + ", Body: " + errorBody);
                    } else {
                        Log.d(TAG, "Insert successful - Code: " + response.code());
                    }
                    return success;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting data: " + e.getMessage(), e);
                return false;
            }
        }, Executors.newCachedThreadPool());
    }

    /**
     * Update existing row by ID
     */
    public CompletableFuture<Boolean> updateById(String id, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = gson.toJson(data);
                String url = baseUrl + "/" + tableName + "?id=eq." + id;

                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    if (!success) {
                        Log.e(TAG, "Update error: " + response.code());
                    }
                    return success;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating data: " + e.getMessage(), e);
                return false;
            }
        }, Executors.newCachedThreadPool());
    }

    /**
     * Delete row by ID
     */
    public CompletableFuture<Boolean> deleteById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = baseUrl + "/" + tableName + "?id=eq." + id;
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    if (!success) {
                        Log.e(TAG, "Delete error: " + response.code());
                    }
                    return success;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting data: " + e.getMessage(), e);
                return false;
            }
        }, Executors.newCachedThreadPool());
    }

    /**
     * Query with filter condition (PostgREST syntax)
     * Example: "email=eq.user@example.com"
     */
    protected <T> CompletableFuture<List<T>> query(String filter, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> results = new ArrayList<>();
            try {
                String url = baseUrl + "/" + tableName + "?" + filter;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        JsonArray arr = gson.fromJson(jsonResponse, JsonArray.class);
                        if (arr != null) {
                            for (int i = 0; i < arr.size(); i++) {
                                results.add(gson.fromJson(arr.get(i), clazz));
                            }
                        }
                    } else {
                        Log.e(TAG, "Error response: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying data: " + e.getMessage(), e);
            }
            return results;
        }, Executors.newCachedThreadPool());
    }
}
