package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.ShipAdapter;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Ship;

import java.util.ArrayList;
import java.util.List;

public class ManageShipsFragment extends Fragment {
    private RecyclerView shipsRecyclerView;
    private EditText nameField;
    private EditText capacityField;
    private EditText searchField;
    private Button saveBtn;
    private Button cancelBtn;
    private Button searchBtn;
    private Button addToggleBtn;
    private View formContainer;
    private ShipDAO shipDAO;
    private List<Ship> shipsList;
    private ShipAdapter shipAdapter;
    private boolean isFormVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_ships, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shipDAO = new ShipDAO(requireContext());
        shipsList = new ArrayList<>();
        initViews(view);
        setupListeners();
        loadShips();
    }

    private void initViews(View view) {
        shipsRecyclerView = view.findViewById(R.id.shipsRecyclerView);
        nameField = view.findViewById(R.id.nameField);
        capacityField = view.findViewById(R.id.capacityField);
        searchField = view.findViewById(R.id.searchField);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        addToggleBtn = view.findViewById(R.id.addToggleBtn);
        formContainer = view.findViewById(R.id.formContainer);

        shipsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        shipAdapter = new ShipAdapter(new ShipAdapter.OnShipClickListener() {
            @Override
            public void onEditClick(Ship ship) {
                nameField.setText(ship.getName());
                capacityField.setText(String.valueOf(ship.getCapacity()));
                toggleForm(true);
            }

            @Override
            public void onDeleteClick(Ship ship) {
                shipDAO.deleteById(ship.getId())
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Ship deleted", Toast.LENGTH_SHORT).show();
                                loadShips();
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete ship", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
            }
        });
        shipsRecyclerView.setAdapter(shipAdapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveShip());
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

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadShips() {
        shipDAO.getAllShips()
            .thenAccept(ships -> {
                if (ships != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        shipsList.clear();
                        shipsList.addAll(ships);
                        updateRecyclerView();
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

    private void saveShip() {
        String name = nameField.getText().toString().trim();
        String capacityStr = capacityField.getText().toString().trim();

        if (name.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid capacity number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (capacity <= 0) {
            Toast.makeText(requireContext(), "Capacity must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Ship existingShip : shipsList) {
            if (existingShip.getName().equalsIgnoreCase(name)) {
                Toast.makeText(requireContext(), "Ship name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String shipId = java.util.UUID.randomUUID().toString();
        Ship ship = new Ship(shipId, name, capacity, "");
        
        // Log what we're sending
        android.util.Log.d("ManageShipsFragment", "Attempting to save ship: " + 
            "id=" + ship.getId() + ", name=" + ship.getName() + 
            ", capacity=" + ship.getCapacity() + ", desc=" + ship.getDescription());
        
        shipDAO.addShip(ship)
            .thenAccept(success -> {
                if (success && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Ship saved successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                        toggleForm(false);
                        loadShips();
                    });
                } else if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to save ship - Check logcat for details", Toast.LENGTH_LONG).show();
                        android.util.Log.e("ManageShipsFragment", "Server returned false when saving ship");
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String errorMsg = "Failed to save ship: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                        android.util.Log.e("ManageShipsFragment", "Error saving ship", e);
                    });
                }
                return null;
            });
    }

    private void clearForm() {
        nameField.setText("");
        capacityField.setText("");
        toggleForm(false);
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            shipAdapter.submitList(new ArrayList<>(shipsList));
            return;
        }

        List<Ship> filteredShips = new ArrayList<>();
        for (Ship ship : shipsList) {
            if (ship.getName().toLowerCase().contains(query)) {
                filteredShips.add(ship);
            }
        }
        shipAdapter.submitList(new ArrayList<>(filteredShips));
    }

    private void updateRecyclerView() {
        shipAdapter.submitList(new ArrayList<>(shipsList));
    }

    private void toggleForm(boolean show) {
        isFormVisible = show;
        formContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        addToggleBtn.setText(show ? "Close Form" : "Add Ship");
        if (show) {
            nameField.requestFocus();
        }
    }
}
