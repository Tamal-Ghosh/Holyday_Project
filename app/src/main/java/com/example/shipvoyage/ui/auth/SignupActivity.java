package com.example.shipvoyage.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;

import java.util.UUID;

public class SignupActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private Button signupBtn;
    private TextView loginLink;
    private String selectedRole = "admin"; // Always admin for this app
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        userDAO = new UserDAO(this);
        selectedRole = "admin"; // Force admin role

        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        signupBtn = findViewById(R.id.signupBtn);
        loginLink = findViewById(R.id.loginLink);

        signupBtn.setOnClickListener(v -> attemptSignup());
        loginLink.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String confirm = confirmPasswordField.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Username, email, and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with Supabase
        String uid = UUID.randomUUID().toString();
        User user = new User(uid, username, email, phone, selectedRole);
        user.setPassword(password); // Store password securely in your Supabase

        userDAO.addUser(user).thenAccept(success -> {
            if (success) {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show();
            }
        }).exceptionally(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });
    }
}
