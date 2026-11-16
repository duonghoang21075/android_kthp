package com.example.android_haui.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.android_haui.R;

import java.util.List;

/**
 * Helper hiển thị hộp thoại chọn (Tầng, Loại phòng, Trạng thái) với UI/UX tối ưu:
 * - Giao diện bo tròn, hiển thị dữ liệu trực tiếp từ CSDL (số lượng phòng tương ứng).
 * - Phản hồi chạm tức thì, đóng hộp thoại mượt mà không bị khựng lag.
 */
public final class OptionPickerHelper {

    public static class OptionItem {
        public final String id;
        public final String title;
        public final String subtitle;
        public final boolean isSelected;

        public OptionItem(String id, String title, String subtitle, boolean isSelected) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.isSelected = isSelected;
        }
    }

    public interface OnOptionSelectedListener {
        void onSelected(OptionItem item, int index);
    }

    private OptionPickerHelper() {
    }

    public static void show(
            Context context,
            String title,
            String subtitle,
            List<OptionItem> items,
            OnOptionSelectedListener listener
    ) {
        if (context == null || items == null || items.isEmpty()) return;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_option_picker, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int marginPx = (int) (20 * context.getResources().getDisplayMetrics().density);
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout(screenWidth - (marginPx * 2), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvPickerTitle = dialogView.findViewById(R.id.tvPickerTitle);

        View btnClosePicker = dialogView.findViewById(R.id.btnClosePicker);
        LinearLayout layoutPickerItems = dialogView.findViewById(R.id.layoutPickerItems);

        if (tvPickerTitle != null && title != null) {
            tvPickerTitle.setText(title);
        }



        if (btnClosePicker != null) {
            btnClosePicker.setOnClickListener(v -> dialog.dismiss());
        }

        if (layoutPickerItems != null) {
            layoutPickerItems.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(context);

            for (int i = 0; i < items.size(); i++) {
                final int index = i;
                final OptionItem item = items.get(i);

                View itemView = inflater.inflate(R.layout.item_picker_option, layoutPickerItems, false);
                LinearLayout layoutRoot = itemView.findViewById(R.id.layoutOptionRoot);
                TextView tvOptionTitle = itemView.findViewById(R.id.tvOptionTitle);
                TextView tvOptionSubtitle = itemView.findViewById(R.id.tvOptionSubtitle);
                ImageView ivOptionRadio = itemView.findViewById(R.id.ivOptionRadio);

                if (tvOptionTitle != null) {
                    tvOptionTitle.setText(item.title);
                }

                if (tvOptionSubtitle != null) {
                    if (item.subtitle != null && !item.subtitle.isEmpty()) {
                        tvOptionSubtitle.setText(item.subtitle);
                        tvOptionSubtitle.setVisibility(View.VISIBLE);
                    } else {
                        tvOptionSubtitle.setVisibility(View.GONE);
                    }
                }

                if (item.isSelected) {
                    if (layoutRoot != null) {
                        layoutRoot.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_option_selected));
                    }
                    if (tvOptionTitle != null) {
                        tvOptionTitle.setTextColor(ContextCompat.getColor(context, R.color.card_blue_bg));
                    }
                    if (ivOptionRadio != null) {
                        ivOptionRadio.setImageResource(R.drawable.ic_radio_selected_inuse);
                    }
                } else {
                    if (layoutRoot != null) {
                        layoutRoot.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_option_unselected));
                    }
                    if (tvOptionTitle != null) {
                        tvOptionTitle.setTextColor(ContextCompat.getColor(context, R.color.text_dark));
                    }
                    if (ivOptionRadio != null) {
                        ivOptionRadio.setImageResource(R.drawable.ic_radio_unselected);
                    }
                }

                itemView.setOnClickListener(v -> {
                    // Cập nhật ngay trạng thái được chọn cho trải nghiệm phản hồi xúc giác mượt mà
                    if (layoutRoot != null) {
                        layoutRoot.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_option_selected));
                    }
                    if (ivOptionRadio != null) {
                        ivOptionRadio.setImageResource(R.drawable.ic_radio_selected_inuse);
                    }
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onSelected(item, index);
                    }
                });

                layoutPickerItems.addView(itemView);
            }
        }

        dialog.show();
    }
}
