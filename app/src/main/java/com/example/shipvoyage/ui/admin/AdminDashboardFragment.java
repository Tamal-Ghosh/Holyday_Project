package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {
    private static final String TAG = "AdminDashboardFragment";

    private TextView lblTotalShips, lblTotalTours, lblTourInstances;
    private TextView lblUpcomingTours, lblCurrentTours, lblTotalBookings, lblTotalCustomers;

    private ShipDAO shipDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private UserDAO userDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shipDAO = new ShipDAO(requireContext());
        tourDAO = new TourDAO(requireContext());
        tourInstanceDAO = new TourInstanceDAO(requireContext());
        bookingDAO = new BookingDAO(requireContext());
        userDAO = new UserDAO(requireContext());

        initViews(view);

        loadDashboardData();
    }

    private void initViews(View view) {
        lblTotalShips = view.findViewById(R.id.lblTotalShips);
        lblTotalTours = view.findViewById(R.id.lblTotalTours);
        lblTourInstances = view.findViewById(R.id.lblTourInstances);
        lblUpcomingTours = view.findViewById(R.id.lblUpcomingTours);
        lblCurrentTours = view.findViewById(R.id.lblCurrentTours);
        lblTotalBookings = view.findViewById(R.id.lblTotalBookings);
        lblTotalCustomers = view.findViewById(R.id.lblTotalCustomers);

        NavController navController = Navigation.findNavController(view);

        Button btnViewShips = view.findViewById(R.id.btnViewShips);
        btnViewShips.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageShipsFragment)
        );

        Button btnViewTours = view.findViewById(R.id.btnViewTours);
        btnViewTours.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageToursFragment)
        );

        Button btnViewInstances = view.findViewById(R.id.btnViewInstances);
        btnViewInstances.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageTourInstancesFragment)
        );

        Button btnViewUpcoming = view.findViewById(R.id.btnViewUpcoming);
        btnViewUpcoming.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageTourInstancesFragment)
        );

        Button btnViewCurrent = view.findViewById(R.id.btnViewCurrent);
        btnViewCurrent.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageTourInstancesFragment)
        );

        Button btnViewBookings = view.findViewById(R.id.btnViewBookings);
        btnViewBookings.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_viewBookingsFragment)
        );

        Button btnViewCustomers = view.findViewById(R.id.btnViewCustomers);
        btnViewCustomers.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_customerListFragment)
        );
    }

    private void loadDashboardData() {
        loadShipsCount();
        loadToursCount();
        loadTourInstancesCount();
        loadBookingsCount();
        loadCustomersCount();
    }

    private void loadShipsCount() {
        shipDAO.getAllShips()
            .thenAccept(ships -> {
                if (ships != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        lblTotalShips.setText(String.valueOf(ships.size()));
                    });
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error loading ships count: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> lblTotalShips.setText("0"));
                }
                return null;
            });
    }

    private void loadToursCount() {
        tourDAO.getAllTours()
            .thenAccept(tours -> {
                if (tours != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        lblTotalTours.setText(String.valueOf(tours.size()));
                    });
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error loading tours count: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> lblTotalTours.setText("0"));
                }
                return null;
            });
    }

    private void loadTourInstancesCount() {
        tourInstanceDAO.getAllTourInstances()
            .thenAccept(instances -> {
                if (instances == null || getActivity() == null) return;
                
                int upcomingCount = 0;
                int currentCount = 0;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date now = new Date();

                for (TourInstance instance : instances) {
                    String startDateStr = instance.getStartDate();
                    String endDateStr = instance.getEndDate();

                    if (startDateStr != null && endDateStr != null) {
                        try {
                            Date startDate = sdf.parse(startDateStr);
                            Date endDate = sdf.parse(endDateStr);

                            if (startDate != null && endDate != null) {
                                if (now.before(startDate)) {
                                    upcomingCount++;
                                } else if (now.after(startDate) && now.before(endDate)) {
                                    currentCount++;
                                }
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing date: " + e.getMessage());
                        }
                    }
                }

                final int upcoming = upcomingCount;
                final int current = currentCount;
                getActivity().runOnUiThread(() -> {
                    lblTourInstances.setText(String.valueOf(instances.size()));
                    lblUpcomingTours.setText(String.valueOf(upcoming));
                    lblCurrentTours.setText(String.valueOf(current));
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error loading tour instances: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        lblTourInstances.setText("0");
                        lblUpcomingTours.setText("0");
                        lblCurrentTours.setText("0");
                    });
                }
                return null;
            });
    }

    private void loadBookingsCount() {
        bookingDAO.getAllBookings()
            .thenAccept(bookings -> {
                if (bookings != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        lblTotalBookings.setText(String.valueOf(bookings.size()));
                    });
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error loading bookings count: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> lblTotalBookings.setText("0"));
                }
                return null;
            });
    }

    private void loadCustomersCount() {
        userDAO.getAllUsers()
            .thenAccept(users -> {
                if (users != null && getActivity() != null) {
                    int customerCount = 0;
                    for (User user : users) {
                        if ("passenger".equalsIgnoreCase(user.getRole())) {
                            customerCount++;
                        }
                    }
                    final int count = customerCount;
                    getActivity().runOnUiThread(() -> {
                        lblTotalCustomers.setText(String.valueOf(count));
                    });
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error loading customers count: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> lblTotalCustomers.setText("0"));
                }
                return null;
            });
    }
}
