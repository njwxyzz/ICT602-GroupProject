package com.example.ict602app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class NewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMap = findViewById(R.id.navMap);
        LinearLayout navAbout = findViewById(R.id.navAbout);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navMap.setOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, MapActivity.class);
            startActivity(intent);
            finish();
        });

        navAbout.setOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, AboutActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
