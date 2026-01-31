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

    // 1. Declare variables (Termasuk Email)
    private EditText etRegUsername, etRegEmail, etRegPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    // UPDATE IP JIKA PERLU
    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 2. Initialize Views (Link dengan ID dalam XML)
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegEmail = findViewById(R.id.etRegEmail);       // <-- Tambah ini
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // 3. Register Button Logic
        btnRegister.setOnClickListener(v -> {
            String user = etRegUsername.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim(); // <-- Ambil text Email
            String pass = etRegPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // A. Check Empty Fields (Termasuk Email)
            if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // B. Check if Passwords Match
            if (!pass.equals(confirmPass)) {
                etConfirmPassword.setError("Passwords do not match!");
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // C. Proceed to Register (Hantar 3 benda: User, Email, Password)
            registerUser(user, email, pass);
        });

        // 4. Back to Login
        tvLoginLink.setOnClickListener(v -> finish());
    }

    // Function updated untuk terima Email
    private void registerUser(String user, String email, String pass) {
        OkHttpClient client = new OkHttpClient();

        // Hantar data ke PHP
        RequestBody formBody = new FormBody.Builder()
                .add("username", user)
                .add("email", email)
                .add("password", pass)
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL + "register.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Network Error: Check IP or Server", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);

                        // PHP perlu return "status" dan "message"
                        String message = jsonObject.optString("message", "Registration Status Unknown");
                        String status = jsonObject.optString("status", "error");

                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            if (status.equals("success")) {
                                finish(); // Tutup page register, balik ke login
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "JSON Error", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Server Error: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}