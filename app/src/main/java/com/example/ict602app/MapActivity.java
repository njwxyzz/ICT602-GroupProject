package com.example.ict602app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// GOOGLE MAPS
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// GPS
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// NETWORK
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // UI Components
    private TextView tvMapCoords, tvHazardCount;

    // SERVER URL (Pastikan IP ni betul)
    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 1. SETUP UI
        tvMapCoords = findViewById(R.id.tvMapCoords);
        tvHazardCount = findViewById(R.id.tvHazardCount);
        CardView cardSearch = findViewById(R.id.cardSearch);

        FloatingActionButton fabLayers = findViewById(R.id.fabLayers);
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        FloatingActionButton fabAddHazard = findViewById(R.id.fabAddHazard);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navAbout = findViewById(R.id.navAbout);

        // 2. SETUP BUTANG
        navHome.setOnClickListener(v -> { finish(); overridePendingTransition(0, 0); });
        navAbout.setOnClickListener(v -> startActivity(new Intent(MapActivity.this, AboutActivity.class)));

        fabMyLocation.setOnClickListener(v -> {
            if (mMap != null && fusedLocationClient != null) checkPermissionAndGetLastLocation();
        });

        fabAddHazard.setOnClickListener(v -> Toast.makeText(this, "Reporting Hazard...", Toast.LENGTH_SHORT).show());
        fabLayers.setOnClickListener(v -> Toast.makeText(this, "Layer options clicked", Toast.LENGTH_SHORT).show());
        cardSearch.setOnClickListener(v -> Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show());

        // 3. LOAD MAP
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        // Default Lokasi
        LatLng defaultLoc = new LatLng(6.4485, 100.2778);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f));

        // Mula Tarik Data
        fetchLocationsFromServer();
        fetchHazardsFromServer();

        // Mula GPS
        checkPermissionAndStartGPS();
    }

    // --- UPDATED: GUNA CLASS LocationLog AWAK ---
    private void fetchLocationsFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_URL + "get_locations.php").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MAP", "Error fetch users: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Gson gson = new Gson();

                    // Tukar List<LocationModel> -> List<LocationLog>
                    List<LocationLog> users = gson.fromJson(json, new TypeToken<List<LocationLog>>(){}.getType());

                    runOnUiThread(() -> {
                        if (mMap != null && users != null) {
                            for (LocationLog user : users) {
                                try {
                                    // Guna Getter (sebab dalam class awak private)
                                    double lat = Double.parseDouble(user.getLatitude());
                                    double lng = Double.parseDouble(user.getLongitude());

                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(user.getUsername()) // Guna .getUsername()
                                            .snippet("Last seen: " + user.getCreatedAt())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                } catch (Exception e) {}
                            }
                        }
                    });
                }
            }
        });
    }

    // --- UPDATED: GUNA CLASS HazardPoint AWAK ---
    private void fetchHazardsFromServer() {
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

                    // Tukar List<HazardModel> -> List<HazardPoint>
                    List<HazardPoint> hazards = gson.fromJson(json, new TypeToken<List<HazardPoint>>(){}.getType());

                    runOnUiThread(() -> {
                        if (hazards != null) {
                            tvHazardCount.setText(hazards.size() + " Hazards");

                            if (mMap != null) {
                                for (HazardPoint hazard : hazards) {
                                    // Guna field public (sebab dalam class awak public)
                                    // Pastikan database tak return null untuk lat/lng
                                    if (hazard.lat != null && hazard.lng != null) {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(hazard.lat, hazard.lng))
                                                .title(hazard.description) // Guna .description
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    // --- BAHAGIAN GPS SAMA MACAM TADI ---
    private void checkPermissionAndStartGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startLocationUpdates();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void checkPermissionAndGetLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15f));
                }
            });
        }
    }

    private void startLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    String latDir = (location.getLatitude() >= 0) ? "N" : "S";
                    String lngDir = (location.getLongitude() >= 0) ? "E" : "W";
                    String coordText = String.format("%.4f°%s, %.4f°%s",
                            Math.abs(location.getLatitude()), latDir,
                            Math.abs(location.getLongitude()), lngDir);

                    tvMapCoords.setText(coordText);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }
}