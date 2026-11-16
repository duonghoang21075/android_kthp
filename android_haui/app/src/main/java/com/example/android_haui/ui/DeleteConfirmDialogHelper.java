package com.example.android_haui.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;

/**
 * Helper hiển thị hộp thoại xác nhận xóa phòng đồng bộ hoàn toàn với theme của ứng dụng.
 */
public final class DeleteConfirmDialogHelper {

    private DeleteConfirmDialogHelper() {
    }

    public static void show(Context context, Room room, Runnable onConfirm) {
        if (context == null || room == null) return;
        show(context, room.getRoomCode(), room.getRoomName(), onConfirm);
    }

    public static void show(Context context, String roomCode, String roomName, Runnable onConfirm) {
        if (context == null || roomCode == null) return;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int marginPx = (int) (24 * context.getResources().getDisplayMetrics().density);
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout(screenWidth - (marginPx * 2), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDeleteRoomCode = dialogView.findViewById(R.id.tvDeleteRoomCode);
        TextView tvDeleteRoomName = dialogView.findViewById(R.id.tvDeleteRoomName);
        View btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);
        View btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);

        if (tvDeleteRoomCode != null) {
            tvDeleteRoomCode.setText(roomCode);
        }

        if (tvDeleteRoomName != null) {
            String displayName = (roomName != null && !roomName.trim().isEmpty())
                    ? roomName
                    : ("Phòng " + roomCode);
            tvDeleteRoomName.setText(displayName);
        }

        if (btnCancelDelete != null) {
            btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirmDelete != null) {
            btnConfirmDelete.setOnClickListener(v -> {
                dialog.dismiss();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            });
        }

        dialog.show();
    }
}
