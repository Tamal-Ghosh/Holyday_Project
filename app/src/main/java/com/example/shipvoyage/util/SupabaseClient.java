package com.example.shipvoyage.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Supabase Client Configuration
 * Manages connections to Supabase PostgreSQL database via REST API
 * Uses OkHttp for HTTP requests and Gson for JSON serialization
 */
public class SupabaseClient {
    private static SupabaseClient instance;
    private final String supabaseUrl;
    private final String supabaseAnonKey;
    private final OkHttpClient httpClient;
    private final Gson gson;

    // Supabase credentials
    private static final String SUPABASE_URL = "https://dgyonbbyifaqsffbzdpk.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRneW9uYmJ5aWZhcXNmZmJ6ZHBrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAwODk0NDUsImV4cCI6MjA4NTY2NTQ0NX0.U0tQdjnCnWBV4jzdcbbzY0buFqxGnEDcDbnZAt6CPmc";

    private SupabaseClient(Context context) {
        this.supabaseUrl = SUPABASE_URL;
        this.supabaseAnonKey = SUPABASE_ANON_KEY;

        // Create OkHttpClient with timeouts
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
                    requestBuilder.addHeader("apikey", this.supabaseAnonKey);
                    requestBuilder.addHeader("Authorization", "Bearer " + this.supabaseAnonKey);
                    requestBuilder.addHeader("Content-Type", "application/json");
                    requestBuilder.addHeader("Prefer", "return=representation");
                    return chain.proceed(requestBuilder.build());
                })
                .build();

        // Create Gson instance with custom date format
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
    }

    /**
     * Get singleton instance of SupabaseClient
     */
    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context);
        }
        return instance;
    }

    /**
     * Get the base URL for API calls
     */
    public String getBaseUrl() {
        return supabaseUrl + "/rest/v1";
    }

    /**
     * Get OkHttpClient for making HTTP requests
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Get Gson instance for JSON serialization/deserialization
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Get the anon key for API authentication
     */
    public String getAnonKey() {
        return supabaseAnonKey;
    }
}
