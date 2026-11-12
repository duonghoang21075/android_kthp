package com.example.android_haui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        setupNavigation();
    }

    private void setupNavigation() {
        // Bottom Nav Listeners
        android.widget.LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);
        if (bottomNavLayout != null && bottomNavLayout.getChildCount() > 0) {
            View bottomNavOverview = bottomNavLayout.getChildAt(0); // Index 0 is Tổng quan
            bottomNavOverview.setOnClickListener(v -> navigateToOverview());

            View bottomNavRooms = bottomNavLayout.getChildAt(1); // Index 1 is Phòng học
            bottomNavRooms.setOnClickListener(v -> {
                Intent intent = new Intent(ManageActivity.this, RoomListActivity.class);
                startActivity(intent);
            });
            
            View bottomNavStats = bottomNavLayout.getChildAt(2); // Index 2 is Thống kê
            bottomNavStats.setOnClickListener(v -> {
                Intent intent = new Intent(ManageActivity.this, StatisticsActivity.class);
                startActivity(intent);
            });
        }

        // Main Action Button (Về tổng quan)
        View btnBackToOverview = findViewById(R.id.btnBackToOverview);
        if (btnBackToOverview != null) {
            btnBackToOverview.setOnClickListener(v -> navigateToOverview());
        }
    }

    private void navigateToOverview() {
        Intent intent = new Intent(ManageActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}