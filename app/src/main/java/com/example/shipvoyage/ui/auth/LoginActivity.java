package com.example.shipvoyage.ui.auth;

import android.content.Intent;
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
import com.example.shipvoyage.ui.admin.AdminMainActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailField;
    private EditText passwordField;
    private Button loginBtn;
    private TextView signupLink;
    private String selectedRole = "admin"; // Always admin
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        selectedRole = "admin"; // Force admin role
        userDAO = new UserDAO(this);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signupLink);

        loginBtn.setOnClickListener(v -> attemptLogin());
        signupLink.setOnClickListener(v -> openSignup());
    }

    private void attemptLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate with Supabase
        userDAO.getUserByEmail(email).thenAccept(users -> {
            if (users != null && !users.isEmpty()) {
                User user = users.get(0);
                // Verify password (in production, use proper password hashing)
                if (user.getPassword() != null && user.getPassword().equals(password)) {
                    if ("admin".equalsIgnoreCase(user.getRole())) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        navigateToAdmin();
                    } else {
                        Toast.makeText(this, "Only admin accounts can login", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).exceptionally(e -> {
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    private void navigateToAdmin() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra("role", "admin");
        startActivity(intent);
    }
}
