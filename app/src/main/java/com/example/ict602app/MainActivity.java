package com.example.ict602app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnSignIn;
    private TextView tvRegister;

    // UPDATE THIS IP ADDRESS
    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvRegister = findViewById(R.id.tvRegister);

        // LOGIN BUTTON CLICK
        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        // REGISTER LINK CLICK
        TextView tvRegister = findViewById(R.id.tvRegister);
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String username, String password) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL + "login.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String status = jsonObject.getString("status");

                        runOnUiThread(() -> {
                            if (status.equals("success")) {
                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                intent.putExtra("USER_NAME", username);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}