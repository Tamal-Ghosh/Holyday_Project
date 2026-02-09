package com.example.shipvoyage.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.BookingAdapter;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ViewBookingsFragment extends Fragment {
    private RecyclerView bookingsRecyclerView;
    private Spinner tourInstanceSpinner;
    private FloatingActionButton addBookingFab;
    private BookingDAO bookingDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private UserDAO userDAO;
    private RoomDAO roomDAO;
    private List<Booking> bookingsList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<TourInstance> instancesList = new ArrayList<>();
    private Map<String, User> usersMap = new HashMap<>();
    private BookingAdapter bookingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bookingDAO = new BookingDAO(requireContext());
        tourDAO = new TourDAO(requireContext());
        tourInstanceDAO = new TourInstanceDAO(requireContext());
        userDAO = new UserDAO(requireContext());
        roomDAO = new RoomDAO(requireContext());
        
        initViews(view);
        loadTours();
    }

    private void initViews(View view) {
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        tourInstanceSpinner = view.findViewById(R.id.tourInstanceSpinner);
        addBookingFab = view.findViewById(R.id.addBookingFab);

        addBookingFab.setOnClickListener(v -> showTourInstanceSelectionDialog());

        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingAdapter = new BookingAdapter(new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onViewClick(Booking booking) {
                showBookingDetails(booking);
            }

            @Override
            public void onCancelClick(Booking booking) {
                cancelBooking(booking);
            }

            @Override
            public void onEditClick(Booking booking) {
                showEditBookingDialog(booking);
            }
        });
        bookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void loadTours() {
        tourDAO.getAllTours().thenAccept(tours -> {
            if (tours != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    toursList.clear();
                    toursList.addAll(tours);
                    loadInstances();
                });
            }
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().thenAccept(instances -> {
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
                    tourInstanceSpinner.setAdapter(adapter);
                    tourInstanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            filterBookings();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                    loadBookings();
                });
            }
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load instances", Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }

    private void loadBookings() {
        bookingDAO.getAllBookings().thenAccept(bookings -> {
            if (bookings != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    bookingsList.clear();
                    bookingsList.addAll(bookings);
                    fetchCustomerDataForBookings();
                });
            }
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }

    private void fetchCustomerDataForBookings() {
        if (bookingsList.isEmpty()) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::filterBookings);
            }
            return;
        }

        CompletableFuture<List<User>> usersFuture = userDAO.getAllUsers();
        CompletableFuture<List<Room>> roomsFuture = roomDAO.getAllRooms();

        CompletableFuture.allOf(usersFuture, roomsFuture).thenAccept(v -> {
            List<User> users = usersFuture.join();
            List<Room> rooms = roomsFuture.join();

            Map<String, User> usersMap = new HashMap<>();
            if (users != null) {
                for (User user : users) {
                    usersMap.put(user.getId(), user);
                }
            }

            Map<String, Room> roomsMap = new HashMap<>();
            if (rooms != null) {
                for (Room room : rooms) {
                    roomsMap.put(room.getId(), room);
                }
            }

            // Match bookings with their customer and room data
            for (Booking booking : bookingsList) {
                User user = usersMap.get(booking.getUserId());
                if (user != null) {
                    booking.setCustomerName(user.getName() != null ? user.getName() : "N/A");
                    booking.setCustomerEmail(user.getEmail() != null ? user.getEmail() : "N/A");
                    booking.setCustomerPhone(user.getPhone() != null ? user.getPhone() : "N/A");
                }

                Room room = roomsMap.get(booking.getRoomId());
                if (room != null) {
                    booking.setRoomName(room.getRoomNumber());
                    booking.setRoomType(room.getType());
                }

                if (booking.getSelectedRooms() == null || booking.getSelectedRooms().isEmpty()) {
                    if (room != null) {
                        List<String> roomNumbers = new ArrayList<>();
                        roomNumbers.add(room.getRoomNumber());
                        booking.setSelectedRooms(roomNumbers);
                    }
                } else {
                    List<String> mappedRooms = new ArrayList<>();
                    for (String selectedRoomId : booking.getSelectedRooms()) {
                        Room mappedRoom = roomsMap.get(selectedRoomId);
                        mappedRooms.add(mappedRoom != null ? mappedRoom.getRoomNumber() : selectedRoomId);
                    }
                    booking.setSelectedRooms(mappedRooms);
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(this::filterBookings);
            }
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::filterBookings);
            }
            return null;
        });
    }

    private void cancelBooking(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Cancel Booking", (dialog, which) -> {
                    booking.setStatus("CANCELLED");
                    bookingDAO.updateBooking(booking.getId(), booking).thenAccept(success -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
                })
                .setNegativeButton("Keep", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showEditBookingDialog(Booking booking) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_form, null);

        TextView selectedRoomInfoText = dialogView.findViewById(R.id.selectedRoomInfoText);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        TextInputEditText emailInput = dialogView.findViewById(R.id.emailInput);
        TextInputEditText adultCountInput = dialogView.findViewById(R.id.adultCountInput);
        TextInputEditText childCountInput = dialogView.findViewById(R.id.childCountInput);
        Spinner paymentMethodSpinner = dialogView.findViewById(R.id.paymentMethodSpinner);
        TextInputEditText paymentDetailsInput = dialogView.findViewById(R.id.paymentDetailsInput);
        TextInputEditText totalPaymentInput = dialogView.findViewById(R.id.totalPaymentInput);
        TextInputEditText advanceAmountInput = dialogView.findViewById(R.id.advanceAmountInput);
        TextView dueAmountText = dialogView.findViewById(R.id.dueAmountText);

        String roomInfo = booking.getSelectedRoomsString();
        selectedRoomInfoText.setText("Selected Rooms: " + roomInfo);

        totalPaymentInput.setText(String.format("%.2f", booking.getTotalPayment()));

        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Bank Transfer", "Mobile Payment"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentAdapter);

        nameInput.setText(booking.getCustomerName());
        phoneInput.setText(booking.getCustomerPhone());
        emailInput.setText(booking.getCustomerEmail());
        adultCountInput.setText(booking.getAdultCount() > 0 ? String.valueOf(booking.getAdultCount()) : "");
        childCountInput.setText(booking.getChildCount() > 0 ? String.valueOf(booking.getChildCount()) : "");
        paymentDetailsInput.setText(booking.getPaymentDetails() != null ? booking.getPaymentDetails() : "");
        advanceAmountInput.setText(String.format("%.2f", booking.getPaidAmount()));

        for (int i = 0; i < paymentMethods.length; i++) {
            if (paymentMethods[i].equalsIgnoreCase(booking.getPaymentMethod())) {
                paymentMethodSpinner.setSelection(i);
                break;
            }
        }

        TextWatcher calculationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                        double total = totalPaymentInput.getText().toString().isEmpty() ? 0 :
                            Double.parseDouble(totalPaymentInput.getText().toString());

                        double advance = advanceAmountInput.getText().toString().isEmpty() ? 0 :
                            Double.parseDouble(advanceAmountInput.getText().toString());
                        double due = total - advance;
                        dueAmountText.setText(String.format("৳%.2f", Math.max(0, due)));
                } catch (NumberFormatException e) {
                    // Invalid number
                }
            }
        };
                totalPaymentInput.addTextChangedListener(calculationWatcher);
        advanceAmountInput.addTextChangedListener(calculationWatcher);

        // Calculate and display initial due amount
        double initialDue = booking.getTotalPayment() - booking.getPaidAmount();
        dueAmountText.setText(String.format("৳%.2f", Math.max(0, initialDue)));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int adultCount = adultCountInput.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(adultCountInput.getText().toString().trim());
                int childCount = childCountInput.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(childCountInput.getText().toString().trim());
                double total = totalPaymentInput.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(totalPaymentInput.getText().toString());
                double advance = advanceAmountInput.getText().toString().isEmpty() ? 0 :
                        Double.parseDouble(advanceAmountInput.getText().toString());
                double due = total - advance;
                String paymentDetails = paymentDetailsInput.getText().toString().trim();

                booking.setCustomerName(name);
                booking.setCustomerPhone(phone);
                booking.setCustomerEmail(email);
                booking.setPaymentMethod(paymentMethodSpinner.getSelectedItem().toString());
                booking.setPaymentDetails(paymentDetails);
                booking.setAdultCount(adultCount);
                booking.setChildCount(childCount);
                booking.setDiscount(0);
                booking.setTotalPayment(Math.max(0, total));
                booking.setPaidAmount(advance);
                booking.setDueAmount(Math.max(0, due));

                bookingDAO.updateBooking(booking.getId(), booking).thenAccept(success -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(), "Booking updated", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadBookings();
                            } else {
                                Toast.makeText(requireContext(), "Failed to update booking", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showBookingDetails(Booking booking) {
        String details = "Booking ID: " + booking.getId() +
                "\nRoom(s): " + booking.getSelectedRoomsString() +
                "\nName: " + booking.getCustomerName() +
                "\nPhone: " + booking.getCustomerPhone() +
                "\nEmail: " + booking.getCustomerEmail() +
                "\nPayment: " + (booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "N/A") +
                "\nTotal: $" + String.format("%.2f", booking.getTotalPayment()) +
                "\nPaid: $" + String.format("%.2f", booking.getPaidAmount()) +
            "\nDue: $" + String.format("%.2f", booking.getDueAmount());

        new AlertDialog.Builder(requireContext())
                .setTitle("Booking Details")
                .setMessage(details)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void filterBookings() {
        int selectedPosition = tourInstanceSpinner.getSelectedItemPosition();
        if (instancesList == null || instancesList.isEmpty()) {
            bookingAdapter.submitList(new ArrayList<>(bookingsList));
            return;
        }
        
        if (selectedPosition == 0) {
            List<Booking> filteredList = new ArrayList<>();
            for (Booking booking : bookingsList) {
                if (booking.getStatus() == null || !booking.getStatus().equalsIgnoreCase("CANCELLED")) {
                    filteredList.add(booking);
                }
            }
            bookingAdapter.submitList(filteredList);
        } else if (selectedPosition - 1 < instancesList.size()) {
            TourInstance selectedInstance = instancesList.get(selectedPosition - 1);
            List<Booking> filteredList = new ArrayList<>();
            for (Booking booking : bookingsList) {
                if (booking.getTourInstanceId().equals(selectedInstance.getId()) &&
                        (booking.getStatus() == null || !booking.getStatus().equalsIgnoreCase("CANCELLED"))) {
                    filteredList.add(booking);
                }
            }
            bookingAdapter.submitList(filteredList);
        }
    }

    private void showTourInstanceSelectionDialog() {
        if (instancesList.isEmpty()) {
            Toast.makeText(requireContext(), "No tour instances available", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tour_instance_selection, null);
        Spinner instanceSpinner = dialogView.findViewById(R.id.instanceSpinner);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button continueButton = dialogView.findViewById(R.id.continueButton);

        List<String> instanceNames = new ArrayList<>();
        instanceNames.add("None");
        for (TourInstance instance : instancesList) {
            String name = (instance.getTourName() != null ? instance.getTourName() : "Tour") + 
                         " (" + instance.getStartDate() + ")";
            instanceNames.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                android.R.layout.simple_spinner_item, instanceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instanceSpinner.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        continueButton.setOnClickListener(v -> {
            int selectedPosition = instanceSpinner.getSelectedItemPosition();
            if (selectedPosition == 0) {
                dialog.dismiss();
                return;
            }
            int instanceIndex = selectedPosition - 1;
            if (instanceIndex >= 0 && instanceIndex < instancesList.size()) {
                TourInstance selectedInstance = instancesList.get(instanceIndex);
                dialog.dismiss();
                showRoomSelectionDialog(selectedInstance);
            }
        });

        dialog.show();
    }

    private void showRoomSelectionDialog(TourInstance tourInstance) {
        RoomSelectionDialog dialog = RoomSelectionDialog.newInstance(tourInstance);
        dialog.setOnRoomsSelectedListener((selectedRooms, totalPrice) -> {
            showBookingForm(tourInstance, selectedRooms, totalPrice);
        });
        dialog.show(getChildFragmentManager(), "room_selection");
    }

    private void showBookingForm(TourInstance tourInstance, List<Room> selectedRooms, double totalPrice) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_form, null);

        TextView selectedRoomInfoText = dialogView.findViewById(R.id.selectedRoomInfoText);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        TextInputEditText emailInput = dialogView.findViewById(R.id.emailInput);
        TextInputEditText adultCountInput = dialogView.findViewById(R.id.adultCountInput);
        TextInputEditText childCountInput = dialogView.findViewById(R.id.childCountInput);
        Spinner paymentMethodSpinner = dialogView.findViewById(R.id.paymentMethodSpinner);
        TextInputEditText paymentDetailsInput = dialogView.findViewById(R.id.paymentDetailsInput);
        TextInputEditText totalPaymentInput = dialogView.findViewById(R.id.totalPaymentInput);
        TextInputEditText advanceAmountInput = dialogView.findViewById(R.id.advanceAmountInput);
        TextView dueAmountText = dialogView.findViewById(R.id.dueAmountText);

        StringBuilder roomInfo = new StringBuilder();
        for (Room room : selectedRooms) {
            if (roomInfo.length() > 0) roomInfo.append(", ");
            roomInfo.append(room.getRoomNumber()).append(" (").append(room.getType()).append(")");
        }
        selectedRoomInfoText.setText("Selected Rooms: " + roomInfo.toString());

        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Bank Transfer", "Mobile Payment"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(requireContext(), 
                android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentAdapter);

        TextWatcher calculationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                        double total = totalPaymentInput.getText().toString().isEmpty() ? 0 : 
                            Double.parseDouble(totalPaymentInput.getText().toString());

                        double advance = advanceAmountInput.getText().toString().isEmpty() ? 0 : 
                            Double.parseDouble(advanceAmountInput.getText().toString());
                        double due = total - advance;
                        dueAmountText.setText(String.format("৳%.2f", Math.max(0, due)));
                } catch (NumberFormatException e) {
                    // Invalid number
                }
            }
        };
        totalPaymentInput.addTextChangedListener(calculationWatcher);
        advanceAmountInput.addTextChangedListener(calculationWatcher);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int adultCount = adultCountInput.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(adultCountInput.getText().toString().trim());
                int childCount = childCountInput.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(childCountInput.getText().toString().trim());
                double total = totalPaymentInput.getText().toString().isEmpty() ? 0 : 
                    Double.parseDouble(totalPaymentInput.getText().toString());
                double advance = advanceAmountInput.getText().toString().isEmpty() ? 0 : 
                        Double.parseDouble(advanceAmountInput.getText().toString());
                double due = total - advance;
                String paymentDetails = paymentDetailsInput.getText().toString().trim();

                Room firstRoom = selectedRooms.get(0);
                Booking booking = new Booking(
                    java.util.UUID.randomUUID().toString(),
                    tourInstance.getId(),
                    firstRoom.getId(),
                    name,
                    phone,
                    email,
                    paymentMethodSpinner.getSelectedItem().toString(),
                    paymentDetails,
                    Math.max(0, total),
                    advance,
                    Math.max(0, due),
                    0,
                    adultCount,
                    childCount
                );

                saveBooking(booking, dialog);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveBooking(Booking booking, AlertDialog dialog) {
        bookingDAO.addBooking(booking).thenAccept(success -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Booking saved successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadBookings();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save booking", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }
}
