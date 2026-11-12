package com.example.android_haui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        setupNavigation();
    }

    private void setupNavigation() {
        android.widget.LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);
        if (bottomNavLayout != null && bottomNavLayout.getChildCount() > 0) {
            View bottomNavOverview = bottomNavLayout.getChildAt(0); // Index 0 is Tổng quan
            bottomNavOverview.setOnClickListener(v -> navigateToOverview());

            View bottomNavRooms = bottomNavLayout.getChildAt(1); // Index 1 is Phòng học
            bottomNavRooms.setOnClickListener(v -> {
                Intent intent = new Intent(StatisticsActivity.this, RoomListActivity.class);
                startActivity(intent);
            });

            View bottomNavManage = bottomNavLayout.getChildAt(3); // Index 3 is Quản lý
            bottomNavManage.setOnClickListener(v -> {
                Intent intent = new Intent(StatisticsActivity.this, ManageActivity.class);
                startActivity(intent);
            });
        }
    }

    private void navigateToOverview() {
        Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}