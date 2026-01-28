package com.example.ict602app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// GOOGLE MAPS COMPONENTS
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

// GPS COMPONENTS
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

// SERVER COMPONENTS (JSON & OKHTTP)
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

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String username;
    private Marker myMarker; // Marker untuk diri sendiri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. SETUP NAVIGATION & USERNAME
        // (Kita tak ada TextView Welcome dalam XML baru, jadi tak payah set text)
        LinearLayout btnNavLogout = findViewById(R.id.btnNavLogout);
        LinearLayout navAbout = findViewById(R.id.navAbout);

        username = getIntent().getStringExtra("USER_NAME");
        if (username == null) username = "Anonymous";

        // 2. SETUP GOOGLE MAPS
        // Kita cari fragment peta yang kita baru letak dalam XML tadi
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Arahkan dia load peta
        }

        // 3. SETUP BUTANG NAVIGASI
        if (btnNavLogout != null) {
            btnNavLogout.setOnClickListener(v -> {
                stopTracking(); // Stop GPS bila logout
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
        if (navAbout != null) {
            navAbout.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AboutActivity.class)));
        }

        // 4. MULA GPS (Cari lokasi semasa)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermissionAndStartGPS();
    }

    // --- BAHAGIAN 1: BILA PETA DAH SIAP LOAD ---
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true); // Ada butang +/-

        // Tarik data marker lama dari database (untuk tunjuk kawan lain)
        fetchLocationsFromServer();
        fetchHazardsFromServer();
    }

    // --- BAHAGIAN 2: GPS TRACKING ---
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
        // Update lokasi setiap 10 saat
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (android.location.Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    LatLng newPos = new LatLng(lat, lng);

                    // A) Update Marker Kita
                    if (mMap != null) {
                        if (myMarker == null) {
                            // Kalau marker belum ada, buat baru
                            myMarker = mMap.addMarker(new MarkerOptions().position(newPos).title("ME (" + username + ")"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 15f)); // Zoom ke lokasi
                        } else {
                            // Kalau dah ada, gerakkan je
                            myMarker.setPosition(newPos);
                        }
                    }

                    // B) Hantar data ke Server
                    sendLocationToServer(lat, lng);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    // --- BAHAGIAN 3: SERVER SEND (WRITE) ---
    private void sendLocationToServer(double lat, double lng) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("latitude", String.valueOf(lat))
                .add("longitude", String.valueOf(lng))
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/crowdtrack_api/update_location.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { Log.e("MAP_UPLOAD", "Gagal hantar: " + e.getMessage()); }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException { /* Success senyap je */ }
        });
    }

    // --- BAHAGIAN 4: SERVER FETCH (READ) ---
    private void fetchLocationsFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2/crowdtrack_api/get_locations.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Gson gson = new Gson();
                    List<LocationLog> list = gson.fromJson(json, new TypeToken<List<LocationLog>>(){}.getType());

                    runOnUiThread(() -> {
                        if (mMap != null) {
                            for (LocationLog log : list) {
                                try {
                                    double lat = Double.parseDouble(log.getLatitude());
                                    double lng = Double.parseDouble(log.getLongitude());
                                    // Plot Marker Kawan-kawan
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(log.getUsername())
                                            .snippet("Last Seen: " + log.getCreatedAt()));
                                } catch (Exception e) {}
                            }
                        }
                    });
                }
            }
        });
    }

    private void stopTracking() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    private void fetchHazardsFromServer() {
        OkHttpClient client = new OkHttpClient();
        // Using the new PHP file we created
        Request request = new Request.Builder()
                .url("http://10.0.2.2/crowdtrack_api/get_hazards.php") // Ensure path is correct
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("HAZARD_FETCH", "Failed to get hazards: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    // We use a simple list of a custom class or Map
                    Gson gson = new Gson();
                    List<HazardModel> hazardList = gson.fromJson(json, new TypeToken<List<HazardModel>>(){}.getType());

                    runOnUiThread(() -> {
                        if (mMap != null) {
                            for (HazardModel hazard : hazardList) {
                                LatLng pos = new LatLng(hazard.lat, hazard.lng);
                                mMap.addMarker(new MarkerOptions()
                                        .position(pos)
                                        .title("⚠️ HAZARD: " + hazard.description)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // Red color for danger
                            }
                        }
                    });
                }
            }
        });
    }

    // Simple Helper Class (Place this at the bottom of HomeActivity.java or in a new file)
    class HazardModel {
        String description;
        double lat;
        double lng;
    }
}