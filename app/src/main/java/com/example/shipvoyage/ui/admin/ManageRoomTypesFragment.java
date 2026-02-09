package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.RoomTypeAdapter;
import com.example.shipvoyage.dao.RoomTypeDAO;
import com.example.shipvoyage.model.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageRoomTypesFragment extends Fragment {
    private RecyclerView roomTypesRecyclerView;
    private EditText roomTypeNameField;
    private Button addRoomTypeBtn;
    private RoomTypeDAO roomTypeDAO;
    private RoomTypeAdapter roomTypeAdapter;
    private List<RoomType> roomTypes = new ArrayList<>();
    private String editingRoomTypeId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_room_types, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomTypeDAO = new RoomTypeDAO(requireContext());
        initViews(view);
        setupListeners();
        loadRoomTypes();
    }

    private void initViews(View view) {
        roomTypesRecyclerView = view.findViewById(R.id.roomTypesRecyclerView);
        roomTypeNameField = view.findViewById(R.id.roomTypeNameField);
        addRoomTypeBtn = view.findViewById(R.id.addRoomTypeBtn);

        roomTypesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        roomTypeAdapter = new RoomTypeAdapter(new RoomTypeAdapter.OnRoomTypeActionListener() {
            @Override
            public void onEdit(RoomType roomType) {
                editingRoomTypeId = roomType.getId();
                roomTypeNameField.setText(roomType.getName());
                addRoomTypeBtn.setText("Update");
            }

            @Override
            public void onDelete(RoomType roomType) {
                roomTypeDAO.deleteById(roomType.getId())
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Room type deleted", Toast.LENGTH_SHORT).show();
                                loadRoomTypes();
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete room type", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
            }
        });
        roomTypesRecyclerView.setAdapter(roomTypeAdapter);
    }

    private void setupListeners() {
        addRoomTypeBtn.setOnClickListener(v -> saveRoomType());
    }

    private void loadRoomTypes() {
        roomTypeDAO.getAllRoomTypes()
            .thenAccept(types -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        roomTypes.clear();
                        if (types != null) {
                            roomTypes.addAll(types);
                        }
                        roomTypeAdapter.submitList(new ArrayList<>(roomTypes));
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

    private void saveRoomType() {
        String name = roomTypeNameField.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a room type name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingRoomTypeId != null) {
            // Update existing room type
            RoomType roomType = new RoomType(editingRoomTypeId, name);
            roomTypeDAO.updateById(editingRoomTypeId, roomType)
                .thenAccept(success -> {
                    if (success && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            roomTypeNameField.setText("");
                            addRoomTypeBtn.setText("Add");
                            editingRoomTypeId = null;
                            Toast.makeText(requireContext(), "Room type updated", Toast.LENGTH_SHORT).show();
                            loadRoomTypes();
                        });
                    } else if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to update room type", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to update room type", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
        } else {
            // Add new room type
            String id = UUID.randomUUID().toString();
            RoomType roomType = new RoomType(id, name);
            roomTypeDAO.insert(roomType)
                .thenAccept(success -> {
                    if (success && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            roomTypeNameField.setText("");
                            Toast.makeText(requireContext(), "Room type added", Toast.LENGTH_SHORT).show();
                            loadRoomTypes();
                        });
                    } else if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to add room type", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to add room type", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
        }
    }
}
