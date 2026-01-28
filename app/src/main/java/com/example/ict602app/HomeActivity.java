package com.example.ict602app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// GPS COMPONENTS
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

// SERVER COMPONENTS
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String username;

    // UI Components
    private TextView tvUsername, tvCoordinates, tvGpsStatus;
    private TextView tvNewsTitle;
    private LinearLayout navMap;
    private ImageView iconLogout;

    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. SETUP UI COMPONENTS
        tvUsername = findViewById(R.id.tvUsername);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        tvGpsStatus = findViewById(R.id.tvGpsLabel);
        tvNewsTitle = findViewById(R.id.tvNewsTitle);
        navMap = findViewById(R.id.navMap);
        iconLogout = findViewById(R.id.iconLogout);

        // 2. LOGIC USERNAME
        username = getIntent().getStringExtra("USER_NAME");

        if (username == null) {
            username = "Guest";
        }
        tvUsername.setText("Hello, " + username + "!");

        // 3. LOGIC BUTTON MAP
        if (navMap != null) {
            navMap.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);
            });
        }

        if (iconLogout != null) {
            iconLogout.setOnClickListener(v -> {
                stopTracking();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // 4. MULA GPS TRACKING
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermissionAndStartGPS();

        // 5. TARIK INFO HAZARD
        fetchLatestHazard(); // Kita cuma PANGGIL function ni kat sini, bukan define dia.
    }

    // --- FUNCTION GPS ---
    private void checkPermissionAndStartGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    // Update UI Dashboard
                    String latDir = (lat >= 0) ? "N" : "S";
                    String lngDir = (lng >= 0) ? "E" : "W";
                    String coordText = String.format("%.4f°%s, %.4f°%s", Math.abs(lat), latDir, Math.abs(lng), lngDir);
                    tvCoordinates.setText(coordText);

                    // Hantar ke Server
                    sendLocationToServer(lat, lng);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopTracking() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // --- FUNCTION SERVER (SEND LOCATION) ---
    private void sendLocationToServer(double lat, double lng) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("latitude", String.valueOf(lat))
                .add("longitude", String.valueOf(lng))
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL + "update_location.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { Log.e("GPS", "Fail send: " + e.getMessage()); }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) { response.close(); }
        });
    }

    // --- FUNCTION SERVER (GET HAZARD) ---
    // (Function ni mesti duduk LUAR onCreate)
    private void fetchLatestHazard() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_URL + "get_hazards.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Gson gson = new Gson();

                    try {
                        List<HazardModel> hazards = gson.fromJson(json, new TypeToken<List<HazardModel>>(){}.getType());

                        if (hazards != null && !hazards.isEmpty()) {
                            // Ambil hazard paling latest
                            HazardModel latest = hazards.get(hazards.size() - 1);

                            runOnUiThread(() -> {
                                // Update text
                                if (tvNewsTitle != null) {
                                    tvNewsTitle.setText("ALERT: " + latest.description);
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e("NEWS", "Error parsing: " + e.getMessage());
                    }
                }
            }
        });
    }

    // Model Class
    class HazardModel {
        String description;
        double lat;
        double lng;
    }
}