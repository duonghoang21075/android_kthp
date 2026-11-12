package com.example.android_haui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class RoomSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search);

        setupNavigation();
        setupBackButton();
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupNavigation() {
        View room1 = findViewById(R.id.room1);
        if (room1 != null) {
            room1.setOnClickListener(v -> {
                Intent intent = new Intent(RoomSearchActivity.this, RoomDetailActivity.class);
                startActivity(intent);
            });
        }
        
        android.widget.LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);
        if (bottomNavLayout != null && bottomNavLayout.getChildCount() > 3) {
            View bottomNavOverview = bottomNavLayout.getChildAt(0); // Index 0 is Tổng quan
            bottomNavOverview.setOnClickListener(v -> {
                Intent intent = new Intent(RoomSearchActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
            
            View bottomNavStats = bottomNavLayout.getChildAt(2); // Index 2 is Thống kê
            bottomNavStats.setOnClickListener(v -> {
                Intent intent = new Intent(RoomSearchActivity.this, StatisticsActivity.class);
                startActivity(intent);
            });
            
            View bottomNavManage = bottomNavLayout.getChildAt(3); // Index 3 is Quản lý
            bottomNavManage.setOnClickListener(v -> {
                Intent intent = new Intent(RoomSearchActivity.this, ManageActivity.class);
                startActivity(intent);
            });
        }
    }
}