package com.example.shipvoyage.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.model.Room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingRoomAdapter extends RecyclerView.Adapter<BookingRoomAdapter.RoomViewHolder> {

    private List<Room> rooms;
    private Set<String> selectedRoomIds;
    private Set<String> disabledRoomIds;
    private OnRoomSelectionChangeListener listener;

    public interface OnRoomSelectionChangeListener {
        void onSelectionChanged(List<Room> selectedRooms, double totalPrice);
    }

    public BookingRoomAdapter(OnRoomSelectionChangeListener listener) {
        this.rooms = new ArrayList<>();
        this.selectedRoomIds = new HashSet<>();
        this.disabledRoomIds = new HashSet<>();
        this.listener = listener;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
        notifyDataSetChanged();
    }

    public void setDisabledRooms(Set<String> disabledRoomIds) {
        this.disabledRoomIds = disabledRoomIds;
        notifyDataSetChanged();
    }

    public List<Room> getSelectedRooms() {
        List<Room> selected = new ArrayList<>();
        for (Room room : rooms) {
            if (selectedRoomIds.contains(room.getId())) {
                selected.add(room);
            }
        }
        return selected;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Room room : getSelectedRooms()) {
            total += room.getPrice();
        }
        return total;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_selection, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room, selectedRoomIds.contains(room.getId()));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView roomNameText;
        private TextView roomTypeText;
        private TextView roomPriceText;
        private CheckBox roomCheckBox;
        private View roomColorBox;
        private TextView bookedBadgeText;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameText = itemView.findViewById(R.id.roomNameText);
            roomTypeText = itemView.findViewById(R.id.roomTypeText);
            roomPriceText = itemView.findViewById(R.id.roomPriceText);
            roomCheckBox = itemView.findViewById(R.id.roomCheckBox);
            roomColorBox = itemView.findViewById(R.id.roomColorBox);
            bookedBadgeText = itemView.findViewById(R.id.bookedBadgeText);

            itemView.setOnClickListener(v -> {
                roomCheckBox.setChecked(!roomCheckBox.isChecked());
            });

            roomCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Room room = rooms.get(getAdapterPosition());
                if (isChecked) {
                    selectedRoomIds.add(room.getId());
                } else {
                    selectedRoomIds.remove(room.getId());
                }
                if (listener != null) {
                    listener.onSelectionChanged(getSelectedRooms(), getTotalPrice());
                }
            });
        }

        public void bind(Room room, boolean isSelected) {
            boolean isDisabled = disabledRoomIds.contains(room.getId());
            
            roomNameText.setText(room.getRoomNumber());
            roomTypeText.setText(room.getType());
            roomPriceText.setText(String.format("$%.2f", room.getPrice()));
            roomCheckBox.setChecked(isSelected);
            roomCheckBox.setEnabled(!isDisabled);
            
            // Set opacity for disabled rooms
            if (isDisabled) {
                itemView.setAlpha(0.5f);
                itemView.setOnClickListener(null);
            } else {
                itemView.setAlpha(1.0f);
                itemView.setOnClickListener(v -> {
                    if (!disabledRoomIds.contains(room.getId())) {
                        roomCheckBox.setChecked(!roomCheckBox.isChecked());
                    }
                });
            }

            // Set color based on room type
            int color;
            String type = room.getType().toLowerCase();
            if (type.contains("single")) {
                color = Color.parseColor("#2196F3"); // Blue
            } else if (type.contains("double")) {
                color = Color.parseColor("#4CAF50"); // Green
            } else if (type.contains("suite")) {
                color = Color.parseColor("#FFD700"); // Gold
            } else {
                color = Color.parseColor("#9E9E9E"); // Gray for others
            }
            roomColorBox.setBackgroundColor(color);
            
            // Show booked badge if room is disabled
            if (isDisabled) {
                bookedBadgeText.setVisibility(View.VISIBLE);
            } else {
                bookedBadgeText.setVisibility(View.GONE);
            }
        }
    }
}
