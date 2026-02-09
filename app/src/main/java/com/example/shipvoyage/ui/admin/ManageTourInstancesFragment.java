package com.example.shipvoyage.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.shipvoyage.adapter.TourInstanceAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.util.ThreadPool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageTourInstancesFragment extends Fragment {
    private RecyclerView instancesRecyclerView;
    private EditText startDateField, endDateField, searchField;
    private Spinner tourSpinner, shipSpinner;
    private Button saveBtn, cancelBtn, searchBtn, addToggleBtn;
    private View formContainer;
    private TourInstanceDAO instanceDAO;
    private TourDAO tourDAO;
    private ShipDAO shipDAO;
    private List<TourInstance> instancesList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<Ship> shipsList = new ArrayList<>();
    private TourInstanceAdapter adapter;
    private String editingInstanceId = null;
    private static final int START_DATE = 1;
    private static final int END_DATE = 2;
    private int currentDateFieldType = 0;
    private boolean isFormVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_tour_instances, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instanceDAO = new TourInstanceDAO(requireContext());
        tourDAO = new TourDAO(requireContext());
        shipDAO = new ShipDAO(requireContext());
        initViews(view);
        setupListeners();
        loadTours();
        loadShips();
        loadInstances();
    }

    private void initViews(View view) {
        instancesRecyclerView = view.findViewById(R.id.instancesRecyclerView);
        startDateField = view.findViewById(R.id.startDateField);
        endDateField = view.findViewById(R.id.endDateField);
        searchField = view.findViewById(R.id.searchField);
        tourSpinner = view.findViewById(R.id.tourSpinner);
        shipSpinner = view.findViewById(R.id.shipSpinner);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        addToggleBtn = view.findViewById(R.id.addToggleBtn);
        formContainer = view.findViewById(R.id.formContainer);

        instancesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TourInstanceAdapter(new TourInstanceAdapter.OnTourInstanceClickListener() {
            @Override
            public void onEditClick(TourInstance instance) {
                editingInstanceId = instance.getId();
                startDateField.setText(instance.getStartDate());
                endDateField.setText(instance.getEndDate());
                
                // Set spinner selections (add 1 to account for "None" at position 0)
                for (int i = 0; i < toursList.size(); i++) {
                    if (toursList.get(i).getId().equals(instance.getTourId())) {
                        tourSpinner.setSelection(i + 1);
                        break;
                    }
                }
                for (int i = 0; i < shipsList.size(); i++) {
                    if (shipsList.get(i).getId().equals(instance.getShipId())) {
                        shipSpinner.setSelection(i + 1);
                        break;
                    }
                }
                toggleForm(true);
            }

            @Override
            public void onDeleteClick(TourInstance instance) {
                instanceDAO.deleteById(instance.getId())
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Instance deleted", Toast.LENGTH_SHORT).show();
                                loadInstances();
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete instance", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
            }
        });
        instancesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        startDateField.setOnClickListener(v -> {
            currentDateFieldType = START_DATE;
            showDatePicker();
        });
        endDateField.setOnClickListener(v -> {
            currentDateFieldType = END_DATE;
            showDatePicker();
        });
        saveBtn.setOnClickListener(v -> saveInstance());
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTours() {
        tourDAO.getAll(Tour.class)
            .thenAccept(tours -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toursList.clear();
                        toursList.addAll(tours);
                        updateTourSpinner();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
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

    private void updateTourSpinner() {
        List<String> tourNames = new ArrayList<>();
        tourNames.add("None");
        for (Tour tour : toursList) {
            tourNames.add(tour.getName());
        }
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                tourNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tourSpinner.setAdapter(adapter);
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
    }

    private void loadInstances() {
        instanceDAO.getAll(TourInstance.class)
            .thenAccept(instances -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Populate tour and ship names from the loaded lists
                        for (TourInstance instance : instances) {
                            for (Tour tour : toursList) {
                                if (tour.getId().equals(instance.getTourId())) {
                                    instance.setTourName(tour.getName());
                                    break;
                                }
                            }
                            for (Ship ship : shipsList) {
                                if (ship.getId().equals(instance.getShipId())) {
                                    instance.setShipName(ship.getName());
                                    break;
                                }
                            }
                        }
                        
                        instancesList.clear();
                        instancesList.addAll(instances);
                        updateRecyclerView();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load instances", Toast.LENGTH_SHORT).show();
                        android.util.Log.e("ManageTourInstancesFragment", "Error loading instances", e);
                    });
                }
                return null;
            });
    }

    private void saveInstance() {
        String startDate = startDateField.getText().toString().trim();
        String endDate = endDateField.getText().toString().trim();
        int tourPosition = tourSpinner.getSelectedItemPosition();
        int shipPosition = shipSpinner.getSelectedItemPosition();

        if (startDate.isEmpty() || endDate.isEmpty() || tourPosition == 0 || shipPosition == 0) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Tour selectedTour = toursList.get(tourPosition - 1);
        Ship selectedShip = shipsList.get(shipPosition - 1);

        String instanceId = editingInstanceId != null ? editingInstanceId : java.util.UUID.randomUUID().toString();
        TourInstance instance = new TourInstance(instanceId, selectedTour.getId(), selectedShip.getId(), startDate, endDate);
        instance.setTourName(selectedTour.getName());
        instance.setShipName(selectedShip.getName());
        
        if (editingInstanceId != null) {
            instanceDAO.updateTourInstance(instanceId, instance)
                .thenAccept(success -> {
                    if (success && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Instance updated successfully", Toast.LENGTH_SHORT).show();
                            clearForm();
                            toggleForm(false);
                            loadInstances();
                        });
                    } else if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to update instance - Check logcat", Toast.LENGTH_LONG).show();
                            android.util.Log.e("ManageTourInstancesFragment", "Server returned false");
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String errorMsg = "Failed to update instance: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                            android.util.Log.e("ManageTourInstancesFragment", "Error updating instance", e);
                        });
                    }
                    return null;
                });
        } else {
            instanceDAO.addTourInstance(instance)
                .thenAccept(success -> {
                    if (success && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Instance saved successfully", Toast.LENGTH_SHORT).show();
                            clearForm();
                            toggleForm(false);
                            loadInstances();
                        });
                    } else if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to save instance - Check logcat", Toast.LENGTH_LONG).show();
                            android.util.Log.e("ManageTourInstancesFragment", "Server returned false");
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String errorMsg = "Failed to save instance: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                            android.util.Log.e("ManageTourInstancesFragment", "Error saving instance", e);
                        });
                    }
                    return null;
                });
        }
    }

    private void clearForm() {
        editingInstanceId = null;
        startDateField.setText("");
        endDateField.setText("");
        toggleForm(false);
    }

    private void toggleForm(boolean show) {
        isFormVisible = show;
        formContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        addToggleBtn.setText(show ? "Close Form" : "Add Instance");
        if (show) {
            startDateField.requestFocus();
        }
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>(instancesList));
            return;
        }

        List<TourInstance> filteredInstances = new ArrayList<>();
        for (TourInstance instance : instancesList) {
            if ((instance.getTourName() != null && instance.getTourName().toLowerCase().contains(query)) ||
                (instance.getShipName() != null && instance.getShipName().toLowerCase().contains(query)) ||
                instance.getStartDate().toLowerCase().contains(query)) {
                filteredInstances.add(instance);
            }
        }
        adapter.submitList(new ArrayList<>(filteredInstances));
    }

    private void updateRecyclerView() {
        adapter.submitList(new ArrayList<>(instancesList));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        // If editing and field already has a date, use that date
        String currentDate = currentDateFieldType == START_DATE ? 
                startDateField.getText().toString() : 
                endDateField.getText().toString();
        
        if (!currentDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                calendar.setTime(sdf.parse(currentDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String selectedDate = sdf.format(calendar.getTime());
                    
                    if (currentDateFieldType == START_DATE) {
                        startDateField.setText(selectedDate);
                    } else {
                        endDateField.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
