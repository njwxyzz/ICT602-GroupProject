package com.example.ict602app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import untuk check log
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // 1. Declare variables
    private EditText etUsername, etPassword;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle full screen insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Initialize Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // 3. Logic Button Click
        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Check kosong ke tak
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            } else {
                // Success!
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // Debug Log: Boleh tengok kat Logcat (bawah Android Studio) nama apa yang dihantar
                Log.d("LOGIN_STATUS", "Sending Username: " + username);

                // Navigate ke HomeActivity
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);

                // --- PENTING: KUNCI INI MESTI SAMA DENGAN HOMEACTIVITY ---
                intent.putExtra("USER_NAME", username);

                startActivity(intent);
                finish(); // Tutup page login supaya tak boleh back button
            }
        });
    }
}