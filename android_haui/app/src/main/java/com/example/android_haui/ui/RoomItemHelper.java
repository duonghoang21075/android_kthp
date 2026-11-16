package com.example.android_haui.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;

import java.util.Locale;

/**
 * Helper chuẩn hóa việc hiển thị từng thẻ phòng học (item_recent_room.xml) và badge trạng thái
 * tái sử dụng cho: Tổng quan, Danh sách phòng, Tìm kiếm phòng và Chi tiết phòng.
 */
public final class RoomItemHelper {

    private RoomItemHelper() {
    }

    /**
     * Gắn dữ liệu Room vào layout item_recent_room.xml.
     */
    public static void bindRoomCard(Context context, View itemView, Room room, View.OnClickListener onClickListener) {
        bindRoomCard(context, itemView, room, onClickListener, null);
    }

    /**
     * Gắn dữ liệu Room vào layout item_recent_room.xml kèm sự kiện nhấn giữ (Long Click) để xóa nhanh.
     */
    public static void bindRoomCard(
            Context context,
            View itemView,
            Room room,
            View.OnClickListener onClickListener,
            View.OnLongClickListener onLongClickListener
    ) {
        if (context == null || itemView == null || room == null) return;

        TextView tvCode = itemView.findViewById(R.id.tvItemRoomCode);
        TextView tvName = itemView.findViewById(R.id.tvItemRoomName);
        FrameLayout badgeStatus = itemView.findViewById(R.id.badgeStatus);
        TextView tvStatus = itemView.findViewById(R.id.tvItemStatus);
        TextView tvFloorAndType = itemView.findViewById(R.id.tvItemFloorAndType);
        TextView tvCapacity = itemView.findViewById(R.id.tvItemCapacity);

        if (tvCode != null) {
            tvCode.setText(room.getRoomCode());
        }

        if (tvName != null) {
            String rawName = room.getRoomName();
            String displayName = (rawName != null && !rawName.trim().isEmpty())
                    ? rawName
                    : ("Phòng " + room.getRoomCode());
            tvName.setText(displayName);
        }

        String type = (room.getRoomType() != null && !room.getRoomType().trim().isEmpty())
                ? room.getRoomType()
                : "Phòng học";
        if (tvFloorAndType != null) {
            tvFloorAndType.setText(String.format(Locale.getDefault(), "Tầng %d · %s", room.getFloor(), type));
        }

        if (tvCapacity != null) {
            if (room.getCapacity() > 0) {
                tvCapacity.setText(context.getString(R.string.room_capacity_format, room.getCapacity()));
                tvCapacity.setVisibility(View.VISIBLE);
            } else {
                tvCapacity.setVisibility(View.GONE);
            }
        }

        applyStatusBadge(context, badgeStatus, tvStatus, room.getStatus());

        if (onClickListener != null) {
            itemView.setOnClickListener(onClickListener);
        }

        if (onLongClickListener != null) {
            itemView.setOnLongClickListener(onLongClickListener);
        }
    }

    /**
     * Cập nhật màu sắc và nhãn cho Badge trạng thái phòng (Trống / Đang sử dụng / Bảo trì).
     */
    public static void applyStatusBadge(Context context, FrameLayout badgeContainer, TextView tvStatusLabel, String status) {
        if (context == null || badgeContainer == null || tvStatusLabel == null) return;

        if (Constants.STATUS_EMPTY_VN.equalsIgnoreCase(status) || Constants.EMPTY.equalsIgnoreCase(status)) {
            badgeContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_empty));
            tvStatusLabel.setText(R.string.status_empty);
            tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_empty_text));
        } else if (Constants.STATUS_IN_USE_VN.equalsIgnoreCase(status) || Constants.IN_USE.equalsIgnoreCase(status)) {
            badgeContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_inuse));
            tvStatusLabel.setText(R.string.status_in_use);
            tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_inuse_text));
        } else if (Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(status) || Constants.MAINTENANCE.equalsIgnoreCase(status)) {
            badgeContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_maint));
            tvStatusLabel.setText(R.string.status_maintenance);
            tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_maint_text));
        } else {
            badgeContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_empty));
            tvStatusLabel.setText(status != null ? status : context.getString(R.string.status_empty));
            tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.status_empty_text));
        }
    }
}
