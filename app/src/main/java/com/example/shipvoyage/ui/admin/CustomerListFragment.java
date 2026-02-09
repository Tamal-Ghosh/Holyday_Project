package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.CustomerAdapter;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerListFragment extends Fragment {
    private RecyclerView customersRecyclerView;
    private Spinner instanceSpinner;
    private EditText searchField;
    private Button searchBtn;
    private UserDAO userDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;
    private List<User> customersList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<TourInstance> instancesList = new ArrayList<>();
    private CustomerAdapter customerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userDAO = new UserDAO(requireContext());
        tourDAO = new TourDAO(requireContext());
        tourInstanceDAO = new TourInstanceDAO(requireContext());
        bookingDAO = new BookingDAO(requireContext());
        roomDAO = new RoomDAO(requireContext());
        
        initViews(view);
        setupListeners();
        loadTours();
        loadCustomers();
    }

    private void initViews(View view) {
        customersRecyclerView = view.findViewById(R.id.customersRecyclerView);
        instanceSpinner = view.findViewById(R.id.instanceSpinner);
        searchField = view.findViewById(R.id.searchField);
        searchBtn = view.findViewById(R.id.searchBtn);

        customersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        customerAdapter = new CustomerAdapter(new CustomerAdapter.OnCustomerClickListener() {
            @Override
            public void onViewClick(User customer) {
                showEditCustomerDialog(customer);
            }

            @Override
            public void onDeleteClick(User customer) {
                userDAO.deleteById(customer.getId())
                    .thenAccept(success -> {
                        if (success && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Customer deleted", Toast.LENGTH_SHORT).show();
                                loadCustomers();
                            });
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete customer", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
            }
        });
        customersRecyclerView.setAdapter(customerAdapter);
    }

    private void setupListeners() {
        searchBtn.setOnClickListener(v -> performSearch());
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

        // Filter when tour instance selection changes
        instanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                performSearch();
            }
        });
    }

    private void loadTours() {
        tourDAO.getAllTours()
            .thenAccept(tours -> {
                if (tours != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toursList.clear();
                        toursList.addAll(tours);
                        loadInstances();
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

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances()
            .thenAccept(instances -> {
                if (instances != null && getActivity() != null) {
                    List<String> instanceNames = new ArrayList<>();
                    instanceNames.add("Select Tour Instance");
                    
                    for (TourInstance instance : instances) {
                        for (Tour tour : toursList) {
                            if (tour.getId().equals(instance.getTourId())) {
                                instance.setTourName(tour.getName());
                                break;
                            }
                        }
                        instanceNames.add(instance.getTourName() + " - " + instance.getStartDate());
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        instancesList.clear();
                        instancesList.addAll(instances);
                        
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, instanceNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        instanceSpinner.setAdapter(adapter);
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load instances", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void loadCustomers() {
        userDAO.getAllUsers()
            .thenAccept(users -> {
                if (users != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        customersList.clear();
                        for (User user : users) {
                            if (user != null && "passenger".equalsIgnoreCase(user.getRole())) {
                                customersList.add(user);
                            }
                        }
                        performSearch();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load customers", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        int selectedPosition = instanceSpinner.getSelectedItemPosition();

        boolean hasValidInstanceSelection = selectedPosition > 0 && (selectedPosition - 1) < instancesList.size();

        if (!hasValidInstanceSelection) {
            // No instance selected: filter only by search
            List<User> filteredList = new ArrayList<>();
            for (User customer : customersList) {
                boolean matchesSearch = query.isEmpty() ||
                        (customer.getName() != null && customer.getName().toLowerCase().contains(query)) ||
                        (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(query)) ||
                        (customer.getPhone() != null && customer.getPhone().contains(query));
                if (matchesSearch) {
                    filteredList.add(customer);
                }
            }
            customerAdapter.submitList(filteredList);
            return;
        }
        
        TourInstance selectedInstance = instancesList.get(selectedPosition - 1);
        String selectedInstanceId = selectedInstance.getId();
        java.util.concurrent.CompletableFuture<List<Booking>> bookingsFuture = bookingDAO.getAllBookings();
        java.util.concurrent.CompletableFuture<List<Room>> roomsFuture = roomDAO.getAllRooms();

        java.util.concurrent.CompletableFuture.allOf(bookingsFuture, roomsFuture)
            .thenAccept(v -> {
                List<Booking> bookings = bookingsFuture.join();
                List<Room> rooms = roomsFuture.join();

                if (bookings != null && getActivity() != null) {
                    java.util.Map<String, Room> roomsMap = new java.util.HashMap<>();
                    if (rooms != null) {
                        for (Room room : rooms) {
                            roomsMap.put(room.getId(), room);
                        }
                    }

                    java.util.LinkedHashMap<String, User> uniqueCustomers = new java.util.LinkedHashMap<>();
                    String instanceLabel = selectedInstance.getTourName() + " - " + selectedInstance.getStartDate();

                    for (Booking booking : bookings) {
                        if (booking == null || !selectedInstanceId.equals(booking.getTourInstanceId())) {
                            continue;
                        }
                        if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("CANCELLED")) {
                            continue;
                        }

                        String name = booking.getName();
                        String email = booking.getEmail();
                        String phone = booking.getPhone();

                        boolean matchesSearch = query.isEmpty() ||
                                (name != null && name.toLowerCase().contains(query)) ||
                                (email != null && email.toLowerCase().contains(query)) ||
                                (phone != null && phone.contains(query));
                        if (!matchesSearch) {
                            continue;
                        }

                        String key = (email != null && !email.isEmpty())
                                ? email.toLowerCase()
                                : (phone != null ? phone : name);

                        if (!uniqueCustomers.containsKey(key)) {
                            Room room = roomsMap.get(booking.getRoomId());
                            User customer = new User();
                            customer.setId(key);
                            customer.setName(name != null ? name : "N/A");
                            customer.setEmail(email != null ? email : "N/A");
                            customer.setPhone(phone != null ? phone : "N/A");
                            customer.setLastInstance(instanceLabel);
                            customer.setRoomType(room != null ? room.getType() : "Room Type");
                            customer.setRoomNumber(room != null ? room.getRoomNumber() : "N/A");
                            customer.setAdultCount(Math.max(0, booking.getAdultCount()));
                            customer.setChildCount(Math.max(0, booking.getChildCount()));
                            String paymentStatus = booking.getDueAmount() > 0 ? "Due" : "Paid";
                            customer.setPaymentStatus(paymentStatus);
                            uniqueCustomers.put(key, customer);
                        }
                    }

                    List<User> filteredList = new ArrayList<>(uniqueCustomers.values());
                    getActivity().runOnUiThread(() -> {
                        customerAdapter.submitList(filteredList);
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to filter by instance", Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void showEditCustomerDialog(User customer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Customer");
        
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Name");
        nameInput.setText(customer.getName());
        layout.addView(nameInput);
        
        final EditText emailInput = new EditText(requireContext());
        emailInput.setHint("Email");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setText(customer.getEmail());
        layout.addView(emailInput);
        
        final EditText phoneInput = new EditText(requireContext());
        phoneInput.setHint("Phone");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setText(customer.getPhone());
        layout.addView(phoneInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            
            userDAO.updateUser(customer)
                .thenAccept(success -> {
                    if (success && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Customer updated successfully", Toast.LENGTH_SHORT).show();
                            loadCustomers();
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to update customer", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
}
