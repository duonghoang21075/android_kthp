package com.example.android_haui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity tìm kiếm phòng học trực tiếp từ CSDL theo từ khóa và trạng thái.
 */
public class RoomSearchActivity extends AppCompatActivity {

    private static final String TAG = "RoomSearchActivity";

    private RoomRepository roomRepository;

    private EditText etSearch;
    private TextView tvResultsCount;
    private TextView tvEmptySearch;
    private LinearLayout layoutSearchResults;

    private TextView filterAll;
    private TextView filterEmpty;
    private TextView filterInUse;
    private TextView filterMaint;

    private String currentQuery = "";
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search);

        roomRepository = RoomRepository.getInstance(this);

        HeaderHelper.setup(findViewById(R.id.headerLayout), R.string.search_title, R.string.search_subtitle, this::finish);
        initViews();
        setupNavigation();
        setupListeners();
        performSearch();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);
        layoutSearchResults = findViewById(R.id.layoutSearchResults);

        filterAll = findViewById(R.id.filterAll);
        filterEmpty = findViewById(R.id.filterEmpty);
        filterInUse = findViewById(R.id.filterInUse);
        filterMaint = findViewById(R.id.filterMaint);
    }

    private void setupNavigation() {
        View bottomNavView = findViewById(R.id.layoutBottomNav);
        if (bottomNavView != null) {
            BottomNavHelper.setup(bottomNavView, Constants.TAB_ROOMS, tabIndex -> {
                Intent intent = new Intent(RoomSearchActivity.this, MainActivity.class);
                intent.putExtra(Constants.EXTRA_SELECTED_TAB, tabIndex);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupListeners() {
        // 1. Text watcher cho ô tìm kiếm
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s != null ? s.toString().trim() : "";
                    performSearch();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // 2. Chips gợi ý tìm kiếm nhanh
        View chipLab = findViewById(R.id.chipLab);
        if (chipLab != null) {
            chipLab.setOnClickListener(v -> {
                if (etSearch != null) etSearch.setText(R.string.suggestion_lab);
            });
        }

        View chip101 = findViewById(R.id.chip101);
        if (chip101 != null) {
            chip101.setOnClickListener(v -> {
                if (etSearch != null) etSearch.setText(R.string.suggestion_101);
            });
        }

        View chipComputer = findViewById(R.id.chipComputer);
        if (chipComputer != null) {
            chipComputer.setOnClickListener(v -> {
                if (etSearch != null) etSearch.setText(R.string.suggestion_computer);
            });
        }

        View chipAll = findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                if (etSearch != null) etSearch.setText("");
            });
        }

        // 3. Nút đặt lại bộ lọc
        View btnResetFilters = findViewById(R.id.btnResetFilters);
        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> {
                if (etSearch != null) etSearch.setText("");
                selectFilter("ALL");
            });
        }

        // 4. Các nút lọc trạng thái
        if (filterAll != null) filterAll.setOnClickListener(v -> selectFilter("ALL"));
        if (filterEmpty != null) filterEmpty.setOnClickListener(v -> selectFilter("EMPTY"));
        if (filterInUse != null) filterInUse.setOnClickListener(v -> selectFilter("IN_USE"));
        if (filterMaint != null) filterMaint.setOnClickListener(v -> selectFilter("MAINT"));
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        updateFilterTabsUI();
        performSearch();
    }

    private void updateFilterTabsUI() {
        int activeBg = R.drawable.bg_filter_active;
        int inactiveBg = R.drawable.bg_filter_inactive;
        int activeText = ContextCompat.getColor(this, R.color.white);
        int inactiveText = ContextCompat.getColor(this, R.color.text_filter_inactive);

        if (filterAll != null) {
            boolean active = "ALL".equals(currentFilter);
            filterAll.setBackground(ContextCompat.getDrawable(this, active ? activeBg : inactiveBg));
            filterAll.setTextColor(active ? activeText : inactiveText);
        }
        if (filterEmpty != null) {
            boolean active = "EMPTY".equals(currentFilter);
            filterEmpty.setBackground(ContextCompat.getDrawable(this, active ? activeBg : inactiveBg));
            filterEmpty.setTextColor(active ? activeText : inactiveText);
        }
        if (filterInUse != null) {
            boolean active = "IN_USE".equals(currentFilter);
            filterInUse.setBackground(ContextCompat.getDrawable(this, active ? activeBg : inactiveBg));
            filterInUse.setTextColor(active ? activeText : inactiveText);
        }
        if (filterMaint != null) {
            boolean active = "MAINT".equals(currentFilter);
            filterMaint.setBackground(ContextCompat.getDrawable(this, active ? activeBg : inactiveBg));
            filterMaint.setTextColor(active ? activeText : inactiveText);
        }
    }

    private void performSearch() {
        roomRepository.searchRooms(currentQuery, new RepositoryCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> allMatches) {
                if (isFinishing() || isDestroyed()) return;

                List<Room> filtered = new ArrayList<>();
                for (Room r : allMatches) {
                    if (matchesFilter(r)) {
                        filtered.add(r);
                    }
                }
                displayResults(filtered);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi tìm kiếm phòng: " + e.getMessage(), e);
            }
        });
    }

    private boolean matchesFilter(Room room) {
        if ("ALL".equals(currentFilter)) return true;
        String s = room.getStatus();
        if (s == null) return false;

        if ("EMPTY".equals(currentFilter)) {
            return Constants.STATUS_EMPTY_VN.equalsIgnoreCase(s) || Constants.EMPTY.equalsIgnoreCase(s);
        } else if ("IN_USE".equals(currentFilter)) {
            return Constants.STATUS_IN_USE_VN.equalsIgnoreCase(s) || Constants.IN_USE.equalsIgnoreCase(s);
        } else if ("MAINT".equals(currentFilter)) {
            return Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(s) || Constants.MAINTENANCE.equalsIgnoreCase(s);
        }
        return true;
    }

    private void displayResults(List<Room> rooms) {
        if (layoutSearchResults == null) return;
        layoutSearchResults.removeAllViews();

        int count = rooms != null ? rooms.size() : 0;
        if (tvResultsCount != null) {
            tvResultsCount.setText(String.valueOf(count));
        }

        if (count == 0) {
            if (tvEmptySearch != null) tvEmptySearch.setVisibility(View.VISIBLE);
            return;
        }

        if (tvEmptySearch != null) tvEmptySearch.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Room room : rooms) {
            View itemView = inflater.inflate(R.layout.item_recent_room, layoutSearchResults, false);
            RoomItemHelper.bindRoomCard(
                    this,
                    itemView,
                    room,
                    v -> {
                        Intent intent = new Intent(RoomSearchActivity.this, RoomDetailActivity.class);
                        intent.putExtra(Constants.EXTRA_ROOM_ITEM, room);
                        intent.putExtra(Constants.EXTRA_ROOM_CODE, room.getRoomCode());
                        startActivity(intent);
                    },
                    v -> {
                        DeleteConfirmDialogHelper.show(RoomSearchActivity.this, room, () -> {
                            roomRepository.deleteRoom(room.getRoomCode(), new RepositoryCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean deleted) {
                                    if (isFinishing() || isDestroyed()) return;
                                    if (Boolean.TRUE.equals(deleted)) {
                                        Toast.makeText(RoomSearchActivity.this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
                                        performSearch();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "Lỗi xóa phòng: " + e.getMessage());
                                }
                            });
                        });
                        return true;
                    }
            );
            layoutSearchResults.addView(itemView);
        }
    }
}
