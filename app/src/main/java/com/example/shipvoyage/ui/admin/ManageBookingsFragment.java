package com.example.shipvoyage.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

public class ManageBookingsFragment extends Fragment {

    private Spinner tourInstanceSpinner;
    private Button addBookingButton;
    private RecyclerView bookingsRecyclerView;
    private TextView emptyStateText;

    private BookingAdapter bookingAdapter;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;
    private TourDAO tourDAO;
    private ShipDAO shipDAO;

    private List<TourInstance> tourInstances;
    private TourInstance selectedTourInstance;
    private List<Tour> tours;
    private List<Ship> ships;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tourInstanceSpinner = view.findViewById(R.id.tourInstanceSpinner);
        addBookingButton = view.findViewById(R.id.addBookingButton);
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        tourInstanceDAO = new TourInstanceDAO(requireContext());
        bookingDAO = new BookingDAO(requireContext());
        roomDAO = new RoomDAO(requireContext());
        tourDAO = new TourDAO(requireContext());
        shipDAO = new ShipDAO(requireContext());

        tourInstances = new ArrayList<>();
        tours = new ArrayList<>();
        ships = new ArrayList<>();

        // Setup RecyclerView
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
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookingsRecyclerView.setAdapter(bookingAdapter);

        // Load data
        loadToursAndShips();

        addBookingButton.setOnClickListener(v -> showRoomSelectionDialog());

        tourInstanceSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < tourInstances.size()) {
                    selectedTourInstance = tourInstances.get(position);
                    addBookingButton.setEnabled(true);
                    loadBookingsForInstance();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedTourInstance = null;
                addBookingButton.setEnabled(false);
            }
        });
    }

    private void loadToursAndShips() {
        tourDAO.getAllTours().thenAccept(loadedTours -> {
            tours = loadedTours;
            shipDAO.getAllShips().thenAccept(loadedShips -> {
                ships = loadedShips;
                loadTourInstances();
            });
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }

    private void loadTourInstances() {
        tourInstanceDAO.getAllTourInstances().thenAccept(instances -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tourInstances = instances;
                    
                    // Populate transient fields
                    for (TourInstance instance : tourInstances) {
                        for (Tour tour : tours) {
                            if (tour.getId().equals(instance.getTourId())) {
                                instance.setTourName(tour.getName());
                                instance.setFromLocation(tour.getFrom());
                                instance.setToLocation(tour.getTo());
                                break;
                            }
                        }
                        for (Ship ship : ships) {
                            if (ship.getId().equals(instance.getShipId())) {
                                instance.setShipName(ship.getName());
                                break;
                            }
                        }
                    }

                    // Setup spinner
                    List<String> instanceNames = new ArrayList<>();
                    for (TourInstance instance : tourInstances) {
                        instanceNames.add(instance.getTourName() + " - " + instance.getShipName() + 
                                        " (" + instance.getStartDate() + ")");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                            android.R.layout.simple_spinner_item, instanceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    tourInstanceSpinner.setAdapter(adapter);
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load tour instances", Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }

    private void loadBookingsForInstance() {
        if (selectedTourInstance == null) return;

        CompletableFuture<List<Booking>> bookingsFuture = bookingDAO.getBookingsByTourInstance(selectedTourInstance.getId());
        CompletableFuture<List<Room>> roomsFuture = roomDAO.getAllRooms();

        CompletableFuture.allOf(bookingsFuture, roomsFuture).thenAccept(v -> {
            List<Booking> bookings = bookingsFuture.join();
            List<Room> rooms = roomsFuture.join();

            Map<String, Room> roomsMap = new HashMap<>();
            if (rooms != null) {
                for (Room room : rooms) {
                    roomsMap.put(room.getId(), room);
                }
            }

            if (bookings != null) {
                List<Booking> activeBookings = new ArrayList<>();
                for (Booking booking : bookings) {
                    if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("CANCELLED")) {
                        continue;
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
                    activeBookings.add(booking);
                }
                bookings = activeBookings;
            }

            final List<Booking> displayBookings = bookings;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (displayBookings == null || displayBookings.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        bookingsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        bookingsRecyclerView.setVisibility(View.VISIBLE);
                        bookingAdapter.submitList(displayBookings);
                    }
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                });
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
                                    Toast.makeText(getContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                                    loadBookingsForInstance();
                                } else {
                                    Toast.makeText(getContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                loadBookingsForInstance();
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

    private void showRoomSelectionDialog() {
        if (selectedTourInstance == null) return;

        RoomSelectionDialog dialog = RoomSelectionDialog.newInstance(selectedTourInstance);
        dialog.setOnRoomsSelectedListener((selectedRooms, totalPrice) -> {
            showBookingForm(selectedRooms, totalPrice);
        });
        dialog.show(getChildFragmentManager(), "room_selection");
    }

    private void showBookingForm(List<Room> selectedRooms, double totalPrice) {
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

        // Set selected room info
        StringBuilder roomInfo = new StringBuilder();
        for (Room room : selectedRooms) {
            if (roomInfo.length() > 0) roomInfo.append(", ");
            roomInfo.append(room.getRoomNumber()).append(" (").append(room.getType()).append(")");
        }
        selectedRoomInfoText.setText("Selected Rooms: " + roomInfo.toString());

        // Setup payment method spinner
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Bank Transfer", "Mobile Payment"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentAdapter);

        // Auto-calculate due amount in real-time
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

        // Payment Details button handler
        AlertDialog dialog = new AlertDialog.Builder(getContext())
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
                Toast.makeText(getContext(), "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int adultCount = adultCountInput.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(adultCountInput.getText().toString().trim());
                int childCount = childCountInput.getText().toString().trim().isEmpty() ? 0 :
                    Integer.parseInt(childCountInput.getText().toString().trim());
                double total = totalPaymentInput.getText().toString().trim().isEmpty() ? 0 :
                    Double.parseDouble(totalPaymentInput.getText().toString().trim());
                double advance = advanceAmountInput.getText().toString().isEmpty() ? 0 : 
                        Double.parseDouble(advanceAmountInput.getText().toString());
                double due = total - advance;
                String paymentDetails = paymentDetailsInput.getText().toString().trim();

                // Create booking for first selected room
                Room firstRoom = selectedRooms.get(0);
                Booking booking = new Booking(
                        UUID.randomUUID().toString(),
                        selectedTourInstance.getId(),
                        firstRoom.getId(),
                        name,
                        phone,
                        email,
                        paymentMethodSpinner.getSelectedItem().toString(),
                    paymentDetails,
                        total,
                        advance,
                        Math.max(0, due),
                        0, // No discount
                        adultCount,
                        childCount
                );

                saveBooking(booking, dialog);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveBooking(Booking booking, AlertDialog dialog) {
        // Log booking details for debugging
        android.util.Log.d("ManageBookings", "Saving booking: " + 
            "\n  ID: " + booking.getId() +
            "\n  Tour Instance: " + booking.getTourInstanceId() +
            "\n  Room: " + booking.getRoomId() +
            "\n  Name: " + booking.getName() +
            "\n  Phone: " + booking.getPhone() +
            "\n  Email: " + booking.getEmail() +
            "\n  Payment Method: " + booking.getPaymentMethod() +
            "\n  Payment Details: " + booking.getPaymentDetails() +
            "\n  Total: " + booking.getTotalPayment() +
            "\n  Paid: " + booking.getPaidAmount() +
            "\n  Due: " + booking.getDueAmount() +
            "\n  Adults: " + booking.getAdultCount() +
            "\n  Children: " + booking.getChildCount());
        
        bookingDAO.addBooking(booking).thenAccept(success -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Booking saved successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadBookingsForInstance();
                    } else {
                        Toast.makeText(getContext(), "Failed to save booking - Check Logcat for details", Toast.LENGTH_LONG).show();
                        android.util.Log.e("ManageBookings", "Booking save returned false - possible database constraint violation");
                    }
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String errorMsg = throwable.getMessage();
                    Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    android.util.Log.e("ManageBookings", "Booking save exception: ", throwable);
                });
            }
            return null;
        });
    }

    private void deleteBooking(Booking booking) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Booking")
                .setMessage("Are you sure you want to delete this booking?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    bookingDAO.deleteBooking(booking.getId()).thenAccept(success -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(getContext(), "Booking deleted", Toast.LENGTH_SHORT).show();
                                    loadBookingsForInstance();
                                } else {
                                    Toast.makeText(getContext(), "Failed to delete booking", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
