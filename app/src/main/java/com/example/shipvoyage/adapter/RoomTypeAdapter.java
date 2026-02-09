package com.example.shipvoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.model.RoomType;

public class RoomTypeAdapter extends ListAdapter<RoomType, RoomTypeAdapter.RoomTypeViewHolder> {

    public interface OnRoomTypeActionListener {
        void onEdit(RoomType roomType);
        void onDelete(RoomType roomType);
    }

    private final OnRoomTypeActionListener listener;

    public RoomTypeAdapter(OnRoomTypeActionListener listener) {
        super(new RoomTypeDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_type, parent, false);
        return new RoomTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomTypeViewHolder holder, int position) {
        RoomType roomType = getItem(position);
        holder.bind(roomType, listener);
    }

    static class RoomTypeViewHolder extends RecyclerView.ViewHolder {
        private final TextView roomTypeName;
        private final Button editRoomTypeBtn;
        private final Button deleteRoomTypeBtn;

        RoomTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTypeName = itemView.findViewById(R.id.roomTypeName);
            editRoomTypeBtn = itemView.findViewById(R.id.editRoomTypeBtn);
            deleteRoomTypeBtn = itemView.findViewById(R.id.deleteRoomTypeBtn);
        }

        void bind(RoomType roomType, OnRoomTypeActionListener listener) {
            roomTypeName.setText(roomType.getName() != null ? roomType.getName() : "N/A");
            editRoomTypeBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(roomType);
                }
            });
            deleteRoomTypeBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(roomType);
                }
            });
        }
    }

    static class RoomTypeDiffCallback extends DiffUtil.ItemCallback<RoomType> {
        @Override
        public boolean areItemsTheSame(@NonNull RoomType oldItem, @NonNull RoomType newItem) {
            if (oldItem.getId() == null && newItem.getId() == null) {
                return oldItem == newItem;
            }
            if (oldItem.getId() == null || newItem.getId() == null) {
                return false;
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoomType oldItem, @NonNull RoomType newItem) {
            if (oldItem.getName() == null && newItem.getName() == null) {
                return true;
            }
            if (oldItem.getName() == null || newItem.getName() == null) {
                return false;
            }
            return oldItem.getName().equals(newItem.getName());
        }
    }
}
