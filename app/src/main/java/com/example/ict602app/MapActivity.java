package com.example.ict602app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;

// UI Imports
import android.app.AlertDialog;
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

// SEARCH
import android.location.Address;
import android.location.Geocoder;
import java.util.Locale;

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
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView tvMapCoords, tvHazardCount;
    private EditText etSearch;

    // --- FIX: THESE VARIABLES WERE MISSING ---
    private CardView cardHazardDetails;
    private TextView tvHazardTitle, tvHazardDesc, tvHazardCoords;
    private ImageView imgHazardIcon;
    private ImageButton btnClosePopup;
    // ----------------------------------------

    // UPDATE YOUR IP HERE IF NEEDED
    private static final String SERVER_URL = "http://10.0.2.2/crowdtrack_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 1. SETUP UI ELEMENTS
        tvMapCoords = findViewById(R.id.tvMapCoords);
        tvHazardCount = findViewById(R.id.tvHazardCount);
        etSearch = findViewById(R.id.etSearch);

        FloatingActionButton fabLayers = findViewById(R.id.fabLayers);
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        FloatingActionButton fabAddHazard = findViewById(R.id.fabAddHazard);
        FloatingActionButton fabZoomIn = findViewById(R.id.fabZoomIn);
        FloatingActionButton fabZoomOut = findViewById(R.id.fabZoomOut);

        // --- POPUP UI INITIALIZATION ---
        cardHazardDetails = findViewById(R.id.cardHazardDetails);
        tvHazardTitle = findViewById(R.id.tvHazardTitle);
        tvHazardDesc = findViewById(R.id.tvHazardDesc);
        tvHazardCoords = findViewById(R.id.tvHazardCoords);
        imgHazardIcon = findViewById(R.id.imgHazardIcon);
        btnClosePopup = findViewById(R.id.btnClosePopup);

        // Hide popup when Close button is clicked
        btnClosePopup.setOnClickListener(v -> cardHazardDetails.setVisibility(android.view.View.GONE));

        // 2. SETUP NAVIGATION BAR
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navNews = findViewById(R.id.navNews);
        LinearLayout navAbout = findViewById(R.id.navAbout);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navNews.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, NewsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navAbout.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, AboutActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // 3. ACTIVATE SEARCH
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String locationName = etSearch.getText().toString();
                searchLocation(locationName);
                return true;
            }
            return false;
        });

        // 4. SETUP BUTTON ACTIONS
        fabZoomIn.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
        });

        fabZoomOut.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        fabMyLocation.setOnClickListener(v -> checkPermissionAndGetLastLocation());
        fabAddHazard.setOnClickListener(v -> showReportDialog());
        fabLayers.setOnClickListener(v -> {
            if (mMap != null) {
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Toast.makeText(this, "Satellite Mode", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Toast.makeText(this, "Normal Mode", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 5. LOAD GOOGLE MAP
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 6. INIT GPS CLIENT
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // --- MARKER CLICK LISTENER ---
        mMap.setOnMarkerClickListener(marker -> {
            // Check if the marker has Hazard data attached
            Object tag = marker.getTag();
            if (tag instanceof HazardPoint) {
                HazardPoint hazard = (HazardPoint) tag;
                showHazardPopup(hazard); // Show our custom card
                return true; // Return true to disable default Google Maps bubble
            }
            return false;
        });

        // Hide popup if user clicks anywhere else on the map
        mMap.setOnMapClickListener(latLng -> cardHazardDetails.setVisibility(android.view.View.GONE));

        LatLng defaultLoc = new LatLng(4.2105, 101.9758);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 6f));

        fetchLocationsFromServer();
        fetchHazardsFromServer();
        checkPermissionAndStartGPS();
    }

    private void searchLocation(String locationName) {
        if (locationName == null || locationName.isEmpty()) return;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                Toast.makeText(this, "Found: " + address.getAddressLine(0), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLocationsFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_URL + "get_locations.php").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { Log.e("MAP", "Error: " + e.getMessage()); }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<LocationLog> users = new Gson().fromJson(json, new TypeToken<List<LocationLog>>(){}.getType());
                    runOnUiThread(() -> {
                        if (mMap != null && users != null) {
                            for (LocationLog user : users) {
                                try {
                                    double lat = Double.parseDouble(user.getLatitude());
                                    double lng = Double.parseDouble(user.getLongitude());
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(user.getUsername())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                } catch (Exception e) {}
                            }
                        }
                    });
                }
            }
        });
    }

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
                    List<HazardPoint> hazards = new Gson().fromJson(json, new TypeToken<List<HazardPoint>>(){}.getType());
                    runOnUiThread(() -> {
                        if (hazards != null) {
                            tvHazardCount.setText(hazards.size() + " Hazards");
                            if (mMap != null) {
                                for (HazardPoint hazard : hazards) {
                                    if (hazard.lat != null && hazard.lng != null) {
                                        float color = BitmapDescriptorFactory.HUE_RED;
                                        String desc = hazard.description.toLowerCase();
                                        if (desc.contains("flood")) color = BitmapDescriptorFactory.HUE_BLUE;
                                        else if (desc.contains("fire")) color = BitmapDescriptorFactory.HUE_ORANGE;
                                        else if (desc.contains("accident")) color = BitmapDescriptorFactory.HUE_YELLOW;

                                        com.google.android.gms.maps.model.Marker m = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(hazard.lat, hazard.lng))
                                                .title(hazard.description)
                                                .icon(BitmapDescriptorFactory.defaultMarker(color)));

                                        // ATTACH DATA
                                        if (m != null) m.setTag(hazard);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void showHazardPopup(HazardPoint hazard) {
        tvHazardDesc.setText(hazard.description);
        tvHazardCoords.setText(String.format("GPS: %.5f, %.5f", hazard.lat, hazard.lng));

        String lowerDesc = hazard.description.toLowerCase();
        if (lowerDesc.contains("flood")) {
            tvHazardTitle.setText("Flood Alert");
            tvHazardTitle.setTextColor(android.graphics.Color.BLUE);
            imgHazardIcon.setImageResource(android.R.drawable.ic_menu_compass);
            imgHazardIcon.setColorFilter(android.graphics.Color.BLUE);
        } else if (lowerDesc.contains("fire")) {
            tvHazardTitle.setText("Fire Hazard");
            tvHazardTitle.setTextColor(android.graphics.Color.RED);
            imgHazardIcon.setImageResource(android.R.drawable.ic_menu_call);
            imgHazardIcon.setColorFilter(android.graphics.Color.RED);
        } else {
            tvHazardTitle.setText("Caution");
            tvHazardTitle.setTextColor(android.graphics.Color.DKGRAY);
            imgHazardIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            imgHazardIcon.setColorFilter(android.graphics.Color.DKGRAY);
        }

        cardHazardDetails.setVisibility(android.view.View.VISIBLE);
        cardHazardDetails.setAlpha(0f);
        cardHazardDetails.animate().alpha(1f).setDuration(300).start();
    }

    // --- GPS LOGIC ---
    private void checkPermissionAndStartGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }

    private void checkPermissionAndGetLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                    String coordText = String.format("%.4f | %.4f", location.getLatitude(), location.getLongitude());
                    tvMapCoords.setText(coordText);
                } else {
                    Toast.makeText(this, "Searching for GPS signal...", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    private void startLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    String coordText = String.format("%.4f | %.4f", location.getLatitude(), location.getLongitude());
                    tvMapCoords.setText(coordText);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionAndStartGPS();
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- HAZARD REPORTING DIALOG ---
    private void showReportDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_report_hazard, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        android.widget.Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        android.widget.EditText etLat = dialogView.findViewById(R.id.etLat);
        android.widget.EditText etLng = dialogView.findViewById(R.id.etLng);
        android.widget.EditText etDescription = dialogView.findViewById(R.id.etDescription);
        android.widget.Button btnGetLocation = dialogView.findViewById(R.id.btnGetLocation);
        android.widget.Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        String[] hazardTypes = {"Flood", "Fire", "Accident", "Construction", "Other"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hazardTypes);
        spinnerType.setAdapter(adapter);

        btnGetLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        etLat.setText(String.valueOf(location.getLatitude()));
                        etLng.setText(String.valueOf(location.getLongitude()));
                    } else {
                        Toast.makeText(this, "GPS signal weak.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String latStr = etLat.getText().toString();
            String lngStr = etLng.getText().toString();
            String details = etDescription.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();

            if (latStr.isEmpty() || lngStr.isEmpty()) {
                Toast.makeText(this, "Enter coords or click 'My GPS'", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double finalLat = Double.parseDouble(latStr);
                double finalLng = Double.parseDouble(lngStr);
                String finalDesc = type + ": " + details;
                sendHazardToServer(finalLat, finalLng, finalDesc);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Coordinates", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void sendHazardToServer(double lat, double lng, String desc) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("latitude", String.valueOf(lat)).add("longitude", String.valueOf(lng)).add("description", desc).build();
        Request request = new Request.Builder().url(SERVER_URL + "add_hazard.php").post(formBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MapActivity.this, "Network Error", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(MapActivity.this, "Reported!", Toast.LENGTH_LONG).show();
                        fetchHazardsFromServer();
                    }
                });
            }
        });
    }

    // Helper Classes
    public class LocationLog {
        private String username;
        private String latitude;
        private String longitude;
        public String getUsername() { return username; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
    }
    public class HazardPoint {
        public Double lat;
        public Double lng;
        public String description;
    }
}
