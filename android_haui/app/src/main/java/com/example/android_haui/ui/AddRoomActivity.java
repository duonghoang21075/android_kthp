package com.example.android_haui.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_haui.R;
import com.example.android_haui.database.RoomDao;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;
import com.example.android_haui.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Thêm phòng học mới.
 * Tích hợp dữ liệu tầng và loại phòng thực tế từ CSDL SQLite,
 * tối ưu UI/UX với hộp thoại chọn bo góc hiện đại, phản hồi tức thì và không bị khựng lag.
 */
public class AddRoomActivity extends AppCompatActivity {

    private static final String TAG = "AddRoomActivity";

    private EditText etRoomCode;
    private EditText etRoomName;
    private TextView tvSelectedFloor;
    private TextView tvSelectedType;
    private EditText etCapacity;
    private FrameLayout badgeInitialStatus;
    private TextView tvInitialStatus;
    private EditText etEquipment;
    private EditText etNote;

    private RoomRepository roomRepository;

    private int selectedFloor = 1;
    private String selectedType = "Lý thuyết";
    private String selectedStatus = Constants.STATUS_EMPTY_VN;

    // Dữ liệu thống kê số phòng thực tế theo tầng và loại phòng từ CSDL
    private final List<RoomDao.FloorStat> dbFloorStats = new ArrayList<>();
    private final List<RoomDao.TypeStat> dbTypeStats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        roomRepository = RoomRepository.getInstance(this);

        HeaderHelper.setup(
                findViewById(R.id.headerLayout),
                R.string.add_room_title,
                this::finish
        );

        initViews();
        setupListeners();
        loadDatabaseOptions();
    }

    private void initViews() {
        etRoomCode = findViewById(R.id.etRoomCode);
        etRoomName = findViewById(R.id.etRoomName);
        tvSelectedFloor = findViewById(R.id.tvSelectedFloor);
        tvSelectedType = findViewById(R.id.tvSelectedType);
        etCapacity = findViewById(R.id.etCapacity);
        badgeInitialStatus = findViewById(R.id.badgeInitialStatus);
        tvInitialStatus = findViewById(R.id.tvInitialStatus);
        etEquipment = findViewById(R.id.etEquipment);
        etNote = findViewById(R.id.etNote);

        RoomItemHelper.applyStatusBadge(this, badgeInitialStatus, tvInitialStatus, selectedStatus);
    }

    private void setupListeners() {
        View btnCancel = findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        View btnSelectFloor = findViewById(R.id.btnSelectFloor);
        if (btnSelectFloor != null) {
            btnSelectFloor.setOnClickListener(v -> showFloorPicker());
        }

        View btnSelectType = findViewById(R.id.btnSelectType);
        if (btnSelectType != null) {
            btnSelectType.setOnClickListener(v -> showRoomTypePicker());
        }

        View btnSelectStatus = findViewById(R.id.btnSelectStatus);
        if (btnSelectStatus != null) {
            btnSelectStatus.setOnClickListener(v -> showStatusPicker());
        }

        View chipEquipTheory = findViewById(R.id.chipEquipTheory);
        if (chipEquipTheory != null) {
            chipEquipTheory.setOnClickListener(v -> {
                if (etEquipment != null) {
                    etEquipment.setText(R.string.add_room_equipment_val_1);
                }
            });
        }

        View chipEquipLab = findViewById(R.id.chipEquipLab);
        if (chipEquipLab != null) {
            chipEquipLab.setOnClickListener(v -> {
                if (etEquipment != null) {
                    etEquipment.setText(R.string.add_room_equipment_val_2);
                }
            });
        }

        View chipEquipHall = findViewById(R.id.chipEquipHall);
        if (chipEquipHall != null) {
            chipEquipHall.setOnClickListener(v -> {
                if (etEquipment != null) {
                    etEquipment.setText(R.string.add_room_equipment_val_3);
                }
            });
        }

        View btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveRoom());
        }
    }

    /**
     * Tải danh sách tầng và loại phòng thực tế từ cơ sở dữ liệu.
     */
    private void loadDatabaseOptions() {
        roomRepository.getFloorStatistics(new RepositoryCallback<List<RoomDao.FloorStat>>() {
            @Override
            public void onSuccess(List<RoomDao.FloorStat> stats) {
                if (isFinishing() || isDestroyed()) return;
                dbFloorStats.clear();
                if (stats != null) {
                    dbFloorStats.addAll(stats);
                    boolean foundCurrent = false;
                    for (RoomDao.FloorStat s : stats) {
                        if (s.floor == selectedFloor) {
                            foundCurrent = true;
                            break;
                        }
                    }
                    if (!foundCurrent && !stats.isEmpty()) {
                        selectedFloor = stats.get(0).floor;
                        if (tvSelectedFloor != null) {
                            tvSelectedFloor.setText("Tầng " + selectedFloor);
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Không thể tải danh sách tầng từ CSDL: " + e.getMessage());
            }
        });

        roomRepository.getTypeStatistics(new RepositoryCallback<List<RoomDao.TypeStat>>() {
            @Override
            public void onSuccess(List<RoomDao.TypeStat> stats) {
                if (isFinishing() || isDestroyed()) return;
                dbTypeStats.clear();
                if (stats != null) {
                    dbTypeStats.addAll(stats);
                    boolean foundCurrent = false;
                    for (RoomDao.TypeStat s : stats) {
                        if (s.type != null && s.type.equalsIgnoreCase(selectedType)) {
                            foundCurrent = true;
                            break;
                        }
                    }
                    if (!foundCurrent && !stats.isEmpty() && stats.get(0).type != null) {
                        selectedType = stats.get(0).type;
                        if (tvSelectedType != null) {
                            tvSelectedType.setText(selectedType);
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Không thể tải danh sách loại phòng từ CSDL: " + e.getMessage());
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }

    /**
     * Hiển thị danh sách tầng chỉ gồm các tầng đã có trong database.
     */
    private void showFloorPicker() {
        hideKeyboard();
        if (dbFloorStats.isEmpty()) {
            roomRepository.getFloorStatistics(new RepositoryCallback<List<RoomDao.FloorStat>>() {
                @Override
                public void onSuccess(List<RoomDao.FloorStat> stats) {
                    if (isFinishing() || isDestroyed()) return;
                    dbFloorStats.clear();
                    if (stats != null) {
                        dbFloorStats.addAll(stats);
                    }
                    displayFloorPicker();
                }

                @Override
                public void onError(Exception e) {
                    displayFloorPicker();
                }
            });
            return;
        }
        displayFloorPicker();
    }

    private void displayFloorPicker() {
        List<OptionPickerHelper.OptionItem> items = new ArrayList<>();

        // Chỉ hiển thị các tầng thực tế đã có trong database
        for (RoomDao.FloorStat stat : dbFloorStats) {
            boolean isSelected = (stat.floor == selectedFloor);
            items.add(new OptionPickerHelper.OptionItem(
                    String.valueOf(stat.floor),
                    "Tầng " + stat.floor,
                    stat.roomCount + " phòng trong hệ thống",
                    isSelected
            ));
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "Chưa có dữ liệu tầng trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        OptionPickerHelper.show(
                this,
                "Chọn tầng phòng học",
                null,
                items,
                (item, index) -> {
                    try {
                        selectedFloor = Integer.parseInt(item.id);
                        if (tvSelectedFloor != null) {
                            tvSelectedFloor.setText("Tầng " + selectedFloor);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
        );
    }

    /**
     * Hiển thị danh sách loại phòng chỉ gồm các loại phòng đã có trong database.
     */
    private void showRoomTypePicker() {
        hideKeyboard();
        if (dbTypeStats.isEmpty()) {
            roomRepository.getTypeStatistics(new RepositoryCallback<List<RoomDao.TypeStat>>() {
                @Override
                public void onSuccess(List<RoomDao.TypeStat> stats) {
                    if (isFinishing() || isDestroyed()) return;
                    dbTypeStats.clear();
                    if (stats != null) {
                        dbTypeStats.addAll(stats);
                    }
                    displayRoomTypePicker();
                }

                @Override
                public void onError(Exception e) {
                    displayRoomTypePicker();
                }
            });
            return;
        }
        displayRoomTypePicker();
    }

    private void displayRoomTypePicker() {
        List<OptionPickerHelper.OptionItem> items = new ArrayList<>();

        // Chỉ hiển thị các loại phòng thực tế đã có trong database
        for (RoomDao.TypeStat stat : dbTypeStats) {
            if (stat.type != null && !stat.type.trim().isEmpty()) {
                String typeName = stat.type.trim();
                boolean isSelected = typeName.equalsIgnoreCase(selectedType);

                items.add(new OptionPickerHelper.OptionItem(
                        typeName,
                        typeName,
                        stat.roomCount + " phòng trong hệ thống",
                        isSelected
                ));
            }
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "Chưa có dữ liệu loại phòng trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        OptionPickerHelper.show(
                this,
                "Chọn loại phòng",
                null,
                items,
                (item, index) -> {
                    selectedType = item.id;
                    if (tvSelectedType != null) {
                        tvSelectedType.setText(selectedType);
                    }
                    // Tự động gợi ý sức chứa phù hợp theo loại phòng đã chọn
                    if (etCapacity != null) {
                        if ("Thực hành".equalsIgnoreCase(selectedType)) {
                            etCapacity.setText("40");
                        } else if ("Hội trường".equalsIgnoreCase(selectedType)) {
                            etCapacity.setText("200");
                        } else {
                            etCapacity.setText("60");
                        }
                    }
                }
        );
    }

    /**
     * Hiển thị danh sách trạng thái ban đầu dạng thẻ chuẩn Material theme.
     */
    private void showStatusPicker() {
        hideKeyboard();

        List<OptionPickerHelper.OptionItem> items = new ArrayList<>();
        items.add(new OptionPickerHelper.OptionItem(
                Constants.STATUS_EMPTY_VN,
                Constants.STATUS_EMPTY_VN,
                "Sẵn sàng bố trí lớp học",
                Constants.STATUS_EMPTY_VN.equalsIgnoreCase(selectedStatus)
        ));
        items.add(new OptionPickerHelper.OptionItem(
                Constants.STATUS_IN_USE_VN,
                Constants.STATUS_IN_USE_VN,
                "Đang có lớp học hoặc sự kiện",
                Constants.STATUS_IN_USE_VN.equalsIgnoreCase(selectedStatus)
        ));
        items.add(new OptionPickerHelper.OptionItem(
                Constants.STATUS_MAINTENANCE_VN,
                Constants.STATUS_MAINTENANCE_VN,
                "Tạm ngưng sử dụng để bảo dưỡng",
                Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(selectedStatus)
        ));

        OptionPickerHelper.show(
                this,
                "Chọn trạng thái ban đầu",
                null,
                items,
                (item, index) -> {
                    selectedStatus = item.id;
                    RoomItemHelper.applyStatusBadge(this, badgeInitialStatus, tvInitialStatus, selectedStatus);
                }
        );
    }

    private void saveRoom() {
        String rawCode = etRoomCode != null ? etRoomCode.getText().toString().trim() : "";
        String roomCode = rawCode.toUpperCase(Locale.getDefault());
        String roomName = etRoomName != null ? etRoomName.getText().toString().trim() : "";
        String capacityText = etCapacity != null ? etCapacity.getText().toString().trim() : "";
        String equipment = etEquipment != null ? etEquipment.getText().toString().trim() : "";
        String note = etNote != null ? etNote.getText().toString().trim() : "";

        if (!ValidationUtils.isValidRoomCode(roomCode)) {
            if (etRoomCode != null) {
                etRoomCode.setError("Vui lòng nhập mã phòng hợp lệ (tối thiểu 2 ký tự)");
                etRoomCode.requestFocus();
            }
            return;
        }

        if (!ValidationUtils.isValidRoomName(roomName)) {
            if (etRoomName != null) {
                etRoomName.setError("Vui lòng nhập tên phòng");
                etRoomName.requestFocus();
            }
            return;
        }

        if (!ValidationUtils.isValidCapacity(capacityText)) {
            if (etCapacity != null) {
                etCapacity.setError("Vui lòng nhập sức chứa hợp lệ (1 - 2000)");
                etCapacity.requestFocus();
            }
            return;
        }

        int capacity = ValidationUtils.parseCapacity(capacityText);

        Room newRoom = new Room();
        newRoom.setRoomCode(roomCode);
        newRoom.setRoomName(roomName);
        newRoom.setFloor(selectedFloor);
        newRoom.setRoomType(selectedType);
        newRoom.setCapacity(capacity);
        newRoom.setStatus(selectedStatus);
        newRoom.setEquipment(equipment.isEmpty() ? "Máy chiếu, Điều hòa, Bàn ghế" : equipment);
        newRoom.setNote(note.isEmpty() ? "Hoạt động bình thường" : note);
        newRoom.setUpdateAt(System.currentTimeMillis() / 1000);

        // Kiểm tra bất đồng bộ qua Repository xem mã phòng đã tồn tại chưa
        roomRepository.getRoomByCode(roomCode, new RepositoryCallback<Room>() {
            @Override
            public void onSuccess(Room existingRoom) {
                if (isFinishing() || isDestroyed()) return;

                if (existingRoom != null) {
                    if (etRoomCode != null) {
                        etRoomCode.setError("Mã phòng đã tồn tại!");
                        etRoomCode.requestFocus();
                    }
                    Toast.makeText(AddRoomActivity.this,
                            "Mã phòng " + roomCode + " đã tồn tại trong hệ thống!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    executeInsertRoom(newRoom);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi kiểm tra mã phòng: " + e.getMessage(), e);
                executeInsertRoom(newRoom);
            }
        });
    }

    private void executeInsertRoom(Room newRoom) {
        roomRepository.insertRoom(newRoom, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (isFinishing() || isDestroyed()) return;

                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(AddRoomActivity.this,
                            getString(R.string.msg_add_room_success, newRoom.getRoomCode()),
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddRoomActivity.this,
                            "Không thể lưu phòng học, vui lòng kiểm tra lại mã phòng!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi khi lưu phòng: " + e.getMessage(), e);
                Toast.makeText(AddRoomActivity.this,
                        "Lỗi khi lưu phòng: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
