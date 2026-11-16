package com.example.android_haui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;

/**
 * Activity cập nhật trạng thái và ghi chú của phòng học trực tiếp vào SQLite CSDL.
 */
public class EditStatusActivity extends AppCompatActivity {

    private static final String TAG = "EditStatusActivity";

    private RoomRepository roomRepository;
    private String roomCode;
    private String selectedStatus = Constants.STATUS_EMPTY_VN;

    private TextView tvRoomCode;
    private TextView tvRoomName;
    private ImageView ivRadioEmpty;
    private ImageView ivRadioInUse;
    private ImageView ivRadioMaintenance;
    private EditText etUpdateNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);

        roomRepository = RoomRepository.getInstance(this);

        HeaderHelper.setup(findViewById(R.id.headerLayout), R.string.edit_status_title, R.string.edit_status_subtitle, this::finish);
        initViews();
        setupButtons();
        loadRoomData();
    }

    private void initViews() {
        tvRoomCode = findViewById(R.id.tvRoomCode);
        tvRoomName = findViewById(R.id.tvRoomName);
        ivRadioEmpty = findViewById(R.id.ivRadioEmpty);
        ivRadioInUse = findViewById(R.id.ivRadioInUse);
        ivRadioMaintenance = findViewById(R.id.ivRadioMaintenance);
        etUpdateNote = findViewById(R.id.etUpdateNote);
    }

    private void setupButtons() {
        View btnCancel = findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        View optionEmpty = findViewById(R.id.optionEmpty);
        if (optionEmpty != null) {
            optionEmpty.setOnClickListener(v -> selectStatus(Constants.STATUS_EMPTY_VN));
        }

        View optionInUse = findViewById(R.id.optionInUse);
        if (optionInUse != null) {
            optionInUse.setOnClickListener(v -> selectStatus(Constants.STATUS_IN_USE_VN));
        }

        View optionMaintenance = findViewById(R.id.optionMaintenance);
        if (optionMaintenance != null) {
            optionMaintenance.setOnClickListener(v -> selectStatus(Constants.STATUS_MAINTENANCE_VN));
        }

        View btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveStatus());
        }
    }

    private void loadRoomData() {
        Intent intent = getIntent();
        if (intent == null) return;

        Room room = (Room) intent.getSerializableExtra(Constants.EXTRA_ROOM_ITEM);
        roomCode = intent.getStringExtra(Constants.EXTRA_ROOM_CODE);

        if (room != null) {
            roomCode = room.getRoomCode();
            bindRoomData(room);
        } else if (roomCode != null && !roomCode.trim().isEmpty()) {
            roomRepository.getRoomByCode(roomCode, new RepositoryCallback<Room>() {
                @Override
                public void onSuccess(Room fetchedRoom) {
                    if (fetchedRoom != null && !isFinishing() && !isDestroyed()) {
                        bindRoomData(fetchedRoom);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Lỗi lấy thông tin phòng: " + e.getMessage());
                }
            });
        }
    }

    private void bindRoomData(Room room) {
        if (room == null) return;

        if (tvRoomCode != null) {
            tvRoomCode.setText(room.getRoomCode());
        }

        if (tvRoomName != null) {
            String name = room.getRoomName();
            if (name == null || name.trim().isEmpty()) {
                name = "Phòng " + room.getRoomCode();
            }
            tvRoomName.setText(name);
        }

        if (etUpdateNote != null && room.getNote() != null && !room.getNote().trim().isEmpty()) {
            etUpdateNote.setText(room.getNote());
        }

        selectStatus(room.getStatus());
    }

    private void selectStatus(String status) {
        if (status == null) status = Constants.STATUS_EMPTY_VN;

        if (Constants.STATUS_EMPTY_VN.equalsIgnoreCase(status) || Constants.EMPTY.equalsIgnoreCase(status)) {
            selectedStatus = Constants.STATUS_EMPTY_VN;
            if (ivRadioEmpty != null) ivRadioEmpty.setImageResource(R.drawable.ic_radio_selected_empty);
            if (ivRadioInUse != null) ivRadioInUse.setImageResource(R.drawable.ic_radio_unselected);
            if (ivRadioMaintenance != null) ivRadioMaintenance.setImageResource(R.drawable.ic_radio_unselected);
        } else if (Constants.STATUS_IN_USE_VN.equalsIgnoreCase(status) || Constants.IN_USE.equalsIgnoreCase(status)) {
            selectedStatus = Constants.STATUS_IN_USE_VN;
            if (ivRadioEmpty != null) ivRadioEmpty.setImageResource(R.drawable.ic_radio_unselected);
            if (ivRadioInUse != null) ivRadioInUse.setImageResource(R.drawable.ic_radio_selected_inuse);
            if (ivRadioMaintenance != null) ivRadioMaintenance.setImageResource(R.drawable.ic_radio_unselected);
        } else {
            selectedStatus = Constants.STATUS_MAINTENANCE_VN;
            if (ivRadioEmpty != null) ivRadioEmpty.setImageResource(R.drawable.ic_radio_unselected);
            if (ivRadioInUse != null) ivRadioInUse.setImageResource(R.drawable.ic_radio_unselected);
            if (ivRadioMaintenance != null) ivRadioMaintenance.setImageResource(R.drawable.ic_radio_selected_maint);
        }
    }

    private void saveStatus() {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            Toast.makeText(this, "Mã phòng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etUpdateNote != null ? etUpdateNote.getText().toString().trim() : null;

        roomRepository.updateRoomStatus(roomCode, selectedStatus, note, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(EditStatusActivity.this, "Đã cập nhật trạng thái phòng thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditStatusActivity.this, "Không tìm thấy phòng để cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi cập nhật trạng thái: " + e.getMessage(), e);
                Toast.makeText(EditStatusActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
