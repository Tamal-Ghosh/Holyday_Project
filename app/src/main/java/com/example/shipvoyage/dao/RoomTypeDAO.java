package com.example.shipvoyage.dao;

import android.content.Context;

import com.example.shipvoyage.model.RoomType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RoomTypeDAO extends BaseSupabaseDAO {
    public RoomTypeDAO(Context context) {
        super(context, "room_types");
    }

    public CompletableFuture<List<RoomType>> getAllRoomTypes() {
        return getAll(RoomType.class);
    }
}
