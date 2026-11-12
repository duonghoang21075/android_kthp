package com.example.android_haui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class RoomDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        setupBackButton();
        setupNavigation();
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnBackToList = findViewById(R.id.btnBackToList);
        if (btnBackToList != null) {
            btnBackToList.setOnClickListener(v -> finish());
        }
            
        View btnEditStatus = findViewById(R.id.btnEditStatus);
        if (btnEditStatus != null) {
            btnEditStatus.setOnClickListener(v -> {
                Intent intent = new Intent(RoomDetailActivity.this, EditStatusActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupNavigation() {
        android.widget.LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);
        if (bottomNavLayout != null && bottomNavLayout.getChildCount() > 0) {
            View bottomNavOverview = bottomNavLayout.getChildAt(0); // Index 0 is Tổng quan
            bottomNavOverview.setOnClickListener(v -> {
                Intent intent = new Intent(RoomDetailActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
    }
}