package com.example.shipvoyage.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.BookingRoomAdapter;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.TourInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RoomSelectionDialog extends DialogFragment {

    private RecyclerView roomsRecyclerView;
    private TextView tourInstanceInfoText;
    private Button cancelButton;
    private Button continueButton;

    private BookingRoomAdapter adapter;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;
    private TourInstance tourInstance;
    private OnRoomsSelectedListener listener;

    public interface OnRoomsSelectedListener {
        void onRoomsSelected(List<Room> selectedRooms, double totalPrice);
    }

    public static RoomSelectionDialog newInstance(TourInstance tourInstance) {
        RoomSelectionDialog dialog = new RoomSelectionDialog();
        Bundle args = new Bundle();
        args.putString("tourInstanceId", tourInstance.getId());
        args.putString("tourName", tourInstance.getTourName());
        args.putString("shipName", tourInstance.getShipName());
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnRoomsSelectedListener(OnRoomsSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_room_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView);
        tourInstanceInfoText = view.findViewById(R.id.tourInstanceInfoText);
        cancelButton = view.findViewById(R.id.cancelButton);
        continueButton = view.findViewById(R.id.continueButton);

        roomDAO = new RoomDAO(requireContext());
        bookingDAO = new BookingDAO(requireContext());

        // Set tour instance info
        if (getArguments() != null) {
            String tourName = getArguments().getString("tourName", "N/A");
            String shipName = getArguments().getString("shipName", "N/A");
            tourInstanceInfoText.setText("Tour: " + tourName + " | Ship: " + shipName);
        }

        // Setup RecyclerView
        adapter = new BookingRoomAdapter((selectedRooms, totalPrice) -> {
            continueButton.setEnabled(!selectedRooms.isEmpty());
        });
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roomsRecyclerView.setAdapter(adapter);

        // Load rooms for the ship
        loadRooms();

        cancelButton.setOnClickListener(v -> dismiss());

        continueButton.setOnClickListener(v -> {
            List<Room> selectedRooms = adapter.getSelectedRooms();
            if (selectedRooms.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one room", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) {
                listener.onRoomsSelected(selectedRooms, adapter.getTotalPrice());
            }
            dismiss();
        });
    }

    private void loadRooms() {
        if (getArguments() == null) return;

        String tourInstanceId = getArguments().getString("tourInstanceId");
        
        // Load bookings for this tour instance and rooms in parallel
        CompletableFuture<List<Booking>> bookingsFuture = bookingDAO.getBookingsByTourInstance(tourInstanceId);
        CompletableFuture<List<Room>> roomsFuture = roomDAO.getAllRooms();
        
        CompletableFuture.allOf(bookingsFuture, roomsFuture).thenAccept(v -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    List<Booking> bookings = bookingsFuture.join();
                    List<Room> rooms = roomsFuture.join();
                    
                    // Get set of booked room IDs for this tour instance
                    Set<String> bookedRoomIds = new HashSet<>();
                    if (bookings != null) {
                        for (Booking booking : bookings) {
                            if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("CANCELLED")) {
                                continue;
                            }
                            bookedRoomIds.add(booking.getRoomId());
                        }
                    }
                    
                    // Show all available rooms, but disable the ones already booked
                    List<Room> availableRooms = new java.util.ArrayList<>();
                    if (rooms != null) {
                        for (Room room : rooms) {
                            if (room.isAvailability()) {
                                availableRooms.add(room);
                            }
                        }
                    }
                    adapter.setRooms(availableRooms);
                    adapter.setDisabledRooms(bookedRoomIds);
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load rooms: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }
}
