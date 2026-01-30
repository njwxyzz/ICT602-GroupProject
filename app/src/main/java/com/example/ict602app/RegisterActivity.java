package com.example.ict602app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    // UPDATE IP IF NEEDED
    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Initialize Views
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword); // New Field
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // 2. Register Button Logic
        btnRegister.setOnClickListener(v -> {
            String user = etRegUsername.getText().toString().trim();
            String pass = etRegPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // A. Check Empty Fields
            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // B. Check if Passwords Match
            if (!pass.equals(confirmPass)) {
                etConfirmPassword.setError("Passwords do not match!");
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // C. Proceed to Register
            registerUser(user, pass);
        });

        // 3. Back to Login
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void registerUser(String user, String pass) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("username", user)
                .add("password", pass)
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL + "register.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // This happens if the server is unreachable (e.g., wrong IP)
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Network Error: Check IP or Server", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Success! 200 OK
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");
                        String status = jsonObject.getString("status");

                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            if (status.equals("success")) {
                                finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // NEW: Handle Server Errors (404, 500, etc.)
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Server Error: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}