package com.example.shipvoyage.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.ui.auth.UserTypeActivity;

import java.util.List;

public class AdminProfileFragment extends Fragment {
    private EditText emailField, phoneField, nameField;
    private EditText currentPasswordField, newPasswordField, confirmPasswordField;
    private Button logoutButton, changePasswordButton, changePasswordToggleButton, editButton;
    private LinearLayout changePasswordSection;
    private UserDAO userDAO;
    private String currentUserId;
    private boolean isEditing = false;
    private User userBackup;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userDAO = new UserDAO(requireContext());
        
        initViews(view);
        loadUserProfile();
    }

    private void initViews(View view) {
        emailField = view.findViewById(R.id.emailField);
        phoneField = view.findViewById(R.id.phoneField);
        nameField = view.findViewById(R.id.nameField);
        currentPasswordField = view.findViewById(R.id.currentPasswordField);
        newPasswordField = view.findViewById(R.id.newPasswordField);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordField);
        logoutButton = view.findViewById(R.id.logoutButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        changePasswordToggleButton = view.findViewById(R.id.changePasswordToggleButton);
        editButton = view.findViewById(R.id.editButton);
        changePasswordSection = view.findViewById(R.id.changePasswordSection);

        logoutButton.setOnClickListener(v -> logout());
        changePasswordButton.setOnClickListener(v -> changePassword());
        changePasswordToggleButton.setOnClickListener(v -> toggleChangePasswordSection());
        editButton.setOnClickListener(v -> toggleEditMode());
    }

    private void toggleEditMode() {
        if (!isEditing) {
            isEditing = true;
            userBackup = new User();
            userBackup.setName(nameField.getText().toString());
            userBackup.setEmail(emailField.getText().toString());
            userBackup.setPhone(phoneField.getText().toString());
            
            nameField.setEnabled(true);
            emailField.setEnabled(true);
            phoneField.setEnabled(true);
            editButton.setText("Save");
            editButton.setTag("save");
        } else {
            saveUserProfile();
        }
    }

    private void saveUserProfile() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Name and email are required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        
        userDAO.updateUser(currentUser)
            .thenAccept(success -> {
                if (success && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isEditing = false;
                        nameField.setEnabled(false);
                        emailField.setEnabled(false);
                        phoneField.setEnabled(false);
                        editButton.setText("Edit Profile");
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void loadUserProfile() {
        userDAO.getAllUsers()
            .thenAccept(users -> {
                if (users == null || users.isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "No admin user found", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }
                
                User adminUser = null;
                for (User user : users) {
                    if ("admin".equalsIgnoreCase(user.getRole())) {
                        adminUser = user;
                        break;
                    }
                }
                
                if (adminUser == null) {
                    adminUser = users.get(0);
                }
                
                currentUser = adminUser;
                currentUserId = adminUser.getId();
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        emailField.setText(currentUser.getEmail());
                        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "Not provided");
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void changePassword() {
        String currentPassword = currentPasswordField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getPassword() == null || !currentUser.getPassword().equals(currentPassword)) {
            Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setPassword(newPassword);
        userDAO.updateUser(currentUser)
            .thenAccept(success -> {
                if (success && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    });
                }
            })
            .exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });
    }

    private void toggleChangePasswordSection() {
        if (changePasswordSection.getVisibility() == View.GONE) {
            changePasswordSection.setVisibility(View.VISIBLE);
        } else {
            changePasswordSection.setVisibility(View.GONE);
        }
    }

    private void logout() {
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireActivity(), UserTypeActivity.class));
        requireActivity().finish();
    }
}