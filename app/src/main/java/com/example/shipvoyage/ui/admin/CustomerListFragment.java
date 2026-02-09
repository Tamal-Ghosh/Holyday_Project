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
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.Ship;
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
    private Button exportPdfBtn;
    private UserDAO userDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;
    private ShipDAO shipDAO;
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
        shipDAO = new ShipDAO(requireContext());
        
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
        exportPdfBtn = view.findViewById(R.id.exportPdfBtn);

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
        exportPdfBtn.setOnClickListener(v -> exportCustomersToPdf());
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
        java.util.concurrent.CompletableFuture<List<TourInstance>> instancesFuture = tourInstanceDAO.getAllTourInstances();
        java.util.concurrent.CompletableFuture<List<Ship>> shipsFuture = shipDAO.getAllShips();
        
        java.util.concurrent.CompletableFuture.allOf(instancesFuture, shipsFuture)
            .thenAccept(v -> {
                List<TourInstance> instances = instancesFuture.join();
                List<Ship> ships = shipsFuture.join();
                
                if (instances != null && getActivity() != null) {
                    List<String> instanceNames = new ArrayList<>();
                    instanceNames.add("Select Tour Instance");
                    
                    for (TourInstance instance : instances) {
                        // Set tour name
                        for (Tour tour : toursList) {
                            if (tour.getId().equals(instance.getTourId())) {
                                instance.setTourName(tour.getName());
                                break;
                            }
                        }
                        // Set ship name
                        if (ships != null) {
                            for (Ship ship : ships) {
                                if (ship.getId().equals(instance.getShipId())) {
                                    instance.setShipName(ship.getName());
                                    break;
                                }
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
                            customer.setAdvanceAmount(booking.getPaidAmount());
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

    private void exportCustomersToPdf() {
        List<User> customersToExport = customerAdapter.getCurrentList();
        
        if (customersToExport == null || customersToExport.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create PDF document
            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();
            Paint titlePaint = new Paint();
            Paint headerPaint = new Paint();
            Paint borderPaint = new Paint();
            
            // Page info
            int pageWidth = 595; // A4 width in points
            int pageHeight = 842; // A4 height in points
            int currentPage = 1;
            int margin = 20;
            int startYPos = 80;
            int yPos = startYPos;
            int rowHeight = 80;
            int itemsPerPage = 16; // Always 8 rows x 2 columns
            
            // Calculate column widths
            int corridorWidth = 60;
            int availableWidth = pageWidth - (2 * margin) - corridorWidth;
            int columnWidth = availableWidth / 2;
            int leftColX = margin;
            int rightColX = margin + columnWidth + corridorWidth;
            
            // Paint settings
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(1.5f);
            
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            
            // Get ship name from selected tour instance
            String shipName = "";
            int selectedPosition = instanceSpinner.getSelectedItemPosition();
            if (selectedPosition > 0 && (selectedPosition - 1) < instancesList.size()) {
                TourInstance selectedInstance = instancesList.get(selectedPosition - 1);
                if (selectedInstance.getShipName() != null && !selectedInstance.getShipName().isEmpty()) {
                    shipName = selectedInstance.getShipName();
                }
            }
            
            // Title
            titlePaint.setTextSize(18);
            titlePaint.setFakeBoldText(true);
            if (!shipName.isEmpty()) {
                canvas.drawText("LAYOUT OF " + shipName.toUpperCase(), margin, 40, titlePaint);
            } else {
                canvas.drawText("LAYOUT OF SHIP", margin, 40, titlePaint);
            }
            
            // Tour Date Info
            paint.setTextSize(11);
            String selectedInstance = instanceSpinner.getSelectedItem() != null ? 
                instanceSpinner.getSelectedItem().toString() : "All Customers";
            canvas.drawText("TOUR: " + selectedInstance, margin, 65, paint);
            
            // Draw corridor border from the start
            canvas.drawRect(leftColX + columnWidth, startYPos, leftColX + columnWidth + corridorWidth, startYPos + (rowHeight * 8), borderPaint);
            
            headerPaint.setTextSize(11);
            headerPaint.setFakeBoldText(true);
            paint.setTextSize(10);
            
            int itemCount = 0;
            boolean leftColumn = true;
            int rowCount = 0;
            
            // Always create 16 card grid (8 rows x 2 columns)
            for (int i = 0; i < itemsPerPage; i++) {
                if (itemCount >= itemsPerPage) {
                    pdfDocument.finishPage(page);
                    currentPage++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPos = startYPos;
                    itemCount = 0;
                    leftColumn = true;
                    rowCount = 0;
                    
                    // Draw corridor border on new page
                    canvas.drawRect(leftColX + columnWidth, startYPos, leftColX + columnWidth + corridorWidth, startYPos + (rowHeight * 8), borderPaint);
                }
                
                int colX = leftColumn ? leftColX : rightColX;
                
                // Draw cell border
                canvas.drawRect(colX, yPos, colX + columnWidth, yPos + rowHeight, borderPaint);
                
                // Only fill with data if customer exists
                if (i < customersToExport.size()) {
                    User customer = customersToExport.get(i);
                    
                    // Room Number (large, left side)
                    String roomNo = customer.getRoomNumber() != null ? customer.getRoomNumber() : "N/A";
                    Paint roomNoPaint = new Paint();
                    roomNoPaint.setTextSize(16);
                    roomNoPaint.setFakeBoldText(true);
                    canvas.drawText(roomNo, colX + 5, yPos + 40, roomNoPaint);
                    
                    // Vertical divider after room number
                    canvas.drawLine(colX + 40, yPos, colX + 40, yPos + rowHeight, borderPaint);
                    
                    // Room Type (bold)
                    String roomType = customer.getRoomType() != null ? customer.getRoomType() : "Room Type";
                    canvas.drawText(roomType.length() > 15 ? roomType.substring(0, 15) : roomType, colX + 45, yPos + 12, headerPaint);
                    
                    // Customer Name
                    canvas.drawText("Name:", colX + 45, yPos + 25, paint);
                    String name = customer.getName() != null ? customer.getName() : "N/A";
                    canvas.drawText(name.length() > 14 ? name.substring(0, 14) : name, colX + 80, yPos + 25, paint);
                    
                    // Phone
                    canvas.drawText("Mob:", colX + 45, yPos + 37, paint);
                    String phone = customer.getPhone() != null ? customer.getPhone() : "N/A";
                    canvas.drawText(phone, colX + 75, yPos + 37, paint);
                    
                    // Adult Count
                    canvas.drawText("Adult:", colX + 45, yPos + 49, paint);
                    canvas.drawText(String.valueOf(customer.getAdultCount()), colX + 80, yPos + 49, paint);
                    
                    // Child Count
                    canvas.drawText("Child:", colX + 45, yPos + 61, paint);
                    canvas.drawText(String.valueOf(customer.getChildCount()), colX + 80, yPos + 61, paint);
                    
                    // Advance Amount (separate row at bottom)
                    String advance = String.format("৳%.2f", customer.getAdvanceAmount());
                    canvas.drawText("Adv:", colX + 45, yPos + 73, paint);
                    canvas.drawText(advance, colX + 75, yPos + 73, paint);
                } else {
                    // Empty cell - just draw the divider
                    canvas.drawLine(colX + 40, yPos, colX + 40, yPos + rowHeight, borderPaint);
                }
                
                if (leftColumn) {
                    // Draw CORRIDOR text in middle
                    if (rowCount == 3 || rowCount == 4) { // Draw in middle rows
                        canvas.save();
                        canvas.rotate(-90, leftColX + columnWidth + corridorWidth / 2, yPos + rowHeight / 2);
                        Paint corridorPaint = new Paint();
                        corridorPaint.setTextSize(14);
                        corridorPaint.setFakeBoldText(true);
                        corridorPaint.setColor(0xFFFF6B6B); // Red color
                        canvas.drawText("C O R R I D O R", leftColX + columnWidth + corridorWidth / 2 - 50, yPos + rowHeight / 2, corridorPaint);
                        canvas.restore();
                    }
                    
                    leftColumn = false;
                } else {
                    // Move to next row after right column
                    yPos += rowHeight;
                    leftColumn = true;
                    rowCount++;
                }
                
                itemCount++;
            }
            
            pdfDocument.finishPage(page);
            
            // Save PDF
            String fileName = "CustomerLayout_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            
            // Share/Open PDF
            Toast.makeText(requireContext(), "PDF exported successfully", Toast.LENGTH_SHORT).show();
            openPdf(file);
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openPdf(File file) {
        Uri uri = FileProvider.getUriForFile(requireContext(), 
            requireContext().getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No PDF viewer installed", Toast.LENGTH_SHORT).show();
        }
    }
}
