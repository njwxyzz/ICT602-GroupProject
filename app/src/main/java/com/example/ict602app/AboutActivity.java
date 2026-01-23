package com.example.ict602app;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        try {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(android.graphics.Color.parseColor("#0A2342"));
        } catch (Exception e) {
            // Kalau phone lama tak support, abaikan je.
        }

        // Setup Action Bar (Butang Back kat atas)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Kosongkan tajuk sebab dah ada kat header besar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0); // Hilangkan bayang supaya bercantum dengan header
            // Tukar warna background Action Bar jadi biru tua juga
            getSupportActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#0A2342")));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Tutup page bila tekan back
        return true;
    }
}