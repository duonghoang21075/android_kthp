package com.example.android_haui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;

public class RoomDetailActivity extends AppCompatActivity {

    private RoomRepository roomRepository;
    private String currentRoomCode;
    private String currentRoomName;

    private TextView tvRoomCode;
    private TextView tvRoomName;
    private FrameLayout bgStatus;
    private TextView tvStatusLabel;
    private TextView tvFloor;
    private TextView tvType;
    private TextView tvCapacity;
    private TextView tvEquipment;
    private TextView tvNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        roomRepository = RoomRepository.getInstance(this);

        HeaderHelper.setup(findViewById(R.id.headerLayout), R.string.detail_title, R.string.detail_subtitle, this::finish);
        initViews();
        setupActionButtons();
        setupNavigation();
        loadRoomData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentRoomCode != null && !currentRoomCode.trim().isEmpty()) {
            roomRepository.getRoomByCode(currentRoomCode, new RepositoryCallback<Room>() {
                @Override
                public void onSuccess(Room fetchedRoom) {
                    if (fetchedRoom != null && !isFinishing() && !isDestroyed()) {
                        bindRoomData(fetchedRoom);
                    }
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }
    }

    private void initViews() {
        tvRoomCode = findViewById(R.id.tvRoomCode);
        tvRoomName = findViewById(R.id.tvRoomName);
        bgStatus = findViewById(R.id.bgStatus);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        tvFloor = findViewById(R.id.tvFloor);
        tvType = findViewById(R.id.tvType);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvEquipment = findViewById(R.id.tvEquipment);
        tvNote = findViewById(R.id.tvNote);
    }

    private void loadRoomData() {
        Intent intent = getIntent();
        if (intent == null) return;

        Room room = (Room) intent.getSerializableExtra(Constants.EXTRA_ROOM_ITEM);
        currentRoomCode = intent.getStringExtra(Constants.EXTRA_ROOM_CODE);

        if (room != null) {
            currentRoomCode = room.getRoomCode();
            bindRoomData(room);
        } else if (currentRoomCode != null && !currentRoomCode.trim().isEmpty()) {
            roomRepository.getRoomByCode(currentRoomCode, new RepositoryCallback<Room>() {
                @Override
                public void onSuccess(Room fetchedRoom) {
                    if (fetchedRoom != null && !isFinishing() && !isDestroyed()) {
                        bindRoomData(fetchedRoom);
                    }
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }
    }

    private void bindRoomData(Room room) {
        if (room == null) return;

        currentRoomCode = room.getRoomCode();
        String rawName = room.getRoomName();
        currentRoomName = (rawName != null && !rawName.trim().isEmpty())
                ? rawName
                : ("Phòng " + room.getRoomCode());

        if (tvRoomCode != null) {
            tvRoomCode.setText(room.getRoomCode());
        }

        if (tvRoomName != null) {
            tvRoomName.setText(currentRoomName);
        }

        if (tvFloor != null) {
            tvFloor.setText("Tầng " + room.getFloor());
        }

        if (tvType != null) {
            tvType.setText(room.getRoomType() != null ? room.getRoomType() : "Phòng học");
        }

        if (tvCapacity != null) {
            tvCapacity.setText(room.getCapacity() > 0 ? (room.getCapacity() + " chỗ ngồi") : "Chưa xác định");
        }

        if (tvEquipment != null && room.getEquipment() != null && !room.getEquipment().trim().isEmpty()) {
            tvEquipment.setText(room.getEquipment());
        }

        if (tvNote != null && room.getNote() != null && !room.getNote().trim().isEmpty()) {
            tvNote.setText(room.getNote());
        }

        RoomItemHelper.applyStatusBadge(this, bgStatus, tvStatusLabel, room.getStatus());
    }

    private void setupActionButtons() {
        View btnBackToList = findViewById(R.id.btnBackToList);
        if (btnBackToList != null) {
            btnBackToList.setOnClickListener(v -> finish());
        }

        View btnEditStatus = findViewById(R.id.btnEditStatus);
        if (btnEditStatus != null) {
            btnEditStatus.setOnClickListener(v -> {
                Intent intent = new Intent(RoomDetailActivity.this, EditStatusActivity.class);
                if (currentRoomCode != null) {
                    intent.putExtra(Constants.EXTRA_ROOM_CODE, currentRoomCode);
                }
                startActivity(intent);
            });
        }

        View btnDeleteRoom = findViewById(R.id.btnDeleteRoom);
        if (btnDeleteRoom != null) {
            btnDeleteRoom.setOnClickListener(v -> confirmAndDeleteRoom());
        }
    }

    private void confirmAndDeleteRoom() {
        if (currentRoomCode == null || currentRoomCode.trim().isEmpty()) return;

        DeleteConfirmDialogHelper.show(this, currentRoomCode, currentRoomName, () -> {
            roomRepository.deleteRoom(currentRoomCode, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean deleted) {
                    if (isFinishing() || isDestroyed()) return;
                    if (Boolean.TRUE.equals(deleted)) {
                        Toast.makeText(RoomDetailActivity.this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(RoomDetailActivity.this, "Không tìm thấy phòng để xóa", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(RoomDetailActivity.this, "Lỗi xóa phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupNavigation() {
        View bottomNavView = findViewById(R.id.layoutBottomNav);
        if (bottomNavView != null) {
            BottomNavHelper.setup(bottomNavView, Constants.TAB_ROOMS, tabIndex -> {
                Intent intent = new Intent(RoomDetailActivity.this, MainActivity.class);
                intent.putExtra(Constants.EXTRA_SELECTED_TAB, tabIndex);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}
