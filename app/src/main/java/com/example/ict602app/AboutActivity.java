package com.example.ict602app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 1. SETUP NAVIGATION BAR
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMap = findViewById(R.id.navMap);
        LinearLayout navNews = findViewById(R.id.navNews);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navMap.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, MapActivity.class);
            startActivity(intent);
            finish();
        });

        navNews.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, NewsActivity.class);
            startActivity(intent);
            finish();
        });

        // 2. SETUP GITHUB BUTTON
        CardView btnGithub = findViewById(R.id.btnGithub);

        btnGithub.setOnClickListener(v -> {
            // URL GitHub
            String url = "https://github.com/njwxyzz/ICT602-GroupProject.git";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }
}
