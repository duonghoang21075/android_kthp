package com.example.android_haui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.utils.Constants;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị giao diện chính
        setContentView(R.layout.activity_main);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(this);

        // Kiểm tra database
        checkDatabase();
        
        setupNavigation();
    }
    
    private void setupNavigation() {
        View room1 = findViewById(R.id.room1);
        if (room1 != null) {
            room1.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RoomDetailActivity.class);
                startActivity(intent);
            });
        }
        
        android.widget.LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);
        if (bottomNavLayout != null && bottomNavLayout.getChildCount() > 3) {
            View bottomNavRooms = bottomNavLayout.getChildAt(1); // Index 1 is Phòng học
            bottomNavRooms.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
                startActivity(intent);
            });
            
            View bottomNavStats = bottomNavLayout.getChildAt(2); // Index 2 is Thống kê
            bottomNavStats.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            });
            
            View bottomNavManage = bottomNavLayout.getChildAt(3); // Index 3 is Quản lý
            bottomNavManage.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ManageActivity.class);
                startActivity(intent);
            });
        }
    }

    private void checkDatabase() {
        // Lấy tổng số phòng
        int totalRooms = dbHelper.getTotalRoomCount();

        // Đếm phòng theo trạng thái
        int emptyRooms = dbHelper.countRoomsByStatus(Constants.EMPTY);
        int inUseRooms = dbHelper.countRoomsByStatus(Constants.IN_USE);
        int maintenanceRooms = dbHelper.countRoomsByStatus(Constants.MAINTENANCE);

        // In kết quả ra Logcat
        Log.d("DATABASE_TEST", "Tổng số phòng: " + totalRooms);
        Log.d("DATABASE_TEST", "Phòng trống: " + emptyRooms);
        Log.d("DATABASE_TEST", "Đang sử dụng: " + inUseRooms);
        Log.d("DATABASE_TEST", "Đang bảo trì: " + maintenanceRooms);

        // Kiểm tra kết quả
        if (totalRooms == 15) {
            Toast.makeText(
                    this,
                    "Khởi tạo database thành công!",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Database chưa khởi tạo đúng!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}