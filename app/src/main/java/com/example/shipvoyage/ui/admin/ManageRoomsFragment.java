package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.RoomAdapter;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.RoomTypeDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.RoomType;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public class ManageRoomsFragment extends Fragment {
    private RecyclerView roomsRecyclerView;
    private EditText roomNumberField, searchField;
    private Spinner shipSpinner, typeSpinner;
    private Button saveBtn, cancelBtn, searchBtn, addToggleBtn;
    private View formContainer;
    private RoomDAO roomDAO;
    private RoomTypeDAO roomTypeDAO;
    private ShipDAO shipDAO;
    private List<Room> roomsList = new ArrayList<>();
    private List<RoomType> roomTypesList = new ArrayList<>();
    private List<String> roomTypeNames = new ArrayList<>();
    private ArrayAdapter<String> typeAdapter;
    private List<Ship> shipsList = new ArrayList<>();
    private RoomAdapter roomAdapter;
    private String editingRoomId = null;
    private boolean isFormVisible = false;
    private String selectedShipId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomDAO = new RoomDAO(requireContext());
        roomTypeDAO = new RoomTypeDAO(requireContext());
        shipDAO = new ShipDAO(requireContext());
        initViews(view);
        setupListeners();
        loadRoomTypes();
        loadShips();
        loadRooms();
    }

    private void initViews(View view) {
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView);
        roomNumberField = view.findViewById(R.id.roomNumberField);
        typeSpinner = view.findViewById(R.id.typeSpinner);
        searchField = view.findViewById(R.id.searchField);
        shipSpinner = view.findViewById(R.id.shipSpinner);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        addToggleBtn = view.findViewById(R.id.addToggleBtn);
        formContainer = view.findViewById(R.id.formContainer);

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        initTypeSpinner();

        roomAdapter = new RoomAdapter(new RoomAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(Room room) {
                editingRoomId = room.getId();
                roomNumberField.setText(room.getRoomNumber());
                
                setTypeSpinnerSelection(room.getType());
                
                // Set ship spinner selection (add 1 to account for "None" at position 0)
                for (int i = 0; i < shipsList.size(); i++) {
                    if (shipsList.get(i).getId().equals(room.getShipId())) {
                        shipSpinner.setSelection(i + 1);
                        break;
                    }
                }
                toggleForm(true);
            }

            @Override
            public void onDelete(Room room) {
                roomDAO.deleteById(room.getId())
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Room deleted", Toast.LENGTH_SHORT).show();
                                loadRooms();
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete room", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
            }
        });
        roomsRecyclerView.setAdapter(roomAdapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveRoom());
        cancelBtn.setOnClickListener(v -> clearForm());
        searchBtn.setOnClickListener(v -> performSearch());
        addToggleBtn.setOnClickListener(v -> {
            if (isFormVisible) {
                clearForm();
                toggleForm(false);
            } else {
                toggleForm(true);
            }
        });
        shipSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedShipId = null;
                    updateRecyclerView();
                } else if (position - 1 < shipsList.size()) {
                    selectedShipId = shipsList.get(position - 1).getId();
                    filterRoomsByShip();
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedShipId = null;
                updateRecyclerView();
            }
        });
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initTypeSpinner() {
        roomTypeNames.clear();
        roomTypeNames.add("None");
        typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roomTypeNames);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
    }

    private void loadRoomTypes() {
        roomTypeDAO.getAllRoomTypes()
            .thenAccept(types -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        roomTypesList.clear();
                        if (types != null) {
                            roomTypesList.addAll(types);
                        }
                        updateTypeSpinner();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load room types", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void updateTypeSpinner() {
        roomTypeNames.clear();
        roomTypeNames.add("None");
        for (RoomType type : roomTypesList) {
            if (type != null && type.getName() != null && !type.getName().trim().isEmpty()) {
                roomTypeNames.add(type.getName());
            }
        }
        typeAdapter.notifyDataSetChanged();
    }

    private void setTypeSpinnerSelection(String type) {
        if (type == null || type.trim().isEmpty()) {
            typeSpinner.setSelection(0);
            return;
        }
        int index = 0;
        for (int i = 0; i < roomTypeNames.size(); i++) {
            if (type.equalsIgnoreCase(roomTypeNames.get(i))) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            roomTypeNames.add(type);
            typeAdapter.notifyDataSetChanged();
            index = roomTypeNames.size() - 1;
        }
        typeSpinner.setSelection(index);
    }

    private void loadShips() {
        shipDAO.getAll(Ship.class)
            .thenAccept(ships -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        shipsList.clear();
                        shipsList.addAll(ships);
                        updateShipSpinner();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load ships", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void updateShipSpinner() {
        List<String> shipNames = new ArrayList<>();
        shipNames.add("None");
        for (Ship ship : shipsList) {
            shipNames.add(ship.getName());
        }
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                shipNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shipSpinner.setAdapter(adapter);
        if (shipNames.size() > 1) {
            shipSpinner.setSelection(0);
            selectedShipId = null;
        }
    }

    private void loadRooms() {
        roomDAO.getAll(Room.class)
            .thenAccept(rooms -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        roomsList.clear();
                        roomsList.addAll(rooms);
                        updateRecyclerView();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void saveRoom() {
        String roomNumber = roomNumberField.getText().toString().trim();

        if (roomNumber.isEmpty() || shipSpinner.getSelectedItemPosition() == 0 || typeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            
            // Get selected ship
            int spinnerPosition = shipSpinner.getSelectedItemPosition();
            if (spinnerPosition <= 0 || spinnerPosition - 1 >= shipsList.size()) {
                Toast.makeText(requireContext(), "Please select a valid ship", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Ship selectedShip = shipsList.get(spinnerPosition - 1);
            
            // Get selected room type
            String type = typeSpinner.getSelectedItem().toString();
            
            // Count existing rooms for this ship
            int existingRoomCount = 0;
            for (Room room : roomsList) {
                if (room.getShipId().equals(selectedShip.getId())) {
                    // Don't count the room being edited
                    if (!room.getId().equals(editingRoomId)) {
                        existingRoomCount++;
                    }
                }
            }
            
            // Check if adding new room would exceed capacity
            if (existingRoomCount >= selectedShip.getCapacity()) {
                Toast.makeText(requireContext(), 
                    "Cannot add more rooms! Ship capacity is " + selectedShip.getCapacity() + 
                    " and already has " + existingRoomCount + " rooms.", 
                    Toast.LENGTH_LONG).show();
                return;
            }

            String roomId = editingRoomId != null ? editingRoomId : java.util.UUID.randomUUID().toString();
            Room room = new Room(roomId, selectedShip.getId(), roomNumber, type, true);
            
            if (editingRoomId != null) {
                roomDAO.updateRoom(roomId, room)
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Room updated successfully", Toast.LENGTH_SHORT).show();
                                clearForm();
                                toggleForm(false);
                                loadRooms();
                            });
                        } else if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to update room - Check logcat", Toast.LENGTH_LONG).show();
                                android.util.Log.e("ManageRoomsFragment", "Server returned false");
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                String errorMsg = "Failed to update room: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                                android.util.Log.e("ManageRoomsFragment", "Error updating room", e);
                            });
                        }
                        return null;
                    });
            } else {
                roomDAO.addRoom(room)
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Room saved successfully", Toast.LENGTH_SHORT).show();
                                clearForm();
                                toggleForm(false);
                                loadRooms();
                            });
                        } else if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to save room - Check logcat", Toast.LENGTH_LONG).show();
                                android.util.Log.e("ManageRoomsFragment", "Server returned false");
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                String errorMsg = "Failed to save room: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                                android.util.Log.e("ManageRoomsFragment", "Error saving room", e);
                            });
                        }
                        return null;
                    });
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving room", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editingRoomId = null;
        roomNumberField.setText("");
        typeSpinner.setSelection(0);
        toggleForm(false);
    }

    private void toggleForm(boolean show) {
        isFormVisible = show;
        formContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        addToggleBtn.setText(show ? "Close Form" : "Add Room");
        if (show) {
            roomNumberField.requestFocus();
        }
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        List<Room> filteredRooms = new ArrayList<>();
        
        // If no ship selected (None), show empty list
        if (selectedShipId == null) {
            roomAdapter.submitList(new ArrayList<>());
            return;
        }
        
        for (Room room : roomsList) {
            if (room.getShipId().equals(selectedShipId) &&
                (query.isEmpty() || room.getRoomNumber().toLowerCase().contains(query) ||
                room.getType().toLowerCase().contains(query))) {
                filteredRooms.add(room);
            }
        }
        roomAdapter.submitList(new ArrayList<>(filteredRooms));
    }

    private void filterRoomsByShip() {
        String query = searchField.getText().toString().trim().toLowerCase();
        List<Room> filteredRooms = new ArrayList<>();
        
        // If no ship selected (None), show empty list
        if (selectedShipId == null) {
            roomAdapter.submitList(new ArrayList<>());
            return;
        }
        
        for (Room room : roomsList) {
            if (room.getShipId().equals(selectedShipId) &&
                (query.isEmpty() || room.getRoomNumber().toLowerCase().contains(query) ||
                room.getType().toLowerCase().contains(query))) {
                filteredRooms.add(room);
            }
        }
        roomAdapter.submitList(new ArrayList<>(filteredRooms));
    }

    private void updateRecyclerView() {
        filterRoomsByShip();
    }
}
