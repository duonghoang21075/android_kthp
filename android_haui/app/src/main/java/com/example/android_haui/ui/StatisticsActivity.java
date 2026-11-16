package com.example.android_haui.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_haui.utils.Constants;

/**
 * Activity chuyển tiếp đến tab Thống kê trên MainActivity để đảm bảo
 * Bottom Navigation luôn cố định và giao diện chuyển đổi mượt mà.
 */
public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.EXTRA_SELECTED_TAB, Constants.TAB_STATS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
