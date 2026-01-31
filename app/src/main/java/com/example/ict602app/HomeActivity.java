package com.example.ict602app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
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
    private TextView tvUsername, tvCoordinates;
    private LinearLayout cardGps;
    private RecyclerView rvNewsList; // Nama variable diselaraskan dengan XML

    // Store Current Location for calculation
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    private LinearLayout navMap, navNews, navAbout;
    private ImageView iconLogout, btnRefreshNews;

    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvUsername = findViewById(R.id.tvUsername);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        cardGps = findViewById(R.id.cardGps);
        btnRefreshNews = findViewById(R.id.btnRefreshNews);
        iconLogout = findViewById(R.id.iconLogout);

        // Setup RecyclerView
        rvNewsList = findViewById(R.id.rvNewsList); // FIX: Guna ID yang betul
        rvNewsList.setLayoutManager(new LinearLayoutManager(this));

        // Setup Bottom Navigation
        navMap = findViewById(R.id.navMap);
        navNews = findViewById(R.id.navNews); // FIX: Tambah navNews
        navAbout = findViewById(R.id.navAbout);

        // 2. LOGIC USERNAME
        username = getIntent().getStringExtra("USER_NAME");
        if (username == null) username = "Guest";
        tvUsername.setText("Hello, " + username + "!");

        // 3. NAVIGATION LOGIC
        if (navMap != null) navMap.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MapActivity.class)));
        if (navNews != null) navNews.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NewsActivity.class)));
        if (navAbout != null) navAbout.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AboutActivity.class)));

        // Logic Logout (Satu tempat sahaja)
        if (iconLogout != null) {
            iconLogout.setOnClickListener(v -> {
                stopTracking(); // Stop GPS dulu
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear history
                startActivity(intent);
                finish();
            });
        }

        // Logic Refresh News Button
        if (btnRefreshNews != null) {
            btnRefreshNews.setOnClickListener(v -> {
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
                fetchAllHazards();
            });
        }

        // 4. START GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermissionAndStartGPS();

        // Refresh Location manually bila tekan kad GPS
        if (cardGps != null) {
            cardGps.setOnClickListener(v -> {
                Toast.makeText(this, "Refreshing Location...", Toast.LENGTH_SHORT).show();
                checkPermissionAndStartGPS();
            });
        }

        // 5. FETCH HAZARDS
        fetchAllHazards();
    }

    // --- FUNCTION GPS ---
    private void checkPermissionAndStartGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission Denied. GPS Unavailable.", Toast.LENGTH_SHORT).show();
            }
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
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();

                    String coordText = String.format("%.4f, %.4f", currentLat, currentLng);
                    tvCoordinates.setText(coordText);

                    sendLocationToServer(currentLat, currentLng);

                    // Update list supaya jarak (distance) hazard dikira semula dari lokasi baru
                    fetchAllHazards();
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

    private void sendLocationToServer(double lat, double lng) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("username", username).add("latitude", String.valueOf(lat)).add("longitude", String.valueOf(lng)).build();
        Request request = new Request.Builder().url(SERVER_URL + "update_location.php").post(formBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { Log.e("GPS", "Fail: " + e.getMessage()); }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) { response.close(); }
        });
    }

    // --- SERVER: GET HAZARDS (WITH 10KM FILTER) ---
    private void fetchAllHazards() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_URL + "get_hazards.php").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Gson gson = new Gson();

                    try {
                        List<HazardModel> allHazards = gson.fromJson(json, new TypeToken<List<HazardModel>>(){}.getType());
                        List<HazardModel> nearbyHazards = new ArrayList<>();

                        if (allHazards != null) {
                            for (HazardModel h : allHazards) {
                                // 1. Calculate Distance
                                float[] results = new float[1];
                                Location.distanceBetween(currentLat, currentLng, h.lat, h.lng, results);
                                float distanceInMeters = results[0];

                                // 2. Filter: Only show if within 10km OR if GPS not ready yet
                                // (Kalau GPS 0.0, kita tunjuk je semua supaya list tak kosong masa mula-mula)
                                if (currentLat == 0.0 || distanceInMeters <= 10000) {
                                    h.distance = distanceInMeters;
                                    nearbyHazards.add(h);
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            // 3. Set Adapter
                            HazardAdapter adapter = new HazardAdapter(nearbyHazards);
                            rvNewsList.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        Log.e("NEWS", "Error parsing: " + e.getMessage());
                    }
                }
            }
        });
    }

    // --- ADAPTER CLASS (Versi Wow Factor) ---
    public class HazardAdapter extends RecyclerView.Adapter<HazardAdapter.HazardViewHolder> {
        private List<HazardModel> list;

        public HazardAdapter(List<HazardModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public HazardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Guna layout item_news_card
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_card, parent, false);
            return new HazardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HazardViewHolder holder, int position) {
            HazardModel hazard = list.get(position);

            String descLower = hazard.description.toLowerCase();
            String title = "Hazard Alert";

            // --- LOGIC BARU: TUKAR WARNA & ICON ---
            if(descLower.contains("flood")) {
                title = "Flash Flood";
                // Set Biru
                holder.iconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"));
                holder.iconImg.setImageResource(android.R.drawable.ic_menu_compass);
                holder.iconImg.setColorFilter(android.graphics.Color.parseColor("#1E88E5"));

            } else if(descLower.contains("fire")) {
                title = "Fire Reported";
                // Set Oren
                holder.iconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#FBE9E7"));
                holder.iconImg.setImageResource(android.R.drawable.ic_menu_call);
                holder.iconImg.setColorFilter(android.graphics.Color.parseColor("#FF5722"));

            } else if(descLower.contains("accident")) {
                title = "Traffic Accident";
                // Set Merah
                holder.iconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                holder.iconImg.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                holder.iconImg.setColorFilter(android.graphics.Color.parseColor("#D32F2F"));

            } else {
                // Default Hijau
                holder.iconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#E0F2F1"));
                holder.iconImg.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.iconImg.setColorFilter(android.graphics.Color.parseColor("#00695C"));
            }

            holder.tvTitle.setText(title);

            // Format Distance
            float distKm = hazard.distance / 1000;
            String info = String.format("%.1f km away • %s", distKm, hazard.description);
            holder.tvDesc.setText(info);

            holder.tvDate.setText("Latest");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        // --- VIEWHOLDER (WAJIB UPDATE BAHAGIAN INI JUGA) ---
        class HazardViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvDate;
            ImageView iconImg;      // Tambah ini
            CardView iconBg;        // Tambah ini (atau FrameLayout, bergantung XML)

            public HazardViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvHazardTitle);
                tvDesc = itemView.findViewById(R.id.tvHazardDesc);
                tvDate = itemView.findViewById(R.id.tvHazardDate);

                // Cari ID icon dan background dalam item_news_card.xml
                // Kalau dalam XML awak guna FrameLayout, tukar CardView jadi FrameLayout
                iconImg = itemView.findViewById(R.id.imgIcon); // Pastikan tambah ID ni kat XML
                iconBg = itemView.findViewById(R.id.cardIconBg); // Pastikan tambah ID ni kat XML
            }
        }
    }

    // Model Class
    class HazardModel {
        String description;
        double lat;
        double lng;
        float distance; // Helper field
    }
}