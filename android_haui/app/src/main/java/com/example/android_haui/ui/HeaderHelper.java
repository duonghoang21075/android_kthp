package com.example.android_haui.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.example.android_haui.R;

/**
 * Helper quản lý thành phần Header (Tiêu đề, Nút Quay lại, Nút Hành động) tái sử dụng trên toàn bộ ứng dụng.
 */
public final class HeaderHelper {

    private HeaderHelper() {
    }

    /**
     * Thiết lập Header không có nút Back (dành cho các Fragment chính trên Bottom Navigation).
     */
    public static void setup(View rootView, @StringRes int titleResId) {
        setupFull(rootView, titleResId, null, 0, null);
    }

    public static void setup(View rootView, @StringRes int titleResId, @StringRes int subtitleResId) {
        setup(rootView, titleResId);
    }

    /**
     * Thiết lập Header có nút Back (dành cho các màn hình con như Chi tiết phòng, Thêm phòng, Sửa trạng thái).
     */
    public static void setup(View rootView, @StringRes int titleResId, Runnable onBackClick) {
        setupFull(rootView, titleResId, onBackClick, 0, null);
    }

    public static void setup(View rootView, @StringRes int titleResId, @StringRes int subtitleResId, Runnable onBackClick) {
        setup(rootView, titleResId, onBackClick);
    }

    /**
     * Thiết lập Header kèm nút hành động bên phải (ví dụ: "+ Thêm phòng" trên màn hình Danh sách phòng).
     */
    public static void setupWithAction(
            View rootView,
            @StringRes int titleResId,
            @StringRes int actionTextResId,
            Runnable onActionClick
    ) {
        setupFull(rootView, titleResId, null, actionTextResId, onActionClick);
    }

    public static void setupWithAction(
            View rootView,
            @StringRes int titleResId,
            @StringRes int subtitleResId,
            @StringRes int actionTextResId,
            Runnable onActionClick
    ) {
        setupWithAction(rootView, titleResId, actionTextResId, onActionClick);
    }

    private static void setupFull(
            View rootView,
            @StringRes int titleResId,
            Runnable onBackClick,
            @StringRes int actionTextResId,
            Runnable onActionClick
    ) {
        if (rootView == null) return;

        TextView tvTitle = rootView.findViewById(R.id.tvHeaderTitle);
        if (tvTitle != null) {
            tvTitle.setText(titleResId);
        }

        ImageView btnBack = rootView.findViewById(R.id.btnBack);
        if (btnBack != null) {
            if (onBackClick != null) {
                btnBack.setVisibility(View.VISIBLE);
                btnBack.setOnClickListener(v -> onBackClick.run());
            } else {
                btnBack.setVisibility(View.GONE);
            }
        }

        TextView btnHeaderAction = rootView.findViewById(R.id.btnHeaderAction);
        if (btnHeaderAction != null) {
            if (onActionClick != null && actionTextResId != 0) {
                btnHeaderAction.setText(actionTextResId);
                btnHeaderAction.setVisibility(View.VISIBLE);
                btnHeaderAction.setOnClickListener(v -> onActionClick.run());
            } else {
                btnHeaderAction.setVisibility(View.GONE);
            }
        }
    }
}
